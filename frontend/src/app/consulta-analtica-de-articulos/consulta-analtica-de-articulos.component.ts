import { Component, HostListener} from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-consulta-analtica-de-articulos',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-analtica-de-articulos.component.html',
  styleUrls: ['./consulta-analtica-de-articulos.component.css']
})
export class ConsultaAnalticaDeArticulosComponent {
  //3 dots menu 
  showMenu = false;
  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.showMenu = !this.showMenu;
  }

  @HostListener('document:click')
  closeMenu(): void {
    this.showMenu = false;
  }

  //global variables
  private entcod: number | null = null;
  public eje: number | null = null;
  articulos: any[] = [];
  private backupArticulos: any[] = [];
  currentMonth: number = new Date().getMonth() + 1;
  page = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  isLoading: boolean = false;
  articuloError: string = '';
  articuloSuccess: string = '';

  ngOnInit(): void{
    this.limpiarMEssages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO'); 

    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}

    if (!entidad || this.entcod === null || !eje || this.eje === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }
    
    this.fetchArticulos();
  }

  fetchArticulos() {
    this.isLoading = true;
    this.limpiarMEssages();

    this.http.get<any>(`${environment.backendUrl}/api/art/fetch-analitica-articulos/${this.entcod}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.articulos = res;
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.articuloError = err.error.error || err.error;
      }
    })
  }
  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0; return;}
    if (this.page >= total) {this.page = total - 1;}
  }
  get paginatedArticulos(): any[] {if (!this.articulos || this.articulos.length === 0) return []; const start = this.page * this.pageSize; return this.articulos.slice(start, start + this.pageSize);}
  get totalPages(): number {return Math.max(1, Math.ceil((this.articulos?.length ?? 0) / this.pageSize));}
  prevPage(): void {if (this.page > 0) this.page--;}
  nextPage(): void {if (this.page < this.totalPages - 1) this.page++;}
  goToPage(event: any): void {const inputPage = Number(event.target.value); if (inputPage >= 1 && inputPage <= this.totalPages) {this.page = inputPage - 1;}}

  //main table functions
  sortField: 'eje'| 'artcod' | 'artdes' | 'afacod' | 'afades' | null = null;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  toggleSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applySort();
    this.page = 0;
    this.updatePagination();
  }

  private applySort(): void {
    if (!this.sortColumn) return;
    
    const parseValue = (val: string) => {
      return val.replace(/(\d+)/g, (match) => {
        return match.padStart(20, '0');
      });
    };

    this.articulos.sort((a, b) => {
      let aValue: string;
      let bValue: string;
      
      if (this.sortColumn === 'afades') {
        aValue = parseValue((a.afa?.afades ?? '').toString().toUpperCase());
        bValue = parseValue((b.afa?.afades ?? '').toString().toUpperCase());
      } else {
        aValue = parseValue((a[this.sortColumn] ?? '').toString().toUpperCase());
        bValue = parseValue((b[this.sortColumn] ?? '').toString().toUpperCase());
      }
      
      const comparison = aValue.localeCompare(bValue);
      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
  }
  private startX: number = 0;
  private startWidth: number = 0;
  private resizingColIndex: number | null = null;
  startResize(event: MouseEvent, colIndex: number) {
    this.resizingColIndex = colIndex;
    this.startX = event.pageX;
    const th = (event.target as HTMLElement).parentElement as HTMLElement;
    this.startWidth = th.offsetWidth;

    document.addEventListener('mousemove', this.onResizeMove);
    document.addEventListener('mouseup', this.stopResize);
  }

  onResizeMove = (event: MouseEvent) => {
    if (this.resizingColIndex === null) return;
    const table = document.querySelector('.main-table') as HTMLTableElement;
    if (!table) return;
    const th = table.querySelectorAll('th')[this.resizingColIndex] as HTMLElement;
    if (!th) return;
    const diff = event.pageX - this.startX;
    th.style.width = (this.startWidth + diff) + 'px';
  };

  stopResize = () => {
    document.removeEventListener('mousemove', this.onResizeMove);
    document.removeEventListener('mouseup', this.stopResize);
    this.resizingColIndex = null;
  };

  DownloadPDF() {
    this.limpiarMEssages();

    const source = this.articulos;
    if (!source?.length) {
      this.articuloError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any) => ({
      eje: this.currentMonth + "/" + this.eje,
      artcod: row.artcod ?? '',
      artdes: row.artdes ?? '',
      afacod: row.afacod ?? '',
      afades: row.afa?.afades ?? ''
    }));

    const columns = [
      { header: 'Ejer/Periodo', dataKey: 'eje' },
      { header: 'Articulo', dataKey: 'artcod' },
      { header: 'Desc. articulo', dataKey: 'artdes' },
      { header: 'Familia', dataKey: 'afacod' },
      { header: 'Desc. familia', dataKey: 'afades' }
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Consulta analîtica de familias', 40, 40);

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      columnStyles: {
        eje: { cellWidth: 15 },
        artcod: { cellWidth: 15 },
        artdes: { cellWidth: 40 },
        afacod: { cellWidth: 15 },
        afades: { cellWidth: 40 }
      }
    });

    doc.save('Consulta_analîtica_de_articulos.pdf');
  }

  DownloadExcel() {
    this.limpiarMEssages();
    const rows = this.articulos;
    if (!rows || rows.length === 0) {
      this.articuloError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map(row => ({
      eje: this.currentMonth + "/" + this.eje,
      artcod: row.artcod ?? '',
      artdes: row.artdes ?? '',
      afacod: row.afacod ?? '',
      afades: row.afa?.afades ?? ''
    }));
    
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Consulta analîtica de articulos']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Ejer/Periodo', 'Articulo', 'Desc. articulo', 'Familias', 'Desc. familia']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 15 },
      { wch: 15 },
      { wch: 40 },
      { wch: 15 },
      { wch: 40 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Articulos');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_analîtica_de_articulos.xlsx'
    );
  }

  //misc
  limpiarMEssages() {
    this.articuloSuccess = '';
    this.articuloError = '';
  }
}

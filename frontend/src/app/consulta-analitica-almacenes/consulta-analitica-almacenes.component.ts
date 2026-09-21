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
  selector: 'app-consulta-analitica-almacenes',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-analitica-almacenes.component.html',
  styleUrls: ['./consulta-analitica-almacenes.component.css']
})
export class ConsultaAnaliticaAlmacenesComponent {
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
  almacenes: any[] = [];
  private backupFacturas: any[] = [];
  currentMonth: number = new Date().getMonth() + 1;
  page = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  isLoading: boolean = false;
  almacenSuccess: string = '';
  almacenError: string = '';
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

    this.fatchAlmacenes();
  }

  fatchAlmacenes() {
    this.isLoading = true;
    this.limpiarMEssages();

    this.http.get<any>(`${environment.backendUrl}/api/dep/fetch-consulta-almacenes/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.almacenes = res;
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.almacenError = err.error.error || err.error;
      }
    })
  }
  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0; return;}
    if (this.page >= total) {this.page = total - 1;}
  }
  get paginatedAlmacenes(): any[] {if (!this.almacenes || this.almacenes.length === 0) return []; const start = this.page * this.pageSize; return this.almacenes.slice(start, start + this.pageSize);}
  get totalPages(): number {return Math.max(1, Math.ceil((this.almacenes?.length ?? 0) / this.pageSize));}
  prevPage(): void {if (this.page > 0) this.page--;}
  nextPage(): void {if (this.page < this.totalPages - 1) this.page++;}
  goToPage(event: any): void {const inputPage = Number(event.target.value); if (inputPage >= 1 && inputPage <= this.totalPages) {this.page = inputPage - 1;}}


  //main table functions
  sortField: 'eje' | 'depcod' | 'cgecod' | 'depdes' | null = null;
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
  
  // Helper to pad numbers for proper sorting
  const parseValue = (val: string) => {
    return val.replace(/(\d+)/g, (match) => {
      return match.padStart(20, '0');
    });
  };

  this.almacenes.sort((a, b) => {
    const aValue = parseValue((a[this.sortColumn] ?? '').toString().toUpperCase());
    const bValue = parseValue((b[this.sortColumn] ?? '').toString().toUpperCase());
    
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

    const source = this.almacenes;
    if (!source?.length) {
      this.almacenError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any) => ({
      eje: this.currentMonth + "/" + row.eje,
      depcod: row.depcod ?? '',
      depdes: row.depdes ?? '',
      cgecod: row.cgecod ?? '',
      instal: row.instal ?? ''
    }));

    const columns = [
      { header: 'Ejer/Periodo', dataKey: 'eje' },
      { header: 'Cód. Almacén', dataKey: 'depcod' },
      { header: 'Descripción', dataKey: 'depdes' },
      { header: 'Centro Gestor', dataKey: 'cgecod' },
      { header: 'Cód. Instalación', dataKey: 'instal' }
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Consulta analîtica de almacenes', 40, 40);

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      columnStyles: {
        eje: { cellWidth: 15 },
        depcod: { cellWidth: 15 },
        depdes: { cellWidth: 40 },
        cgecod: { cellWidth: 10 },
        instal: { cellWidth: 10 }
      }
    });

    doc.save('Consulta_analîtica_de_almacenes.pdf');
  }

  downloadExcel() {
    this.limpiarMEssages();
    const rows = this.almacenes;
    if (!rows || rows.length === 0) {
      this.almacenError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map(row => ({
      eje: this.currentMonth + "/" + row.eje,
      depcod: row.depcod ?? '',
      depdes: row.depdes ?? '',
      cgecod: row.cgecod ?? '',
      instal: row.instal ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Consulta analîtica de almacenes']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Ejer/Periodo', 'Cód. Almacén', 'Descripción', 'Centro Gestor', 'Cód. Instalación']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 15 },
      { wch: 15 },
      { wch: 40 },
      { wch: 10 },
      { wch: 15 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Almacenes');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_analîtica_de_almacenes.xlsx'
    );
  }

  //misc
  limpiarMEssages() {
    this.almacenSuccess = '';
    this.almacenError = '';
  }
}

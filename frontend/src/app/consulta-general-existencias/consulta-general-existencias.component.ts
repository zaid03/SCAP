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
  selector: 'app-consulta-general-existencias',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-general-existencias.component.html',
  styleUrls: ['./consulta-general-existencias.component.css']
})
export class ConsultaGeneralExistenciasComponent {
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
  existencias: any[] = [];
  page = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  isLoading: boolean = false;
  existenciasSuccess: string = '';
  existenciasError: string = '';

  ngOnInit(): void{
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');

    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}

    if (!entidad || this.entcod === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchExistencias();
  }

  fetchExistencias() {
    this.isLoading = true;
    this.http.get(`${environment.backendUrl}/api/art/Existencias/${this.entcod}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.existencias = Array.isArray(res) ? [...res] : [];
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.existenciasError = err.error.error ?? err.error;
      }
    })
  }
  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0; return;}
    if (this.page >= total) {this.page = total - 1;}
  }
  get paginatedExistencias(): any[] {
    if (!this.existencias || this.existencias.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.existencias.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.existencias?.length ?? 0) / this.pageSize));
  }
  prevPage(): void {
    if (this.page > 0) this.page--;
  }
  nextPage(): void {
    if (this.page < this.totalPages - 1) this.page++;
  }
  goToPage(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {
      this.page = inputPage - 1;
    }
  }

  //main table functions
  sortField: 'afacod' | 'asucod' | 'artcod' | 'artdes' | 'artuni' | 'artref' | 'artsol' | 'artrec' | 'kEstVir' | null = null;
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

    this.existencias.sort((a, b) => {
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

  excelDownload() {
    this.limpiarMessages();
    const rows = this.paginatedExistencias;
    if (!rows || rows.length === 0) {
      this.existenciasError = 'No hay datos para exportar.';
      return;
    }

    const exportRows = rows.map((row, index) => ({
      afacod: row.afacod ?? '',
      asucod: row.asucod ?? '',
      artcod: row.artcod ?? '',
      afades: row.afades ?? '',
      artuni: row.artuni ?? '',
      artref: row.artref ?? '',
      artsol: row.artsol ?? '',
      artrec: row.artrec ?? '',
      kEstVir: this.calculateKEstVir(row.artuni, row.artsol, row.artrec) ?? ''
    }));

    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de existencias']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Familia', 'Subfamilia', 'Código', 'Descripción', 'Existencias', 'Referencia universal', 'Pte. Servir', 'Pte. Entrada', 'Estocaje virtual']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 30 },
      { wch: 20 },
      { wch: 30 },
      { wch: 20 },
      { wch: 20 },
      { wch: 20 }
    ];

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Existencias');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Existencias.xlsx'
    );
  }

  exportPdf() {
    this.limpiarMessages();
    const source = this.existencias;
    if (!source?.length) {
      this.existenciasError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      afacod: row.afacod ?? '',
      asucod: row.asucod ?? '',
      artcod: row.artcod ?? '',
      afades: row.afades ?? '',
      artuni: row.artuni ?? '',
      artref: row.artref ?? '',
      artsol: row.artsol ?? '',
      artrec: row.artrec ?? '',
      kEstVir: this.calculateKEstVir(row.artuni, row.artsol, row.artrec) ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de existencias', 40, 40);

    const columns = [
      { header: 'Familia', dataKey: 'afacod' },
      { header: 'Subfamilia', dataKey: 'asucod' },
      { header: 'Código', dataKey: 'artcod' },
      { header: 'Descripción', dataKey: 'afades' },
      { header: 'Existencias', dataKey: 'artuni' },
      { header: 'Referencia universal', dataKey: 'artref' },
      { header: 'Pte. Servir', dataKey: 'artsol' },
      { header: 'Pte. Entrada', dataKey: 'artrec' },
      { header: 'Estocaje virtual', dataKey: 'getkEstVir' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('Existencias.pdf');
  }

  campo: string = '';
  afacod: string = '';
  asucod: string = '';
  searchExistencias() {
    this.limpiarMessages();
    let params = new HttpParams();
    if (this.campo) {params = params.set('campo', this.campo);}
    if (this.afacod) {params = params.set('afacod', this.afacod);}
    if (this.asucod) {params = params.set('asucod', this.asucod);}

    if (params.keys().length === 0) {return;}

    this.isLoading = true;
    this.http.get(`${environment.backendUrl}/api/art/existencias/${this.entcod}/search`, { params }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.existencias = Array.isArray(res) ? [...res] : [];
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.existenciasError = err.error.error ?? err.error;
      }
    })
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.campo = '';
    this.afacod = '';
    this.asucod = '';
    this.fetchExistencias();
  }
  
  //detail grid functions
  selectedArticulo: any = null;
  articuloDetailError: string = '';
  articuloDetailSuccess: string = '';
  isUpdating: boolean = false;
  showDetails(articulo: any) {
    this.limpiarMessages();
    this.selectedArticulo = articulo;
    this.tempArticulo = articulo;
    this.fetchTipos();
  }

  closeDetails() {
    this.selectedArticulo = null;
    this.tempArticulo = [];
    this.limpiarMessages();
    this.clearTipos();
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  kestvir: number | null = null;
  calculateKEstVir(artuni: number, artsol: number, artrec: number) {
    this.kestvir = artuni - artsol + artrec;
    return this.kestvir;
  }
  calculateKvalExi(artuni: number, artpmp: number) {
    return artuni * artpmp;
  }
  calculateKUniSol(artuni: number, artsol: number, artrec: number, artmin: number, artopt: number): number {
    const kestvir = this.calculateKEstVir(artuni, artsol, artrec);
    if (!kestvir || kestvir < artmin) {return 0;}
    this.kestvir = artopt - kestvir;
    return this.kestvir;
  }

  tempArticulo: any = {};
  isUpdate: boolean = false;

  tipos: any = null;
  auncodMod: string = '';
  fetchTipos() {
    this.http.get(`${environment.backendUrl}/api/aun/get-all/${this.entcod}`).subscribe({
      next: (res) => {
        this.tipos = res;
        const found = this.tipos.find(
          (t: any) => t.aundes === this.tempArticulo?.aun_AUNDES
        );

        this.auncodMod = found?.auncod ?? this.tipos[0]?.auncod ?? '';
      },
      error: (err) => {
        console.warn(err.error.error ?? err.error);
      }
    })
  }

  clearTipos() {
    this.tipos = null;
    this.auncodMod = '';
  }

  //misc
  limpiarMessages() {
    this.existenciasSuccess = '';
    this.existenciasError = '';
    this.articuloDetailError = '';
    this.articuloDetailSuccess = '';
  }
}
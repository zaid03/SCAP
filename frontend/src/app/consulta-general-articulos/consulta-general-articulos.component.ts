import { Component, HostListener} from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';
import { DatePipe } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-consulta-general-articulos',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-general-articulos.component.html',
  styleUrls: ['./consulta-general-articulos.component.css']
})
export class ConsultaGeneralArticulosComponent {
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
  private eje: number | null = null;
  articulos: any[] = [];
  private backupArticulos: any[] = [];
  page = 0;
  pageSize = 20;
  public Math = Math;

  constructor(private http: HttpClient, private router: Router) {}

  isLoading: boolean = false;
  articuloSuccess: string = '';
  articuloError: string = '';
  ngOnInit(): void{
    this.limpiarMessages();
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
    this.getPagination();
  }

  fetchArticulos() {
    this.isLoading = true;
    this.limpiarMessages();

    this.http.get<any>(`${environment.backendUrl}/api/art/fetch-consulta-general/${this.entcod}?page=${this.page}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.articulos = res;
        this.backupArticulos = [...this.articulos];
        this.getPagination();
      },
      error: (err) => {
        this.articuloError = err.error.error || err.error;
        this.isLoading = false;
      }
    })
  }
  totalPagesMain: number = 0;
  getPagination() {
    this.http.get<any>(`${environment.backendUrl}/api/art/get-pag/${this.entcod}`).subscribe({
      next: (res) => {
        this.totalPagesMain = this.Math.ceil(res/20);
      },
      error: (err) => {
        console.warn(err.error.error || err.error);
      }
    })
  }
  is_main_fetch: boolean = true;
  is_search: boolean = false;
  get paginatedArticulos(): any[] {
    if (this.is_main_fetch) {
      return this.articulos ?? [];
    }
    if (this.is_search) {
      if (this.articulos.length > 20) {
        const start = this.page * this.pageSize;

        return this.articulos.slice(start, start + this.pageSize);
      } else {
        return this.articulos ?? [];
      }
    }
    return [];
  }
  prevPage(): void {
    if (this.is_main_fetch) {
      this.page--;
      this.fetchArticulos();
    }
    if (this.is_search) {
      if (this.page > 0) this.page--;
    }
  }
  nextPage(): void {
    if (this.is_main_fetch) {
      this.page++;
      this.fetchArticulos();
    }
    if (this.is_search) {
      if (this.page < this.totalPagesMain - 1) this.page++;
    }
  }
  goToPage(event: any): void {
    if (this.is_main_fetch) {
      const inputPage = Number(event.target.value);
      if (inputPage >= 1 && inputPage <= this.totalPagesMain) {
        this.page = inputPage - 1;
        this.fetchArticulos();
      }
    }
    if (this.is_search) {
      const inputPage = Number(event.target.value);
      if (inputPage >= 1 && inputPage <= this.totalPagesMain) {
        this.page = inputPage - 1;
      }
    }
  }

  isBloqueado(artblo: number): string {
    if (artblo == 1) return 'Sí'
    return 'No';
  }

  //main table functions
  sortField: 'afacod' | 'asucod' | 'artcod' | 'artdes' | 'artuni' | 'artref' | 'artblo' | 'afades' | null = null;
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
  }

  private applySort(): void {
    if (!this.sortColumn) return;
    
    const parseValue = (val: string) => {
      return val.replace(/(\d+)/g, (match) => {
        return match.padStart(20, '0');
      });
    };

    this.articulos.sort((a, b) => {
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

  exportArticulos: any = [];
  isPdf: boolean = false;
  isExcel: boolean = false;
  getExportData() {
    this.http.get(`${environment.backendUrl}/api/art/export-consulta-general/${this.entcod}`).subscribe({
      next: (res) => {
        this.exportArticulos = res;
        if (this.exportArticulos.length === 0) {
          this.articuloError = 'No hay datos para exportar.';
          return;
        }

        if (this.isPdf) {
          this.preparePDF();
          return;
        }
        if (this.isExcel) {
          this.prepareExcel();
          return;
        }
      },
      error: (err) => {
        console.warn(err.error.error ?? err.error);
      }
    })
  }

  DownloadPDF() {
    this.limpiarMessages();

    this.isPdf = true;
    if (this.exportArticulos.length === 0) {
      this.getExportData();
    } else {
      this.preparePDF();
    }
  }

  preparePDF() {
    const rows = this.exportArticulos.map((row: any) => ({
      afacod: row.afacod ?? '',
      asucod: row.asucod ?? '',
      artcod: row.artcod ?? '',
      artdes: row.artdes ?? '',
      artuni: row.artuni ?? '',
      artref: row.artref ?? '',
      artblo: this.isBloqueado(row.artblo) ?? ''
    }));

    const columns = [
      { header: 'Familia', dataKey: 'afacod' },
      { header: 'Subfamilia', dataKey: 'asucod'},
      { header: 'Código', dataKey: 'artcod'},
      { header: 'Descripción', dataKey: 'artdes'},
      { header: 'Estocaje', dataKey: 'artuni'},
      { header: 'Referencia Universal', dataKey: 'artref'},
      { header: 'Bloqueo', dataKey: 'artblo'}
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Consulta general de artículos', 40, 40);

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map((row:any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      columnStyles: {
        afacod: { cellWidth: 10 },
        asucod: { cellWidth: 10 },
        artcod: { cellWidth: 10 },
        artdes: { cellWidth: 40 },
        artuni: { cellWidth: 10 },
        artref: { cellWidth: 25 },
        artblo: { cellWidth: 8 }
      }
    });

    doc.save('Consulta_general_de_articulos.pdf');
  }

  downloadExcel() {
    this.limpiarMessages();

    this.isExcel = true;
    if (this.exportArticulos.length === 0) {
      this.getExportData();
    } else {
      this.prepareExcel();
    }
  }

  prepareExcel() {
    const exportRows = this.exportArticulos.map((row: any) => ({
      afacod: row.afacod ?? '',
      asucod: row.asucod ?? '',
      artcod: row.artcod ?? '',
      artdes: row.artdes ?? '',
      artuni: row.artuni ?? '',
      artref: row.artref ?? '',
      artblo: this.isBloqueado(row.artblo) ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Consulta general de articulos']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Familias', 'Subfamilia', 'Código', 'Descripción', 'Estocaje', 'Referencia universal', 'Bloqueo']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 50 },
      { wch: 10 },
      { wch: 15 },
      { wch: 8 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Articulos');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_general_de_articulos.xlsx'
    );
  }

  search: string = '';
  afacod: string ='';
  asucod: string = '';
  bloqueado: 'bloqueado' | 'nobloqueado' | 'todos' = 'nobloqueado';
  searchArticulo () {
    this.limpiarMessages();
    this.page = 0;
    this.isLoading = true;

    this.is_main_fetch = false;
    this.is_search = true;
    const params = new URLSearchParams();
    params.append('page', this.page.toString());
    if (this.search) params.append('search', this.search);
    if (this.afacod) params.append('afacod', this.afacod);
    if (this.asucod) params.append('asucod', this.asucod);
    params.append('bloqueado', this.bloqueado);

    this.http.get<any>(
      `${environment.backendUrl}/api/art/search/${this.entcod}?${params.toString()}`
    ).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.articulos = res;
        this.page = 0;
        this.totalPagesMain = Math.ceil(this.articulos.length / this.pageSize);
      },
      error: (err) => {
        this.articulos = [];
        this.isLoading = false;
        this.articuloError = err.error?.message || err.error;
      }
    });
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.search = '';
    this.afacod ='';
    this.asucod = '';
    this.bloqueado = 'nobloqueado';
    this.is_main_fetch = true;
    this.is_search = false;
    this.page = 0;
    this.fetchArticulos();
  }

  checkUpperFour(term: any): string {
    if (!term) {return '';}   
    return term.toUpperCase();
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
    this.showProveedores();
    this.fetchTipos();
  }

  closeDetails() {
    this.selectedArticulo = null;
    this.tempArticulo = [];
    this.limpiarMessages();
    this.activeDetailTab = null;
    this.showProveedoresGrid = false;
    this.showExistenciasGrid = false;
    this.proveedores = [];
    this.existencias = [];
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
  
  // sub details functions
  activeDetailTab: 'proveedores' | 'existencias' | null = null;
  proveedoresError: string = '';
  showProveedoresGrid: boolean = false;
  isLoadingProveedores: boolean = false;
  showProveedores() {
    this.limpiarMessages();
    this.showProveedoresGrid = true;
    this.activeDetailTab = 'proveedores';
    this.showExistenciasGrid = false;
    this.fetchProveedores();
  }

  proveedores: any = [];
  pageProv: number = 0;
  fetchProveedores() {
    this.limpiarMessages();
    this.isLoadingProveedores = true;
    const afacod = this.selectedArticulo.afacod;
    const asucod = this.selectedArticulo.asucod;
    const artcod = this.selectedArticulo.artcod;

    this.http.get(`${environment.backendUrl}/api/more/proveedores-por-articulo/${this.entcod}/${afacod}/${asucod}/${artcod}`).subscribe({
      next: (res) => {
        this.isLoadingProveedores = false;
        this.proveedores = res;
        this.pageProv = 0;
      },
      error: (err) => {
        this.pageProv = 0;
        this.proveedores = [];
        this.isLoadingProveedores = false;
        this.proveedoresError = err.error?.error || err.error;
      }
    })
  }
  get paginatedProveedores(): any[] {if (!this.proveedores || this.proveedores.length === 0) return [];
    const start = this.pageProv * this.pageSize; return this.proveedores.slice(start, start + this.pageSize);
  }
  get totalPagesProveedores(): number {return Math.max(1, Math.ceil((this.proveedores?.length ?? 0) / this.pageSize));}
  prevPageProv(): void {if (this.pageProv > 0) this.pageProv--;}
  nextPageProv(): void {if (this.pageProv < this.totalPagesProveedores - 1) this.pageProv++;}
  goToPageProv(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPagesProveedores) {this.pageProv = inputPage - 1;}
  }

  showExistenciasGrid: boolean = false;
  existenciasError: string = '';
  isLoadingExtencias: boolean = false;
  showExistencias() {
    this.limpiarMessages();
    this.activeDetailTab = 'existencias';
    this.showExistenciasGrid = true;
    this.showProveedoresGrid = false;
    this.fetchEcistencias(); 
  }

  existencias: any = [];
  pageExistencia: number = 0;
  fetchEcistencias() {
    this.limpiarMessages();
    this.isLoadingExtencias = true;
    const afacod = this.selectedArticulo.afacod;
    const asucod = this.selectedArticulo.asucod;
    const artcod = this.selectedArticulo.artcod;

    this.http.get(`${environment.backendUrl}/api/mea/existencias-por-articulo/${this.entcod}/${afacod}/${asucod}/${artcod}`).subscribe({
      next: (res) => {
        this.isLoadingExtencias = false;
        this.existencias = res;
        this.pageExistencia = 0;
      },
      error: (err) => {
        this.pageExistencia = 0;
        this.existencias = [];
        this.isLoadingExtencias = false;
        this.existenciasError = err.error?.error || err.error;
      }
    })
  }
  get paginatedExistencias(): any[] {if (!this.existencias || this.existencias.length === 0) return [];
    const start = this.pageExistencia * this.pageSize; return this.existencias.slice(start, start + this.pageSize);
  }
  get totalPagesExistencias(): number {return Math.max(1, Math.ceil((this.existencias?.length ?? 0) / this.pageSize));}
  prevPageExistencias(): void {if (this.pageExistencia > 0) this.pageExistencia--;}
  nextPageExistencias(): void {if (this.pageExistencia < this.totalPagesExistencias - 1) this.pageExistencia++;}
  goToPageExistencias(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPagesExistencias) {this.pageExistencia = inputPage - 1;}
  }

  //misc
  limpiarMessages() {
    this.articuloError = '';
    this.articuloSuccess = '';
    this.articuloDetailError = '';
    this.articuloDetailSuccess = '';
    this.proveedoresError = '';
    this.existenciasError = '';
  }
}
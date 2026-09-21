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
  selector: 'app-mantinimiento-articulos-almacen',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './mantinimiento-articulos-almacen.component.html',
  styleUrls: ['./mantinimiento-articulos-almacen.component.css']
})
export class MantinimientoArticulosAlmacenComponent {
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
  private cge: string = '';
  private percod: string = '';
  almacenes: any[] = [];
  page = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  isLoading: boolean = false;
  almacenSuccess: string = '';
  almacenError: string = '';
  ngOnInit(): void{
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO'); 
    const centrogestor = sessionStorage.getItem('CENTROGESTOR');
    const nombre = sessionStorage.getItem('USUCOD');

    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}
    if (centrogestor) {const parsed = JSON.parse(centrogestor); this.cge = parsed.value;}
    if (nombre) {this.percod = nombre;}

    if (!entidad || this.entcod === null || !eje || this.eje === null ) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchAlmacenes();
    this.fetchAlmacenesSearch();
    this.getPagination();
  }

  fetchAlmacenes() {
    this.isLoading = true;
    this.limpiarMessages();

    this.http.get<any>(`${environment.backendUrl}/api/mea/fetch-articulos-por-almacen/${this.entcod}?page=${this.page}`).subscribe({
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

  pagination: number = 0;
  getPagination() {
    this.http.get<any>(`${environment.backendUrl}/api/mea/get-pag/${this.entcod}`).subscribe({
      next: (res) => {
        this.pagination = Math.ceil(res/ 20);
      },
      error: (err) => {
        console.warn(err.error.error || err.error);
      }
    })
  }

  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0; return;}
  }
  get paginatedAlmacen(): any[] {return this.almacenes || [];}
  get totalPages(): number {return Math.max(1, Math.ceil((this.almacenes?.length ?? 0) / this.pageSize));}
  prevPage() {
    if (this.page == 0) return;
    this.page = this.page - 1;
    this.isSearching ? this.search() : this.fetchAlmacenes();
    return;
  }
  nextPage() {
    this.page = this.page + 1;
    this.isSearching ? this.search() : this.fetchAlmacenes();
    return;
  }

  isBloqueado(artblo: number): string {
    if (artblo == 1) return 'Sí'
    return 'No';
  }

  //main table functions
  sortField: 'art_Afa_AFACOD' | 'art_Asu_ASUCOD' | 'art_ARTCOD' | 'art_ARTDES' | 'art_ARTUNI' | 'art_ARTREF' | 'art_ARTBLO' | null = null;
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
    this.limpiarMessages();

    const source = this.paginatedAlmacen;
    if (!source?.length) {
      this.almacenError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any) => ({
      afacod: row.art_Afa_AFACOD ?? '',
      asucod: row.art_Asu_ASUCOD ?? '',
      artcod: row.art_ARTCOD ?? '',
      artdes: row.art_ARTDES ?? '',
      artuni: row.art_ARTUNI ?? '',
      artref: row.art_ARTREF ?? '',
      artblo: this.isBloqueado(row.art_ARTBLO) ?? ''
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
    doc.text('Consulta de articulos por almacen', 40, 40);

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      columnStyles: {
        afacod: { cellWidth: 10 },
        asucod: { cellWidth: 10 },
        artcod: { cellWidth: 10 },
        artdes: { cellWidth: 60 },
        artuni: { cellWidth: 10 },
        artref: { cellWidth: 15 },
        artblo: { cellWidth: 8 }
      }
    });

    doc.save('Consulta_de_articulos_por_almacen.pdf');
  }

  downloadExcel() {
    this.limpiarMessages();
    const rows = this.paginatedAlmacen;
    if (!rows || rows.length === 0) {
      this.almacenError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map(row => ({
      afacod: row.art_Afa_AFACOD ?? '',
      asucod: row.art_Asu_ASUCOD ?? '',
      artcod: row.art_ARTCOD ?? '',
      artdes: row.art_ARTDES ?? '',
      artuni: row.art_ARTUNI ?? '',
      artref: row.art_ARTREF ?? '',
      artblo: this.isBloqueado(row.art_ARTBLO) ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Consulta de artoculos por almacen']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Familias', 'Subfamilia', 'Código', 'Descripción', 'Estocaje', 'Referencia universal', 'Bloqueo']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 60 },
      { wch: 10 },
      { wch: 15 },
      { wch: 8 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Almacen');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_articulos_por_almacen.xlsx'
    );
  }

  almacenesSearch: any[] = [];
  fetchAlmacenesSearch() {
    this.http.get<any>(`${environment.backendUrl}/api/dep/fetch-almacenes-nombre/${this.entcod}/${this.eje}/${this.percod}/${this.cge}`).subscribe({
      next: (res) => {
        this.almacenesSearch = res;
        this.selectedAlmacenNombre = this.almacenesSearch[0]?.depcod;
        this.selectedAlmacenNombreSearch = this.almacenesSearch[0]?.depdes;
      },
      error: (err) => {
        console.error(err.error.error || err.error);
      }
    })
  }

  selectedAlmacenNombre: string = '';
  selectedAlmacenNombreSearch: string = '';
  mainSearch: string = '';
  familia: string = '';
  subfamilia: string = '';
  bloqueado:  'No bloqueados' | 'Bloqueados' | 'Todos' = 'No bloqueados';
  isSearching: boolean = false;
  search() {
    this.isLoading = true;
    this.isSearching = true;
    this.limpiarMessages();
    this.page = 0;

    let params = new HttpParams();
    
    if (this.mainSearch?.trim()) {
      params = params.set('mainSearch', this.mainSearch.trim());
    }
    if (this.familia?.trim()) {
      params = params.set('afaCod', this.familia.trim());
    }
    if (this.subfamilia?.trim()) {
      params = params.set('asuCod', this.subfamilia.trim());
    }
    if (this.bloqueado) {
      params = params.set('bloqueado', this.bloqueado);
    }
    if (this.selectedAlmacenNombre?.trim()) {
      params = params.set('almacen', this.selectedAlmacenNombre.trim());
    }
    
    params = params.set('page', this.page.toString());

    this.http.get<any>(`${environment.backendUrl}/api/mea/search-articulos/${this.entcod}`, { params }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.almacenes = res;
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.almacenError = err.error.error || err.error;
      }
    });
  }

  limpiarSearch() {
    this.mainSearch = '';
    this.familia = '';
    this.subfamilia = '';
    this.bloqueado = 'No bloqueados';
    this.isSearching = false;
  }

  //detail grid functions
  selectedAlmacen: any = null;
  almacenDetailError: string = '';
  almacenDetailSuccess: string = '';
  isUpdating: boolean = false;
  showDetails(almacen: any) {
    this.limpiarMessages();
    this.selectedAlmacen = almacen;
    this.tempAlmacen = almacen;
    this.showProveedores();
  }

  closeDetails() {
    this.selectedAlmacen = null;
    this.tempAlmacen = [];
    this.limpiarMessages();
    // this.activeDetailTab = null;
    // this.showProveedoresGrid = false;
    // this.showExistenciasGrid = false;
    // this.proveedores = [];
    // this.existencias = [];
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  kestvir: number | null = null;
  calculateKEstVir(meauni: number, measol: number, mearec: number) {
    if (!meauni || !measol || !mearec) {return;}
    this.kestvir = meauni - measol + mearec 
    return this.kestvir;
  }
  calculateKvalExi(meauni: number, meapmp: number) {
    if (!meauni || !meapmp) {return;}
    return meauni * meapmp;
  }
  calculateKUniSol(meauni: number, measol: number, mearec: number, meamin: number, meaopt: number): number {
    const kestvir = this.calculateKEstVir(meauni, measol, mearec);
    if (!kestvir || kestvir < meamin) {return 0;}
    this.kestvir = meaopt - kestvir;
    return this.kestvir;
  }

  tempAlmacen: any = {};
  isUpdate: boolean = false;
  allowToUpdate: boolean = false;

  // sub details functions
  activeDetailTab: 'proveedores' | null = null;
  proveedoresError: string = '';
  showProveedoresGrid: boolean = false;
  isLoadingProveedores: boolean = false;
  showProveedores() {
    this.limpiarMessages();
    this.showProveedoresGrid = true;
    this.activeDetailTab = 'proveedores';
    this.fetchProveedores();
  }

  proveedores: any = [];
  pageProv: number = 0;
  fetchProveedores() {
    this.limpiarMessages();
    this.isLoadingProveedores = true;
    const afacod = this.selectedAlmacen.art_Afa_AFACOD;
    const asucod = this.selectedAlmacen.art_Asu_ASUCOD;
    const artcod = this.selectedAlmacen.art_ARTCOD;

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
    if (inputPage >= 1 && inputPage <= this.totalPages) {this.pageProv = inputPage - 1;}
  }

  //misc
  limpiarMessages() {
    this.almacenSuccess = '';
    this.almacenError = '';
    this.almacenDetailError = '';
    this.almacenDetailSuccess = '';
    this.proveedoresError = '';
  }
}
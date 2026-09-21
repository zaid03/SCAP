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
  selector: 'app-mantenimiento-general-articulos',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './mantenimiento-general-articulos.component.html',
  styleUrls: ['./mantenimiento-general-articulos.component.css']
})
export class MantenimientoGeneralArticulosComponent {
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
          this.updatePagination;
        },
        error: (err) => {
          this.articuloError = err.error.error || err.error;
          this.isLoading = false;
        }
      })
    }
  
    pagination: number = 0;
    getPagination() {
      this.http.get<any>(`${environment.backendUrl}/api/art/get-pag/${this.entcod}`).subscribe({
        next: (res) => {
          this.pagination = this.Math.ceil(res/20);
        },
        error: (err) => {
          console.warn(err.error.error || err.error);
        }
      })
    }
    private updatePagination(): void {const total = this.totalPages;
      if (total === 0) {this.page = 0; return;}
    }
    get paginatedArticulos(): any[] {return this.articulos || [];}
    get totalPages(): number {return Math.max(1, Math.ceil((this.articulos?.length ?? 0) / this.pageSize));}
    prevPage() {
      if (this.page == 0) return;
      this.page = this.page - 1;
      this.isSearching ? this.searchArticulo() : this.fetchArticulos();
      return;
    }
    nextPage() {
      this.page = this.page + 1;
      this.isSearching ? this.searchArticulo() : this.fetchArticulos();
      return;
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
  
      const source = this.paginatedArticulos;
      if (!source?.length) {
        this.articuloError = 'No hay datos para exportar.';
        return;
      }
  
      const rows = source.map((row: any) => ({
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
        body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
        styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
        headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
        columnStyles: {
          afacod: { cellWidth: 10 },
          asucod: { cellWidth: 10 },
          artcod: { cellWidth: 10 },
          artdes: { cellWidth: 50 },
          artuni: { cellWidth: 10 },
          artref: { cellWidth: 15 },
          artblo: { cellWidth: 8 }
        }
      });
  
      doc.save('Consulta_general_de_articulos.pdf');
    }
  
    downloadExcel() {
      this.limpiarMessages();
      const rows = this.paginatedArticulos;
      if (!rows || rows.length === 0) {
        this.articuloError = 'No hay datos para exportar.';
        return;
      }
    
      const exportRows = rows.map(row => ({
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
    isSearching: boolean = false;
    searchArticulo () {
      this.limpiarMessages();
      this.page = 0;
      this.isLoading = true;
      this.isSearching = true; 
  
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
      this.isSearching = false; 
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
    backupData: any = [];
    modificar() {
      this.isUpdate = true;
      this.backupData = this.selectedArticulo ? { ...this.selectedArticulo } : {};
    }
  
    cancelar() {
      this.isUpdate = false;
      this.tempArticulo = { ...this.backupData };
    }
  
    updateSuccess() {
      this.isUpdate = false;
      this.allowToUpdate = false;
    }
  
    allowToUpdate: boolean = false;
    isUpdateAllowed() {
      if (this.allowToUpdate) {
        this.updateArticulo();
      } else {
        return;
      }
    }
  
    updateArticulo() {
      this.limpiarMessages();
      Object.assign(this.selectedArticulo, this.tempArticulo);
  
      const payload = {
        "ARTDES": this.selectedArticulo.artdes, 
        "ARTREF": this.selectedArticulo.artref, 
        "ARTBLO": this.selectedArticulo.artblo, 
        "AUNCOD": this.auncodMod,
        "ARTUCO": this.selectedArticulo.artuco,
        "ARTUEM": this.selectedArticulo.artuem,
        "ARTMIN": this.selectedArticulo.artmin,
        "ARTOPT": this.selectedArticulo.artopt
      }
      if (!payload) {return;}
  
      const afacod = this.selectedArticulo.afacod;
      const subfamilia = this.selectedArticulo.asucod;
      const articulo = this.selectedArticulo.artcod;
      this.isUpdating = true;
  
      this.http.patch<any>(`${environment.backendUrl}/api/art/update-art/${this.entcod}/${afacod}/${subfamilia}/${articulo}`, payload).subscribe({
        next: (res) => {
          this.isUpdating = false;
          this.updateSuccess();
          this.fetchArticulos();
          this.articuloDetailSuccess = 'Artículo actualizado correctamente';
        },
        error: (err) => {
          this.articuloDetailError = err.error.error ?? err.error;
          this.isUpdating = false;
        }
      })
    }
  
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
    
    showDeleteConfirm: boolean = false;
    openDeleteConfirm() {
      this.showDeleteConfirm = true;
    }
  
    closeDeleteGrid() {
      this.showDeleteConfirm = false;
    }
  
    confirmDelete(): void {
      if (this.selectedArticulo) {
        this.deleteArticulo();
      }
    }
  
    isDeleting: boolean = false;
    delErr: string = '';
    deleteArticulo() {
      this.limpiarMessages();
      this.isDeleting = true;
  
      const afacod = this.selectedArticulo.afacod;
      const subfamilia = this.selectedArticulo.asucod;
      const articulo = this.selectedArticulo.artcod;
  
      this.http.delete(`${environment.backendUrl}/api/art/delete-art/${this.entcod}/${afacod}/${subfamilia}/${articulo}`).subscribe({
        next: (res) => {
          this.isDeleting = false;
          this.articuloDetailSuccess = 'Artículo eliminado correctamente';
          this.fetchArticulos();
          this.closeDeleteGrid();
          this.closeDetails();
        },
        error: (err) => {
          this.isDeleting = false;
          this.delErr = err.error.error ?? err.error
        }
      })
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
      if (inputPage >= 1 && inputPage <= this.totalPages) {this.pageProv = inputPage - 1;}
    }
  
    updateProveedor(tercodUp: number, aprref: string, apruem: any, aprobs: string) {
      this.limpiarMessages();
  
      const afacod = this.selectedArticulo.afacod;
      const asucod = this.selectedArticulo.asucod;
      const artcod = this.selectedArticulo.artcod;
      const tercod = tercodUp;
  
      const payload = {
        "APRREF": aprref,
        "APROBS": aprobs,
        "APRUEM": apruem
      }
  
      this.isUpdating = true;
      this.http.patch(`${environment.backendUrl}/api/more/update-prov-info/${this.entcod}/${afacod}/${asucod}/${artcod}/${tercod}`, payload).subscribe({
        next: (res) => {
          this.isUpdating = false;
          this.fetchProveedores();
          this.articuloDetailSuccess = 'Proveedor actualizado correctamente';
        },
        error: (err) => {
          this.proveedoresError = err.error.error ?? err.error;
          this.isUpdating = false;
        }
      })
    }
  
    deleteProveedorGrid: boolean = false;
    delErrProv: string = '';
    delTercod: number | null = null;
    showdeleteProv(tercodUp: number) {
      this.deleteProveedorGrid = true;
      this.delTercod = tercodUp;
    }
  
    closeDeleteProv() {
      this.limpiarMessages();
      this.deleteProveedorGrid = false;
      this.delTercod = null;
    }
  
    deleteProveedor() {
      const afacod = this.selectedArticulo.afacod;
      const asucod = this.selectedArticulo.asucod;
      const artcod = this.selectedArticulo.artcod;
      const tercod = this.delTercod;
  
      this.isDeleting = true;
      this.http.delete(`${environment.backendUrl}/api/more/delete-proveedor-art/${this.entcod}/${afacod}/${asucod}/${artcod}/${tercod}`).subscribe({
        next: (res) => {
          this.isDeleting = false;
          this.fetchProveedores();
          this.closeDeleteProv();
          this.articuloDetailSuccess = 'Proveedor eliminada exitosamente';
        },
        error: (err) => {
          this.delErrProv = err.error.error ?? err.error;
          this.isDeleting = false;
        }
      })
    }
  
    showProveedorGrid: boolean = false;
    error: string = '';
    openProveedores() {
      this.limpiarMessages();
      this.showProveedorGrid = true;
      this.fetchProveedoresAdd();
    }
  
    closeProveedores() {
      this.showProveedorGrid = false;
      this.proveedores = null;
      this.searchTerm = '';
      this.filterOption = 'noBloqueados';
      this.proveedoresToAdd = null;
      this.provsNumber = 0;
    }
  
    proveedoresAdd: any = [];
    isLoadingPro: boolean = false;
    fetchProveedoresAdd() {
      this.isLoadingPro = true;
      this.http.get<any>(`${environment.backendUrl}/api/ter/by-ent/${this.entcod}`).subscribe({
        next: (response) => {
          this.proveedoresAdd = response;
          this.pagePro = 0;
          this.isLoadingPro = false;
          this.updatePaginationPro();
        },
        error: (err) => {
          this.proveedoresAdd = [];
          this.error = err.error.error ?? err.error;
          this.isLoading = false;
        }
      });
    }
    pagePro = 0;
    pageSizePro = 20;
    get totalPagesPro(): number {
      return Math.max(1, Math.ceil((this.proveedoresAdd?.length ?? 0) / this.pageSizePro));
    }
    get paginatedProveedoresAdd() {
      if (!this.proveedoresAdd || this.proveedoresAdd.length === 0) return [];
      const start = this.pagePro * this.pageSizePro;
      return this.proveedoresAdd.slice(start, start + this.pageSizePro);
    }
    nextPagePro() {if (this.pagePro < this.totalPagesPro - 1) {this.pagePro++;}}
    prevPagePro() {if (this.pagePro > 0) {this.pagePro--;}}
    goToPagePro(event: any) {const inputPage = Number(event.target.value); if (inputPage >= 1 && inputPage <= this.totalPagesPro) {this.pagePro = inputPage - 1;}}
    private updatePaginationPro(): void {const total = this.totalPagesPro;
      if (total === 0) {this.pagePro = 0; return;}
      if (this.pagePro >= total) {this.pagePro = total - 1;}
    }
  
    searchTerm: string = '';
    filterOption: string = 'Nobloqueado';
    searchProveedor() {
      this.limpiarMessages();
      this.isLoadingPro = true
      const params = {
        ent: this.entcod || '',
        searchMode: this.filterOption,
        term: this.searchTerm
      };
      this.http.get<any[]>(`${environment.backendUrl}/api/ter/search-proveedores`, {params}).subscribe({
        next: (response) => {
          this.proveedoresAdd = response;
          this.page = 0;
          this.isLoadingPro = false;
        },
        error: (err) => {
          this.proveedoresAdd = [];
          this.error = err.error.error ?? err.error;
          this.isLoadingPro = false;
        }
      })
    }
  
    clearSearch() {
      this.limpiarMessages();
      this.fetchProveedores();
      this.filterOption = 'Nobloqueado';
      this.page = 0;
      this.searchTerm = '';
    }
  
    proveedoresToAdd: any = [];
    provsNumber: number = 0;
    selectProveedor(tercod: any) {
      if (this.proveedoresToAdd.includes(tercod)) {
        this.proveedoresToAdd = this.proveedoresToAdd.filter((item: any) => item !== tercod);
      } else {
        this.proveedoresToAdd = [...this.proveedoresToAdd, tercod];
      }
  
      this.provsNumber = this.proveedoresToAdd.length;
    }
  
    isSelected(tercod: number) {
      if (this.proveedoresToAdd.includes(tercod)) {
        return true;
      }
      return false;
    }
  
    addProveedorToArticulo() {
      const afacod = this.selectedArticulo.afacod;
      const asucod = this.selectedArticulo.asucod;
      const artcod = this.selectedArticulo.artcod;
      const tercod = this.proveedoresToAdd;
  
      const payload = {
        "ENT": this.entcod,
        "AFACOD": afacod,
        "ASUCOD": asucod,
        "ARTCOD": artcod,
        "tercods": tercod
      }
  
      this.isLoadingPro = true;
      this.http.post(`${environment.backendUrl}/api/more/add-terceros`, payload, { responseType: 'text' as const }).subscribe({
         next: (res: string) => {
          this.isLoadingPro = false;
          this.closeProveedores();
          this.fetchProveedores();
          this.articuloDetailSuccess = res;
        },
        error: (err) => {
          this.isLoadingPro = false;
          this.delErrProv = err.error.error ?? err.error;
        }
      })
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
      if (inputPage >= 1 && inputPage <= this.totalPages) {this.pageExistencia = inputPage - 1;}
    }
  
    //add articulo grid funtions
    showAddGrid: boolean = false;
    addArticuloError: string = '';
    isAdding: boolean =false;
    openAdd() {
      this.limpiarMessages();
      this.showAddGrid = true;
      this.fetchTipos();
      this.fetchFamilias();
    }
  
    closeAdd() {
      this.showAddGrid = false;
      this.clearAdd();
      this.clearTipos();
    }
  
    familiaAdd: string = '';
    subfamiliasAdd: string = '';
    codigoAdd: string ='';
    bloqAdd: number = 0;
    existenciasAdd: number = 0;
    descriptionAdd: string = '';
    uniEmbalaje: number = 0;
    estoMinimo: number = 0;
    refUni: string = '';
    pteServir: number = 0;
    preMePon: number = 0;
    estoOptimo: number = 0;
    pteEntrada: number = 0;
    uniDeCon: number = 0;
    toUpperCase(event: Event) {
      const input = event.target as HTMLInputElement;
      input.value = input.value.toLocaleUpperCase();
    }
  
    clearAdd() {
      this.codigoAdd = '';
      this.bloqAdd = 0;
      this.existenciasAdd = 0;
      this.descriptionAdd = '';
      this.uniEmbalaje = 0;
      this.estoMinimo = 0;
      this.refUni = '';
      this.pteServir = 0;
      this.preMePon = 0;
      this.estoOptimo = 0;
      this.pteEntrada = 0;
      this.uniDeCon = 0;
      this.familiaAdd = '';
      this.subfamiliasAdd = '';
      this.familias = [];
      this.subfamilias = [];
    }
  
    familias: any = [];
    fetchFamilias() {
      this.http.get(`${environment.backendUrl}/api/afa/fetching-fams/${this.entcod}`).subscribe({
        next: (res) => {
          this.familias = res;
        },
        error: (err) => {
          console.warn(err.error.error ?? err.error);
        }
      })
    }
  
    subfamilias: any = [];
    fetchSubFamilias(afacod: string) {
      this.http.get(`${environment.backendUrl}/api/asu/fetching-subs/${this.entcod}/${afacod}`).subscribe({
        next: (res) => {
          this.subfamilias = res;
        },
        error: (err) => {
          console.warn(err.error.error ?? err.error);
        }
      })
    }
  
    addArticulos() {
      this.limpiarMessages();
  
      if (this.familiaAdd === '' || this.subfamiliasAdd === '' || this.codigoAdd === '' || this.descriptionAdd === '') {
        this.addArticuloError = 'Rellene todos los campos obligatorios.';
        return;
      }
  
      const payload = {
        "ENT": this.entcod,
        "AFACOD": this.familiaAdd,
        "ASUCOD": this.subfamiliasAdd,
        "ARTCOD": this.codigoAdd,
        "ARTDES": this.descriptionAdd,
        "ARTREF": this.refUni,
        "ARTBLO": this.bloqAdd,
        "ARTUNI": this.existenciasAdd,
        "ARTSOL": this.pteServir,
        "ARTREC": this.pteEntrada,
        "AUNCOD": this.auncodMod,
        "ARTUCO": this.uniDeCon,
        "ARTUEM": this.uniEmbalaje,
        "ARTPMP": this.preMePon,
        "ARTMIN": this.estoMinimo,
        "ARTOPT": this.estoOptimo 
      }
      
      this.isAdding = true
      this.http.post(`${environment.backendUrl}/api/art/add-art`, payload).subscribe({
        next: (res) => {
          this.isAdding = false;
          this.closeAdd();
          this.fetchArticulos();
        },
        error: (err) => {
          this.addArticuloError = err.error.error ?? err.error;
          this.isAdding = false;
        }
      })
    }
  
    //misc
    limpiarMessages() {
      this.articuloError = '';
      this.articuloSuccess = '';
      this.articuloDetailError = '';
      this.articuloDetailSuccess = '';
      this.proveedoresError = '';
      this.existenciasError = '';
      this.delErr = '';
      this.addArticuloError = '';
      this.delErrProv = '';
    }
}
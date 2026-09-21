import { Component, HostListener } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, TitleStrategy } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { environment } from '../../environments/environment';
import { CurrencyPipe } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-contratos',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  providers: [CurrencyPipe],
  templateUrl: './contratos.component.html',
  styleUrls: ['./contratos.component.css']
})
export class ContratosComponent {

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

  constructor(private http: HttpClient, private router: Router, private currencyPipe: CurrencyPipe) {}

  //global variables
  entcod: string | null = null;
  eje: number | null = null;
  orgCode: string | null = null;
  entidad: string | null = null;
  isLoading: boolean = false;
  contratos: any[] = [];
  backupContratos: any[] = [];
  defaultContratos: any[] = [];
  mainError: string = '';
  mainSuccess: string = '';
  page = 0;
  pageSize = 20;

  ngOnInit(): void {
    this.limpiarMessages();
    const ent = sessionStorage.getItem('Entidad');
    const session = sessionStorage.getItem('EJERCICIO');
    const orgnizacion = sessionStorage.getItem('WSORG');
    const entcod = sessionStorage.getItem('WSENT');
   
    if (ent) { const parsed = JSON.parse(ent); this.entcod = parsed.ENTCOD;}
    if (session) { const parsed = JSON.parse(session); this.eje = parsed.eje;}
    if (orgnizacion) {const parsed = JSON.parse(orgnizacion); this.orgCode = parsed.WSORG;}
    if (entcod) {const parsed = JSON.parse(entcod); this.entidad = parsed.WSENT};

    if (this.entcod == null || !this.eje || this.orgCode === '' || this.entidad === '') {
      alert('Missing session data. reiniciar el flujo.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchContratos();
  }

  //main functions
  fetchContratos() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/con/fetch-contratos/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.contratos = res;
        this.backupContratos = Array.isArray(res) ? [...res] : [];
        this.defaultContratos = [...this.backupContratos];
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.contratos = [];
        this.mainError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    })
  }

  get paginatedContratos(): any[] {
    if (!this.contratos || this.contratos.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.contratos.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.contratos?.length ?? 0) / this.pageSize));
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

  toggleSort(field: 'concod' | 'conlot' | 'condes' | 'confin' | 'conffi' | 'conblo' | 'tercod' | 'ternom'): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.contratos = [...this.defaultContratos];
      this.page = 0;
      this.updatePagination();
      return;
    }

    this.applySort();
  }

  sortField: 'concod' | 'conlot' | 'condes' | 'confin' | 'conffi' | 'conblo' | 'tercod' | 'ternom' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const sorted = [...this.contratos].sort((a, b) => {
      const extract = (item: any, prop: string) =>
        (item?.[prop] ?? item?.[prop.toUpperCase()] ?? '').toString();
      const aVal = extract(a, field);
      const bVal = extract(b, field);
      return this.sortDirection === 'asc'
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
    });

    this.contratos = sorted;
    this.page = 0;
    this.updatePagination();
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

  private updatePagination(): void {
    const total = this.totalPages;
    if (total === 0) {
      this.page = 0;
      return;
    }
    if (this.page >= total) {
      this.page = total - 1;
    }
  }

  setBloqueado(conblo: number) {
    if (conblo == 0) {return 'No';}
    else if (conblo == 1) {return 'Si';}
    else {return 'no'}
  }

  excelDownload() {
    this.limpiarMessages();
    const rows = this.contratos;
    if (!rows || rows.length === 0) {
      this.mainError = 'No hay datos para exportar.';
      return;
    }

    const exportRows = rows.map((row, index) => ({
      '#': index + 1,
      Número: row.concod ?? '',
      Económica : row.conlot ?? '',
      Descripción: row.condes ?? '',
      Fecha_inicio: row.confin ?? '',
      Fecha_fin: row.conffi ?? '',
      Bloqueado: this.setBloqueado(row.conblo) ?? '',
      Cód_Proveedor: row.tercod ?? '',
      Proveedor: row.ternom ?? ''
    }));

    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de contratos']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['#', 'Número', 'Económica', 'Descripción', 'Fecha inicio', 'Fecha fin', 'Bloqueado', 'Cód Proveedor', 'Proveedor']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 6 },
      { wch: 10 },
      { wch: 25 },
      { wch: 55 },
      { wch: 25 },
      { wch: 25 },
      { wch: 10 },
      { wch: 10 },
      { wch: 40 }
    ];

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'contratos');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'contratos.xlsx'
    );
  }

  exportPdf() {
    this.limpiarMessages();
    const source = this.contratos;
    if (!source?.length) {
      this.mainError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      index: index + 1,
      concod: row.concod ?? '',
      conlot: row.conlot ?? '',
      condes: row.condes ?? '',
      confin: (row.confin ?? '').toString().slice(0, 10),
      conffi: (row.conffi ?? '').toString().slice(0, 10),
      conblo: this.setBloqueado(row.conblo) ?? '',
      tercod: row.tercod ?? '',
      ternom: row.ternom ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de contratos', 40, 40);

    const columns = [
      { header: '#', dataKey: 'index' },
      { header: 'Número', dataKey: 'concod' },
      { header: 'Económica', dataKey: 'conlot' },
      { header: 'Descripción', dataKey: 'condes' },
      { header: 'Fecha_inicio', dataKey: 'confin' },
      { header: 'Fecha_fin', dataKey: 'conffi' },
      { header: 'Bloqueado', dataKey: 'conblo' },
      { header: 'Cód_Proveedor', dataKey: 'tercod' },
      { header: 'Proveedor', dataKey: 'ternom' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => (row as any)[col.dataKey] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('contratos.pdf');
  }

  term: string = '';
  searchMode: string = 'noBloque';
  search() {
    this.limpiarMessages();
    this.isLoading = true;

    this.http.get<any>(`${environment.backendUrl}/api/con/searchByCodigoBloque/${this.entcod}/${this.eje}/${this.searchMode}`, {params: { term: this.term || '' }}).subscribe({
      next: (res) => {
        this.contratos = res;
        this.backupContratos = Array.isArray(res) ? [...res] : [];
        this.defaultContratos = [...this.backupContratos];
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.contratos = [];
        this.mainError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    })
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.fetchContratos();
    this.searchMode = 'noBloque'
    this.term = '';
  }

  //detail grid
  selectedContrato: any = null;
  updateContratoSuccess: string = '';
  updateContratoError: string = '';
  isUpdating: boolean = false;
  openDetails(contrato: any) {
    this.limpiarMessages();
    this.selectedContrato = contrato;
    this.tempContrato = contrato;
    const concod = this.selectedContrato.concod;
    this.showArticulos(concod);
  }

  closeDetails() {
    this.limpiarMessages();
    this.selectedContrato = null;
    this.centroGestor = [];
    this.articulos = [];
    this.showCentroGestorGrid = false;
    this.showArticulosGrid = false;
    this.activeDetailTab = null;
  }

  closeDetailsSure() {if (this.isUpdate) {return;}
    else {this.closeDetails();}
  }

  tempContrato: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.tempContrato ? { ...this.tempContrato } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempContrato = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }


  allowToUpdate: boolean = false;
  isUpdateAllowed(numero: number, bloque: number, fecha_ini: string, fecha_fin: string, descripción: String) {
    if (this.allowToUpdate) {
      this.updateContrato(numero, bloque, fecha_ini, fecha_fin, descripción);
    } else {
      return;
    }
  }

  updateContrato(numero: number, bloque: number, fecha_ini: string, fecha_fin: string, descripción: String) {
    this.limpiarMessages();
    this.isUpdating = true;

    Object.assign(this.selectedContrato, this.tempContrato);

    const payload = {
      "CONBLO": bloque,
      "CONFIN": fecha_ini ? new Date(fecha_ini).toISOString() : null,
      "CONFFI": fecha_fin ? new Date(fecha_fin).toISOString() : null,
      "CONDES": descripción
    }

    this.http.patch(`${environment.backendUrl}/api/con/update-contrato/${this.entcod}/${this.eje}/${numero}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.isUpdating = false;
        this.updateContratoSuccess = 'contrato actualizado exitosamente';
      },
      error: (err) => {
        this.isUpdating = false;
        this.updateContratoError = err.error.error ?? err.error;
      }
    })
  }

  //add grid
  showAddGrid: boolean = false;
  addContratoError: string = '';
  isAdding: boolean = false;
  openAdd() {
    this.limpiarMessages();
    this.showAddGrid = true;
  }

  closeAdd() {
    this.showAddGrid = false;
    this.proveedores = [];
    this.proveedorTercod = null;
    this.proveedorTernom = null;
    this.economicaAdd = '';
    this.description = '';
    this.fecha_ini = '';
    this.fecha_final = '';
  }

  showProveedorGrid: boolean = false;
  error: string = '';
  openProveedores() {
    this.limpiarMessages();
    this.showProveedorGrid = true;
    this.error = 'No hay proveedores';
  }

  closeProveedores() {
    this.showProveedorGrid = false;
    this.proveedores = null;
    this.searchTerm = '';
    this.filterOption = 'Nobloqueado';
  }

  proveedores: any = [];
  isLoadingPro: boolean = false;
  pagePro = 0;
  pageSizePro = 20;
  get totalPagesPro(): number {
    return Math.max(1, Math.ceil((this.proveedores?.length ?? 0) / this.pageSizePro));
  }
  get paginatedProveedores() {
    if (!this.proveedores || this.proveedores.length === 0) return [];
    const start = this.pagePro * this.pageSizePro;
    return this.proveedores.slice(start, start + this.pageSizePro);
  }
  nextPagePro() {
    if (this.pagePro < this.totalPagesPro - 1) {this.pagePro++;}
  }
  prevPagePro() {
    if (this.pagePro > 0) {this.pagePro--;}
  }
  goToPagePro(event: any) {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPagesPro) {this.pagePro = inputPage - 1;}
  }
  private updatePaginationPro(): void {
    const total = this.totalPagesPro;
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
        this.proveedores = response;
        this.page = 0;
        this.isLoadingPro = false;
      },
      error: (err) => {
        this.proveedores = [];
        this.error = err.error.error ?? err.error;
        this.isLoadingPro = false;
      }
    })
  }

  clearSearch() {
    this.limpiarMessages();
    this.error = 'No hay proveedores';
    this.filterOption = 'Nobloqueado';
    this.page = 0;
    this.searchTerm = '';
  }

  proveedorTercod: number | null = null
  proveedorTernom: string | null = null;
  selectProveedor(codigo: any) {
    this.proveedorTercod = codigo.tercod;
    this.proveedorTernom = codigo.ternom;
    this.closeProveedores();
  }

  onEconomicaInput(event: any): void {
    let value = event.target.value;
    let formatted = value.replace(/\D/g, '');
    formatted = formatted.substring(0, 5);
    event.target.value = formatted;
    this.economicaAdd = formatted;
  }

  conblo: number = 0;
  economicaAdd: string = '';
  description: string = '';
  fecha_ini: string = '';
  fecha_final: string = '';
  addContrato() {
    this.limpiarMessages();

    if (this.economicaAdd === '' || this.description === '') {this.addContratoError = 'Económica y -	Descripción no pueden estar vacías.'; return;}
    if (this.proveedorTercod === null) {this.addContratoError = 'Seleccione una proveedor'; return;}

    const payload = {
      "ENT": this.entcod,
      "EJE": this.eje,
      "CONLOT": this.economicaAdd,
      "CONDES": this.description,
      "CONFIN": this.fecha_ini ? new Date(this.fecha_ini).toISOString() : null,
      "CONFFI": this.fecha_final ? new Date(this.fecha_final).toISOString() : null,
      "CONBLO": this.conblo,
      "TERCOD": this.proveedorTercod
    }

    this.isAdding = true;
    this.http.post<any>(`${environment.backendUrl}/api/con/add-contrato`, payload).subscribe({
      next: (res) => {
        this.isAdding = false;
        this.numeroContrato = res[0].concod;
        this.contratos = [...this.contratos, res[0]];
        console.log(" new cntns", this.contratos)
        this.contratosAddConfirmationOpen();
      },
      error: (err) => {
        this.addContratoError = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }

  cntrtsAddCnfrm: boolean = false;
  numeroContrato?: number;
  contratosAddConfirmationOpen() {
    this.limpiarMessages();
    this.cntrtsAddCnfrm = true;
  }

  contratosAddConfirmationClose() {
    this.cntrtsAddCnfrm = false;
    this.numeroContrato = undefined;
    this.fetchContratos();
    this.closeAdd();
  }

  openNewCntrtDetails() {
    this.limpiarMessages();
    this.openDetails(this.contratos.at(-1));
    this.cntrtsAddCnfrm = false;
    this.closeAdd();
  }

  //sub detail's articulo's grids
  articulosError: string = '';
  articulosSuccess: string = '';
  showArticulosGrid = false;
  articulos: any[] = [];
  showArticulos(numero: number) {
    this.limpiarMessages();
    this.showArticulosGrid = true;
    this.activeDetailTab = 'articulos';
    this.showCentroGestorGrid = false;
    this.centroGestor = [];
    this.fetchArticulos(numero);
  }

  isLoadingArticulos: boolean = false;
  articuloError: string = '';
  fetchArticulos(numero: number) {
    this.isLoadingArticulos = true;
    const concod = numero;
    this.http.get<any[]>(`${environment.backendUrl}/api/coa/fetch-articulos/${this.entcod}/${this.eje}/${concod}`).subscribe({
      next: (res) => {
        this.isLoadingArticulos = false;
        this.articulos = res;
        this.pageARt = 0;
      },
      error: (err) => {
        this.isLoadingArticulos = false;
        this.articuloError = err.error.error ?? err.error;
      }
    })
  }
  pageARt = 0;
  get paginatedArticulos(): any[] {
    if (!this.articulos || this.articulos.length === 0) return [];
    const start = this.pageARt * this.pageSize;
    return this.articulos.slice(start, start + this.pageSize);
  }
  get totalPagesART(): number {
    return Math.max(1, Math.ceil((this.articulos?.length ?? 0) / this.pageSize));
  }
  prevPageART(): void {
    if (this.pageARt > 0) this.pageARt--;
  }
  nextPageART(): void {
    if (this.pageARt < this.totalPagesART - 1) this.pageARt++;
  }
  goToPageART(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPagesART) {
      this.pageARt = inputPage - 1;
    }
  }

  updateArticulo(numero: number, familia: string, subfamilia: string, articulo: string, precio: any) {
    this.isUpdating = true;
    this.limpiarMessages();

    const concod = numero;
    const afacod = familia;
    const asucod = subfamilia;
    const artcod = articulo;
    if (!concod || !afacod || !asucod || !artcod) {return;}

    const payload = {
      "COAPRE": precio
    }

    this.http.patch(`${environment.backendUrl}/api/coa/update-articulo/${this.entcod}/${this.eje}/${concod}/${afacod}/${asucod}/${artcod}`, payload).subscribe({
      next: (res) => {
        this.isUpdating = false;
        this.articulosSuccess = 'Artículo actualizado con éxito';
      },
      error: (err) => {
        this.isUpdating = false;
        this.articulosError = err.error.error ?? err.error ?? 'wtf';
      }
    })
  }

  formatPrice(value: number | null): string {
    return this.currencyPipe.transform(value ?? 0, 'EUR', 'symbol', '1.2-2', 'es-ES') ?? '';
  }

  startEditingPrice(item: any, event: FocusEvent) {
    item.editingPrice = true;
    item.editingValue = item.coapre != null ? item.coapre.toFixed(2) : '';
    (event.target as HTMLInputElement).value = item.editingValue;
  }

  stopEditingPrice(item: any, event: FocusEvent) {
    item.editingPrice = false;
    (event.target as HTMLInputElement).value = this.formatPrice(item.coapre);
  }

  onPriceTyping(event: Event, item: any) {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/[^0-9.,-]/g, '').replace(',', '.');
    item.editingValue = raw;
    item.coapre = raw ? Number(raw) : 0;
  }

  isDeleting: boolean = false;
  deleteArticulo(numero: number, familia: string, subfamilia: string, articulo: string) {
    this.limpiarMessages();
    this.isDeleting = true;

    const concod = numero;
    const afacod = familia;
    const asucod = subfamilia;
    const artcod = articulo;
    if (!concod || !afacod || !asucod || !artcod) {return;}

    this.http.delete(`${environment.backendUrl}/api/coa/delete-articulo/${this.entcod}/${this.eje}/${concod}/${afacod}/${asucod}/${artcod}`).subscribe({
      next: (res) => {
        this.isDeleting = false;
        this.articulosSuccess = 'Artículo eliminado exitosamente';
        this.fetchArticulos(concod);
        this.closeDeleteArticulo();
      },
      error: (err) => {
        this.isDeleting = false;
        this.detallesMessageErrorDelete = err.error.error ?? err.error ?? 'wtf';
      }
    })
  }

  deleteArticuloGrid: boolean = false;
  articuloToDelete: any[] = [];
  detallesMessageErrorDelete: string = '';
  openDeleteArticulo(contrato: any, articulo:any) {
    this.deleteArticuloGrid = true;
    this.articuloToDelete = [contrato, articulo];
  }

  closeDeleteArticulo() {
    this.deleteArticuloGrid = false;
    this.detallesMessageErrorDelete = '';
  }

  //articulos add grid
  articulosAddGrid: boolean = false;
  ArticuloAddSuccess: string = '';
  articulosAddError: string = '';
  isLoadingArticulo: boolean = false;
  searchValue: string = '';
  articulosAdd: any[] = [];
  conlot: number = 0;
  showArticulosAddGrid(conlot: number) {
    this.articulosAddGrid = true;
    this.conlot = conlot;
    this.fetchArticulosAdd(conlot);
  }

  closeArticuloAddGrid() {
    this.articulosAddGrid = false;
    this.searchValue = '';
    this.searchPage = 0;
    this.articulosAdd = [];
    this.coughtArticulos = [];
    this.isSearchDisabled = false;
  }

  isSearchDisabled: boolean = false;
  fetchArticulosAdd(conlot: number) {
    this.limpiarMessages();
    this.isLoadingArticulo = true;

    this.http.get<any>(`${environment.backendUrl}/api/art/art-cont/${this.entcod}/${conlot}`).subscribe({
      next: (res) => {
        this.articulosAdd = res;
        this.isLoadingArticulo = false;
      },
      error: (err) => {
        this.isLoadingArticulo = false;
        if (err.status === 404) {
          this.isSearchDisabled = true;
          this.articulosAdd = [];
        }
        this.articulosAddError = err.error.error ?? err.error;
      }
    })
  }

  searchArticuloAdd() {
    this.limpiarMessages();
    this.coughtArticulos = []
    if (this.searchValue === '') {return;}

    const isOnlyNumbers = (value: string) => /^\d+$/.test(value);

    this.isLoadingArticulo = true;
    if(isOnlyNumbers(this.searchValue)) {
      this.http.get<any>(`${environment.backendUrl}/api/art/search-art-cont/${this.entcod}/${this.conlot}/${this.searchValue}`).subscribe({
        next: (res) => {
          this.articulosAdd = res;
          this.isLoadingArticulo = false;
        },
        error: (err) => {
          this.articulosAdd = [];
          this.isLoadingArticulo = false;
          this.articulosAddError = err.error.error ?? err.error;
        }
      })
    } else {
      this.http.get<any>(`${environment.backendUrl}/api/art/search-art-cont-des/${this.entcod}/${this.conlot}/${this.searchValue}`).subscribe({
        next: (res) => {
          this.articulosAdd = res;
          this.isLoadingArticulo = false;
        },
        error: (err) => {
          this.articulosAdd = [];
          this.isLoadingArticulo = false;
          this.articulosAddError = err.error.error ?? err.error;
        }
      })
    }
  }

  limpiarArticulosAdd() {
    this.limpiarMessages();
    this.searchValue = '';
    this.searchPage = 0;
    this.fetchArticulosAdd(this.conlot);
    this.coughtArticulos = [];
  }
  searchPage: number = 0;
  searchPageSize: number = 5;
  get paginatedSearchResults() {const start = this.searchPage * this.searchPageSize; return this.articulosAdd.slice(start, start + this.searchPageSize);}
  get searchTotalPages() {return Math.ceil(this.articulosAdd.length / this.searchPageSize);}

  coughtArticulos: any[] = [];
  selectArticulosAdd(articulos: any) {
    if (this.coughtArticulos.includes(articulos)) {
      const index = this.coughtArticulos.indexOf(articulos);
      if(index !== -1) {
        this.coughtArticulos.splice(index, 1);
      }
    } else {
      this.coughtArticulos = [...this.coughtArticulos, articulos];
    }

  }

  isArticulosSelected(a: any): boolean {
    return this.coughtArticulos.includes(a);
  }

  isAddingArticulo: boolean = false;
  savedNames: any[] = [];
  unSavedNames: any[] = [];
  saveArticulos() {
    this.limpiarMessages();
    this.isAddingArticulo = true;

    const concod = this.selectedContrato.concod;

    const payload = this.coughtArticulos.map(obj => ({
      ent: this.entcod,
      eje: this.eje,
      concod: concod,
      afacod: obj.afacod,
      asucod: obj.asucod,
      artcod: obj.artcod,
      COAPRE: 0,
      COAPR2: 0,
      COAPR3: 0,
      COAPR4: 0,
      COAPR5: 0
    }));

    this.http.post<any>(`${environment.backendUrl}/api/coa/save-articulos`, payload).subscribe({
      next: (res) => {
        this.savedNames = res.savedNames;
        this.unSavedNames = res.unsavedNames;
        this.isAddingArticulo = false;
        this.openSavedArtculos()
      },
      error: (err) => {
        this.articulosAddError = err.error.error ?? err.error;
        this.isAddingArticulo = false;
      }
    })
  }

  openSavedMessagesGrid: boolean = false;
  openSavedArtculos() {
    this.openSavedMessagesGrid = true;
  }

  closeSavedArticulos() {
    this.openSavedMessagesGrid = false;
    this.closeArticuloAddGrid();    
    this.showArticulos(this.selectedContrato.concod);
  }

  //sub detail's centro gestor's grid
  showCentroGestorGrid = false;
  centroGestor: any[] = [];
  cgeError: string = '';
  cgeSuccess: string = '';
  activeDetailTab: 'centroGestor' | 'articulos' | null = null;
  showcentroGestor(numero: number) {
    this.limpiarMessages();
    this.showCentroGestorGrid = true;
    this.showArticulosGrid = false;
    this.activeDetailTab = 'centroGestor';
    this.articulos = [];
    this.fetchCentroGestor(numero);
  }

  isLoadingCentroGestor: boolean = false;
  fetchCentroGestor(numero: number) {
    this.isLoadingCentroGestor = true;
    const concod = numero;
    this.http.get<any[]>(`${environment.backendUrl}/api/cog/fetch-centros/${this.entcod}/${this.eje}/${concod}`).subscribe({
      next: (res) => {
        this.centroGestor = res;
        this.pageCNT = 0;
        this.isLoadingCentroGestor = false;
      },
      error: (err) => {
        this.centroGestor = [];
        this.pageCNT = 0;
        this.isLoadingCentroGestor = false;
        this.cgeError = err.error.error ?? err.error;
      }
    })
  }
  pageCNT = 0;
  get paginatedCentros(): any[] {
    if (!this.centroGestor || this.centroGestor.length === 0) return [];
    const start = this.pageCNT * this.pageSize;
    return this.centroGestor.slice(start, start + this.pageSize);
  }
  get totalPagesCNT(): number {
    return Math.max(1, Math.ceil((this.centroGestor?.length ?? 0) / this.pageSize));
  }
  prevpageCNT(): void {
    if (this.pageCNT > 0) this.pageCNT--;
  }
  nextpageCNT(): void {
    if (this.pageCNT < this.totalPagesCNT - 1) this.pageCNT++;
  }
  goTopageCNT(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPagesCNT) {
      this.pageCNT = inputPage - 1;
    }
  }

  getKdisponible(COGIMP: number, COGIM2: number, COGIAP: number) {
    return (COGIMP + COGIM2) - COGIAP;
  }

  centroGestorDelete: boolean = false;
  isDeletingCentro: boolean = false;
  centroToDelete: any = [];
  centroDeleteError: string = '';
  showDeleteCentro(centro: any) {
    this.limpiarMessages();
    this.centroGestorDelete = true;
    this.centroToDelete = centro;
  }

  closeDeleteCentro() {
    this.limpiarMessages();
    this.centroGestorDelete = false;
    this.centroToDelete = [];
  }

  deleteCentroGestor(cge: string) {
    this.limpiarMessages();
    this.isDeletingCentro = true;
    const concod = this.selectedContrato.concod;
    const cgecod = cge;
    if (!concod || !cgecod) {return;}

    this.http.delete(`${environment.backendUrl}/api/cog/delete-centro/${this.entcod}/${this.eje}/${concod}/${cgecod}`).subscribe({
      next: (res) => {
        this.isDeletingCentro = false;
        this.closeDeleteCentro();
        this.fetchCentroGestor(concod);
        this.cgeSuccess = 'cge se ha eliminado correctamente';
      },
      error: (err) => {
        this.isDeletingCentro = false;
        this.centroDeleteError = err.error.error ?? err.error;
      }
    })
  }

  addCentroGestor: boolean = false;
  isAddingCentro: boolean = false;
  centroErrorMessage: string = '';
  isLoadingCentroAdd: boolean = false;
  searchCentro: string = '';
  centroGestoresAdd: any[] = [];
  showAddCentro() {
    this.addCentroGestor = true;
    this.fetchCentroGestores();
  }

  closeAddCentro() {
    this.limpiarMessages();
    this.addCentroGestor = false;
    this.centroGestoresAdd = [];
    this.coughtCentros = [];
  }

  fetchCentroGestores() {
    this.isLoadingCentroAdd = true;
    this.http.get<any>(`${environment.backendUrl}/api/cge/fetch-all/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.isLoadingCentroAdd = false;
        this.centroGestoresAdd = res;
        this.searchPageCentro = 0;
      },
      error: (err) => {
        this.centroGestoresAdd = [];
        this.isLoadingCentroAdd = false;
        this.centroErrorMessage = err.error.error ?? err.error;
      }
    })
  }

  searchCentroGestores() {
    this.isLoadingCentroAdd = true;
    if (this.searchCentro.length <= 2) {
      this.http.get<any>(`${environment.backendUrl}/api/cge/search-centros-codigo/${this.entcod}/${this.eje}/${this.searchCentro}`).subscribe({
        next: (res) => {
          this.isLoadingCentroAdd = false;
          this.centroGestoresAdd = res;
          this.searchPageCentro = 0;
        },
        error: (err) => {
          this.centroGestoresAdd = [];
          this.isLoadingCentroAdd = false;
          this.centroErrorMessage = err.error.error ?? err.error;
        }
      })
    } else if (this.searchCentro.length > 2 && this.searchCentro.length <= 4) {
      this.http.get<any>(`${environment.backendUrl}/api/cge/search-centros/${this.entcod}/${this.eje}/${this.searchCentro}`).subscribe({
        next: (res) => {
          this.isLoadingCentroAdd = false;
          this.centroGestoresAdd = res;
          this.searchPageCentro = 0;
        },
        error: (err) => {
          this.centroGestoresAdd = [];
          this.isLoadingCentroAdd = false;
          this.centroErrorMessage = err.error.error ?? err.error;
        }
      })
    } else {
      this.http.get<any>(`${environment.backendUrl}/api/cge/search-centros-description/${this.entcod}/${this.eje}/${this.searchCentro}`).subscribe({
        next: (res) => {
          this.isLoadingCentroAdd = false;
          this.centroGestoresAdd = res;
          this.searchPageCentro = 0;
        },
        error: (err) => {
          this.centroGestoresAdd = [];
          this.isLoadingCentroAdd = false;
          this.centroErrorMessage = err.error.error ?? err.error;
        }
      })
    }
  }

  limpiarSearchCentroGetores() {
    this.limpiarMessages();
    this.fetchCentroGestores();
    this.searchCentro = '';
    this.coughtCentros = [];
    this.searchPage = 0;
  }
  searchPageCentro: number = 0;
  searchPageSizeCentro: number = 5;
  get paginatedSearchResultsCentro() {const start = this.searchPageCentro * this.searchPageSizeCentro; return this.centroGestoresAdd.slice(start, start + this.searchPageSizeCentro);}
  get searchTotalPagesCentro() {return Math.ceil(this.centroGestoresAdd.length / this.searchPageSizeCentro);}

  coughtCentros: any[] = [];
  selectCentrosAdd(centro: any) {
    if (this.coughtCentros.includes(centro)) {
      const index = this.coughtCentros.indexOf(centro);
      if(index !== -1) {
        this.coughtCentros.splice(index, 1);
      }
    } else {
      this.coughtCentros = [...this.coughtCentros, centro];
    }
  }

  isCentroSelected(a: any): boolean {
    return this.coughtCentros.includes(a);
  }

  saveCentroGestores() {
    this.limpiarMessages();
    this.isLoadingCentroAdd = true;
    const concod = this.selectedContrato.concod;

    const payload = this.coughtCentros.map(obj => ({
      ent: this.entcod,
      eje: this.eje,
      concod: concod,
      cgecod: obj.cgecod,
      cogimp: 0,
      cogiap: 0
    }));

    this.http.post(`${environment.backendUrl}/api/cog/save-centroGestores`, payload).subscribe({
      next: (res) => {
        this.closeAddCentro();
        this.fetchCentroGestor(concod);
        this.isLoadingCentroAdd = false;
        this.cgeSuccess = 'artículos añadidos con éxito';
        this.showcentroGestor(concod);
      },
      error: (err) => {
        this.centroErrorMessage = err.error.error ?? err.error;
        this.isLoadingCentroAdd = false;
      }
    })
  }

  //D related functions
  COGOPD: string = '';
  COGOP2: string = '';
  organica: string = '';
  programa: string = '';
  economica: string = '';
  cgecod: string = '';
  DError: string = '';
  referencia: string = '';
  checkBeforeAdd(centro: any) {
    this.limpiarMessages();

    this.COGOPD = centro.cogopd ?? '';
    this.COGOP2 = centro.cogop2 ?? '';
    this.referencia = centro.cogopd ?? '';

    if (this.COGOPD.trim() != '' && this.COGOP2.trim() != '') {
      this.DError = 'Ya tiene dos “D” para este Centro Gestor'
    } else {
      this.cgecod = centro.cgecod;
      this.organica = centro.cge.cgeorg;
      this.programa = centro.cge.cgefun;
      this.economica = this.selectedContrato.conlot;
      this.openAddD();
    }
  }

  DGridShow: boolean = false;
  isAddingD: boolean = false;
  isLoadingD: boolean = false;
  DErrorMessage: string = '';
  listaDeD: any[] = [];
  openAddD() {
    this.limpiarMessages();
    this.DGridShow = true;
    this.fetchD();
  }

  closeAddD() {
    this.DGridShow = false;
    this.listaDeD = [];
    this.COGOPD = '';
    this.COGOP2 = '';
    this.organica = '';
    this.programa = '';
    this.economica = '';
    this.cogimp = null;
    this.cogopd = '';
    this.cgecod = '';
  }

  cogimp: number | null = null;
  cogopd: string = '';
  cogim2: number | null = null;
  cogop2: string = '';
  cogrf2: string = '';
  fetchD() {
    let codigoOperacion = 220;
    const oficina = 'AL';
    this.isLoadingD = true;

    this.http.get<any>(`${environment.backendUrl}/api/sical/operaciones?orgCode=${this.orgCode}&entidad=${this.entidad}&codigoOperacion=${codigoOperacion}&organica=${this.organica}&funcional=${this.programa}&economica=${this.economica}&oficina=${oficina}&eje=${this.eje}`).subscribe({
      next: (res) => {
        this.isLoadingD = false;
        if (!Array.isArray(res) || res.length === 0) {
          this.listaDeD = [];
          this.DErrorMessage = 'No hay operaciones D para los datos requeridos';
          return;
        }

        this.listaDeD = res;
      },
      error: (err) => {
        this.isLoadingD = false;
        this.DErrorMessage = err.error.error ?? err.error;
      }
    })
  }
  searchPageD: number = 0;
  searchPageSizeD: number = 5;
  get paginatedSearchResultsD() {const start = this.searchPageD * this.searchPageSizeD; return this.listaDeD.slice(start, start + this.searchPageSizeD);}
  get searchTotalPagesD() {return Math.ceil(this.listaDeD.length / this.searchPageSizeD);}

  updateDForContrato(D: any) {
    if (this.COGOPD.trim() === '') {
      this.updateD(D);
    } else if (this.COGOP2.trim() === '') {
      this.updateD2(D);
    }
  }
  updateD(D: any) {
    this.limpiarMessages();

    this.cogimp = D.lineaList[0].limporte;
    this.cogopd = D.lineaList[0].referencia;
    const concod = this.selectedContrato.concod;

    const payload = {
      "COGIMP": this.cogimp,
      "COGOPD": this.cogopd
    }

    this.isAddingD = true;
    this.http.patch(`${environment.backendUrl}/api/cog/update-centro-D/${this.entcod}/${this.eje}/${concod}/${this.cgecod}`, payload).subscribe({
      next: (res) => {
        this.isAddingD = false;
        this.closeAddD();
        this.closeUpdateSure();
        this.fetchCentroGestor(concod);
      },
      error: (err) => {
        this.isAddingD = false;
        this.DErrorMessage = err.error.error ?? err.error;
      }
    })
  }

  updateD2(D: any) {
    this.limpiarMessages();

    this.cogim2 = D.lineaList[0].limporte;
    this.cogop2 = D.numope;
    this.cogrf2 = D.lineaList[0].referencia;
    const concod = this.selectedContrato.concod;

    const payload = {
      "COGIM2": this.cogim2,
      "COGOP2": this.cogop2,
      "COGRF2": this.cogrf2
    }

    this.isAddingD = true;
    this.http.patch(`${environment.backendUrl}/api/cog/update-centro-D2/${this.entcod}/${this.eje}/${concod}/${this.cgecod}`, payload).subscribe({
      next: (res) => {
        this.isAddingD = false;
        this.closeAddD();
        this.closeUpdateSure();
        this.fetchCentroGestor(concod);
      },
      error: (err) => {
        this.isAddingD = false;
        this.DErrorMessage = err.error.error ?? err.error;
      }
    })
  }

  updateSure: boolean = false;
  toUpdate: any;
  dMessageConfirm: string = '';
  openUpdateSure(D: any){
    this.updateSure = true;
    this.toUpdate = D;

    if (this.referencia === '') {
      this.dMessageConfirm = '¿Está seguro de añadir D a este centro gestor?';
    } else {
      this.dMessageConfirm = '¿Está seguro de cambiar D para este centro gestor?';
    }
  }

  closeUpdateSure() {
    this.updateSure = false;
    this.toUpdate = [];
    this.referencia = '';
  }

  deleteDSureGrid: boolean = false;
  openDeleteDSureGrid() {
    this.limpiarMessages();
    this.deleteDSureGrid = true;
  }

  closeDeleteDSureGrid() {
    this.limpiarMessages();
    this.deleteDSureGrid = false;
  }

  whichD: number = 0;
  concod: string = '';
  cgecodDelete: string = '';
  openDeleteD(D: any) {
    console.log(D)
    this.limpiarMessages();
    this.cgecodDelete = D.cgecod ?? '';
    this.concod = this.selectedContrato.concod ?? '';
    let cogopd = D.cogopd ?? '';
    let cogop2 = D.cogop2 ?? '';
    let cogiap = D.cogiap ?? '';
    let cogimp = D.cogimp ?? '';

    console.log("cgecod",this.cgecodDelete)
    console.log("concod", this.concod);
    console.log("cogopd", cogopd);
    console.log("cogop2", cogop2);
    console.log("cogiap", cogiap);
    console.log("cogimp", cogimp);

    if (cogopd.trim() === '' && cogop2.trim() === '') {
      console.log("nothing case");
      return;
    }

    if (cogopd.trim() != '' && cogop2.trim() === '') {
      if (cogimp < cogiap) {
        this.openDeleteDSureGrid();
        this.deleteDGridMessage = 'No tendrá Disponible para contabilizar los pedidos pendientes. ¿Quiere seguir?';
        this.whichD = 1;
        console.log(this.whichD);
        return;
      }
      this.deleteFirstD();
    } else {
      if (cogiap > 0) {
        this.openDeleteDSureGrid();
        this.deleteDGridMessage = 'No tendrá Disponible para contabilizar los pedidos pendientes. ¿Quiere seguir?';
        this.whichD = 2;
        console.log(this.whichD);
        return;
      }
      this.deleteSecondD();
    }
  }

  closeDeleteD() {
    this.whichD = 0;
    this.concod = '';
    this.cgecodDelete = '';
  }

  whichDDelete() {
    if (this.whichD === 1) {
      console.log("here");
      this.deleteFirstD();
      this.closeDeleteDSureGrid();
    } else if (this.whichD === 2) {
      console.log("here 2");
      this.deleteSecondD();
      this.closeDeleteDSureGrid();
    }
  }

  deleteDGridMessages: boolean = false;
  deleteDGridMessage: string = '';
  openDeleteDGridMessages() {
    console.log("finito");
    this.deleteDGridMessages = true;
  }

  closeDeleteDGridMessages() {
    console.log("closed finito");
    this.limpiarMessages();
    this.fetchCentroGestor(this.selectedContrato.concod);
    this.deleteDGridMessages = false;
  }

  idDeletingDCge: boolean = false;
  deletingDCgeError: string = '';
  deletingDCgeSuccess = '';
  deleteFirstD() {
    this.idDeletingDCge = true;
    this.openDeleteDGridMessages();
    this.http.delete(`${environment.backendUrl}/api/cog/delete-D/${this.entcod}/${this.eje}/${this.concod}/${this.cgecodDelete}`).subscribe({
      next: (res) => {
        this.idDeletingDCge = false;
        this.closeDeleteD();
        this.closeDeleteDSureGrid();
        this.openDeleteDGridMessages();
        this.deletingDCgeSuccess = 'Se eliminó la primera D';
      },
      error: (err) => {
        this.idDeletingDCge = false;
        this.deletingDCgeError = err.error.error || err.error;
      }
    })
  }

  deleteSecondD() {
    this.idDeletingDCge = true;
    this.openDeleteDGridMessages();
    this.http.delete(`${environment.backendUrl}/api/cog/delete-D2/${this.entcod}/${this.eje}/${this.concod}/${this.cgecodDelete}`).subscribe({
      next: (res) => {
        this.idDeletingDCge = false;
        this.closeDeleteD();
        this.closeDeleteDSureGrid();
        this.openDeleteDGridMessages();
        this.deletingDCgeSuccess = 'Segunda D eliminado';
      },
      error: (err) => {
        this.idDeletingDCge = false;
        this.deletingDCgeError = err.error.error || err.error;;
      }
    })
  }

  //actualizar saldo
  actualizarGrid: boolean = false;
  actualizarMessage: string = '';
  isActualizar: boolean = false;
  openActualizar() {
    this.limpiarMessages();
    this.actualizarMessage = '¿Quiere actualizar el saldo de todas las AD?';
    this.actualizarGrid = true;
  }

  closeActualizar() {
    if (this.isActualizar) {
      return;
    }

    this.actualizarGrid = false;
  }

  update() {
    this.isActualizar = true;

    const requests = this.centroGestor.flatMap(item => {
      const requests = [];

      const cogopd = (item?.cogopd ?? '').trim();
      const cogop2 = (item?.cogop2 ?? '').trim();
      if (cogopd === '' && cogop2 === '') {
        this.isActualizar = false;
        this.actualizarGrid = false;
        this.DError = 'Todas las operaciones están vacías';
        return;
      }

      if (cogopd !== '') {
        requests.push(
          this.http.get<any>(`${environment.backendUrl}/api/sical/operaciones`, {
            params: {
              orgCode: this.orgCode ?? '',
              entidad: this.entidad ?? '',
              numeroOperDesde: item.cogopd,
              numeroOperHasta: item.cogopd,
              referencia: item.cogrfd ?? '',
              organica: item?.cge?.cgeorg ?? '',
              funcional: item?.cge?.cgefun ?? '',
              economica: this.selectedContrato?.conlot ?? '',
              eje: this.eje ?? '',
              numRegDev: 1
            }
          }).pipe(
            switchMap(res => {
              const operation = Array.isArray(res) ? res[0] : res;
              const line = Array.isArray(operation?.l_linea)
                ? operation.l_linea[0]
                : operation?.l_linea;

              if (line?.saldo == null) {
                return of(null);
              }

              item.cogimp = Number(line.saldo);

              return this.http.patch(
                `${environment.backendUrl}/api/cog/updateD1/${this.entcod}/${this.eje}/${this.selectedContrato.concod}/${item.cgecod}`,
                { COGIMP: item.cogimp }
              );
            })
          )
        );
      }

      if (cogop2 !== '') {
        requests.push(
          this.http.get<any>(`${environment.backendUrl}/api/sical/operaciones`, {
            params: {
              orgCode: this.orgCode ?? '',
              entidad: this.entidad ?? '',
              numeroOperDesde: item.cogop2,
              numeroOperHasta: item.cogop2,
              referencia: item.cogrf2 ?? '',
              organica: item?.cge?.cgeorg ?? '',
              funcional: item?.cge?.cgefun ?? '',
              economica: this.selectedContrato?.conlot ?? '',
              eje: this.eje ?? '',
              numRegDev: 1
            }
          }).pipe(
            switchMap(res => {
              const operation = Array.isArray(res) ? res[0] : res;
              const line = Array.isArray(operation?.l_linea)
                ? operation.l_linea[0]
                : operation?.l_linea;

              if (line?.saldo == null) {
                return of(null);
              }

              item.cogim2 = Number(line.saldo);

              return this.http.patch(
                `${environment.backendUrl}/api/cog/updateD2/${this.entcod}/${this.eje}/${this.selectedContrato.concod}/${item.cgecod}`,
                { COGIM2: item.cogim2 }
              );
            })
          )
        );
      }

      return requests;
    });

    forkJoin(requests).pipe(
      finalize(() => {
        this.isActualizar = false;
        this.actualizarGrid = false;
      })
    ).subscribe({
      next: () => {
        this.fetchCentroGestor(this.selectedContrato.concod);
      },
      error: err => {
        console.error('Error updating balances:', err);
      }
    });
  }

  //misc
  limpiarMessages() {
    this.mainError = '';
    this.mainSuccess = '';
    this.updateContratoSuccess = '';
    this.updateContratoError = '';
    this.addContratoError = '';
    this.error = '';
    this.articulosError = '';
    this.articulosSuccess = '';
    this.cgeError = '';
    this.cgeSuccess = '';
    this.detallesMessageErrorDelete = '';
    this.ArticuloAddSuccess = '';
    this.articulosAddError = '';
    this.centroDeleteError = '';
    this.centroErrorMessage = '';
    this.DErrorMessage = '';
    this.articuloError = '';
    this.DError = '';
    this.deleteDGridMessage = '';
    this.deletingDCgeError = '';
    this.deletingDCgeSuccess = '';
  }
}
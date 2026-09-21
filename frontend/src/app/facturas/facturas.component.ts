import { Component, HostListener} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';
import { DatePipe } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { CurrencyPipe } from '@angular/common';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-proveedorees',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  providers: [DatePipe, CurrencyPipe],
  templateUrl: './facturas.component.html',
  styleUrls: ['./facturas.component.css']
})
export class FacturasComponent {
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
  public centroGestor: string = '';
  private WSent: string = '';
  private WSorg: string = '';
  public estadogc: number | null = null;
  public facturaSearchTouched: boolean = false;
  public searchQueryTouched: boolean = false;
  private initialCentroGestor: string = '';
  facturas: any[] = [];
  private backupFacturas: any[] = [];
  page = 0;
  pageSize = 20;
  facturaMessageIsSuccess: boolean = false;
  estadoMessage: string = '';
  isEstadoMessage: boolean = false;
  public Math = Math;

  constructor(private http: HttpClient, private router: Router, private datePipe: DatePipe, private currencyPipe: CurrencyPipe ) {}

  private defaultFacturas: any[] = [];
  isLoading: boolean = false;
  ngOnInit(): void{
    this.limpiarMEssages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const cge = sessionStorage.getItem('CENTROGESTOR');
    const estadoCentroGestor = sessionStorage.getItem('ESTADOGC');
    const entSession = sessionStorage.getItem('WSENT'); 
    const orgSession = sessionStorage.getItem('WSORG');   

    if (cge){ const parsed = JSON.parse(cge); this.centroGestor = parsed.value; this.initialCentroGestor = this.centroGestor;}
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}
    if (entSession) {const parsed = JSON.parse(entSession); this.WSent = parsed.WSENT;}
    if (orgSession) {const parsed = JSON.parse(orgSession); this.WSorg = parsed.WSORG;}

    if (!entidad || this.entcod === null || !eje || this.eje === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    if(estadoCentroGestor){
      const parsed = JSON.parse(estadoCentroGestor);
      this.estadogc = parsed.value;

      if (this.estadogc === 1) {
        this.isEstadoMessage = true;
        this.estadoMessage = 'Centro Gestor CERRADO';
      }
      if ( this.estadogc === 2) {
        this.isEstadoMessage = true;
        this.estadoMessage = 'Centro Gestor CERRADO para CONTABILIZAR';
      }
    }

    this.fetchFacturas();
  }

  fetchFacturas() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/fac/noCont/${this.entcod}/${this.eje}/${this.centroGestor}`).subscribe({
      next: (response) => {
        if (!Array.isArray(response) || response.length === 0) {
          this.facturaMessageIsSuccess = true;
          this.filterFacturaMessage = 'No hay facturas por las medidas de búsqueda';
          this.facturas = [];
          this.defaultFacturas = [];
          this.updatePagination();
          this.isLoading = false;
        } else {
          this.facturas = response;
          this.backupFacturas = Array.isArray(response) ? [...response] : [];
          this.defaultFacturas = [...this.backupFacturas];
          this.page = 0;
          this.updatePagination();
          this.isLoading = false;
        }
      }, error: (err) => {
        this.filterFacturaMessage= err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  //main table functions
  sortField: 'facnum' | 'tercod' | 'ter_TERNOM' | 'ter_TERNIF' | 'facfre' | 'facimp' | 'facdoc' | 'facann' | 'facfac' | 'facdat' | 'facado' | 'facfco' |'getPendingApply(p)' | 'cgecod' | 'getStaus(p.facado, p.facimp, p.faciec, p.facidi)' | null = null;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  private defaultProveedores: any[] = [];
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
    this.facturas.sort((a, b) => {
      let aValue: any;
      let bValue: any;

      if (this.sortColumn === 'getPendingApply(p)') {
        aValue = this.getPendingApply(a);
        bValue = this.getPendingApply(b);
      } else if (this.sortColumn === 'getStaus(p.facado, p.facimp, p.faciec, p.facidi)') {
        aValue = this.getStaus(a.facado, a.facimp, a.faciec, a.facidi);
        bValue = this.getStaus(b.facado, b.facimp, b.faciec, b.facidi);
      } else {
        aValue = a[this.sortColumn];
        bValue = b[this.sortColumn];
      }

      const aNum = Number(aValue);
      const bNum = Number(bValue);
      if (!isNaN(aNum) && !isNaN(bNum)) {
        return this.sortDirection === 'asc' ? aNum - bNum : bNum - aNum;
      }

      aValue = (aValue ?? '').toString().toUpperCase();
      bValue = (bValue ?? '').toString().toUpperCase();
      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

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

  get paginatedFacturas(): any[] {
    if (!this.facturas || this.facturas.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.facturas.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.facturas?.length ?? 0) / this.pageSize));
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

  formatDate = (v: any) => {
    if (!v && v !== 0) return '';
    const s = String(v);
    const d = new Date(s);
    return isNaN(d.getTime()) ? s : d.toLocaleDateString('es-ES');
  };

  formatCurrency = (value: any) => {
    if (value === null || value === undefined || value === '') return '';
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 2
    }).format(Number(value));
  };

  DownloadPDF() {
    this.limpiarMEssages();

    const source = this.facturas;
    if (!source?.length) {
      this.filterFacturaMessage = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      facnum: row.facnum ?? '',
      tercod: row.tercod ?? '',
      ternom: row.ter_TERNOM ?? '',
      ternif: row.ter_TERNIF ?? '',
      facfre: this.formatDate(row.facfre),
      facimp: this.formatCurrency(row.facimp),
      facdoc: row.facdoc ?? '',
      facann: row.facann ?? '',
      facfac: row.facfac ?? '',
      facdat: this.formatDate(row.facdat),
      facado: row.facado ?? '',
      facfco: this.formatDate(row.facfco),
      pendingApply: this.formatCurrency(this.getPendingApply(row)),
      cgecod: row.cgecod ?? '',
      estado: this.getStaus(row.facado, row.facimp, row.faciec, row.facidi)
    }));

    const columns = [
      { header: 'Número Registro', dataKey: 'facnum' },
      { header: 'Código Prov', dataKey: 'tercod' },
      { header: 'Nombre Proveedor', dataKey: 'ternom' },
      { header: 'NIF Prov', dataKey: 'ternif' },
      { header: 'F.Registro', dataKey: 'facfre' },
      { header: 'Importe total', dataKey: 'facimp' },
      { header: 'Núm. Factura', dataKey: 'facdoc' },
      { header: 'Año', dataKey: 'facann' },
      { header: 'R.C.F', dataKey: 'facfac' },
      { header: 'F.Factura', dataKey: 'facdat' },
      { header: 'ADO', dataKey: 'facado' },
      { header: 'F. Contable', dataKey: 'facfco' },
      { header: 'Pte. Aplicar', dataKey: 'pendingApply' },
      { header: 'C. Gestor', dataKey: 'cgecod' },
      { header: 'Estado', dataKey: 'estado' }
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(12);

    autoTable(doc, {
      startY: 20,
      columns,
      body: rows.map(row => columns.map(c => row[c.dataKey as keyof typeof row] ?? '')),
      styles: { fontSize: 8 },
      tableWidth: 'wrap',
      headStyles: {
        fillColor: [240, 240, 240],
        textColor: [33, 53, 71],
        fontStyle: 'bold',
        halign: 'left'
      },
      columnStyles: {
        facnum: { cellWidth: 15 },
        tercod: { cellWidth: 15 },
        ternom: { cellWidth: 28 },
        ternif: { cellWidth: 18 },
        facfre: { cellWidth: 18 },
        facimp: { cellWidth: 15 },
        facdoc: { cellWidth: 20 },
        facann: { cellWidth: 15 },
        facfac: { cellWidth: 15 },
        facdat: { cellWidth: 28 },
        facado: { cellWidth: 15 },
        facfco: { cellWidth: 15 },
        pendingApply: { cellWidth: 15 },
        cgecod: { cellWidth: 14 },
        estado: { cellWidth: 20 }
      },
      didDrawPage: (dataArg) => {
        doc.setFontSize(10);
        doc.text('Lista de facturas', 14, 10);
      }
    });

    doc.save('facturas.pdf');
  }

  downloadExcel() {
    this.limpiarMEssages();
    const rows = this.facturas;
    if (!rows || rows.length === 0) {
      this.filterFacturaMessage = 'No hay datos para exportar.';
      return;
    }

    const exportRows = rows.map(row => ({
      facnum: row.facnum ?? '',
      tercod: row.tercod ?? '',
      ternom: row.ter_TERNOM ?? '',
      ternif: row.ter_TERNIF ?? '',
      facfre: row.facfre ?? '',
      facimp: row.facimp ?? '',
      facdoc: row.facdoc ?? '',
      facann: row.facann ?? '',
      facfac: row.facfac ?? '',
      facdat: row.facdat ?? '',
      facado: row.facado ?? '',
      facfco: row.facfco ?? '',
      getPendingApply: this.getPendingApply(row) ?? '',
      cgecod: row.cgecod ?? '',
      getStaus: this.getStaus(row.facado, row.facimp, row.faciec, row.facidi) ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de facturas']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['N.Registro', 'Código Prov', 'Nombre Proveedor', 'NIF', 'F.Registro', 'Importe total', 'Num.Factura', 'Año', 'R.C.F', 'F.Factura', 'Ado', 'F.Contable', 'Pte. Aplicar', 'C.gestor', 'Estado']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 12 },
      { wch: 40 },
      { wch: 20 },
      { wch: 20 },
      { wch: 10 },
      { wch: 20 },
      { wch: 15 },
      { wch: 20 },
      { wch: 20 },
      { wch: 15 },
      { wch: 15 },
      { wch: 15 },
      { wch: 30 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Facturas');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Facturas.xlsx'
    );
  }

  //detail grid functions
  selectedFacturas: any = null;
  detallesMessage: String = '';
  fettalesIsError: boolean = false;

  showDetails(factura: any) {
    this.limpiarMEssages();

    this.selectedFacturas = { ...factura };
    this.tempFactura = { ...factura };

    if (this.selectedFacturas.facfre) {
      this.selectedFacturas.facfre = this.datePipe.transform(this.selectedFacturas.facfre, 'yyyy-MM-dd');
    }
    if (this.tempFactura.facfre) {
      this.tempFactura.facfre = this.datePipe.transform(this.tempFactura.facfre, 'yyyy-MM-dd');
    }

    this.openAlbarnaes();
  }

  closeDetails() {
    this.selectedFacturas = null;
    this.limpiarMEssages();
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  public getPendingApply(f: any): number {
    if (!f) return 0;
    const toNum = (v: any) => (v === null || v === undefined || v === '' ? 0 : Number(v) || 0);
    let pending = toNum(f.facimp) - (toNum(f.faciec) + toNum(f.facidi));
    return Math.round(pending * 100) / 100;
  }

  public getStaus(facado: any, facimp: any, faciec: any, facidi:any ){
    if (facado !== null && facado !== undefined && facado !== '') {
      return 'contabilizada';
    }

    const FACIEC = Number(faciec);
    const FACIMP = Number(facimp);
    const FACIDI = Number(facidi);
    const imp = Math.round(FACIMP * 100) / 100;
    const applied = Math.round((FACIEC + FACIDI) * 100) / 100;

    if ((facado === null || facado === undefined || facado === '') && imp === applied) {
      return 'Pte. Aplicada';
    }

    return 'Pte. Sin aplicar';
  }

  formatPrice(value: number | null): string {
    return this.currencyPipe.transform(
      value ?? 0,
      'EUR',
      'symbol',
      '1.2-2',
      'es-ES'
    ) ?? '';
  }

  startEditingPrice(item: any) {
    item.editingPrice = true;
    item.editingValue = item.FDEDIF === 0
      ? ''
      : item.FDEDIF.toString().replace('.', ',');
  }

  onPriceTyping(event: Event, item: any) {
    const input = event.target as HTMLInputElement;

    item.editingValue = input.value;
  }

  stopEditingPrice(item: any) {
    item.editingPrice = false;

    const value = item.editingValue
      ?.replace(',', '.');

    item.FDEDIF = value === '' || value == null
      ? 0
      : Number(value);
  }

  //search functions
  onEjeInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const sanitized = input.value.replace(/\D+/g, '').slice(0, 4);
    input.value = sanitized;
    this.facturaSearch = sanitized;
    this.facturaSearchTouched = true;
  }
  
  filterFacturaMessage: string = '';
  
  fechaTipo: 'registro' | 'factura' | 'contable' | 'Fecha' | '' = '';
  estadoTipo: 'contabilizadas' | 'noContabilizadas' | 'ptApplidas' | 'sinPtApplicar' | '' = 'noContabilizadas';
  fromDate: string = '';
  toDate: string = '';
  facturaSearch: string = '';
  public searchQuery: string = '';
  filterFacturas(): void {
    this.limpiarMEssages();

    this.isLoading = true;
    if (this.entcod == null || this.eje == null || !this.centroGestor) {
      this.filterFacturaMessage = 'Faltan datos de sesión.';
      this.isLoading = false;
      return;
    }
    if ((this.fromDate || this.toDate) && !this.fechaTipo) {
      this.filterFacturaMessage = 'Seleccione un tipo de fecha antes de buscar.';
      this.isLoading = false;
      return;
    }

    const params: Record<string, string> = {
      ent: String(this.entcod),
      eje: String(this.eje),
      cgecod: this.centroGestor,
    };

    if (this.estadoTipo) {
      params['estado'] = this.estadoTipo;
    }

    if (this.fechaTipo) {
      params['fecha'] = this.fechaTipo;
    }

    if (this.fromDate && this.fromDate.trim() !== '') {
      params['fromDate'] = this.fromDate;
    }

    if (this.toDate && this.toDate.trim() !== '') {
      params['toDate'] = this.toDate;
    }

    if (this.facturaSearch && this.facturaSearch.trim() !== '') {
      params['ej_factura'] = this.facturaSearch;
    }

    if (this.searchQuery && this.searchQuery.trim() !== '') {
      params['main_filter'] = this.searchQuery;
    }

    this.http.get<any[]>(`${environment.backendUrl}/api/fac/search-factura`, {params} )
    .subscribe({
      next: (res) => {
        this.facturas = res;
        this.defaultFacturas = [...this.facturas]
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.facturas = [];
        this.filterFacturaMessage = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  forgetAll(): void{
    this.limpiarMEssages();
    this.facturaSearch = '';
    this.searchQuery = '';
    this.centroGestor = this.initialCentroGestor || '';
    this.facturaSearchTouched = false;
    this.searchQueryTouched = false;
    this.fechaTipo = '';
    this.estadoTipo = 'noContabilizadas';
    this.fromDate = '';
    this.toDate = '';
    this.page = 0;
    this.sortField = null;
    this.sortDirection = 'asc';

    this.fetchFacturas();
  }

  tempFactura: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedFacturas ? { ...this.selectedFacturas } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempFactura = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed() {
    if (this.allowToUpdate) {
      this.updateFactura();
    } else {
      return;
    }
  }

  isUpdatingFactura: boolean = false;
  facturaDetailSuccess: string = '';
  facturaDetailError: string = '';
  updateFactura() {
    this.limpiarMEssages();
    this.isUpdatingFactura = true;
    const backendDate = this.toBackendDate(this.tempFactura.facfre);
    const facnum = this.tempFactura.facnum;

    Object.assign(this.selectedFacturas, this.tempFactura);
    
    const payload = {
      "FACOBS": this.tempFactura.facobs,
      "CONCTP": this.tempFactura.conctp,
      "CONCPR": this.tempFactura.concpr, 
      "CONCCR": this.tempFactura.conccr,
      "FACFPG": this.tempFactura.facfpg,
      "FACOPG": this.tempFactura.facopg,
      "FACTPG": this.tempFactura.factpg,
      "FACOCT": this.tempFactura.facoct
    }

    this.http.patch(`${environment.backendUrl}/api/fac/update-factura/${this.entcod}/${this.eje}/${facnum}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.isUpdatingFactura = false;
        this.facturaDetailSuccess = 'factura actualizada exitosamente';
      },
      error: (err) => {
        this.isUpdatingFactura = false;
        this.facturaDetailError = err.error.error ?? err.error
      }
    })
  }

  //sub detail grid's functions
  detailView: 'Albaranes' | 'Contabilización' = 'Albaranes';
  albaranesOptio: 'albaranes' | 'aplicaciones' | 'descuentos' = 'albaranes';
  albaranes: any[] = [];
  backipAlbaranes: any[] = [];
  apalicaciones: any[] = [];
  backupAplicaciones: any[] = [];
  descuentos: any[] = [];
  backupDescuentos: any[] = [];
  moreInfoMessageSuccess: string = '';
  moreInfoMessageIsSuccess: boolean = false;
  moreInfoMessageError: string = '';
  moreInfoMessageIsError: boolean = false;
  AlbaranesGrid: boolean = false;
  ContabilizacionGrid: boolean = false;
  albaranesGrid: boolean = false;
  aplicacionesGrid: boolean = false;
  descuentosGrid: boolean = false;
  openAlbarnaes() {
    this.limpiarMEssages();
    this.AlbaranesGrid = true;
    this.ContabilizacionGrid = false;
    this.detailView = 'Albaranes';
    this.setAlbaranesOptio('albaranes', this.selectedFacturas.facnum);
  }

  // openContabilizacion() {
  //   this.limpiarMEssages();
  //   this.AlbaranesGrid = false;
  //   this.ContabilizacionGrid = true;
  //   this.detailView = 'Contabilización';
  // }

  setAlbaranesOptio(option: 'albaranes' | 'aplicaciones' | 'descuentos', facnum: number): void {
    this.albaranesOptio = option;
    this.limpiarMEssages();
    this.isLoading = true;
    this.albaranes = [];
    this.apalicaciones = [];
    this.descuentos = [];

    if ( option === 'albaranes') {this.fetchAlbaranesDEtail(facnum);}
    if ( option === 'aplicaciones') {this.fetchApplicacionesDetails(facnum);}
    if ( option === 'descuentos') {this.fetchDescuentosDetails(facnum);}
  }

  albaranError: string = '';
  fetchAlbaranesDEtail(facnum: number) {
    this.http.get<any>(`${environment.backendUrl}/api/alb/albaranes/${this.entcod}/${this.eje}/${facnum}`).subscribe({
      next: (response) => {
        this.albaranes = response;
        this.backipAlbaranes = Array.isArray(response) ? [...response] : [];
        this.isLoading = false;
        this.pageAlbaranes = 0;
      }, error: (err) => {
        this.albaranes = [];
        this.albaranError = err.error.error ?? err.error;
        this.isLoading = false;
        this.pageAlbaranes = 0;
      }
    });
  }
  pageAlbaranes = 0;
  get paginatedAlbaranes(): any[] {if (!this.albaranes || this.albaranes.length === 0) return []; const start = this.pageAlbaranes * this.pageSize; return this.albaranes.slice(start, start + this.pageSize);}
  get totalPagesAlbaranes(): number {return Math.max(1, Math.ceil((this.albaranes?.length ?? 0) / this.pageSize));}
  prevPageAlbaranes(): void {if (this.pageAlbaranes > 0) this.pageAlbaranes--;}
  nextPageAlbaranes(): void {if (this.pageAlbaranes < this.totalPagesAlbaranes - 1) this.pageAlbaranes++;}
  goToPageAlbaranes(event: any): void { const inputPage = Number(event.target.value); 
    if (inputPage >= 1 && inputPage <= this.totalPagesAlbaranes) {this.pageAlbaranes = inputPage - 1;}
  }

  fetchApplicacionesDetails(facnum: number) {
    this.limpiarMEssages();

    this.http.get<any>(
      `${environment.backendUrl}/api/fde/${this.entcod}/${this.eje}/${facnum}`
    ).subscribe({
      next: (response) => {

        this.apalicaciones = response.map((item: any) => {

          if (item.FDEDIF !== undefined && item.FDEDIF !== null && item.FDEDIF !== '') {
            let value = String(item.FDEDIF)
              .replace(/[^\d,.-]/g, '')
              .replace(',', '.');

            const num = parseFloat(value);

            item.FDEDIF = isNaN(num) ? 0 : num;
          } else {
            item.FDEDIF = 0;
          }

          return item;
        });

        this.backupAplicaciones = [...this.apalicaciones];
        this.isLoading = false;
        this.pageAplicaiones = 0;
      },

      error: (err) => {
        this.apalicaciones = [];
        this.isLoading = false;
        this.pageAplicaiones = 0;
        this.moreInfoMessageError = err.error.error ?? err.error;
      }
    });
  }
  pageAplicaiones = 0;
  get paginatedApiciones(): any[] {if (!this.apalicaciones || this.apalicaciones.length === 0) return []; const start = this.pageAplicaiones * this.pageSize; return this.apalicaciones.slice(start, start + this.pageSize);}
  get totalPagesAplicaciones(): number {return Math.max(1, Math.ceil((this.apalicaciones?.length ?? 0) / this.pageSize));}
  prevPageAplicaiones(): void {if (this.pageAplicaiones > 0) this.pageAplicaiones--;}
  nextPageAplicaciones(): void {if (this.pageAplicaiones < this.totalPagesAplicaciones - 1) this.pageAplicaiones++;}
  goToPageAplicaciones(event: any): void { const inputPage = Number(event.target.value); 
    if (inputPage >= 1 && inputPage <= this.totalPagesAplicaciones) {this.pageAplicaiones = inputPage - 1;}
  }

  descuentosError: string = '';
  fetchDescuentosDetails(facnum: number) {
    this.http.get<any>(`${environment.backendUrl}/api/fdt/${this.entcod}/${this.eje}/${facnum}`).subscribe({
      next: (response) => {
        this.descuentos = response;
        this.backupDescuentos = Array.isArray(response) ? [...response] : [];
        this.isLoading = false;
        this.pageDescuentos = 0;
      }, error: (err) => {
        this.descuentos = [];
        this.descuentosError = err.error.error ?? err.error;
        this.isLoading = false;
        this.pageDescuentos = 0;
      }
    });
  }
  pageDescuentos = 0;
  get paginatedDescuentos(): any[] {if (!this.descuentos || this.descuentos.length === 0) return []; const start = this.pageDescuentos * this.pageSize; return this.descuentos.slice(start, start + this.pageSize);}
  get totalPagesDescuentos(): number {return Math.max(1, Math.ceil((this.descuentos?.length ?? 0) / this.pageSize));}
  prevPageDescuentos(): void {if (this.pageDescuentos > 0) this.pageDescuentos--;}
  nextPageDescuentos(): void {if (this.pageDescuentos < this.totalPagesDescuentos - 1) this.pageDescuentos++;}
  goToPageDescuentos(event: any): void { const inputPage = Number(event.target.value); 
    if (inputPage >= 1 && inputPage <= this.totalPagesDescuentos) {this.pageDescuentos = inputPage - 1;}
  }

  //adding albaranes 
  albaranesAddGrid: boolean = false;
  isLoadingAlbaranes: boolean = false;
  isAddingAlbaranes: boolean = false;
  albaranesError: string = '';
  albaranesDesde: string = '';
  albaranesHasta: string = '';
  tercod: number = 0;
  albaranesAdd: any[] = [];
  openAlbaranesAdd() {
    this.limpiarMEssages();
    this.albaranesAddGrid = true;
    this.tercod = this.selectedFacturas.tercod;
    this.fetchAlrabanes();
  }

  closeAlbaranesAdd() {
    this.albaranesAddGrid = false;
    this.limpiarSearch();
  }

  fetchAlrabanes() {
    this.limpiarMEssages();
    this.isLoadingAlbaranes = true;

    this.http.get<any>(`${environment.backendUrl}/api/alb/albaranes-factura/${this.entcod}/${this.tercod}/${this.eje}/${this.centroGestor}`).subscribe({
      next: (res) => {
        this.isLoadingAlbaranes = false;
        this.albaranesAdd = res;
        this.pageAlbaranesAdd = 0;
      },
      error: (err) => {
        this.albaranesAdd = [];
        this.albaranesError = err.error.error ?? err.error;
        this.isLoadingAlbaranes = false;
        this.pageAlbaranesAdd = 0;
      }
    })
  }
  pageAlbaranesAdd = 0;
  get paginatedAlbaranesAdd(): any[] {if (!this.albaranesAdd || this.albaranesAdd.length === 0) return []; const start = this.pageAlbaranesAdd * this.pageSize; return this.albaranesAdd.slice(start, start + this.pageSize);}
  get totalPagesAlbaranesAdd(): number {return Math.max(1, Math.ceil((this.albaranesAdd?.length ?? 0) / this.pageSize));}
  prevPageAlbaranesAdd(): void {if (this.pageAlbaranesAdd > 0) this.pageAlbaranesAdd--;}
  nextPageAlbaraneAdd(): void {if (this.pageAlbaranesAdd < this.totalPagesAlbaranesAdd - 1) this.pageAlbaranesAdd++;}
  goToPageAlbaranesAdd(event: any): void { const inputPage = Number(event.target.value); 
    if (inputPage >= 1 && inputPage <= this.totalPagesAlbaranesAdd) {this.pageAlbaranesAdd = inputPage - 1;}
  }

  searchAlbaranes() {
    this.limpiarMEssages();
    if (this.albaranesDesde === '' && this.albaranesHasta === '') {
      this.albaranesError = 'Falta un fecha';
    }
    
    if (this.albaranesDesde && !this.albaranesHasta) {
      const backendDate = this.toBackendDate(this.albaranesDesde);
      this.isLoadingAlbaranes = true;
      this.http.get<any>(`${environment.backendUrl}/api/alb/search-albaranes-Desde/${this.entcod}/${this.tercod}/${backendDate}/${this.eje}/${this.centroGestor}`).subscribe({
        next: (res) => {
          this.isLoadingAlbaranes = false;
          this.albaranesAdd = res;
          this.pageAlbaranesAdd = 0;
        },
        error: (err) => {
          this.albaranesAdd = [];
          this.albaranesError = err.error.error ?? err.error;
          this.isLoadingAlbaranes = false;
          this.pageAlbaranesAdd = 0;
        }
      })
    } else if (!this.albaranesDesde && this.albaranesHasta) {      
      const backendDate = this.toBackendDate(this.albaranesHasta);
      this.isLoadingAlbaranes = true;
      this.http.get<any>(`${environment.backendUrl}/api/alb/search-albaranes-Hasta/${this.entcod}/${this.tercod}/${backendDate}/${this.eje}/${this.centroGestor}`).subscribe({
        next: (res) => {
          this.isLoadingAlbaranes = false;
          this.albaranesAdd = res;
          this.pageAlbaranesAdd = 0;
        },
        error: (err) => {
          this.albaranesAdd = [];
          this.albaranesError = err.error.error ?? err.error;
          this.isLoadingAlbaranes = false;
          this.pageAlbaranesAdd = 0;
        }
      })
    }
  }
  toBackendDate(dateStr: string): string {
    if (!dateStr) return '';
    return `${dateStr}T00:00:00`;
  }

  limpiarSearch() {
    this.limpiarMEssages();
    this.emptySearchAlbaranes();
    this.fetchAlrabanes();
  }

  emptySearchAlbaranes() {
    this.albaranesDesde = '';
    this.albaranesHasta = '';
    this.albaranesAdd = []; 
    this.caughtAlbranaes = [];
  }

  caughtAlbranaes: any[] = [];
  selectAlbaranesAdd(A: any) {
    if (this.caughtAlbranaes.includes(A)) {
      const index = this.caughtAlbranaes.indexOf(A);
      if(index !== -1) {
        this.caughtAlbranaes.splice(index, 1);
      }
    } else {
      this.caughtAlbranaes = [...this.caughtAlbranaes, A];
    }
  }

  isAlbaranesSelected(a: any): boolean {
    return this.caughtAlbranaes.includes(a);
  }

  adingAlbaranes() {
    this.limpiarMEssages();
    this.isAddingAlbaranes = true;
    const facnum = this.selectedFacturas.facnum;

    const payload = this.caughtAlbranaes.map(Obj => ({
      "ENT": this.entcod,
      "EJE": this.eje,
      "ALBNUM": Obj.albnum,
      "CONCTP": Obj.conctp,
      "CONCPR": Obj.concpr,
      "CONCCR": Obj.conccr,
      "ALBBIM": Obj.albbim,
      "FACNUM": facnum
    }))

    this.http.patch(`${environment.backendUrl}/api/alb/add-albaranes`, payload).subscribe({
      next: (res) => {
        this.updateFacturaInfo();
        this.isAddingAlbaranes = false;
        this.fetchAlbaranesDEtail(facnum);
        this.closeAlbaranesAdd();
        this.moreInfoMessageSuccess = 'albaranes añadido correctamente';
        this.openAlbarnaes();
      },
      error: (err) => {
        this.isAddingAlbaranes = false;
        this.albaranesError = err.error.error ?? err.error;
      }
    })
  }

  updateFacturaInfo() {
    const facnum = this.selectedFacturas.facnum;
    if (!facnum) return;
    this.http.get(`${environment.backendUrl}/api/fac/factura/${this.entcod}/${this.eje}/${facnum}`).subscribe({
      next: (res) => {
        const updatedFactura = Array.isArray(res) ? res[0] : res;

        const index = this.facturas.findIndex((f: any) => Number(f.facnum) === Number(facnum));

        if (index !== -1) {
          this.facturas[index] = {
            ...this.facturas[index],
            ...updatedFactura
          };
        }

        this.selectedFacturas = {
          ...this.selectedFacturas,
          ...updatedFactura
        };

        if (this.tempFactura) {
          this.tempFactura = { ...this.selectedFacturas };
        }
      },
      error: (err) => {
        console.warn(err.error.error ?? err.error);
      }
    })
  }

  //quitar albaranes (delete)
  deleteAlbaranGrid: boolean = false;
  dalbaranesDeleteMessage: string = '';
  isDeletingAlbaranes: boolean = false;
  albaranToDelete: any = null;
  showDeleteAlbaran(albaran: any) {
    this.limpiarMEssages()
    if (!albaran) {return;}
    
    this.albaranToDelete = albaran;
    this.deleteAlbaranGrid = true;
  }

  closeDeleteAlbaran() {
    this.limpiarMEssages();
    this.deleteAlbaranGrid = false;
    this.albaranToDelete = null;
  }

  quitarAlbaran() {
    this.limpiarMEssages();
    const albnum = this.albaranToDelete.albnun;
    const facnum = this.selectedFacturas.facnum;
    const albbim = this.albaranToDelete.albbim;
    this.isDeletingAlbaranes = true;

    const payload = {
      "ENT": this.entcod,
      "EJE": this.eje,
      "ALBNUM": albnum,
      "FACNUM": facnum,
      "ALBBIM": albbim
    }

    this.http.patch(`${environment.backendUrl}/api/alb/quitar-albaranes`, payload).subscribe({
      next: (res) => {
        this.updateFacturaInfo();
        this.isDeletingAlbaranes = false;
        this.closeDeleteAlbaran();
        this.fetchAlbaranesDEtail(facnum);
      },
      error: (err) => {
        this.isDeletingAlbaranes = false;
        this.dalbaranesDeleteMessage = err.error.error ?? err.error;
      }
    })
  }
  
  //updating applicaciones
  isUpdatingApplicaciones: boolean = false;
  updateApplicaciones(a: any) {
    this.limpiarMEssages();
    const numero = this.selectedFacturas.facnum;
    const referencia = a.FDEREF;

    const payload = {
      "FDEDIF": this.parseCurrency(a.FDEDIF),
      "FACIDI": this.totalFDEDIF
    }

    this.isUpdatingApplicaciones = true;
    this.http.patch(`${environment.backendUrl}/api/fde/update-diferencias/${this.entcod}/${this.eje}/${numero}/${referencia}`, payload).subscribe({
      next: (res) => {
        this.updateFacturaInfo();
        this.isUpdatingApplicaciones = false;
        this.selectedFacturas.facidi = this.totalFDEDIF;
      },
      error: (err) => {
        this.isUpdatingApplicaciones = false;
        this.moreInfoMessageError = err.error.error ?? err.error;
      }
    })
  }

  get totalFDEDIF(): number {
    return this.apalicaciones
    .map(a => this.parseCurrency(a.FDEDIF))
    .reduce((sum, val) => sum + val, 0);
  }

  private parseCurrency(value: any): number {
    if (value === undefined || value === null || value === '') return 0;

    if (typeof value === 'number') {
      return value;
    }

    const cleaned = String(value)
      .replace(/\s/g, '')
      .replace(/[€$]/g, '')
      .replace(/\./g, '')
      .replace(',', '.');

    const num = parseFloat(cleaned);

    return isNaN(num) ? 0 : num;
  }

  //adding facturas from sicalwin
  addFacturaGrid: boolean = false;
  facturasWb: any[] = [];
  facturasErrorMessage: string = '';
  isLoadingFactura: boolean = false;
  isAddingFactura: boolean = false;
  openFacturaAdd() {
    this.limpiarMEssages();
    this.addFacturaGrid = true;
  }

  closeFacturaAdd() {
    this.limpiarMEssages();
    this.emptySearch();
    this.facturasWb = [];
    this.addFacturaGrid = false;
    this.caughtFacturas = [];
  }

  pageFacturas = 0;
  get paginatedFacturaWs(): any[] {if (!this.facturasWb || this.facturasWb.length === 0) return []; const start = this.pageFacturas * this.pageSize; return this.facturasWb.slice(start, start + this.pageSize);}
  get totalPagesFacturas(): number {return Math.max(1, Math.ceil((this.facturasWb?.length ?? 0) / this.pageSize));}
  prevPageFacturas(): void {if (this.pageFacturas > 0) this.pageFacturas--;}
  nextPageFacturas(): void {if (this.pageFacturas < this.totalPagesFacturas - 1) this.pageFacturas++;}
  goToPageFacturas(event: any): void { const inputPage = Number(event.target.value); 
    if (inputPage >= 1 && inputPage <= this.totalPagesFacturas) {this.pageFacturas = inputPage - 1;}
  }

  proveedor: string = '';
  facturaNumero: string = '';
  RcfDesde: string = '';
  RcfHasta: string = '';
  fechaFacturaDesde: string = '';
  fechaFacturaHasta: string = '';
  searchFacturas() {
    this.limpiarMEssages();    
    const payload: any = {
      "org": this.WSorg,
      "ent": this.WSent,
      "eje": this.eje,
      "usu": environment.sicalUsername,
      "pwd": environment.sicalPassword,
      "publicKey": environment.sicalPublicKey,
      "tipoDocumento": 0,
      "cge": this.centroGestor,
      "situacionIgual": "08",
      "estado": 0
    }

    if (this.proveedor) payload.tercero = this.proveedor;
    if (this.facturaNumero) payload.docProveedor = this.facturaNumero;
    if (this.RcfDesde) payload.fecRegDesde = `${this.RcfDesde}T00:00:00`;
    if (this.RcfHasta) payload.fecRegHasta = `${this.RcfHasta}T23:59:59`;

    if (this.fechaFacturaDesde) payload.fecDocDesde = `${this.fechaFacturaDesde}T00:00:00`;
    if (this.fechaFacturaHasta) payload.fecDocHasta = `${this.fechaFacturaHasta}T23:59:59`;
    this.isLoadingFactura = true;
    this.http.post<any[]>(`${environment.backendUrl}/api/facturas/consulta`, payload).subscribe({
      next: (response) => {
        this.facturasWb = response;
        this.updatePagination();
        this.isLoadingFactura = false;
      },
      error: (err) => {
        this.facturasWb = [];
        this.facturasErrorMessage = err.error?.error ?? err.error;
        this.isLoadingFactura = false;
      }
    });
  }

  clearSearch() {
    this.limpiarMEssages();
    this.emptySearch();
  }

  emptySearch() {
    this.proveedor = '';
    this.facturaNumero = '';
    this.RcfDesde = '';
    this.RcfHasta = '';
    this.fechaFacturaDesde = '';
    this.fechaFacturaHasta = '';
    this.facturasWb = [];
  }

  caughtFacturas: any[] = [];
  selectFacturasAdd(F: any) {
    if (this.caughtFacturas.includes(F)) {
      const index = this.caughtFacturas.indexOf(F);
      if(index !== -1) {
        this.caughtFacturas.splice(index, 1);
      }
    } else {
      this.caughtFacturas = [...this.caughtFacturas, F];
    }
  }

  isFacturaSelected(a: any): boolean {
    return this.caughtFacturas.includes(a);
  }

  facturasSuccessMessage: string = '';
  cargarFacturaError: string = '';
  addingFacturas() {
    this.limpiarMEssages();

    const today = new Date();
    const payload = this.caughtFacturas.map(Obj => {
      const [day, month, year] = Obj.fechaDocumento.split("/");
      return {
        "ENT": this.entcod,
        "EJE": this.eje,
        "tercero": Obj.tercero,
        "CGECOD": this.centroGestor,
        "FACIMP": Number(Obj.impFactura.replace(",", ".")),
        "FACIEC": 0,
        "FACIDI": 0,
        "FACTDC": Obj.tipoRegistro,
        "FACANN": Number(Obj.annoRegistro),
        "FACFAC": Number(Obj.numRegistro),
        "FACDOC": Obj.numDocumento,
        "FACDAT": `${year}-${month}-${day}T00:00:00`,
        "FACTXT": Obj.Texto,
        "FACDTO": 0,
        "FACFRE": today.toISOString()
      };
    });

    if (this.caughtFacturas.some(obj => obj.numDocumento == null)) {
      return;
    }

    this.isAddingFactura = true;
    this.http.post<any>(`${environment.backendUrl}/api/fac/add-facturas`, payload).subscribe({
      next: (res) => {
        this.isAddingFactura = false;
        this.openFacturaMessages();
        this.savedNames = res.savedNames;
        this.unsavedNames = res?.unsavedNames;
        this.missingProviders = res?.missingProviders.join(', ') ?? '';
      },
      error: (err) => {
        this.isAddingFactura = true;
        this.cargarFacturaError = err.error.error ?? err.error
      }
    })
  }

  savedNames: any = [];
  unsavedNames: any = [];
  missingProviders: any = [];
  addFacturaMessage: boolean = false;
  openFacturaMessages() {
    this.addFacturaMessage = true;
  }

  closeFacturasMessages() {
    this.limpiarMEssages();
    this.addFacturaMessage = false;
    this.closeFacturaAdd();
  }

  //misc 
  limpiarMEssages() {
    this.filterFacturaMessage = '';
    this.moreInfoMessageSuccess = '';
    this.moreInfoMessageError = '';
    this.facturasErrorMessage = '';
    this.facturasSuccessMessage = '';
    this.albaranesError = '';
    this.dalbaranesDeleteMessage = '';
    this.facturaDetailSuccess = '';
    this.facturaDetailError = '';
    this.albaranError = '';
    this.descuentosError = '';
    this.cargarFacturaError = '';
  }
}
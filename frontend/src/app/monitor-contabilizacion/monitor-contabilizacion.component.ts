import { Component, HostListener} from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  selector: 'app-monitor-contabilizacion',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  providers: [DatePipe],
  templateUrl: './monitor-contabilizacion.component.html',
  styleUrls: ['./monitor-contabilizacion.component.css']
})
export class MonitorContabilizacionComponent {
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

  facturas: any[] = [];
  page = 0;
  pageSize = 20;
  estadoMessage: string = '';
  public Math = Math;

  constructor(private http: HttpClient, private router: Router, private datePipe: DatePipe ) {}

  isLoading: boolean = false;
  ngOnInit(): void{
    this.limpiarMEssages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const cge = sessionStorage.getItem('CENTROGESTOR');
    const estadoCentroGestor = sessionStorage.getItem('ESTADOGC');
    const entSession = sessionStorage.getItem('WSENT'); 
    const orgSession = sessionStorage.getItem('WSORG');   

    if (cge){ const parsed = JSON.parse(cge); this.centroGestor = parsed.value;}
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}
    if (entSession) {const parsed = JSON.parse(entSession); this.WSent = parsed.WSENT;}
    if (orgSession) {const parsed = JSON.parse(orgSession); this.WSorg = parsed.WSORG;}

    console.log(this.WSorg)
    console.log(this.WSent)
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
        this.estadoMessage = 'Centro Gestor CERRADO';
      }
      if ( this.estadogc === 2) {
        this.estadoMessage = 'Centro Gestor CERRADO para CONTABILIZAR';
      }
    }

    this.fetchFacturas();
  }

  //main table functions
  filterFacturaMessage:string = '';
  fetchFacturas() {
    this.limpiarMEssages();
    this.isLoading = true;

    this.http.get<any>(`${environment.backendUrl}/api/fac/contabilizacion/search?ent=${this.entcod}&eje=${this.eje}&cgecod=${this.centroGestor}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.page = 0;
        this.facturas = res;
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.filterFacturaMessage = err.error.error ?? err.error;
      }
    })
  }
  
  sortField: 'facnum' | 'tercod' | 'ternom' | 'ternif' | 'facfre' | 'facimp' | 'facdto' | 'facdoc' | 'facann' | 'facfac' | 'facdat' | 'factxt' | 'facado' |'FFACFCO' | null = null;
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
      aValue = a[this.sortColumn];
      bValue = b[this.sortColumn];

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

  get paginatedFacturas(): any[] { if (!this.facturas || this.facturas.length === 0) return [];
    const start = this.page * this.pageSize; return this.facturas.slice(start, start + this.pageSize);
  }
  get totalPages(): number { return Math.max(1, Math.ceil((this.facturas?.length ?? 0) / this.pageSize)); }
  prevPage(): void { if (this.page > 0) this.page--; }
  nextPage(): void { if (this.page < this.totalPages - 1) this.page++; }
  goToPage(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) { this.page = inputPage - 1; }
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
      index: index + 1,
      facnum: row.facnum ?? '',
      tercod: row.tercod ?? '',
      ternom: row.ter.ternom ?? '',
      ternif: row.ter.ternif ?? '',
      facfre: this.formatDate(row.facfre),
      facimp: this.formatCurrency(row.facimp),
      facdto: row.facdto ?? '',
      facdoc: row.facdoc ?? '',
      facann: row.facann ?? '',
      facfac: row.facfac ?? '',
      facdat: this.formatDate(row.facdat),
      fctxt: row.factxt ?? '',
      facado: row.facado ?? '',
      facfco: this.formatDate(row.facfco)
    }));

    const columns = [
      { header: '#', dataKey: 'index' },
      { header: 'Número Registro', dataKey: 'facnum' },
      { header: 'Código Prov', dataKey: 'tercod' },
      { header: 'Nombre Proveedor', dataKey: 'ternom' },
      { header: 'NIF Prov', dataKey: 'ternif' },
      { header: 'F.Registro', dataKey: 'facfre' },
      { header: 'Importe', dataKey: 'facimp' },
      { header: 'Descuentos', dataKey: 'facdto'},
      { header: 'Núm. Factura', dataKey: 'facdoc' },
      { header: 'Año', dataKey: 'facann' },
      { header: 'R.C.F', dataKey: 'facfac' },
      { header: 'F.Factura', dataKey: 'facdat' },
      { header: 'Descripción', dataKey: 'factxt'},
      { header: 'OP.Contable', dataKey: 'facado' },
      { header: 'F.Contable', dataKey: 'facfco' }
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Monitor de contabilización', 10, 20);

    autoTable(doc, {
      startY: 30,
      theme: 'plain',
      head: [columns.map(c => c.header)],
      body: rows.map(row => columns.map(c => row[c.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 8, cellPadding: 4 },
      headStyles: {
        fillColor: [240, 240, 240],
        textColor: [33, 53, 71],
        fontStyle: 'bold',
        halign: 'left'
      },
      tableLineColor: [200, 200, 200],
      tableLineWidth: 0.5,
      columnStyles: {
        index: { cellWidth: 8 },
        facnum: { cellWidth: 18 },
        tercod: { cellWidth: 18 },
        ternom: { cellWidth: 36 },
        ternif: { cellWidth: 20 },
        facfre: { cellWidth: 20 },
        facimp: { cellWidth: 22 },
        facdto: { cellWidth: 10},
        facdoc: { cellWidth: 20 },
        facann: { cellWidth: 15 },
        facfac: { cellWidth: 18 },
        facdat: { cellWidth: 20 },
        factxt: { cellWidth: 36},
        facado: { cellWidth: 15 },
        facfco: { cellWidth: 20 }
      }
    });

    doc.save('Monitor_contabilización.pdf');
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
      ternom: row.ter.ternom ?? '',
      ternif: row.ter.ternif ?? '',
      facfre: row.facfre ?? '',
      facimp: row.facimp ?? '',
      facdto: row.facdto ?? '',
      facdoc: row.facdoc ?? '',
      facann: row.facann ?? '',
      facfac: row.facfac ?? '',
      facdat: row.facdat ?? '',
      factxt: row.factxt ?? '',
      facado: row.facado ?? '',
      facfco: row.facfco ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de facturas']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['N.Registro', 'Código Prov', 'Nombre Proveedor', 'NIF', 'F.Registro', 'Importe', 'Descuentos', 'Núm. Factura', 'Año', 'R.C.F', 'F.Factura', 'Descripción', 'OP.Contable', 'F.Contable']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 12 },
      { wch: 40 },
      { wch: 20 },
      { wch: 20 },
      { wch: 10 },
      { wch: 20 },
      { wch: 22 },
      { wch: 15 },
      { wch: 15 },
      { wch: 40 },
      { wch: 30 },
      { wch: 30 },
      { wch: 30 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Facturas');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'MonitorContabilizacione.xlsx'
    );
  }

  facturaSearch: string = '';
  ejerSearch: string = '';
  fechasType: 'registro' | 'factura' = 'factura';
  desde: string = '';
  hasta: string = '';
  searchError: string = '';
  search() {
    this.limpiarMEssages();
    this.caughtFacturas = [];
    this.isLoading = true;

    let params = `ent=${this.entcod}&eje=${this.eje}&cgecod=${this.centroGestor}`;

    params += `&fechaType=${this.fechasType}`;
    if (this.desde && this.desde.trim() !== '') {
      params += `&desde=${this.desde}T00:00:00`;
    }
    if (this.hasta && this.hasta.trim() !== '') {
      params += `&hasta=${this.hasta}T23:59:59`;
    }
    if (this.ejerSearch && this.ejerSearch.trim() !== '') {
      params += `&facann=${this.ejerSearch}`;
    }
    if (this.facturaSearch && this.facturaSearch.trim() !== '') {
      params += `&search=${encodeURIComponent(this.facturaSearch)}`;
    }

    this.http.get<any>(`${environment.backendUrl}/api/fac/contabilizacion/search?${params}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.page = 0;
        this.facturas = res;
        this.updatePagination();
      },
      error: (err) => {
        this.isLoading = false;
        this.searchError = err.error.error ?? err.error;
        this.facturas = [];
      }
    });
  }

  limpiarSearch() {
    this.facturaSearch = '';
    this.ejerSearch = '';
    this.fechasType = 'factura';
    this.desde = '';
    this.hasta = '';
    this.caughtFacturas = [];
    this.facturas = [];
    this.fetchFacturas();
    this.fechaContable = '';
  }

  //detail grid functions
  selectedFacturas: any = null;
  detallesMessage: String = '';
  detailView: 'Albaranes' | 'Contabilización' = 'Albaranes';
  albaranesOptio: 'albaranes' | 'aplicaciones' | 'descuentos' = 'albaranes';
  albaranes: any[] = [];
  apalicaciones: any[] = [];
  descuentos: any[] = [];
  moreInfoMessageSuccess: string = '';
  moreInfoMessageIsSuccess: boolean = false;
  moreInfoMessageError: string = '';
  isUpdatingFactura: boolean = false;
  facturaDetailError: string = '';
  facturaDetailSuccess: string = '';
  showDetails(factura: any) {
    this.limpiarMEssages();
    this.selectedFacturas = { ...factura };
    if (this.selectedFacturas.facfre) {
      this.selectedFacturas.facfre = this.datePipe.transform(this.selectedFacturas.facfre, 'yyyy-MM-dd');
    }
    this.openAlbarnaes();
  }

  closeDetails() {
    this.selectedFacturas = null;
    this.limpiarMEssages();
  }

  public getPendingApply(f: any): number {
    if (!f) return 0;
    const toNum = (v: any) => (v === null || v === undefined || v === '' ? 0 : Number(v) || 0);
    let pending = toNum(f.facimp) - (toNum(f.faciec) + toNum(f.facidi));
    return Math.round(pending * 100) / 100;
  }

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

  openContabilizacion() {
    this.limpiarMEssages();
    this.AlbaranesGrid = false;
    this.ContabilizacionGrid = true;
    this.detailView = 'Contabilización';
  }

  setAlbaranesOptio(option: 'albaranes' | 'aplicaciones' | 'descuentos', facnum: number): void {
    this.albaranesOptio = option;
    this.limpiarMEssages();
    this.isLoading = true;
    this.albaranes = [];
    this.apalicaciones = [];
    this.descuentos = [];

    if ( option === 'albaranes') {this.fetchAlbaranesDEtail(facnum);}
    if ( option === 'aplicaciones') {
      this.fetchApplicacionesDetails(facnum);
    }
    if ( option === 'descuentos') {this.fetchDescuentosDetails(facnum);}
  }

  fetchAlbaranesDEtail(facnum: number) {
    this.http.get<any>(`${environment.backendUrl}/api/alb/albaranes/${this.entcod}/${this.eje}/${facnum}`).subscribe({
      next: (response) => {
        if (!Array.isArray(response) || response.length === 0) {
          this.moreInfoMessageIsSuccess = true;
          this.moreInfoMessageSuccess = 'No hay albaranes por las medidas de búsqueda';
          this.albaranes = [];
          this.isLoading = false;
          this.pageAlbaranes = 0;
        } else {
          this.albaranes = response;
          this.isLoading = false;
          this.pageAlbaranes = 0;
        }
      }, error: (err) => {
        this.albaranes = [];
        this.moreInfoMessageError = err.error.error ?? err.error;
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
    this.http.get<any>(`${environment.backendUrl}/api/fde/${this.entcod}/${this.eje}/${facnum}`).subscribe({
      next: (response) => {
        if (!Array.isArray(response) || response.length === 0) {
          this.moreInfoMessageIsSuccess = true;
          this.moreInfoMessageSuccess = 'No hay aplicaciones por las medidas de búsqueda';
          this.apalicaciones = []
          this.isLoading = false;
          this.pageAplicaiones = 0
        } else {

          this.apalicaciones = response.map((item: any) => {
            if (item.FDEDIF !== undefined && item.FDEDIF !== null && item.FDEDIF !== '') {
              let value = String(item.FDEDIF)
                .replace(/[^\d,.-]/g, '') 
                .replace(/\s/g, '');      

              if (value.indexOf(',') > -1) {
                value = value.replace(/\./g, '');
              }
              value = value.replace(',', '.');

              let num = parseFloat(value);
              if (!isNaN(num)) {
                item.FDEDIF = num.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
              }
            }
            return item;
          });
          this.isLoading = false;
          this.pageAplicaiones = 0
        }
      }, error: (err) => {
        this.apalicaciones = [];
        this.moreInfoMessageError = err.error.error ?? err.error;
        this.isLoading = false;
        this.pageAplicaiones = 0
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

  fetchDescuentosDetails(facnum: number) {
    this.http.get<any>(`${environment.backendUrl}/api/fdt/${this.entcod}/${this.eje}/${facnum}`).subscribe({
      next: (response) => {
        if (!Array.isArray(response) || response.length === 0) {
          this.moreInfoMessageIsSuccess = true;
          this.moreInfoMessageSuccess = 'No hay descuentos por las medidas de búsqueda';
          this.descuentos = []
          this.isLoading = false;
          this.pageDescuentos = 0;
        } else {
          this.descuentos = response;
          this.isLoading = false;
          this.pageDescuentos = 0;
        }
      }, error: (err) => {
        this.descuentos = [];
        this.moreInfoMessageError = err.error.error ?? err.error;
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

  //contabilizar functions
  caughtFacturas: any[] =[];
  selectFacturas(f: any) {
    if (this.caughtFacturas.includes(f)) {
      const index = this.caughtFacturas.indexOf(f);
      if(index !== -1) {
        this.caughtFacturas.splice(index, 1);
      }
    } else {
      this.caughtFacturas = [...this.caughtFacturas, f];
    }
  }

  isFacturaSelected(a: any) {
    return this.caughtFacturas.includes(a);
  }

  contaConfirm: boolean = false;
  openContaConfirm() {
    this.limpiarMEssages();
    this.contaConfirm = true;
  }

  closeContaConfirm() {
    this.limpiarMEssages();
    this.contaConfirm = false;
  }

  contabilizarChecks() {
    if (this.fechaContable === '') {
      this.searchError = 'Falta fecha contable';
      return;
    } else {
      let year = this.fechaContable.split('-')[0];
      if (Number(year) != Number(this.eje)) {
        this.searchError = 'La fecha contable debe pertenecer al ejercicio contable';
        return;
      }
    }

    if (this.caughtFacturas.length === 0) {
      this.searchError = 'Debe seleccionar al menos una factura';
      return;
    }

    this.openContaConfirm();
  }

  fechaContable: string = '';
  ESCONTRATO: boolean = false;
  contaFac: number = 0;
  totalContaFac: number = 0;
  contabilizarResults: { facnum: number; success: boolean; message: string }[] = [];
  async contabilizar() {
    this.closeContaConfirm();
    this.limpiarMEssages();
    this.contabilizarResults = [];

    if (this.fechaContable === '') {
      this.searchError = 'Falta fecha contable';
      return;
    }

    const year = this.fechaContable.split('-')[0];
    if (Number(year) !== Number(this.eje)) {
      this.searchError = 'La fecha contable debe pertenecer al ejercicio contable';
      return;
    }

    if (this.caughtFacturas.length === 0) {
      this.searchError = 'Debe seleccionar al menos una factura';
      return;
    }
    
    this.contaFac = 0;
    this.totalContaFac = this.caughtFacturas.length;
    this.isContabilizando = true;

    for (const factura of this.caughtFacturas) {
      await this.contabilizarFacturaAsync(factura);
      this.contaFac++;
    }

    this.isContabilizando = false;

    const exitosas = this.contabilizarResults.filter(r => r.success).length;
    const fallidas = this.contabilizarResults.filter(r => !r.success).length;
    
    if (exitosas > 0 && fallidas === 0) {
      this.filterfacturaSuccess = `${exitosas} factura(s) contabilizada(s) correctamente`;
    } else if (exitosas > 0 && fallidas > 0) {
      this.filterfacturaSuccess = `${exitosas} factura(s) contabilizada(s)`;
      this.filterFacturaMessage = `${fallidas} factura(s) con errores`;
    } else if (fallidas > 0) {
      this.filterFacturaMessage = `${fallidas} factura(s) con errores`;
    }

    this.openMonitorMessages();
    const successFacnums = this.contabilizarResults.filter(r => r.success).map(r => r.facnum);
    this.caughtFacturas = this.caughtFacturas.filter(f => !successFacnums.includes(f.facnum));
  }

  filterfacturaSuccess: string = '';
  newFacado: string = '';
  isContabilizando: boolean = false;
  contabilizarFacturaAsync(factura: any): Promise<void> {
    return new Promise(async (resolve) => {
      const payload = {
        pwd: ".",
        publicKey: "llave1",
        org: this.WSorg,
        ent: this.WSent,       
        entcod: this.entcod,
        eje: String(this.eje),
        usu: "SICALWIN",
        facnum: factura.facnum,
        cgecod: this.centroGestor,
        fechaContable: this.fechaContable,
        esContrato: this.ESCONTRATO
      };

      this.http.post<any>(`${environment.backendUrl}/api/contabilizacion/generar`, payload).subscribe({
        next: async (response) => {
          if (response.exito) {
            this.newFacado = response.opesical;
            
            try {
              await this.updateFactura(factura.facnum, this.newFacado, this.fechaContable);
              
              this.contabilizarResults.push({
                facnum: factura.facnum,
                success: true,
                message: `OP: ${this.newFacado}`
              });
            } catch (err: any) {
              this.contabilizarResults.push({
                facnum: factura.facnum,
                success: false,
                message: `Sicalwin OK pero error al guardar: ${err}`
              });
            }

          } else {
            this.contabilizarResults.push({
              facnum: factura.facnum,
              success: false,
              message: response.mensaje || 'Error desconocido'
            });
          }
          resolve();
        },
        error: (err) => {
          this.contabilizarResults.push({
            facnum: factura.facnum,
            success: false,
            message: err.error?.mensaje || err.error?.error || err.message || 'Error de conexión'
          });
          resolve();
        }
      });
    });
  }

  monitoresMessage: boolean = false;
  openMonitorMessages() {
    this.monitoresMessage = true;
  }

  closeMonitoresMessages() {
    this.limpiarMEssages();
    this.caughtFacturas = [];
    this.monitoresMessage = false;
    this.fetchFacturas();
    this.limpiarSearch();
  }
  updateFactura(facnum: number, facado: any, facfco: string) {
    this.closeContaConfirm();
    this.limpiarMEssages();
    return new Promise<void>((resolve, reject) => {
      const payload = {
        "ENT": this.entcod,
        "EJE": this.eje,
        "FACNUM": facnum,
        "FACADO": facado,
        "FACFCO": facfco,
        "CGECOD": this.centroGestor,
        "ESCONTRATO": this.ESCONTRATO
      };

      this.http.patch<any>(`${environment.backendUrl}/api/facturas/contabilizar-facturas`, payload).subscribe({
        next: () => {
          resolve();
        },
        error: (err) => {
          reject(err.error || err.message || 'Error al actualizar factura');
        }
      });
    });
  }
  

  //misc 
  limpiarMEssages() {
    this.filterFacturaMessage = '';
    this.filterfacturaSuccess = '';
    this.moreInfoMessageSuccess = '';
    this.moreInfoMessageError = '';
    this.facturaDetailSuccess = '';
    this.facturaDetailError = '';
    this.searchError = '';
  }
}

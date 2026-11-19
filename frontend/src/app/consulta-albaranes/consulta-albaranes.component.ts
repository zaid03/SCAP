import { Component, HostListener} from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  selector: 'app-consulta-albaranes',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-albaranes.component.html',
  styleUrls: ['./consulta-albaranes.component.css']
})
export class ConsultaAlbaranesComponent {
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
  eje: number | null = null;
  centroGestor: string = '';
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void{
    this.limpiarMEssages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const cge = sessionStorage.getItem('CENTROGESTOR');

    if (cge){const parsed = JSON.parse(cge); this.centroGestor = parsed.value}
    this.centroDep = this.centroGestor;
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}

    if (!entidad || this.entcod === null || !eje || this.eje === null || !cge || this.centroGestor === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    console.log(this.centroGestor)
    this.fetchAlbaranes();
    this.fetchItemsAmount()
  }

  albaranes: any[] = [];
  isLoading: boolean = false;
  albaranesSuccess: string = '';
  albaranesError: string = '';
  page = 0;
  main_search: string = '';
  centroDep: string = '';
  comprador: string = '';
  ejeSearch: string = '';
  fechaTipo: 'registro' | 'albaran' | '' = '';
  fromDate: string = '';
  toDate: string = '';
  estadoTipo: 'facturado' | 'sin_factura' | 'no_facturable' | '' = 'sin_factura';
  isSearching: boolean = false;
  numberOnly(event: Event) {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, '');
  }

  fetchAlbaranes() {
    this.limpiarMEssages();

    let params = `ent=${this.entcod}&page=${this.page}`;

    if (this.isSearching) {
      if (this.main_search) params += `&main_search=${encodeURIComponent(this.main_search)}`;
      if (this.centroDep) params += `&centroDep=${encodeURIComponent(this.centroDep)}`;
      if (this.comprador) params += `&comprador=${encodeURIComponent(this.comprador)}`;
      if (this.ejeSearch) params += `&ejeSearch=${encodeURIComponent(this.ejeSearch)}`;
      if (this.fechaTipo) params += `&fechaTipo=${encodeURIComponent(this.fechaTipo)}`;
      if (this.fromDate) params += `&fromDate=${encodeURIComponent(this.fromDate + 'T00:00:00')}`;
      if (this.toDate) params += `&toDate=${encodeURIComponent(this.toDate + 'T23:59:59')}`;
      if (this.estadoTipo) params += `&estadoTipo=${encodeURIComponent(this.estadoTipo)}`;
    }

    if (this.sortColumn) {
      params += `&sort=${encodeURIComponent(this.sortColumn)}`;
      params += `&direction=${this.sortDirection}`;
    }
    
    let url = `${environment.backendUrl}/api/alb/consulta-albaranes?${params}`;

    this.isLoading = true;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.albaranes = res;
      },
      error: (err) => {
        this.albaranes = [];
        this.isLoading = false;
        this.albaranesError = err.error.error || err.error;
      }
    })
  }

  search(): void {
    const hasFilters = this.main_search.trim() !== '' || this.centroDep.trim() !== '' || this.comprador.trim() !== '' || this.ejeSearch.trim() !== '' || this.fechaTipo !== '' || this.fromDate !== '' || this.toDate !== '' || this.estadoTipo !== '';

    this.isSearching = hasFilters;
    this.page = 0;
    this.sortColumn = '';
    this.fetchAlbaranes();
    this.fetchItemsAmount();
  }

  limpiarSearch() {
    this.isSearching = false;
    this.page = 0;
    this.sortColumn = '';
    this.main_search = '';
    this.centroDep = this.centroGestor;
    this.comprador = '';
    this.ejeSearch = '';
    this.fechaTipo = '';
    this.fromDate = '';
    this.toDate = '';
    this.estadoTipo = 'sin_factura';
    this.sortDirection = 'asc';
    this.fetchAlbaranes();
    this.fetchItemsAmount();
  }

  totalPages: number = 0;
  fetchItemsAmount() {
    let params = `ent=${this.entcod}`;

    if (this.isSearching) {
      if (this.main_search) params += `&main_search=${encodeURIComponent(this.main_search)}`;
      if (this.centroDep) params += `&centroDep=${encodeURIComponent(this.centroDep)}`;
      if (this.comprador) params += `&comprador=${encodeURIComponent(this.comprador)}`;
      if (this.ejeSearch) params += `&ejeSearch=${encodeURIComponent(this.ejeSearch)}`;
      if (this.fechaTipo) params += `&fechaTipo=${encodeURIComponent(this.fechaTipo)}`;
      if (this.fromDate) params += `&fromDate=${encodeURIComponent(this.fromDate + 'T00:00:00')}`;
      if (this.toDate) params += `&toDate=${encodeURIComponent(this.toDate + 'T23:59:59')}`;
      if (this.estadoTipo) params += `&estadoTipo=${encodeURIComponent(this.estadoTipo)}`;
    }

    this.http.get<any>(`${environment.backendUrl}/api/alb/count-albaranes?${params}`).subscribe({
      next: (res) => {
        this.totalRowCount = Number(res);
        this.totalPages = Math.ceil(res / 20);
      },
      error: (err) => {
        console.warn(err.error.error || err.error);
      }
    })
  }
  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.fetchAlbaranes();
    } 
  }
  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.fetchAlbaranes();
    }
  }
  goToPage(event: any): void {
    const requestedPage = Number(event.target.value) - 1;

    if (
      requestedPage >= 0 &&
      requestedPage < this.totalPages
    ) {
      this.page = requestedPage;
      this.fetchAlbaranes();
    }
  }

  //main table functions
  sortField: 'albnum' | 'ter.tercod' | 'ter.ternom' | 'albref' | 'albdat' | 'albbim' | 'albfre' | 'depcod' | 'albcom' | 'solnum' | 'facnum' | null = null;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  toggleSort(column: string): void {
    this.sortColumn =
      this.sortColumn === column
        ? this.sortColumn
        : column;

    this.sortDirection =
      this.sortColumn === column
        ? (this.sortDirection === 'asc' ? 'desc' : 'asc')
        : 'asc';

    this.page = 0;
    this.fetchAlbaranes();
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
    const isoDate = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (isoDate) return `${isoDate[3]}/${isoDate[2]}/${isoDate[1]}`;

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

  private parseMoney(value: any): number {
    if (value === null || value === undefined || value === '') return 0;
    if (typeof value === 'number') return value;

    let stringValue = String(value).trim();
    const isParenthesizedNegative = /^\(.*\)$/.test(stringValue);

    if (isParenthesizedNegative) {
      stringValue = stringValue.replace(/[()]/g, '');
    }

    stringValue = stringValue
      .replace(/\u00A0/g, ' ')
      .replace(/[^\d.,-]/g, '');

    if (stringValue.includes(',')) {
      stringValue = stringValue.replace(/\./g, '').replace(',', '.');
    }

    const numberValue = parseFloat(stringValue);
    if (isNaN(numberValue)) return 0;

    return isParenthesizedNegative ? -numberValue : numberValue;
  }

  private applyExcelCurrencyFormat(
    worksheet: XLSX.WorkSheet,
    rowCount: number,
    currencyColumns: number[]
  ): void {
    const currencyFormat = '#,##0.00 [$€-es-ES]';

    for (let rowIndex = 2; rowIndex < rowCount + 2; rowIndex++) {
      for (const columnIndex of currencyColumns) {
        const cellAddress = XLSX.utils.encode_cell({
          r: rowIndex,
          c: columnIndex
        });

        const cell = worksheet[cellAddress];
        if (!cell) continue;

        cell.v = this.parseMoney(cell.v);
        cell.t = 'n';
        cell.z = currencyFormat;
      }
    }
  }

  private buildPdfRows(rows: any[]): any[] {
    return rows.map(row => ({
      albnum: row.albnum ?? '',
      tercod: row.ter?.tercod ?? '',
      ternom: row.ter?.ternom ?? '',
      albref: row.albref ?? '',
      albdat: this.formatDate(row.albdat),
      albbim: this.formatCurrency(row.albbim),
      albfre: this.formatDate(row.albfre),
      depcod: row.depcod ?? '',
      albcom: row.albcom ?? '',
      pedido: `${row.solnum ?? ''}-${row.solsub ?? ''}`,
      factura: `${this.eje ?? ''}-${row.facnum ?? ''}`
    }));
  }

  fetchExportPage(page: number, pageSize: number): Promise<any[]> {
    let params =
      `ent=${this.entcod}&page=${page}&size=${pageSize}`;

    if (this.isSearching) {
      if (this.main_search) {
        params += `&main_search=${encodeURIComponent(this.main_search)}`;
      }
      if (this.centroDep) {
        params += `&centroDep=${encodeURIComponent(this.centroDep)}`;
      }
      if (this.comprador) {
        params += `&comprador=${encodeURIComponent(this.comprador)}`;
      }
      if (this.ejeSearch) {
        params += `&ejeSearch=${encodeURIComponent(this.ejeSearch)}`;
      }
      if (this.fechaTipo) {
        params += `&fechaTipo=${encodeURIComponent(this.fechaTipo)}`;
      }
      if (this.fromDate) {
        params += `&fromDate=${encodeURIComponent(this.fromDate + 'T00:00:00')}`;
      }
      if (this.toDate) {
        params += `&toDate=${encodeURIComponent(this.toDate + 'T23:59:59')}`;
      }
      if (this.estadoTipo) {
        params += `&estadoTipo=${encodeURIComponent(this.estadoTipo)}`;
      }
    }

    if (this.sortColumn) {
      params += `&sort=${encodeURIComponent(this.sortColumn)}`;
      params += `&direction=${this.sortDirection}`;
    }

    return new Promise((resolve, reject) => {
      this.http
        .get<any[]>(
          `${environment.backendUrl}/api/alb/consulta-albaranes?${params}`
        )
        .subscribe({
          next: resolve,
          error: reject
        });
    });
  }

  totalRowCount: number = 0;
  async downloadPdf(): Promise<void> {

    const doc = new jsPDF({
      orientation: 'landscape',
      unit: 'mm',
      format: 'a4'
    });

    const columns = [
      { header: 'Albarán', dataKey: 'albnum' },
      { header: 'Proveedor', dataKey: 'tercod' },
      { header: 'Nombre', dataKey: 'ternom' },
      { header: 'Ref. Proveedor', dataKey: 'albref' },
      { header: 'Fecha Alb.', dataKey: 'albdat' },
      { header: 'Importe', dataKey: 'albbim' },
      { header: 'Fecha Reg.', dataKey: 'albfre' },
      { header: 'Destino', dataKey: 'depcod' },
      { header: 'Comprador', dataKey: 'albcom' },
      { header: 'Pedido', dataKey: 'pedido' },
      { header: 'Factura', dataKey: 'factura' }
    ];

    const exportPageSize = 2000;
    const totalExportPages = Math.ceil(this.totalRowCount / exportPageSize);

    for (let page = 0; page < totalExportPages; page++) {
      const rows = await this.fetchExportPage(page, exportPageSize);

      if (!rows.length) continue;

      autoTable(doc, {
        startY: page === 0 ? 20 : undefined,
        columns,
        body: this.buildPdfRows(rows)
          .map(row =>
            columns.map(column =>
              row[column.dataKey as keyof typeof row] ?? ''
            )
          ),
        styles: { font: 'helvetica', fontSize: 8 },
        tableWidth: 'wrap',
        headStyles: {
          fillColor: [240, 240, 240],
          textColor: [33, 53, 71],
          fontStyle: 'bold',
          halign: 'left'
        },
        columnStyles: {
          albbim: {
            halign: 'right'
          }
        },
        showHead: 'everyPage',
        didDrawPage: () => {
          doc.setFontSize(10);
        }
      });
    }

    doc.save('albaranes.pdf');
  }

  async downloadExcel(): Promise<void> {
    this.limpiarMEssages();

    if (!this.totalRowCount) {
      this.albaranesError = 'No hay datos para exportar.';
      return;
    }

    const exportPageSize = 2000;
    const totalExportPages = Math.ceil(
      this.totalRowCount / exportPageSize
    );

    const headers = [
      'Albarán',
      'Proveedor',
      'Nombre',
      'Ref. Proveedor',
      'Fecha Alb.',
      'Importe',
      'Fecha Reg.',
      'Destino',
      'Comprador',
      'Pedido',
      'Factura'
    ];

    const exportRows: any[][] = [];

    for (let page = 0; page < totalExportPages; page++) {
      const rows = await this.fetchExportPage(page, exportPageSize);

      for (const row of rows) {
        exportRows.push([
          row.albnum ?? '',
          row.ter?.tercod ?? '',
          row.ter?.ternom ?? '',
          row.albref ?? '',
          this.formatDate(row.albdat),
          this.parseMoney(row.albbim),
          this.formatDate(row.albfre),
          row.depcod ?? '',
          row.albcom ?? '',
          `${row.solnum ?? ''}-${row.solsub ?? ''}`,
          `${this.eje ?? ''}-${row.facnum ?? ''}`
        ]);
      }
    }

    if (!exportRows.length) {
      this.albaranesError = 'No hay datos para exportar.';
      return;
    }

    const worksheet = XLSX.utils.aoa_to_sheet([
      ['Listado de albaranes'],
      headers,
      ...exportRows
    ]);

    worksheet['!merges'] = [
      { s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }
    ];

    worksheet['!cols'] = [
      { wch: 12 },
      { wch: 12 },
      { wch: 35 },
      { wch: 20 },
      { wch: 15 },
      { wch: 15 },
      { wch: 15 },
      { wch: 15 },
      { wch: 20 },
      { wch: 15 },
      { wch: 15 }
    ];

    this.applyExcelCurrencyFormat(worksheet, exportRows.length, [5]);

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Albaranes');

    const buffer = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array',
      cellStyles: true
    });

    saveAs(
      new Blob([buffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      }),
      'Albaranes.xlsx'
    );
  }

  //details functions
  selectedAlbaran: any = null;
  detailSuccess: String = '';
  showDetails(albaran: any) {
    this.limpiarMEssages();
    this.selectedAlbaran = albaran;
    this.showArticulos(albaran);
  }

  closeDetails() {
    this.selectedAlbaran = null;
    this.limpiarMEssages();
  }

  facturaEstado(facnum: any): string {
    if (facnum != null) {
      if (facnum === 0) {
        return 'SIN FACTURA';
      } else if (facnum > 0) {
        return 'FACTURADO';
      } else if (facnum < 0) {
        return 'No FACTURABLE';
      }
    }
    return '';
  }

  //subDetails functions
  detailView: 'Albaranes' | 'Contabilización' = 'Albaranes';
  albaranesOptio: 'albaranes' | 'aplicaciones' | 'descuentos' = 'albaranes';
  AlbaranesGrid: boolean = false;
  showArticulos(albaran: any) {
    this.limpiarMEssages();
    this.AlbaranesGrid = true;
    this.detailView = 'Albaranes';
    let albnum = albaran.albnum;
    this.fetchArticulos(albnum);
  }

  isLoadingDetails: boolean = false;
  articulosError: string = '';
  articulos: any = [];
  pageServices: number = 0;
  fetchArticulos(albnum: number) {
    this.isLoadingDetails = true;
    this.http.get<any>(`${environment.backendUrl}/api/ade/fetch-articulos/${this.entcod}/${albnum}`).subscribe({
      next: (res) => {
        this.isLoadingDetails = false;
        this.articulos = res;
      },
      error: (err) => {
        this.isLoadingDetails = false;
        this.articulos = [];
        this.articulosError = err.error.error || err.error;
      }
    })
  }
  get paginatedArticulos(): any[] {
    const start = this.pageServices * this.pageSize;
    return this.articulos.slice(start, start + this.pageSize);
  }
  get totalPagesServices(): number {
    return Math.max(
      1,
      Math.ceil((this.articulos?.length ?? 0) / this.pageSize)
    );
  }

  prevPageService(): void {
    if (this.pageServices > 0) {
      this.pageServices--;
    }
  }

  nextPageService(): void {
    if (this.pageServices < this.totalPagesServices - 1) {
      this.pageServices++;
    }
  }

  goToPageService(event: any): void {
    const inputPage = Number(event.target.value);

    if (
      Number.isInteger(inputPage) &&
      inputPage >= 1 &&
      inputPage <= this.totalPagesServices
    ) {
      this.pageServices = inputPage - 1;
    }
  }


  //misc
  limpiarMEssages() {
  this.albaranesSuccess = '';
  this.albaranesError = '';
  this.detailSuccess = '';
  this.articulosError = '';
  }
}
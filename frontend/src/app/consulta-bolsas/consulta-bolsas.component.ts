import { Component, HostListener} from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { finalize } from 'rxjs';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-consulta-bolsas',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, CurrencyPipe],
  templateUrl: './consulta-bolsas.component.html',
  styleUrls: ['./consulta-bolsas.component.css'],
  providers: [CurrencyPipe]
})
export class ConsultaBolsasComponent {
  private fetchCancel$ = new Subject<void>();

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
  entcod: string | null = null;
  eje: number | null = null;
  cge: string = '';
  orgCode: string | null = null;
  entidad: string | null = null;
  backUpCge: string = '';
  creditos: any[] = [];
  private backupCreditos: any[] = [];
  private defaultCreditos: any[] = [];
  wsData: any[] = [];
  public Math = Math;
  page = 0;
  pageSize = 20;
  tableMessage: string = '';
  tableIsError: boolean = false;
  guardarMesage: string = '';
  guardarisError: boolean = false;
  guardarMesageSuccess: string = '';
  guardarisSuccess : boolean = false;

  constructor(private http: HttpClient, private router: Router, private currency: CurrencyPipe) {}

  ngOnInit(): void {
    this.limpiarMessages();
    this.tableIsError = false;

    const ent = sessionStorage.getItem('Entidad');
    const session = sessionStorage.getItem('EJERCICIO');
    const centroGestor = sessionStorage.getItem('CENTROGESTOR');
    const orgnizacion = sessionStorage.getItem('WSORG');
    const entcod = sessionStorage.getItem('WSENT');
    if (ent) { const parsed = JSON.parse(ent); this.entcod = parsed.ENTCOD;}
    if (session) { const parsed = JSON.parse(session); this.eje = parsed.eje;}
    if (centroGestor) { const parsed = JSON.parse(centroGestor); this.cge = parsed.value; this.backUpCge = this.cge;}
    if (orgnizacion) {const parsed = JSON.parse(orgnizacion); this.orgCode = parsed.WSORG;}
    if (entcod) {const parsed = JSON.parse(entcod); this.entidad = parsed.WSENT};

    if (!this.entcod ||  !this.eje || !this.cge || this.orgCode === '' || this.entidad === '') {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchBolsas();
    this.fetchCentroGestorInfo();
  }

  //main table functions`
  organigrama: string = '';
  programa: string = '';
  description: string = '';
  estado: number = 0;
  fetchCentroGestorInfo() {
    this.fetchCancel$.next();

    this.limpiarMessages();
    this.http.get<any>(`${environment.backendUrl}/api/cge/search-centros-codigo/${this.entcod}/${this.eje}/${this.cge}`).subscribe({
      next: (res) => {
        this.cge = res[0].cgecod;
        this.organigrama = res[0].cgeorg;
        this.programa = res[0].cgefun;
        this.description = res[0].cgedes
        this.estado = res[0].cgecic;
      },
      error: (err) => {
        this.organigrama = '';
        this.programa ='';
        this.description = '';
        this.estado = 0;
        console.warn(err.error.error ?? err.error);
      }
    })
  }

  isLoading: boolean = false;
  swLoading: boolean = false;
  fetchBolsas() {
    this.fetchCancel$.next();
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/gbs/fetch-all/${this.entcod}/${this.eje}/${this.cge}`).subscribe({
      next: (response) => {
        this.creditos = Array.isArray(response) ? [...response] : [];
        this.backupCreditos = [...this.creditos];
        this.defaultCreditos = [...this.creditos];
        this.swLoading = true;
        let pendingRequests = this.creditos.length * 2;
        this.creditos.forEach((item, idx) => {
          const numeroOper = item?.gbsope ?? '';
          const referencia = item?.gbsref ?? '';
          const org = item?.gbsorg ?? '';
          const fun = item?.gbsfun ?? '';
          const eco = item?.gbseco ?? '';
          this.http.get<any>(`${environment.backendUrl}/api/sical/partidas?clorg=${org}&clfun=${fun}&cleco=${eco}&orgCode=${this.orgCode}&entidad=${this.entidad}&eje=${this.eje}`).pipe(takeUntil(this.fetchCancel$), finalize(() => { pendingRequests--; if (pendingRequests === 0) {this.swLoading = false;}})).subscribe({
            next: (partidas) => {
              const partidasArr = Array.isArray(partidas) ? partidas : [];
              this.creditos[idx].partidas = partidasArr;
              const des = partidasArr[0]?.desc ?? '';
              this.creditos[idx].partidaDesc = des;
            },
            error: () => {
              this.creditos[idx].partidas = [];
            },
          });
          this.creditos[idx].saldo = 0;
          this.creditos[idx].limporte = 0;
          this.http.get<any>(`${environment.backendUrl}/api/sical/operaciones?orgCode=${this.orgCode}&entidad=${this.entidad}&numeroOperDesde=${numeroOper}&numeroOperHasta=${numeroOper}&referencia=${referencia}&organica=${org}&funcional=${fun}&economica=${eco}&eje=${this.eje}&numRegDev=${1}`).pipe(takeUntil(this.fetchCancel$), finalize(() => {pendingRequests--;if (pendingRequests === 0) {this.swLoading = false;}})).subscribe({
            next: (operaciones) => {
              const operacionesArr = Array.isArray(operaciones) ? operaciones : [];
              this.creditos[idx].operaciones = operacionesArr;
              const firstLinea = operacionesArr[0]?.lineaList?.[0] ?? {};
              const saldo = this.creditos[idx].saldo = firstLinea?.saldo ?? 0;
              const limporte = this.creditos[idx].limporte = firstLinea?.limporte ?? 0;
            },
            error: () => {
              this.creditos[idx].operaciones = [];
            },
          });
        });
        this.sortDirection = 'asc';
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.creditos = [];
        console.warn = err.error.error ?? err.error;
        this.tableMessage = 'Centro Gestor no existe';
        this.isLoading = false;
        this.swLoading = false;
      }
    });
  }
  
  sortField: string | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  setSort(field: string) {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.applySort();
  }

  applySort() {
    if (!this.sortField) return;
    const field = this.sortField;

    this.creditos = [...this.creditos].sort((a, b) => {
      let aVal: any;
      let bVal: any;

      if (field === 'partidaDesc') {
        aVal = a?.partidas?.[0]?.desc ?? '';
        bVal = b?.partidas?.[0]?.desc ?? '';
        return this.sortDirection === 'asc'
          ? String(aVal).localeCompare(String(bVal), 'es')
          : String(bVal).localeCompare(String(aVal), 'es');
      }

      if (field === 'gbsref') {
        aVal = a?.gbsref ?? '';
        bVal = b?.gbsref ?? '';
        return this.sortDirection === 'asc'
          ? String(aVal).localeCompare(String(bVal), 'es')
          : String(bVal).localeCompare(String(aVal), 'es');
      }

      if (field === 'gbseco' || field === 'gbsope') {
        aVal = Number(a?.[field] ?? 0);
        bVal = Number(b?.[field] ?? 0);
        return this.sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
      }

      if (field === 'limporte' || field === 'saldo') {
        aVal = this.parseMoney(a?.[field]);
        bVal = this.parseMoney(b?.[field]);

        return this.sortDirection === 'asc'
          ? aVal - bVal
          : bVal - aVal;
      }

      if (field === 'gbsiut') {
        aVal = this.getkAcPeCo(a?.gbsiut, a?.gbsict);
        bVal = this.getkAcPeCo(b?.gbsiut, b?.gbsict);

        return this.sortDirection === 'asc'
          ? aVal - bVal
          : bVal - aVal;
      }

      if (field === 'gbsict') {
        aVal = this.getkdispon(
          a?.saldo,
          this.getkAcPeCo(a?.gbsiut, a?.gbsict)
        );

        bVal = this.getkdispon(
          b?.saldo,
          this.getkAcPeCo(b?.gbsiut, b?.gbsict)
        );

        return this.sortDirection === 'asc'
          ? aVal - bVal
          : bVal - aVal;
      }

      if (field === 'acpeco') {
        aVal = this.getkAcPeCo(a.gbsiut, a.gbsict);
        bVal = this.getkAcPeCo(b.gbsiut, b.gbsict);
         return this.sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
       }

      if (field === 'disponible') {
        aVal = this.getkdispon(a.saldo, this.getkAcPeCo(a.gbsiut, a.gbsict));
        bVal = this.getkdispon(b.saldo, this.getkAcPeCo(b.gbsiut, b.gbsict));
        return this.sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
       }

      aVal = a?.[field] ?? '';
      bVal = b?.[field] ?? '';
      return this.sortDirection === 'asc'
        ? String(aVal).localeCompare(String(bVal), 'es')
        : String(bVal).localeCompare(String(aVal), 'es');
    });
    this.page = 0;
    this.updatePagination();
  }

  private updatePagination(): void {
    const total = this.totalPages;
    if (this.page >= total) {
      this.page = Math.max(0, total - 1);
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

  DownloadPDF() {
    this.limpiarMessages();
    const source = this.creditos;

    if (!source?.length) {
      this.tableMessage = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      combined: row.gbsorg + '-' + row.gbsfun + '-' + row.gbseco || '',
      partidas: row.partidas?.[0]?.desc ?? '',
      gbsope: row.gbsope ?? '',
      gbsref: row.gbsref ?? '',
      limporte: this.formatCurrency(row.limporte) ?? '',
      saldo: this.formatCurrency(row.saldo) ?? '',
      getkAcPeCo: this.formatCurrency(this.getkAcPeCo(row.gbsiut, row.gbsict)) ?? '',
      getkdispon: this.formatCurrency(this.getkdispon(row.saldo, this.getkAcPeCo(row?.gbsiut, row?.gbsict))) ?? '',
      gbsimp: this.formatCurrency(row.gbsimp) ?? '',
      gbsibg: this.formatCurrency(row.gbsibg) ?? '',
      gbsius: this.formatCurrency(row.gbsius) ?? '',
      getKBoldis: this.formatCurrency(this.getKBoldis(row.gbsimp, row.gbsibg, row.gbsius)) ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de bolsas', 40, 40);
    const columns = [
      { header: 'Aplicación', dataKey: 'combined' },
      { header: 'Desc.Aplicación', dataKey: 'partidas' },
      { header: 'Operación contable', dataKey: 'gbsope' },
      { header: 'Ref.contable', dataKey: 'gbsref' },
      { header: 'Imp.Operación', dataKey: 'limporte' },
      { header: 'Saldo Operación', dataKey: 'saldo' },
      { header: 'Pte.Contabilizar SCAP', dataKey: 'getkAcPeCo' },
      { header: 'Disponible', dataKey: 'getkdispon' },
      { header: 'Importe bolsa', dataKey: 'gbsimp' },
      { header: 'Importe bolsa gestión', dataKey: 'gbsibg' },
      { header: 'Usado bolsa', dataKey: 'gbsius' },
      { header: 'Disponible bolsa', dataKey: 'getKBoldis' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => (row as any)[col.dataKey] ?? '')),
      styles: {fontSize: 8},
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      didParseCell: (data) => {
        if (data.section === 'body' && data.column.index >= 4) {
          data.cell.styles.halign = 'right';
        }
      },
      columnStyles: {
        combined: { cellWidth: 25 },
        partidas: { cellWidth: 30 },
        gbsope: { cellWidth: 20 },
        gbsref: { cellWidth: 20 },
        limporte: { cellWidth: 20 },
        saldo: { cellWidth: 20 },
        acpeco: { cellWidth: 20 },
        disponible: { cellWidth: 20 },
        gbsimp: { cellWidth: 20 },
        gbsibg: { cellWidth: 20 },
        gbsius: { cellWidth: 20 },
        getKBoldis: { cellWidth: 20, halign: 'right' }
      }
    });

    doc.save('Bolsas.pdf');
  }

  private formatCurrency(value: any): string {
    if (value === null || value === undefined || value === '') return '';
    const numberValue = typeof value === 'number' ? value : Number(value);
    if (isNaN(numberValue)) return '';
    const formatted = new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 2
    }).format(numberValue);
    return formatted;
  }

  private applyExcelCurrencyFormat(
    worksheet: XLSX.WorkSheet,
    rowCount: number
  ): void {
    const currencyFormat = '[$€-es-ES] #,##0.00';

    for (let rowIndex = 2; rowIndex < rowCount + 2; rowIndex++) {
      for (let columnIndex = 4; columnIndex <= 11; columnIndex++) {
        const cellAddress = XLSX.utils.encode_cell({
          r: rowIndex,
          c: columnIndex
        });

        const cell = worksheet[cellAddress];

        if (!cell) {
          continue;
        }

        cell.v = this.parseMoney(cell.v);
        cell.t = 'n';

        cell.z = currencyFormat;
      }
    }
  }

  excelDownload() {
    this.limpiarMessages();

    const rows = this.creditos;

    if (!rows || rows.length === 0) {
      this.tableMessage = 'No hay datos para exportar.';
      return;
    }

    const exportRows = rows.map((row: any) => ({
      Aplicación: `${row.gbsorg}-${row.gbsfun}-${row.gbseco}`,
      Desc_Aplicación: row.partidas?.[0]?.desc ?? '',
      Operación_contable: row.gbsope ?? '',
      Ref_contable: row.gbsref ?? '',
      Imp_Operación: this.parseMoney(row.limporte),
      Saldo_Operación: this.parseMoney(row.saldo),
      Pte_Contabilizar_SCAP: this.parseMoney(this.getkAcPeCo(row.gbsiut, row.gbsict)),
      Disponible: this.parseMoney(this.getkdispon(row.saldo, this.getkAcPeCo(row.gbsiut, row.gbsict))),
      Importe_bolsa: this.parseMoney(row.gbsimp),
      Importe_bolsa_gestión: this.parseMoney(row.gbsibg),
      Usado_bolsa: this.parseMoney(row.gbsius),
      Disponible_bolsa: this.parseMoney(this.getKBoldis(row.gbsimp, row.gbsibg, row.gbsius))
    }));

    const worksheet = XLSX.utils.aoa_to_sheet([]);

    XLSX.utils.sheet_add_aoa(
      worksheet,
      [['Listado de bolsas']],
      { origin: 'A1' }
    );

    worksheet['!merges'] = [
      {
        s: { r: 0, c: 0 },
        e: { r: 0, c: 3 }
      }
    ];

    XLSX.utils.sheet_add_aoa(
      worksheet,
      [[
        'Aplicación',
        'Desc.Aplicación',
        'Operación contable',
        'Ref.contable',
        'Imp.Operación',
        'Saldo Operación',
        'Pte Contabilizar SCAP',
        'Disponible',
        'Importe bolsa',
        'Importe bolsa gestión',
        'Usado bolsa',
        'Disponible bolsa'
      ]],
      { origin: 'A2' }
    );

    XLSX.utils.sheet_add_json(
      worksheet,
      exportRows,
      {
        origin: 'A3',
        skipHeader: true
      }
    );

    this.applyExcelCurrencyFormat(
      worksheet,
      exportRows.length
    );

    worksheet['!cols'] = [
      { wch: 32 },
      { wch: 40 },
      { wch: 24 },
      { wch: 26 },
      { wch: 24 },
      { wch: 24 },
      { wch: 28 },
      { wch: 28 },
      { wch: 24 },
      { wch: 24 },
      { wch: 24 },
      { wch: 24 }
    ];

    const workbook = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(
      workbook,
      worksheet,
      'bolsas'
    );

    const buffer = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array',
      cellStyles: true
    });

    saveAs(
      new Blob([buffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      }),
      'Bolsas.xlsx'
    );
  }

  public getkAcPeCo(gbsiut: any, gbsict: any): number {
    let a = gbsiut;
    let b = gbsict;

    let value = a - b;

    return value;
  }

  get paginatedBolsas(): any[] {
    if (!this.creditos || this.creditos.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.creditos.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.creditos?.length ?? 0) / this.pageSize));
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

  searchBolsas() {
    if (this.cge === '') {
      this.tableMessage = 'Falta Centro Gestor';
      return;
    }
    this.fetchCancel$.next();
    this.fetchCentroGestorInfo();
    this.fetchBolsas();
  }

  setInputToUpper(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    let upper = (target.value ?? '').toUpperCase();
    if(upper.length > 4) {
      upper = upper.slice(0, 4);
    }
    target.value = upper;
    this.cge = upper;
  }

  limpiarSearch() {
    this.cge = this.backUpCge;
    this.fetchCancel$.next();
    this.limpiarMessages();
    this.fetchBolsas();
    this.fetchCentroGestorInfo();
  }
  
  //main detail grid functions
  selectedBolsas: any = null;
  showDetails(factura: any) {
    this.limpiarMessages();
    this.guardarisError = false;
    this.guardarisSuccess = false;
    this.selectedBolsas = factura;
    const org = factura?.gbsorg ?? '';
    const fun = factura?.gbsfun ?? '';
    const eco = factura?.gbseco ?? '';
    this.bolsaCombo = `${org} - ${fun} - ${eco}`;
  }

  public bolsaCombo: string = '';
  saveDetails() {
    this.limpiarMessages();
    if (!this.selectedBolsas) return;
    const parts = this.bolsaCombo.split(' - ');
    this.selectedBolsas.gbsorg = parts[0] ?? '';
    this.selectedBolsas.gbsfun = parts[1] ?? '';
    this.selectedBolsas.gbseco = parts[2] ?? '';
  } 

  closeDetails() {
    this.limpiarMessages();
    this.selectedBolsas = null;
  }

  public getkCGECIC(cgecic: any): string {
    const toNum = (v: any) => {
      if (v === null || v === undefined || v === '') return 0;
      const n = Number(v);
      return isNaN(n) ? 0 : n;
    };
    const a = toNum(cgecic);
    if (a === 0) return 'No';
    if ( a === 1) return 'Si';
    if ( a === 2) return 'Cierre para contabilizar';
    return '';
  }

  public getKBoldis(gbsimp: any, gbsibg: any, gbsius: any): number {
    let a = this.cleaningCurrency(gbsimp)
    let b = this.cleaningCurrency(gbsibg);
    let c = Number(gbsius);
    let sum = a + b - c;

    return sum;
  }

  public getkdispon(saldo: any, getkAcPeCo: any): number {
    let a = this.parseMoney(saldo);
    let b = this.parseMoney(getkAcPeCo);
    let value = a - b;
    return value;
  }

  private parseMoney(val: any): number {
    if (val === null || val === undefined || val === '') return 0;
    if (typeof val === 'number') return val;
    let s = String(val).trim();
    const isParenNeg = /^\(.*\)$/.test(s);
    if (isParenNeg) s = s.replace(/[()]/g, '');
    s = s.replace(/\u00A0/g, ' ').replace(/[^\d.,\-]/g, '');

    if (s.includes(',')) {
      s = s.replace(/\./g, '').replace(',', '.');
    }

    const n = parseFloat(s);
    if (isNaN(n)) return 0;
    return isParenNeg ? -n : n;
  }

  formatGbsimp() {
    if (!this.selectedBolsas || this.selectedBolsas.gbsimp === undefined || this.selectedBolsas.gbsimp === null) return;
    let value = String(this.selectedBolsas.gbsimp)
      .replace(/\s/g, '')
      .replace(/\./g, '')     
      .replace(',', '.')       
      .replace(/[^\d.-]/g, '');
    let num = parseFloat(value);
    if (!isNaN(num)) {
      this.selectedBolsas.gbsimp = num.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
    }
  }
  formateGbsibg() {
    if (!this.selectedBolsas || this.selectedBolsas.gbsibg === undefined || this.selectedBolsas.gbsibg === null) return;
    let value = String(this.selectedBolsas.gbsibg)
      .replace(/\s/g, '')
      .replace(/\./g, '')     
      .replace(',', '.')       
      .replace(/[^\d.-]/g, '');
    let num = parseFloat(value);
    if (!isNaN(num)) {
      this.selectedBolsas.gbsibg = num.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
    }
  }

  cleaningCurrency(value: any) {
    return this.parseMoney(value);
  }

  //misc
  limpiarMessages () {
    this.tableMessage = '';
    this.guardarMesageSuccess = '';
    this.guardarMesage = '';
  }
}

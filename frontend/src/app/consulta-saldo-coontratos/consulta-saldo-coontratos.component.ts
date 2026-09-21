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
  selector: 'app-consulta-saldo-coontratos',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-saldo-coontratos.component.html',
  styleUrls: ['./consulta-saldo-coontratos.component.css']
})

export class ConsultaSaldoCoontratosComponent {
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
  contratos: any = [];
  page = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  isLoading: boolean = false;
  ContratosSuccess: string = '';
  ContratosError: string = '';
  ngOnInit(): void{
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO'); 

    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}

    if (!entidad || this.entcod === null || !eje || this.eje === null ) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchContratos();
  }

  fetchContratos() {
    this.isLoading = true;
    this.http.get(`${environment.backendUrl}/api/cog/Saldo-contrato/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.contratos = res;
      },
      error: (err) => {
        this.contratos = [];
        this.isLoading = false;
        this.ContratosError = err.error.error || err.error;
      }
    })
  }
  get paginatedContratos(): any[] { if (!this.contratos || this.contratos.length === 0) return [];
    const start = this.page * this.pageSize; return this.contratos.slice(start, start + this.pageSize);
  }
  get totalPages(): number {return Math.max(1, Math.ceil((this.contratos?.length ?? 0) / this.pageSize)); }
  prevPage(): void {if (this.page > 0) this.page--; }
  nextPage(): void {if (this.page < this.totalPages - 1) this.page++;}
  goToPage(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {this.page = inputPage - 1;}
  }
  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0;return;}
    if (this.page >= total) {this.page = total - 1;}
  }

  cge: string = '';
  contrato: string = '';
  proveedor: string = '';
  search() {
    this.limpiarMessages();
    
    let params = new HttpParams;
    if (this.cge?.trim()) {
      params = params.set('cge', this.cge?.trim());
    }
    if (this.contrato?.trim()) {
      params = params.set('contrato', this.contrato?.trim());
    }
    if (this.proveedor?.trim()) {
      params = params.set('proveedor', this.proveedor?.trim());
    }
    this.isLoading = true;
    this.http.get(`${environment.backendUrl}/api/cog/search-saldo-contrato/${this.entcod}/${this.eje}`, {params}).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.contratos = res;
      },
      error:(err) => {
        this.isLoading = false;
        this.contratos = [];
        this.ContratosError = err.error.error || err.error;
      }
    })
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.cge = '';
    this.contrato = '';
    this.proveedor = '';
    this.fetchContratos();
  }

  toggleSort(field: 'concod' | 'cot.conn.conlot' | 'cot.conn.condes' | 'cot.tercod' | 'cot.ter.ternom' | 'cot.ter.ternif' | 'cgecod' | 'cge.cgedes' | 'cogopd' | 'cogop2' | 'calculateSaldoTotal' | 'cogiap' | 'calculateSaldo'): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.page = 0;
      this.updatePagination();
      return;
    }

    this.applySort();
  }

  sortField: 'concod' | 'cot.conn.conlot' | 'cot.conn.condes' | 'cot.tercod' | 'cot.ter.ternom' | 'cot.ter.ternif' | 'cgecod' | 'cge.cgedes' | 'cogopd' | 'cogop2' | 'calculateSaldoTotal' | 'cogiap' | 'calculateSaldo' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const direction = this.sortDirection === 'asc' ? 1 : -1;

    const getValue = (item: any, path: string): string | number => {
      if (path === 'calculateSaldoTotal') {
        return (item?.cogimp ?? 0) + (item?.cogim2 ?? 0);
      }

      if (path === 'calculateSaldo') {
        return (item?.cogimp ?? 0)
          + (item?.cogim2 ?? 0)
          - (item?.cogiap ?? 0);
      }

      return path
        .split('.')
        .reduce((value, property) => value?.[property], item) ?? '';
    };

    this.contratos = [...this.contratos].sort((first, second) => {
      const firstValue = getValue(first, field);
      const secondValue = getValue(second, field);

      if (typeof firstValue === 'number' && typeof secondValue === 'number') {
        return (firstValue - secondValue) * direction;
      }

      return new Intl.Collator('es', {
        numeric: true,
        sensitivity: 'base'
      }).compare(
        String(firstValue),
        String(secondValue)
      ) * direction;
    });

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

  calculateSaldoTotal(cogimp: number, cogim2: number): number {
    return cogimp + cogim2;
  }

  calculateSaldo(cogimp: number, cogim2: number, cogiap: number): number {
    return (cogimp + cogim2) - cogiap;
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

  excelDownload() {
    this.limpiarMessages();
    const rows = this.contratos;
    if (!rows || rows.length === 0) {
      this.ContratosError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row: any, index: number) => ({
      Contrato: row.concod ?? '',
      Económica: row.cot?.conn?.conlot ?? '',
      Descripción: row.cot?.conn?.condes ?? '',
      Cód_Proveedor : row.cot?.tercod ?? '',
      Proveedor: row.cot?.ter?.ternom ?? '',
      NIF: row.cot?.ter?.ternif ?? '',
      Cód_C_Gestor: row.cgecod ?? '',
      Centro_Gestor: row.cge?.cgedes ?? '',
      AD_principal: row.cogopd ?? '',
      AD_secundaria : row.cogop2 ?? '',
      Saldo_total_AD : this.formatCurrency(this.calculateSaldoTotal(row?.cogimp, row?.cogim2)),
      Pedidos_Pte_Contabilizar : this.formatCurrency(row.cogiap),
      Saldo: this.formatCurrency(this.calculateSaldo(row?.cogimp, row?.cogim2, row?.cogiap))
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de saldo de contratos']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Contrato', 'Económica', 'Descripción', 'Cód. Proveedor', 'Proveedor', 'NIF', 'Cód. C. Gestor', 'AD principal', 'AD secundaria', 'Saldo total AD', 'Pedidos Pte. Contabilizar', 'Saldo']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 10 },
      { wch: 40 },
      { wch: 10 },
      { wch: 40 },
      { wch: 15 },
      { wch: 15 },
      { wch: 40 },
      { wch: 20 },
      { wch: 20 },
      { wch: 25 },
      { wch: 25 },
      { wch: 25 },
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'contratos');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_saldo_contratos.xlsx'
    );
  }

  pdfDownload() {
    this.limpiarMessages();
    const source = this.contratos;
    if (!source?.length) {
      this.ContratosError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      Contrato: row.concod ?? '',
      conlot: row.cot?.conn?.conlot ?? '',
      condes: row.cot?.conn?.condes ?? '',
      tercod : row.cot?.tercod ?? '',
      ternom: row.cot?.ter?.ternom ?? '',
      ternif: row.cot?.ter?.ternif ?? '',
      cgecod: row.cgecod ?? '',
      cgedes: row.cge?.cgedes ?? '',
      cogopd: row.cogopd ?? '',
      cogop2 : row.cogop2 ?? '',
      calculateSaldoTotal : this.formatCurrency(this.calculateSaldoTotal(row?.cogimp, row?.cogim2)),
      cogiap : this.formatCurrency(row.cogiap),
      calculateSaldo: this.formatCurrency(this.calculateSaldo(row?.cogimp, row?.cogim2, row?.cogiap))
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de saldo de contratos', 40, 40);

    const columns = [
      { header: 'Contrato', dataKey: 'Contrato' },
      { header: 'Económica', dataKey: 'conlot' },
      { header: 'Descripción', dataKey: 'condes' },
      { header: 'Cód.Proveedor', dataKey: 'tercod' },
      { header: 'Proveedor', dataKey: 'ternom' },
      { header: 'NIF', dataKey: 'ternif' },
      { header: 'Cód.C.Gestor', dataKey: 'cgecod' },
      { header: 'Centro Gestor', dataKey: 'cgedes' },
      { header: 'AD principal', dataKey: 'cogopd' },
      { header: 'AD secundaria', dataKey: 'cogop2' },
      { header: 'Saldo total AD', dataKey: 'calculateSaldoTotal' },
      { header: 'Pedidos Pte Contabilizar', dataKey: 'cogiap' },
      { header: 'Saldo', dataKey: 'calculateSaldo' }
    ];

    autoTable(doc, {
      startY: 15,
      head: [columns.map(col => col.header)],
      body: rows.map((row: any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 8 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33 },
      columnStyles: {
        Contrato: { cellWidth: 10 },
        conlot: { cellWidth: 10 },
        condes: { cellWidth: 40 },
        tercod: { cellWidth: 10 },
        ternom: { cellWidth: 40 },
        ternif: { cellWidth: 15 },
        cgecod: { cellWidth: 15 },
        cgedes: { cellWidth: 40 },
        cogopd: { cellWidth: 20 },
        cogop2: { cellWidth: 20 },
        calculateSaldoTotal: { cellWidth: 25 },
        cogiap: { cellWidth: 25 },
        calculateSaldo: { cellWidth: 25 },
      }
    });

    doc.save('consulta-saldo-contratos.pdf');
  }

  //misc
  limpiarMessages() {
    this.ContratosSuccess = '';
    this.ContratosError = '';
  }
}

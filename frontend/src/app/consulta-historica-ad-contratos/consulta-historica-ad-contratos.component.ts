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
  selector: 'app-consulta-historica-ad-contratos',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-historica-ad-contratos.component.html',
  styleUrls: ['./consulta-historica-ad-contratos.component.css']
})

export class ConsultaHistoricaAdContratosComponent {
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
    this.http.get(`${environment.backendUrl}/api/coh/historia-ADcontrato/${this.entcod}/${this.eje}`).subscribe({
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
    this.http.get(`${environment.backendUrl}/api/coh/search-historia-ADcontrato/${this.entcod}/${this.eje}`, {params}).subscribe({
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

  toggleSort(field: 'concod' | 'con.conlot' | 'con.condes' | 'con.cots.0.tercod' | 'con.cots.0.ter.ternom' | 'con.cots.0.ter.ternif' | 'cgecod' | 'cge.cgedes' | 'cohopd' | 'cohrfd' | 'cohfec' | 'bloqueado'): void {
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

  sortField: 'concod' | 'con.conlot' | 'con.condes' | 'con.cots.0.tercod' | 'con.cots.0.ter.ternom' | 'con.cots.0.ter.ternif' | 'cgecod' | 'cge.cgedes' | 'cohopd' | 'cohrfd' | 'cohfec' | 'bloqueado' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const direction = this.sortDirection === 'asc' ? 1 : -1;

    const getValue = (item: any, path: string): string | number => {
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

  formatDate = (v: any) => {
    if (!v && v !== 0) return '';
    const s = String(v);
    const d = new Date(s);
    return isNaN(d.getTime()) ? s : d.toLocaleDateString('es-ES');
  };

  excelDownload() {
    this.limpiarMessages();
    const rows = this.contratos;
    if (!rows || rows.length === 0) {
      this.ContratosError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row: any) => ({
      Contrato: row.concod ?? '',
      Económica: row.con?.conlot ?? '',
      Descripción: row.con?.condes ?? '',
      Cód_Proveedor : row.con?.cots[0]?.tercod ?? '',
      Proveedor: row.con?.cots[0]?.ter?.ternom ?? '',
      NIF: row.con?.cots[0]?.ter?.ternif ?? '',
      Cód_C_Gestor: row.cgecod ?? '',
      Centro_Gestor: row.cge?.cgedes ?? '',
      Operación_AD: row.cohopd ?? '',
      Referencia_AD : row.cohrfd ?? '',
      Fecha: this.formatDate(row?.cohfec),
      Bloqueado: row.bloqueado,
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de hístorica de AD de contratos']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [[
      'Contrato',
      'Económica',
      'Descripción',
      'Cód. Proveedor',
      'Proveedor',
      'NIF',
      'Cód. C. Gestor',
      'Centro Gestor',
      'Operación AD',
      'Referencia AD',
      'Fecha',
      'Bloqueado'
    ]], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 8 },
      { wch: 8 },
      { wch: 40 },
      { wch: 15 },
      { wch: 35 },
      { wch: 15 },
      { wch: 5 },
      { wch: 35 },
      { wch: 20 },
      { wch: 20 },
      { wch: 15 },
      { wch: 10 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'contratos');
    const buffer = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array',
      cellStyles: true
    });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_historica_ad_contratos.xlsx'
    );
  }

  pdfDownload() {
    this.limpiarMessages();
    const source = this.contratos;
    if (!source?.length) {
      this.ContratosError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any) => ({
      Contrato: row.concod ?? '',
      Económica: row.con?.conlot ?? '',
      Descripción: row.con?.condes ?? '',
      Cód_Proveedor : row.con?.cots[0]?.tercod ?? '',
      Proveedor: row.con?.cots[0]?.ter?.ternom ?? '',
      NIF: row.con?.cots[0]?.ter?.ternif ?? '',
      Cód_C_Gestor: row.cgecod ?? '',
      Centro_Gestor: row.cge?.cgedes ?? '',
      Operación_AD: row.cohopd ?? '',
      Referencia_AD : row.cohrfd ?? '',
      Fecha: this.formatDate(row?.cohfec),
      Bloqueado: row.bloqueado,
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de saldo de contratos', 40, 40);

    const columns = [
      { header: 'Contrato', dataKey: 'Contrato' },
      { header: 'Económica', dataKey: 'Económica' },
      { header: 'Descripción', dataKey: 'Descripción' },
      { header: 'Cód.Proveedor', dataKey: 'Cód_Proveedor' },
      { header: 'Proveedor', dataKey: 'Proveedor' },
      { header: 'NIF', dataKey: 'NIF' },
      { header: 'Cód.C.Gestor', dataKey: 'Cód_C_Gestor' },
      { header: 'Centro Gestor', dataKey: 'Centro_Gestor' },
      { header: 'Operación AD', dataKey: 'Operación_AD' },
      { header: 'Referencia AD', dataKey: 'Referencia_AD' },
      { header: 'Fecha', dataKey: 'Fecha' },
      { header: 'Bloqueado', dataKey: 'Bloqueado' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map((row: any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 8 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      didParseCell: (data) => {
        if (data.section === 'body' && [1, 3, 10, 11, 12].includes(data.column.index)) {
          data.cell.styles.halign = 'right';
        }
      },
      columnStyles: {
        Contrato: { cellWidth: 8 },
        Económica: { cellWidth: 8 },
        Descripción: { cellWidth: 40 },
        Cód_Proveedor: { cellWidth: 15 },
        Proveedor: { cellWidth: 35 },
        NIF: { cellWidth: 15 },
        Cód_C_Gestor: { cellWidth: 5 },
        Centro_Gestor: { cellWidth: 35 },
        Operación_AD: { cellWidth: 20 },
        Referencia_AD: { cellWidth: 20 },
        Fecha: { cellWidth: 15 },
        Bloqueado: { cellWidth: 10 }
      }
    });

    doc.save('consulta_historica_ad_contratos.pdf');
  }

  //misc
  limpiarMessages() {
    this.ContratosSuccess = '';
    this.ContratosError = '';
  }
}

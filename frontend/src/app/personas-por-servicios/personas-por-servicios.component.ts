import { Component, HostListener } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { environment } from '../../environments/environment';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-personas-por-servicios',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent ],
  templateUrl: './personas-por-servicios.component.html',
  styleUrls: ['./personas-por-servicios.component.css']
})
export class PersonasPorServiciosComponent {
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

  constructor(private http: HttpClient, private router: Router) {}

  //global variables
  private entcod: number | null = null;
  private eje: number | null = null;
  personasServicesSuccess: string = '';
  personasServicesError: string = '';
  services: any[] = [];
  private backupServices: any[] = [];
  private defaultServices: any[] = [];
  page = 0;
  pageSize = 20;
  pageLoad = 1;

  ngOnInit() {
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
    this.fetchServices();
    this.getNumberPages();
  }

  //global functions
  isLoading: boolean = false;
  isNotSearch: boolean = false;
  pageNum: number = 0;
  fetchServices() {
    if (this.entcod === null || this.eje === null) return;
    this.inSearch = false;
    this.isNotSearch = true;
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/depe/personas-servicios/${this.entcod}/${this.eje}/${this.pageLoad}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.services = res;
        this.backupServices = Array.isArray(res) ? [...res] : [];
        this.defaultServices = [...this.backupServices];
        this.page = 0;
        this.updatePagination();

        if (this.sortField) {
          this.applySort();
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.personasServicesError = err.error.error ?? err.error;
      }
    });
  };

  getNumberPages() {
    if (this.entcod === null || this.eje === null) return;
    this.http.get<any>(`${environment.backendUrl}/api/depe/personas-servicios-pagination/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.pageNum = Math.ceil(res / 20);
      },
      error: (err) => {
        console.warn(err.error.error ?? err.error);
      }
    })
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
  get paginatedServices(): any[] {if (!this.services || this.services.length === 0) return [];
    const start = this.pageSearch * this.pageSize;
    return this.services.slice(start, start + this.pageSize);}
  get totalPages(): number {return Math.max(1, Math.ceil((this.services?.length ?? 0) / this.pageSize));}
  prevPage() { if (this.pageLoad === 1) return;
    this.pageLoad--; this.fetchServices(); }
  nextPage() { this.pageLoad++; this.fetchServices(); }

  goToPage(event: any): void { const inputPage = Number(event.target.value);
    this.pageLoad = inputPage; this.fetchServices(); }

  toggleSort(field: 'percod' | 'cgedes' | 'depint' | 'cgecod' | 'depdes' | 'depalm' | 'depcom' | 'depcod' | 'pernom'): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.services = [...this.defaultServices];
      this.page = 0;
      this.updatePagination();
      return;
    }

    this.applySort();
  }

  sortField: 'percod' | 'cgedes' | 'depint' | 'cgecod' | 'depdes' | 'depalm' | 'depcom' | 'depcod' | 'pernom' | null = null
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const getValue = (item: any, field: string) => {
      switch (field) {
        case 'percod':
          return item.percod ?? '';
        case 'pernom':
          return item.per?.pernom ?? '';
        case 'depcod':
          return item.depcod ?? '';
        case 'depdes':
          return item.dep?.depdes ?? '';
        case 'depalm':
          return item.dep?.depalm ?? '';
        case 'depcom':
          return item.dep?.depcom ?? '';
        case 'depint':
          return item.dep?.depint ?? '';
        case 'cgecod':
          return item.dep?.cge?.cgecod ?? '';
        case 'cgedes':
          return item.dep?.cge?.cgedes ?? '';
        default:
          return item[field] ?? '';
      }
    };

    const sorted = [...this.services].sort((a, b) => {
      const aVal = getValue(a, field);
      const bVal = getValue(b, field);

      if (['percod', 'depcod', 'depalm', 'depcom', 'depint', 'cgecod'].includes(field)) {
        const aNum = Number(aVal);
        const bNum = Number(bVal);
        if (!isNaN(aNum) && !isNaN(bNum)) {
          return this.sortDirection === 'asc' ? aNum - bNum : bNum - aNum;
        }
      }
      return this.sortDirection === 'asc'
        ? collator.compare(aVal.toString(), bVal.toString())
        : collator.compare(bVal.toString(), aVal.toString());
    });

    this.services = sorted;
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

  campo: 'depalm' | 'depcom' | 'depint' | null = null;
  checkIfChecked(campo: number) {
    if (campo === 0) {
      return 'Si';
    } else if (campo === 1) {
      return 'No';
    }
    return 'No';
  }

  excelDownload() {
    this.limpiarMessages(); 
    const rows = this.services;
    if (!rows || rows.length === 0) {
      this.personasServicesError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Cód_Persona: row.percod ?? '',
      Nombre: row.per?.pernom ?? '',
      Cód_Servicio: row.depcod ?? '',
      Servicio: row.dep.depdes ?? '',
      Almacén_Farmacia: this.checkIfChecked(row.dep.depalm) ?? '',
      Comprador: this.checkIfChecked(row.dep.depcom) ?? '',
      Contable: this.checkIfChecked(row.dep.depint) ?? '',
      Cód_C_G: row.dep.cge.cgecod ?? '',
      Centro_Gestor: row.dep.cge.cgedes ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de personas de servicios']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Cód. Persona', 'Nombre', 'Cód. Servicio', 'Servicio', 'Almacén / Farmacia', 'Comprador', 'Contable', 'Cód. C.G', 'Centro Gestor']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 20 },
      { wch: 45 },
      { wch: 10 },
      { wch: 20 },
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 45 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Personas por servicios');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Personas_por_servicios.xlsx'
    );
  }

  toPrint() {
    this.limpiarMessages();
    const source = this.services;
    if (!source?.length) {
      this.personasServicesError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      Cód_Persona: row.percod ?? '',
      Nombre: row.per?.pernom ?? '',
      Cód_Servicio: row.depcod ?? '',
      Servicio: row.dep.depdes ?? '',
      Almacén_Farmacia: this.checkIfChecked(row.dep.depalm) ?? '',
      Comprador: this.checkIfChecked(row.dep.depcom) ?? '',
      Contable: this.checkIfChecked(row.dep.depint) ?? '',
      Cód_C_G: row.dep.cge.cgecod ?? '',
      Centro_Gestor: row.dep.cge.cgedes ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Personas por servicios', 40, 40);

    const columns = [
      { header: 'Cód. Persona', dataKey: 'Cód_Persona' },
      { header: 'Nombre', dataKey: 'Nombre' },
      { header: 'Cód. Servicio', dataKey: 'Cód_Servicio' },
      { header: 'Servicio', dataKey: 'Servicio' },
      { header: 'Almacén / Farmacia', dataKey: 'Almacén_Farmacia' },
      { header: 'Comprador', dataKey: 'Comprador' },
      { header: 'Contable', dataKey: 'Contable' },
      { header: 'Cód. C.G', dataKey: 'Cód_C_G' },
      { header: 'Centro Gestor', dataKey: 'Centro_Gestor' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('personas_por_servicios.pdf');
  }

  servicio: string = '';
  persona: string = '';
  centroGestor: string = '';
  perfilServicio: 'almacen' | 'comprador' | 'contabilidad' | 'peticionario' | 'todos'  = 'todos';
  inSearch: boolean = false;

  handleSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = (input.value ?? '').toUpperCase();
    if(value.length > 4) {
      value = value.slice(0, 4);
    }
    this.centroGestor = value;
    input.value = value;
  }

  serviceUpper(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = (input.value ?? '').toUpperCase();

    this.servicio = value;
    input.value = value;
  }

  search() {
    this.limpiarMessages();
    this.isNotSearch = false;

    this.services = [];
    if (this.perfilServicio === 'todos' && this.servicio === '' && this.persona === '' && this.centroGestor === '') {
      this.fetchServices();
      return;
    } else {
      this.isLoading = true;
      let params = `ent=${this.entcod}&eje=${this.eje}`;
  
      if (this.servicio && this.servicio.trim() !== '') {
        params += `&servicio=${this.servicio}`;
      }
      if (this.persona && this.persona.trim() !== '') {
        params += `&persona=${this.persona}`;
      }
      if (this.centroGestor && this.centroGestor.trim() !== '') {
        params += `&cgecod=${this.centroGestor}`;
      }
      if (this.perfilServicio && this.perfilServicio !== 'todos') {
        params += `&perfil=${this.perfilServicio}`;
      }

      this.http.get<any>(`${environment.backendUrl}/api/depe/personas-servicios/search?${params}`).subscribe({
        next: (res) => {
          this.services = res;
          this.pageSearch = 0;
          this.updatePagination();
          this.isLoading = false;
          this.inSearch = true;
        },
        error: (err) => {
          this.services = [];
          this.personasServicesError = err.error.error ?? err.error;
          this.isLoading = false;
        }
      });
    }
  }
  pageSearch = 0;
  get totalPageSearchs(): number {
    return Math.max(1, Math.ceil((this.services?.length ?? 0) / this.pageSize));
  }
  prevPageSearch(): void {
    if (this.pageSearch > 0) this.pageSearch--;
  }
  nextPageSearch(): void {
    if (this.pageSearch < this.totalPages - 1) this.pageSearch++;
  }
  goToPageSearch(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {
      this.pageSearch = inputPage - 1;
    }
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.servicio = '';
    this.persona = '';
    this.centroGestor = '';
    this.perfilServicio = 'todos';
    this.inSearch = false;
    this.pageLoad = 1;
    this.fetchServices();
    this.getNumberPages();
  }

  //misc
  limpiarMessages() {
    this.personasServicesSuccess = '';
    this.personasServicesError = '';
  }
}

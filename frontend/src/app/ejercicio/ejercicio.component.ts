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
  selector: 'app-ejercicio',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent ],
  templateUrl: './ejercicio.component.html',
  styleUrls: ['./ejercicio.component.css']
})
export class EjercicioComponent {
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
  ejercicios: any[] = [];
  private backupEjercicios: any[] = [];
  private defaultEjercicios: any[] = [];
  page = 0;
  pageSize = 20;
  ejercicioSuccess: string = '';
  ejercicioError: string = '';
  isLoading: boolean = false;

  ngOnInit() {
    this.isLoading = true;
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (!entidad || this.entcod === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }
    
    this.fetchEjercicios();
  }

  //main table functions
  private fetchEjercicios(): void {
    if (this.entcod === null) return;
    this.http.get<any>(`${environment.backendUrl}/api/cfg/fetch-Eje/${this.entcod}`).subscribe({
      next: (res) => {
        this.ejercicios = res;
        this.backupEjercicios = Array.isArray(res) ? [...res] : [];
        this.defaultEjercicios = [...this.backupEjercicios];
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.ejercicios = [];
        this.ejercicioError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  toggleSort(field: 'eje' | 'cfgest'): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.ejercicios = [...this.defaultEjercicios];
      this.page = 0;
      this.updatePagination();
      return;
    }

    this.applySort();
  }

  sortField: 'eje' | 'cfgest' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const sorted = [...this.ejercicios].sort((a, b) => {
      const extract = (item: any, prop: string) =>
        (item?.[prop] ?? item?.[prop.toUpperCase()] ?? '').toString();

      const aVal = extract(a, field);
      const bVal = extract(b, field);
      return this.sortDirection === 'asc'
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
    });

    this.ejercicios = sorted;
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

  get paginatedEjercicios(): any[] {
    if (!this.ejercicios || this.ejercicios.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.ejercicios.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.ejercicios?.length ?? 0) / this.pageSize));
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

  getstatus(cfgest: number) {
    if (cfgest === 1) {return "Inactivo";}
    if (cfgest === 0) {return "Activo"}
    return "Extraviado";
  }

  excelDownload() {
    this.limpiarMessages();
    const rows = this.ejercicios;
    if (!rows || rows.length === 0) {
      this.ejercicioError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Ejercicio: row.eje ?? '',
      Estado: this.getstatus(row.cfgest) ?? '',
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de Ejercicios']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Ejercicio', 'Estado']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 15 },
      { wch: 20 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Ejercicios');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'ejercicios.xlsx'
    );
  }
  
  pdfDownload() {
    this.limpiarMessages();
    this.ejercicioError = '';
    const source = this.ejercicios;
    if (!source?.length) {
      this.ejercicioError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      eje: row.eje ?? '',
      status: this.getstatus(row.cfgest)
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Ejercicios', 40, 40);

    const columns = [
      { header: 'Ejercicio', dataKey: 'eje' },
      { header: 'Estado', dataKey: 'status' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('ejercicios.pdf');
  }

  searchTerm = '';
  setInputToLimit(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    let service = (target.value ?? '');
    if(service.length > 4) {
      service = service.slice(0, 4);
    }
    target.value = service;
    this.searchTerm = service;
  }
  
  search() {
    this.limpiarMessages();
    this.isLoading = true;

    if(this.searchTerm === '') {
      this.fetchEjercicios();
      return;
    }

    this.http.get<any>(`${environment.backendUrl}/api/cfg/search-Eje/${this.entcod}/${this.searchTerm}`).subscribe({
      next: (res) => {
        this.ejercicios = res;
        this.defaultEjercicios = [...this.ejercicios];
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.ejercicios = [];
        this.ejercicioError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.searchTerm = '';
    this.page = 0;
    this.ejercicios = [...this.backupEjercicios];
    this.defaultEjercicios = [...this.backupEjercicios];
  }
  
  //misc
  limpiarMessages() {
    this.ejercicioSuccess = '';
    this.ejercicioError = '';
  }
} 

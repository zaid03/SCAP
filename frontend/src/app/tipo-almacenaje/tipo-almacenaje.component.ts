import { Component, ComponentFactoryResolver, HostListener } from '@angular/core';
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
  selector: 'app-tipo-almacenaje',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent ],
  templateUrl: './tipo-almacenaje.component.html',
  styleUrls: ['./tipo-almacenaje.component.css']
})
export class TipoAlmacenajeComponent {
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
  almacenajes: any = [];
  page = 0;
  pageSize = 20;
  almacenajesSuccess: string = '';
  almacenajesError: string = '';
  isLoadingAlmacenajes: boolean = false;

  ngOnInit() {
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (!entidad || this.entcod === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }
    
    this.fetchAlmacenajes();
  }

  //main table functions
  fetchAlmacenajes() {
    this.isLoadingAlmacenajes = true;
    this.http.get(`${environment.backendUrl}/api/mta/all-mta/${this.entcod}`).subscribe({
      next: (res) => {
        this.isLoadingAlmacenajes = false;
        this.almacenajes = res;
        this.page = 0;
      },
      error: (err) => {
        this.isLoadingAlmacenajes = false;
        this.almacenajesError = err.error.error ?? err.error
      }
    })
  }
  get paginatedAlmacenajes(): any[] { if (!this.almacenajes || this.almacenajes.length === 0) return [];
    const start = this.page * this.pageSize; return this.almacenajes.slice(start, start + this.pageSize);
  }
  get totalPages(): number {return Math.max(1, Math.ceil((this.almacenajes?.length ?? 0) / this.pageSize)); }
  prevPage(): void {if (this.page > 0) this.page--; }
  nextPage(): void {if (this.page < this.totalPages - 1) this.page++;}
  goToPage(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {this.page = inputPage - 1;}
  }
  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0;return;}
    if (this.page >= total) {this.page = total - 1;}
  }

  toggleSort(field: 'mtacod' | 'mtades'): void {
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

  sortField: 'mtacod' | 'mtades' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const sorted = [...this.almacenajes].sort((a, b) => {
      const extract = (item: any, prop: string) =>
        (item?.[prop] ?? item?.[prop.toUpperCase()] ?? '').toString();

      const aVal = extract(a, field);
      const bVal = extract(b, field);
      return this.sortDirection === 'asc'
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
    });

    this.almacenajes = sorted;
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

  searchTerm: string = '';
  search() {
    const numsOnly = /^\d+$/;
    const term = String(this.searchTerm).trim();

    if (numsOnly.test(term)) {
      this.isLoadingAlmacenajes = true;
      this.http.get(`${environment.backendUrl}/api/mta/mta-filter/${this.entcod}/${term}`).subscribe({
        next: (res) => {
          this.isLoadingAlmacenajes = false;
          this.almacenajes = res;
          this.page = 0;
        },
        error: (err) => {
          this.isLoadingAlmacenajes = false;
          this.almacenajesError = err.error.error ?? err.error
        }
      })
    } else {
      this.isLoadingAlmacenajes = true;
      this.http.get(`${environment.backendUrl}/api/mta/search-almacenaje/${this.entcod}/${term}`).subscribe({
        next: (res) => {
          this.isLoadingAlmacenajes = false;
          this.almacenajes = res;
          this.page = 0;
        },
        error: (err) => {
          this.isLoadingAlmacenajes = false;
          this.almacenajesError = err.error.error ?? err.error
        }
      })
    }
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.searchTerm = '';
    this.fetchAlmacenajes();
  }

  excelDownload() {
    this.limpiarMessages();
    const rows = this.almacenajes;
    if (!rows || rows.length === 0) {
      this.almacenajesError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row: any, index: number) => ({
      '#': index + 1,
      Entidad: row.ent ?? '',
      Ejercicio: row.mtacod ?? '',
      Estado: row.mtades ?? '',
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de tipos de Almacenajes']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['#', 'Entidad', 'Código', 'Descripción']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 6 },
      { wch: 12 },
      { wch: 15 },
      { wch: 20 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'almacenajes');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'tipos-almacenaje.xlsx'
    );
  }

  pdfDownload() {
    this.limpiarMessages();
    const source = this.almacenajes;
    if (!source?.length) {
      this.almacenajesError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      ent: row.ent ?? '',
      mtacod: row.mtacod ?? '',
      mtades: row.mtades ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de tipos de almacenajes', 40, 40);

    const columns = [
      { header: 'Entidad', dataKey: 'ent' },
      { header: 'Código', dataKey: 'mtacod' },
      { header: 'Descripción', dataKey: 'mtades' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map((row: any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('tipos-almacenajes.pdf');
  }

  //detail grid functions
  selectedAlmacenaje: any = [];
  almacenajeDetail: boolean = false;
  almacenajeDetailError: string = '';
  almacenajeDetailSuccess: string = '';
  isUpdatingAlmacenaje: boolean = false;
  openDetail(p: any) {
    this.limpiarMessages();
    this.selectedAlmacenaje = p;
    this.tempAlmacenaje = {...p}
    this.almacenajeDetail = true;
  }

  closeDetails() {
    this.limpiarMessages();
    this.almacenajeDetail = false;
    this.selectedAlmacenaje = [];
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  tempAlmacenaje: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedAlmacenaje ? { ...this.selectedAlmacenaje } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempAlmacenaje = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }


  allowToUpdate: boolean = false;
  isUpdateAllowed() {
    if (this.allowToUpdate) {
      this.updateAlmacenaje();
    } else {
      return;
    }
  }

  updateAlmacenaje() {
    this.limpiarMessages();
    this.isUpdatingAlmacenaje = true;

    Object.assign(this.selectedAlmacenaje, this.tempAlmacenaje);

    const mtacod = this.selectedAlmacenaje.mtacod
    const payload = {
      "MTADES": this.selectedAlmacenaje.mtades
    }

    this.http.patch(`${environment.backendUrl}/api/mta/update-almacenaje/${this.entcod}/${mtacod}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.isUpdatingAlmacenaje = false;
        this.almacenajeDetailSuccess = 'tipo de almacenaje se ha actualizado correctamente';
      },
      error: (err) => {
        this.isUpdatingAlmacenaje = false;
        this.almacenajeDetailError = err.error.error ?? err.error
      }
    })
  }

  showDeleteConfirm: boolean = false;
  isDeletingAlmacenaje: boolean = false;
  deleteMessageError: string = '';
  openDelete() {
    this.limpiarMessages();
    this.showDeleteConfirm = true;
  }

  closeDelete() {
    this.limpiarMessages();
    this.showDeleteConfirm = false;
  }

  confirmDelete() {
    this.limpiarMessages();
    this.isDeletingAlmacenaje = true;

    const mtacod = this.selectedAlmacenaje.mtacod;
    this.http.delete(`${environment.backendUrl}/api/mta/delete-almacenaje/${this.entcod}/${mtacod}`).subscribe({
      next: (res) => {
        this.isDeletingAlmacenaje = false;
        this.fetchAlmacenajes();
        this.closeDelete();
        this.closeDetails();
        this.almacenajesSuccess = 'El tipo de almacenamiento se ha eliminado correctamente';
      },
      error: (err) => {
        this.isDeletingAlmacenaje = false;
        this.deleteMessageError = err.error.error ?? err.error
      }
    })
  }

  addAlmacenajeGrid: boolean = false;
  addAlmacenajeError: string = '';
  isAddingAlmacenaje: boolean = false;
  openAddAlmacenaje() {
    this.addAlmacenajeGrid = true;
  }

  closeAddAlmacenaje() {
    this.addAlmacenajeGrid = false;
  }

  confirmAdd(desc: string) {
    this.limpiarMessages();
    this.isAddingAlmacenaje = true;

    const payload = {
      "ENT": this.entcod,
      "MTADES": desc
    }

    this.http.post(`${environment.backendUrl}/api/mta/add-almacenaje`, payload).subscribe({
      next: (res) => {
        this.isAddingAlmacenaje = false;
        this.fetchAlmacenajes();
        this.closeAddAlmacenaje();
        this.almacenajesSuccess = 'tipo de almacenaje se ha agregado exitosamente';
      },
      error: (err) => {
        this.isAddingAlmacenaje = false;
        this.addAlmacenajeError = err.error.error ?? err.error;
      }
    })
  }

  //misc
  limpiarMessages() {
    this.almacenajesSuccess = '';
    this.almacenajesError = '';
    this.almacenajeDetailError = '';
    this.almacenajeDetailSuccess = '';
    this.deleteMessageError = '';
    this.addAlmacenajeError = '';
  }
}

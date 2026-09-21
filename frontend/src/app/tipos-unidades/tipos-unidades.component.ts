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
  selector: 'app-tipos-unidades',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent ],
  templateUrl: './tipos-unidades.component.html',
  styleUrls: ['./tipos-unidades.component.css']
})
export class TiposUnidadesComponent {
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
  unidades: any = [];
  page = 0;
  pageSize = 20;
  unidadesSuccess: string = '';
  unidadesError: string = '';
  isLoadingUnidades: boolean = false;

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
    
    this.fetchUnidades();
  }

  //main grid functions
  fetchUnidades() {
    this.isLoadingUnidades = true;
    this.http.get(`${environment.backendUrl}/api/aun/fetch-list/${this.entcod}`).subscribe({
      next: (res) => {
        this.isLoadingUnidades = false;
        this.unidades = res;
        this.page = 0;
      },
      error: (err) => {
        this.isLoadingUnidades = false;
        this.unidadesError = err.error.error ?? err.error
      }
    })
  }
  get paginatedUnidades(): any[] { if (!this.unidades || this.unidades.length === 0) return [];
    const start = this.page * this.pageSize; return this.unidades.slice(start, start + this.pageSize);
  }
  get totalPages(): number {return Math.max(1, Math.ceil((this.unidades?.length ?? 0) / this.pageSize)); }
  prevPage(): void {if (this.page > 0) this.page--; }
  nextPage(): void {if (this.page < this.totalPages - 1) this.page++;}
  goToPage(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {this.page = inputPage - 1;}
  }
  private updatePagination(): void {const total = this.totalPages;
    if (total === 0) {this.page = 0;return;}
    if (this.page >= total) {this.page = total - 1;}
  }

  toggleSort(field: 'auncod' | 'aundes'): void {
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

  sortField: 'auncod' | 'aundes' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const sorted = [...this.unidades].sort((a, b) => {
      const extract = (item: any, prop: string) =>
        (item?.[prop] ?? item?.[prop.toUpperCase()] ?? '').toString();

      const aVal = extract(a, field);
      const bVal = extract(b, field);
      return this.sortDirection === 'asc'
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
    });

    this.unidades = sorted;
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
    if (!this.searchTerm) {this.fetchUnidades(); return;}

    if (this.searchTerm.length === 3) {
      this.isLoadingUnidades = true;
      this.http.get(`${environment.backendUrl}/api/aun/search-codigo/${this.entcod}/${this.searchTerm}`).subscribe({
        next: (res) => {
          this.isLoadingUnidades = false;
          this.unidades = res;
          this.page = 0;
        },
        error: (err) => {
          this.isLoadingUnidades = false;
          this.unidadesError = err.error.error ?? err.error
        }
      })
    } else {
      this.isLoadingUnidades = true;
      this.http.get(`${environment.backendUrl}/api/aun/search-decripcion/${this.entcod}/${this.searchTerm}`).subscribe({
        next: (res) => {
          this.isLoadingUnidades = false;
          this.unidades = res;
          this.page = 0;
        },
        error: (err) => {
          this.isLoadingUnidades = false;
          this.unidadesError = err.error.error ?? err.error
        }
      })
    }
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.searchTerm = '';
    this.fetchUnidades();
  }

  excelDownload() {
    this.limpiarMessages();
    const rows = this.unidades;
    if (!rows || rows.length === 0) {
      this.unidadesError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row: any, index: number) => ({
      '#': index + 1,
      Entidad: row.ent ?? '',
      Código: row.auncod ?? '',
      Descripción: row.aundes ?? '',
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de tipos de unidades']], { origin: 'A1' });
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
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Tipo de unidades');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'tipos-Unidades.xlsx'
    );
  }

  pdfDownload() {
    this.limpiarMessages();
    const source = this.unidades;
    if (!source?.length) {
      this.unidadesError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      ent: row.ent ?? '',
      auncod: row.auncod ?? '',
      aundes: row.aundes ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de tipos de unidades', 40, 40);

    const columns = [
      { header: 'Entidad', dataKey: 'ent' },
      { header: 'Código', dataKey: 'auncod' },
      { header: 'Descripción', dataKey: 'aundes' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map((row: any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('Tipos-Unidades.pdf');
  }

  //detail grid functions
  selectedUnidad: any = [];
  unidadDetail: boolean = false;
  unidadDetailError: string = '';
  unidadDetailSuccess: string = '';
  isUpdatingUnidad: boolean = false;
  openDetail(p: any) {
    this.limpiarMessages();
    this.selectedUnidad = p;
    this.tempUnidad = {...p};
    this.unidadDetail = true;
  }

  closeDetails() {
    this.limpiarMessages();
    this.unidadDetail = false;
    this.selectedUnidad = [];
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  tempUnidad: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
      this.backupData = this.selectedUnidad ? { ...this.selectedUnidad } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempUnidad = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }


  allowToUpdate: boolean = false;
  isUpdateAllowed() {
    if (this.allowToUpdate) {
      this.updateUnidad();
    } else {
      return;
    }
  }

  updateUnidad() {
    this.limpiarMessages();
    this.isUpdatingUnidad = true;

    Object.assign(this.selectedUnidad, this.tempUnidad);

    const auncod = this.selectedUnidad.auncod
    const aundes = this.selectedUnidad.aundes

    if (!aundes) {this.unidadDetailError = 'Descripción requerida'; return;}
    const payload = {
      "AUNDES": aundes
    }

    this.http.patch(`${environment.backendUrl}/api/aun/update-unidad/${this.entcod}/${auncod}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.isUpdatingUnidad = false;
        this.unidadDetailSuccess = 'tipo de almacenaje se ha actualizado correctamente';
      },
      error: (err) => {
        this.isUpdatingUnidad = false;
        this.unidadDetailError = err.error.error ?? err.error
      }
    })
  }

  showDeleteConfirm: boolean = false;
  isDeletingUnidad: boolean = false;
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
    this.isDeletingUnidad = true;

    const auncod = this.selectedUnidad.auncod;
    this.http.delete(`${environment.backendUrl}/api/aun/delete-unidad/${this.entcod}/${auncod}`).subscribe({
      next: (res) => {
        this.isDeletingUnidad = false;
        this.fetchUnidades();
        this.closeDelete();
        this.closeDetails();
        this.unidadesError = 'El tipo de unidad se ha eliminado correctamente';
      },
      error: (err) => {
        this.isDeletingUnidad = false;
        this.deleteMessageError = err.error.error ?? err.error
      }
    })
  }

  addUnidadGrid: boolean = false;
  addUnidadError: string = '';
  isAddingUnidad: boolean = false;
  openAddUnidad() {
    this.addUnidadGrid = true;
  }

  closeAddUnidad() {
    this.addUnidadGrid = false;
  }
  
  confirmAdd(codigo: String, desc: string) {
    this.limpiarMessages();

    if (!desc || !codigo) {this.addUnidadError = 'todos los campos son obligatorios'; return;}

    const payload = {
      "ENT": 1,
      "AUNCOD": codigo,
      "AUNDES": desc
    }

    this.isAddingUnidad = true;
    this.http.post(`${environment.backendUrl}/api/aun/add-unidad`, payload).subscribe({
      next: (res) => {
        this.isAddingUnidad = false;
        this.fetchUnidades();
        this.closeAddUnidad();
        this.addUnidadError = 'tipo de unidad se ha agregado exitosamente';
      },
      error: (err) => {
        this.isAddingUnidad = false;
        this.addUnidadError = err.error.error ?? err.error;
      }
    })
  }

  setInputToUpper(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    let upper = (target.value ?? '').toUpperCase();
    target.value = upper;
  }

  //misc 
  limpiarMessages() {
    this.unidadesError = '';
    this.unidadesSuccess = '';
    this.deleteMessageError = '';
    this.unidadDetailError = '';
    this.unidadDetailSuccess = '';
  }
}

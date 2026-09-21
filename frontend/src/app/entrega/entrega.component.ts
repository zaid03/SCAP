import { Component, HostListener} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { environment } from '../../environments/environment';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-entrega',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './entrega.component.html',
  styleUrls: ['./entrega.component.css']
})
export class EntregaComponent {

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

  //main table functions
  isLoading: boolean = false;
  entregas: any[] = [];
  private backupentregas: any[] = [];
  page = 0;
  pageSize = 20;
  entregasError: string = '';
  ngOnInit(): void{
    this.emptyAllMessages();
    this.isLoading = true;
    this.http.get<any[]>(`${environment.backendUrl}/api/Len/fetch-all`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.entregas = res;
        this.backupentregas = [...this.entregas];
        if(this.entregas.length === 0) {
          this.entregasError = 'no hay lugares de entrega';
        }
      },
      error: (err) => {
        this.entregas = [];
        this.isLoading = false;
        this.entregasError = err.error.error ?? err.error;
      }
    })
  }

  get paginatedEntregas(): any[] {
    if (!this.entregas || this.entregas.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.entregas.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.entregas?.length ?? 0) / this.pageSize));
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

  sortField: 'lendes' | 'lencod' | null = null;
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
    this.entregas.sort((a, b) => {
      let aValue: any = a[this.sortColumn];
      let bValue: any = b[this.sortColumn];

      if (this.sortColumn === 'lencod') {
        aValue = Number(aValue);
        bValue = Number(bValue);
        return this.sortDirection === 'asc' ? aValue - bValue : bValue - aValue;
      }

      if (this.sortColumn === 'lendes') {
        aValue = (aValue ?? '').toString().toUpperCase();
        bValue = (bValue ?? '').toString().toUpperCase();
        if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
        if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
        return 0;
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

  searchText: string = '';
  filterEntrega() {
    this.emptyAllMessages();
    this.isLoading = true;

    if(!this.searchText) {
      this.fetchEntregas()
      this.isLoading = false;
      return;;
    }

    const isNumber = /^[0-9]+$/;
    const isChar = /^[A-Za-z]+$/;
    const isAlphanumeric = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]+$/;

    if(isNumber.test(this.searchText)) {
      this.http.get<any[]>(`${environment.backendUrl}/api/Len/filter-lencod/${this.searchText}`).subscribe({
        next: (res) => {
          this.entregas = res;
          this.isLoading = false;
        },
        error: (err) => {
          this.entregas = [];
          this.entregasError = err.error.error ?? err.error;
          this.isLoading = false;
        }
      });
    }
    else {
      this.http.get<any[]>(`${environment.backendUrl}/api/Len/filter-lendes/${this.searchText}`).subscribe({
        next: (res) => {
          this.entregas = res;
          this.isLoading = false;
        },
        error: (err) => {
          this.entregas = [];
          this.entregasError = err.error.error ?? err.error;
          this.isLoading = false;
        }
      });
    }
  }

  limpiarFiltro() {
    this.entregas = [...this.backupentregas];
    this.searchText = '';
    this.entregasError = '';
    this.page = 0;
  }

  downloadExcel() {
    this.emptyAllMessages();
    const rows = this.entregas;
    if (!rows || rows.length === 0) {
      this.entregasError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Código: row.lencod ?? '',
      Descripción: row.lendes ?? '',
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de Lugares de Entrega']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Código', 'Descripción']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 15 },
      { wch: 40 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Lugares_Entrega');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Lugares_Entrega.xlsx'
    );
  }

  exportPdf() {
    this.emptyAllMessages();
    const source = this.entregas;

    if (!source?.length) {
      this.entregasError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      lencod: row.lencod ?? '',
      lendes: row.lendes ?? '',
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Lugares de Entrega', 40, 40);

    const columns = [
      { header: 'Código', dataKey: 'lencod' },
      { header: 'Descripción', dataKey: 'lendes' },
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('lugares_entrega.pdf');
  }
  
  //detail grid functions
  selectedEntregas: any = null;
  detallesMessageError: String = '';
  detallesMessageSuccess: string = '';
  fettalesIsError: boolean = false;

  showDetails(entrega: any) {
    this.selectedEntregas = entrega;
    this.tempEntrega = entrega;
  }

  closeDetails() {
    this.selectedEntregas = null;
    this.emptyAllMessages();
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  tempEntrega: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedEntregas ? { ...this.selectedEntregas } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempEntrega = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed(lencod: number, lendes: string, lentxt: string) {
    if (this.allowToUpdate) {
      this.updateEntrega(lencod, lendes, lentxt);
    } else {
      return;
    }
  }

  isUpdating: boolean = false;
  updateEntrega(lencod: number, lendes: string, lentxt: string) {
    this.emptyAllMessages();
    this.isUpdating = true;

    Object.assign(this.selectedEntregas, this.tempEntrega);

    if (!lencod || !lendes) {
      this.detallesMessageError = 'Descripción requirido'
      return;
    }
    const payload = {
      "LENDES" : lendes,
      "LENTXT" : lentxt
    }

    this.http.patch(`${environment.backendUrl}/api/Len/update-lugar/${lencod}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.detallesMessageSuccess = 'Lugares de entrega actualizada exitosamente';
        this.isUpdating = false;
      },
      error: (err) => {
        this.detallesMessageError = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    })
  }

  detallesMessageErrorDelete: string = '';
  entregaSuccess: string = '';
  deleteEntrega(lencod: number) {
    this.isDeleting = true;
    this.emptyAllMessages();
    if (!lencod) {
      return;
    }

    this.http.delete(`${environment.backendUrl}/api/Len/delete-lugar/${lencod}`).subscribe({
      next: (res) => {
        this.entregaSuccess = 'Lugar de entrega eliminado exitosamente'
        this.entregas = this.entregas.filter((e: any) => e.lencod !== lencod);
        this.closeDeleteConfirm();
        this.closeDetails();
        this.isDeleting = false;
      },
      error: (err) => {
        this.detallesMessageErrorDelete = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    })
  }

  showDeleteConfirm = false;
  entregaToDelete: any = null;
  openDeleteConfirm(entrega: any) {
    this.entregaToDelete = entrega;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.entregaToDelete = null;
  }

  isDeleting: boolean = false;
  confirmDelete() {
    if (this.entregaToDelete) {
      this.deleteEntrega(this.entregaToDelete.lencod);
    }
  }

  //adding entrega grid functions
  showAddGrid = false;
  personaInfo: any = [];
  showAdd() {
    this.showAddGrid = true;
    this.personaInfo = this.selectedEntregas;
  }

  hideAdd() {
    this.showAddGrid = false;
  }

  errorAddEntrega: string = '';
  isAdding: boolean = false;
  addEntrega(lendes: string, lentxt: string) {
    this.isAdding = true;
    this.emptyAllMessages();
    if (!lendes) {
      this.errorAddEntrega = 'Descripción requirido'
      return;
    }
    const payload = {
      "lendes" : lendes,
      "lentxt" : lentxt
    }
    
    this.http.post(`${environment.backendUrl}/api/Len/add-lugar`, payload).subscribe({
      next: (res) => {
        this.entregaSuccess = 'Lugar de entrega agregada exitosamente';
        this.fetchEntregas();
        this.hideAdd();
        this.isAdding = false;
      },
      error: (err) => {
        this.errorAddEntrega = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }

  fetchEntregas() {
    this.http.get<any[]>(`${environment.backendUrl}/api/Len/fetch-all`).subscribe({
      next: (res) => {
        this.entregas = res;
        this.backupentregas = [...this.entregas];
      },
      error: (err) => {
        this.entregasError = err.error.error ?? err.error;
      }
    });
  }

  //misc
  emptyAllMessages() {
    this.detallesMessageErrorDelete = '';
    this.entregasError = '';
    this.detallesMessageError = '';
    this.detallesMessageSuccess = '';
    this.entregaSuccess = '';
    this.errorAddEntrega = '';
  }
}

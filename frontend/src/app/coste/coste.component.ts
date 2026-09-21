import { Component, HostListener} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { environment } from '../../environments/environment';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { retry } from 'rxjs';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-entrega',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './coste.component.html',
  styleUrls: ['./coste.component.css']
})
export class CosteComponent {
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

  //main table functions
  isLoading: boolean = false;
  costes: any[] = [];
  private backupCostes: any[] = [];
  page = 0;
  pageSize = 20;
  costeError: string = '';
  ngOnInit(): void{
    this.emptyAllMessages();
    this.isLoading = true;

    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    if (entidad) {
      const parsed = JSON.parse(entidad);
      this.entcod = parsed.ENTCOD;
    }
    if (eje) {
      const parsed = JSON.parse(eje);
      this.eje = parsed.eje;
    }
    if (!entidad || this.entcod === null || !eje || this.eje === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchCostes();
  }

  fetchCostes() {
    this.http.get<any[]>(`${environment.backendUrl}/api/cco/fetch-all/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.costes = res;
        this.backupCostes = [...this.costes];
        if(this.costes.length === 0) {
          this.costeError = 'no hay lugares de entrega';
        }
      },
      error: (err) => {
        this.costes = [];
        this.isLoading = false;
        this.costeError = err.error.error ?? err.error;
      }
    })
  }

  get paginatedCostes(): any[] {
    if (!this.costes || this.costes.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.costes.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.costes?.length ?? 0) / this.pageSize));
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

  sortField: 'ccocod' | 'ccodes' | null = null;
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
    this.costes.sort((a, b) => {
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
      this.fetchCostes();
      this.isLoading = false;
      return;
    }

    const isNumber = /^[0-9]+$/;
    const isChar = /^[A-Za-z]+$/;
    const isAlphanumeric = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]+$/;

    if(isNumber.test(this.searchText)) {
      this.http.get<any[]>(`${environment.backendUrl}/api/cco/filter-by/${this.entcod}/${this.eje}/${this.searchText}`).subscribe({
        next: (res) => {
          this.costes = res;
          this.isLoading = false
        },
        error: (err) => {
          this.costes = [];
          this.costeError = err.error.error ?? err.error;
          this.isLoading = false
        }
      });
    }
    if(isChar.test(this.searchText) || isAlphanumeric.test(this.searchText)) {
      this.http.get<any[]>(`${environment.backendUrl}/api/cco/filter-by-des/${this.entcod}/${this.eje}/${this.searchText}`).subscribe({
        next: (res) => {
          this.costes = res;
          this.isLoading = false
        },
        error: (err) => {
          this.costes = [];
          this.costeError = err.error.error ?? err.error;
          this.isLoading = false
        }
      });
    }
  }

  limpiarFiltro() {
    this.costes = [...this.backupCostes];
    this.searchText = '';
    this.costeError = '';
    this.page = 0;
  }

  downloadExcel() {
    this.emptyAllMessages();
    const rows = this.costes;
    if (!rows || rows.length === 0) {
      this.costeError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Código: row.ccocod ?? '',
      Descripción: row.ccodes ?? '',
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de Centro de Coste']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Código', 'Descripción']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 15 },
      { wch: 40 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Centro_Coste');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Centro_Coste.xlsx'
    );
  }

  exportPdf() {
    this.emptyAllMessages();
    const source = this.costes;

    if (!source?.length) {
      this.costeError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      ccocod: row.ccocod ?? '',
      ccodes: row.ccodes ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Centro de Coste', 40, 40);

    const columns = [
      { header: 'Código', dataKey: 'ccocod' },
      { header: 'Descripción', dataKey: 'ccodes' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('centros_coste.pdf');
  }

  //detail grid functions
  selectedCoste: any = null;
  detallesMessageError: String = '';
  detallesMessageSuccess: string = '';
  fettalesIsError: boolean = false;

  showDetails(coste: any) {
    this.selectedCoste = coste;
    this.tempCoste = { ...coste };
  }

  closeDetails() {
    this.selectedCoste = null;
    this.emptyAllMessages();
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  filterDigits(event: Event) {
    const textarea = event.target as HTMLTextAreaElement;
    textarea.value = textarea.value.replace(/\D/g, '').slice(0, 4);
    this.selectedCoste.ccocod = textarea.value;
  }

  tempCoste: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedCoste ? { ...this.selectedCoste } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempCoste = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed(ccocod: string, ccodes: string) {
    if (this.allowToUpdate) {
      this.updateEntrega(ccocod, ccodes);
    } else {
      return;
    }
  }

  isUpdating: boolean = false;
  updateEntrega(ccocod: string, ccodes: string) {
    this.emptyAllMessages();
    this.isUpdating = true;

    Object.assign(this.selectedCoste, this.tempCoste);
    if (!ccodes) {
      this.detallesMessageError = 'descripción requerida'
      this.isUpdating = false;
      return;
    }
    const payload = {
      "CCODES" : ccodes
    }

    this.http.patch(`${environment.backendUrl}/api/cco/update-centro/${this.entcod}/${this.eje}/${ccocod}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.detallesMessageSuccess = 'Lugares de entrega actualizada exitosamente'
        this.isUpdating = false;
      },
      error: (err) => {
        this.detallesMessageError = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    })
  }

  showDeleteConfirm = false;
  costeToDelete: any = null;
  openDeleteConfirm(coste: any) {
    this.costeToDelete = coste;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.costeToDelete = null;
  }

  confirmDelete() {
    if (this.costeToDelete) {
      this.deleteCoste(this.costeToDelete.ccocod);
    }
  }

  detallesMessageErrorDelete: string = '';
  costeSuccess: string = '';
  isDeleting: boolean = false;
  deleteCoste(ccocod: number) {
    this.isDeleting = true;
    this.emptyAllMessages();
    if (!ccocod) {
      return;
    }

    this.http.delete(`${environment.backendUrl}/api/cco/delete-coste/${this.entcod}/${this.eje}/${ccocod}`).subscribe({
      next: (res) => {
        this.costeSuccess = 'Lugar de entrega eliminado exitosamente'
        this.costes = this.costes.filter((e: any) => e.ccocod !== ccocod);
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

  //adding entrega grid functions
  showAddGrid = false;
  costesNew: any = [];
  errorAddcoste: string = '';
  showAdd() {
    this.showAddGrid = true;
    this.costesNew = this.selectedCoste;
  }

  hideAdd() {
    this.showAddGrid = false;
  }

  isAdding: boolean = false;
  addCoste(ccocod: string, ccodes: string) {
    this.isAdding = true;
    this.emptyAllMessages();
    if(!ccocod || !ccodes) {
      this.errorAddcoste = 'Todos los campos son obligatorios';
      return;
    }

    const payload = {
      "ENT" : this.entcod,
      "EJE" : this.eje,
      "CCOCOD" : ccocod,
      "CCODES" : ccodes
    }

    this.http.post(`${environment.backendUrl}/api/cco/Insert-centro`, payload).subscribe({
      next: (res) => {
        this.costeSuccess = 'centro de coste a agregada exitosamente';
        this.hideAdd();
        this.fetchCostes();
        this.isAdding = false;
      },
      error: (err) => {
        this.errorAddcoste = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }
  //misc
  emptyAllMessages() {
    this.costeError = '';
    this.detallesMessageError = '';
    this.detallesMessageSuccess = '';
    this.costeSuccess = '';
    this.detallesMessageErrorDelete = '';
    this.errorAddcoste = '';
  }
}

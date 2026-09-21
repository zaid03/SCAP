import { Component, HostListener } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, provideCloudinaryLoader } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';
@Component({
  selector: 'app-cge',
  standalone: true,
  imports: [CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './cge.component.html',
  styleUrls: ['./cge.component.css']
})
export class CgeComponent {
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
  public cge: string = '';
  centroGestores: any[] = [];
  private backupCentroGestores: any[] = [];
  private defaultCentroGestores: any[] = [];
  page = 0;
  pageSize = 20;
  isLoading: boolean = false;

  ngOnInit() {
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const cge = sessionStorage.getItem('CENTROGESTOR');

    if (entidad) {
      const parsed = JSON.parse(entidad);
      this.entcod = parsed.ENTCOD;
    }

    if (eje) {
      const parsed = JSON.parse(eje);
      this.eje = parsed.eje;
    }

    if (cge) {
      const parsed = JSON.parse(cge);
      this.cge = parsed.value;
    }

    if (!entidad || this.entcod === null || this.eje === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchCentro();
  }

  //main table functions
  fetchCentro() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/cge/fetch-all/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.centroGestores = Array.isArray(res) ? [...res] : [];
        this.backupCentroGestores = [...this.centroGestores];
        this.defaultCentroGestores = [...this.centroGestores];
        this.page = 0;
        this.updatePagination();
        if ( res.status === 404 ) {
          this.centroGestores = [];
          this.SearchDownMessageError = typeof res.body === 'string' ? res.body : 'sin resultados.';
        }
        this.isLoading = false;
      }, error: (err) => {
        this.centroGestores = [];
        this.SearchDownMessageError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    })
  }
  get paginatedCentroGestor(): any[] {
    if (!this.centroGestores || this.centroGestores.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.centroGestores.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.centroGestores?.length ?? 0) / this.pageSize));
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

  getkCGECIC(cgecic: number) {
    if (cgecic === 0) {
      return 'No';
    } else if (cgecic === 1) {
      return 'Sí';
    } else if (cgecic === 2) {
      return 'Cierre para contabilizar';
    }
    return;
  }

  setCgecic(value: number): void {
    if (!this.tempCentroGestor) {
      return;
    }
    this.tempCentroGestor.cgecic = value;
  }

  isCgecic(value: number): boolean {
    return (this.tempCentroGestor?.cgecic ?? null) === value;
  }

  SearchDownMessageError: string = '';
  public searchTerm: string = '';
  handleSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = (input.value ?? '').toUpperCase();
    if(value.length > 4) {
      value = value.slice(0, 4);
    }
    this.searchTerm = value;
    input.value = value;
  }

  searchCentroGestor(): void {
    this.isLoading = true;
    this.limpiarMessages();
    const term = this.searchTerm.trim();

    if (!term || term.length < 2) {
      this.fetchCentro();
      this.isLoading = false;
      return;
    }

    this.http.get<any>(`${environment.backendUrl}/api/cge/search-centros/${this.entcod}/${this.eje}/${term}`).subscribe({
      next: (res) => {
        this.centroGestores = Array.isArray(res) ? [...res] : [];
        this.defaultCentroGestores = [...this.centroGestores];
        this.page = 0;
        this.updatePagination();
        if ( res.status === 404 ) {
          this.centroGestores = [];
          this.SearchDownMessageError = typeof res.body === 'string' ? res.body : 'sin resultados.';
        }
        this.isLoading = false;
      }, error: (err) => {
        this.centroGestores = [];
        this.SearchDownMessageError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    })
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.centroGestores = [...this.backupCentroGestores];
    this.defaultCentroGestores = [...this.backupCentroGestores];
    this.page = 0
    this.searchTerm = '';
    this.sortField = null;
    this.sortDirection = 'asc';
  }

  sortField: string | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  toggleSort(field: string): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.centroGestores = [...this.defaultCentroGestores];
      this.page = 0;
      this.updatePagination();
      return;
    }
    this.applySort();
  }

  private applySort(): void {
    if (!this.sortField) return;

    const base = [...this.defaultCentroGestores];
    const sorted = base.sort((a, b) => {
      let aVal: any, bVal: any;
      if (this.sortField === 'cgecicDisplay') {
        aVal = this.getkCGECIC(a.cgecic);
        bVal = this.getkCGECIC(b.cgecic);
        aVal = (aVal ?? '').toString().toUpperCase();
        bVal = (bVal ?? '').toString().toUpperCase();
        return this.sortDirection === 'asc'
          ? aVal.localeCompare(bVal, 'es')
          : bVal.localeCompare(aVal, 'es');
      }
      aVal = a[this.sortField!];
      bVal = b[this.sortField!];
      aVal = (aVal ?? '').toString().toUpperCase();
      bVal = (bVal ?? '').toString().toUpperCase();
      return this.sortDirection === 'asc'
        ? aVal.localeCompare(bVal, 'es')
        : bVal.localeCompare(aVal, 'es');
    });
    this.centroGestores = sorted;
    this.page = 0;
    this.updatePagination();
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

  excelDownload() {
    this.limpiarMessages();
    const rows = this.centroGestores;
    if (!rows || rows.length === 0) {
      this.SearchDownMessageError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Código: row.cgecod ?? '',
      Descripción: row.cgedes ?? '',
      Orgánica: row.cgeorg ?? '',
      Programa: row.cgefun ?? '',
      Cierre_contable: this.getkCGECIC(row.cgecic) ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de Centros de gestor']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Código', 'Descripción', 'Orgánica', 'Programa', 'Cierre_contable']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 12 },
      { wch: 55 },
      { wch: 12 },
      { wch: 12 },
      { wch: 25 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Centros_gestor');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Centros_gestor.xlsx'
    );
  }

  pdfDownload() {
    this.limpiarMessages();
    const source = this.centroGestores;
    if (!source?.length) {
      this.SearchDownMessageError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      cgecod: row.cgecod ?? '',
      cgedes: row.cgedes ?? '',
      cgeorg: row.cgeorg ?? '',
      cgefun: row.cgefun ?? '',
      cgecic: this.getkCGECIC(row.cgecic) ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Centros de gestor', 40, 40);

    const columns = [
      { header: 'Código', dataKey: 'cgecod' },
      { header: 'Descripción', dataKey: 'cgedes' },
      { header: 'Orgánica', dataKey: 'cgeorg' },
      { header: 'Programa', dataKey: 'cgefun' },
      { header: 'Cierre contable', dataKey: 'cgecic' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('Centros_gestor.pdf');
  }

  //detail grid functions
  selectedCentroGestor: any = null;
  centroGestorSuccessMessage: String = '';
  centroGestorErrorMessage: string = '';
  tempCentroGestor: any = {};
  showDetails(centroGestor: any) {
    this.limpiarMessages();
    this.selectedCentroGestor = centroGestor;
    this.tempCentroGestor = { ...centroGestor };
    this.isUpdate = false;
  }

  closeDetails() {
    this.selectedCentroGestor = null;
    this.limpiarMessages();
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedCentroGestor ? { ...this.selectedCentroGestor } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempCentroGestor = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed(cge: string, des: string, org: string, fun: string, dat: string) {
    if (this.allowToUpdate) {
      this.updateCentroGestor(cge, des, org, fun, dat);
    } else {
      return;
    }
  }

  isUpdating: boolean = false;
  updateCentroGestor(cge: string, des: string, org: string, fun: string, dat: string) {
    this.isUpdating = true;
    this.limpiarMessages();
    Object.assign(this.selectedCentroGestor, this.tempCentroGestor);
    const payload = {
      cgedes: des,
      cgeorg: org,
      cgefun: fun,
      cgedat: dat,
      cgecic: this.tempCentroGestor.cgecic
    }

    if (!org || !fun) {
      this.centroGestorErrorMessage = 'Se requieren organica y programa';
      return;
    }

    this.http.patch<any>(`${environment.backendUrl}/api/cge/update-cge/${this.entcod}/${this.eje}/${cge}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.centroGestorSuccessMessage = 'Centro gestor actualizado con éxito';
        this.isUpdating = false;
      }, 
      error: (err) => {
        const message = err.error.error ?? err.error;
        this.centroGestorErrorMessage = message;
        this.isUpdating = false;
      }
    })
  }

  allowOnlyDigits(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    target.value = target.value.replace(/[^0-9]/g, '');
    if (this.tempCentroGestor) {
      this.tempCentroGestor.cgefun = target.value;
    }
  }

  showDeleteConfirm: boolean = false;
  centroGestorToDelete: any = null;
  openDeleteConfirm(cgecod: string) {
    this.limpiarMessages();
    this.centroGestorToDelete = cgecod;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.centroGestorToDelete = null;
    this.showDeleteConfirm = false;
  }

  confirmDelete(): void {
    if (this.centroGestorToDelete) {
      this.deleteCentroGestor(this.centroGestorToDelete);
    }
  }

  isDeleting: boolean = false;
  deleErr: string = '';
  deleteCentroGestor(cgecod: string) {
    this.isDeleting = true;
    this.limpiarMessages();
    this.http.delete<any>(`${environment.backendUrl}/api/cge/delete-centro-gestor/${this.entcod}/${this.eje}/${cgecod}`).subscribe({
      next: (res) => {
        this.successAddCentroGestor = 'cge eliminado exitosamente';
        this.centroGestores = this.centroGestores.filter(c => c.cgecod !== cgecod);
        this.backupCentroGestores = this.backupCentroGestores.filter(c => c.cgecod !== cgecod);
        this.closeDeleteConfirm();
        this.closeDetails();
        this.isDeleting = false;
      }, error: (err) => {
        this.deleErr = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    })
  }

  //add grid functions
  showAddConfirm: boolean = false;
  centroGestorAddError: string = '';
  launchAddCentroGestor() {
    this.limpiarMessages();
    this.showAddConfirm = true;
  }

  closeAddConfirm() {
    this.showAddConfirm = false;
    this.limpiarMessages();
  }

  newCgecic = 0;
  setAddCgecic(value: number): void {
    this.newCgecic = value;
  }
  newCge = '';
  setCgeToUpper(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    let upper = (target.value ?? '').toUpperCase();
    if(upper.length > 4) {
      upper = upper.slice(0, 4);
    }
    target.value = upper;
    this.newCge = upper;
  }

  successAddCentroGestor = '';
  isAdding: boolean = false;
  AddCentroGestor(cod: string, org: string, fun: string, des: string,  dat: string) {
    this.limpiarMessages();

    if (!cod || !des) {
      this.centroGestorAddError = 'Se requieren codigo y descripción'
      return;
    }
    
    this.isAdding = true;

    const payload = {
      "ent" : this.entcod,
      "eje" : this.eje,
      "cgecod" : this.newCge,
      "cgedes" : des,
      "cgeorg" : org,
      "cgefun" : fun,
      "cgedat" : dat,
      "cgecic" : this.newCgecic
    }

    this.http.post<any>(`${environment.backendUrl}/api/cge/Insert-familia`,payload).subscribe({
      next: (res) => {
        this.successAddCentroGestor = 'centro gestor añadido con éxito'
        this.fetchCentro();
        this.closeAddConfirm();
        this.isAdding = false;
      },
      error: (err) => {
        this.centroGestorAddError = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }

  //misc
  limpiarMessages() {
    this.SearchDownMessageError = '';
    this.successAddCentroGestor = '';
    this.centroGestorSuccessMessage = '';
    this.centroGestorErrorMessage = '';
    this.deleErr = '';
    this.centroGestorAddError = '';
  }
}

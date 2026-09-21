import { Component, HostListener } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-familia',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './familia.component.html',
  styleUrls: ['./familia.component.css']
})
export class FamiliaComponent {
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
  familias: any[] = [];
  private backupFamilias: any[] = [];
  tableMessage: string = '';
  page = 0;
  pageSize = 20;
  isLoading: boolean = false;

  ngOnInit():void {
    this.limpiarMessages();
    this.isLoading = true;
    const entidad = sessionStorage.getItem('Entidad');
    if (entidad) {
      const parsed = JSON.parse(entidad);
      this.entcod = parsed.ENTCOD;
    }

    if (!entidad || this.entcod === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }
    this.fetchFamilias();
  }

  //main table functions
  fetchFamilias() {
    this.http.get<any>(`${environment.backendUrl}/api/afa/by-ent/${this.entcod}`).subscribe({
      next: (response) => {
        if (response.error) {
          this.tableMessage = 'Error: ' + response.error || 'Server error';
          this.isLoading = false;
        } else {
          this.familias = Array.isArray(response) ? [...response] : [];
          this.backupFamilias = [...this.familias];
          this.defaultFamilias = [...this.familias];
          this.page = 0;
          this.isLoading = false;
        }
      },error: (err) => {
        this.tableMessage = err.error.error ?? err.error;
        this.isLoading = false;
      }
    })
  }

  public searchTerm: string = '';
  searchFamilias(): void {
    this.isLoading = true;
    this.limpiarMessages();
    const term = this.searchTerm.trim();

    if (!term) {
      this.fetchFamilias();
      this.isLoading = false;
      return;
    }

    const numbersOnly = /^\d+$/
    if (numbersOnly.test(term)) {
      if (term.length <= 5) {
        this.familias = this.backupFamilias.filter(
          (f) => f.afacod?.toString() === term
        );
        this.isLoading = false;
      } else {
        const lower = term.toLowerCase();
        this.familias = this.backupFamilias.filter((f) =>
          f.afades?.toString().toLowerCase().includes(lower)
        );
        this.isLoading = false;
      }
    } else {
      const lower = term.toLowerCase();
      this.familias = this.backupFamilias.filter((f) =>
        f.afades?.toString().toLowerCase().includes(lower)
      );
      this.isLoading = false;
    }

    if (this.familias.length === 0) {
      this.tableMessage = 'No resultado';
    }
    
    this.defaultFamilias = [...this.familias];
    this.sortField = null;
    this.sortDirection = 'asc';
    this.page = 0;
  }

  limpiarSearch() {
    this.limpiarMessages();
    this.familias = [...this.backupFamilias];
    this.defaultFamilias = [...this.backupFamilias];
    this.searchTerm = '';
    this.sortField = null;
    this.sortDirection = 'asc';
    this.page = 0;
  }

  excelDownload() {
    this.limpiarMessages();
    const rows = this.familias;
    if (!rows || rows.length === 0) {
      this.tableMessage = 'No hay datos para exportar.';
      return;
    }

    const exportRows = rows.map((row, index) => ({
      '#': index + 1,
      Entidad: row.ent ?? '',
      Familia: row.afacod ?? '',
      Descripción: row.afades ?? '',
    }));

    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de familias']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['#', 'Entidad', 'Familia', 'Descripción']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 6 },
      { wch: 12 },
      { wch: 12 },
      { wch: 40 },
    ];

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Familias');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'familias.xlsx'
    );
  }

  toPrint() {
    this.limpiarMessages();

    const source = this.familias;
    if (!source?.length) {
      this.tableMessage = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      ent: row.ent ?? '',
      afacod: row.afacod ?? '',
      afades: row.afades ?? ''
    }));

    const columns = [
      { header: 'Entidad', dataKey: 'ent' },
      { header: 'Familia', dataKey: 'afacod' },
      { header: 'Descripción', dataKey: 'afades' }
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de familias', 10, 15);

    autoTable(doc, {
      startY: 20,
      head: [columns.map(col => col.header)],
      body: rows.map((row: any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      columnStyles: {
        ent: { cellWidth: 24 },
        afacod: { cellWidth: 28 },
        afades: { cellWidth: 50 }
      }
    });

    doc.save('familias.pdf');
  }

  private defaultFamilias: any[] = [];
  sortField: 'afacod' | 'afades' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  toggleSort(field: 'afacod' | 'afades'): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
      this.defaultFamilias = [...this.familias];
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.familias = [...this.defaultFamilias];
      this.page = 0;
      return;
    }

    this.applySort();
  }

  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const base = [...this.defaultFamilias];
    const sorted = base.sort((a, b) => {
      let aVal = a[this.sortField!];
      let bVal = b[this.sortField!];

      if (this.sortField === 'afacod') {
        const aNum = Number(aVal);
        const bNum = Number(bVal);
        const bothNumeric = !isNaN(aNum) && !isNaN(bNum);

        if (bothNumeric) {
          return this.sortDirection === 'asc' ? aNum - bNum : bNum - aNum;
        } else {
          aVal = (aVal ?? '').toString().toUpperCase();
          bVal = (bVal ?? '').toString().toUpperCase();
          return this.sortDirection === 'asc'
            ? aVal.localeCompare(bVal)
            : bVal.localeCompare(aVal);
        }
      }

      if (this.sortField === 'afades') {
        aVal = (aVal ?? '').toString().toUpperCase();
        bVal = (bVal ?? '').toString().toUpperCase();
        return this.sortDirection === 'asc'
          ? aVal.localeCompare(bVal)
          : bVal.localeCompare(aVal);
      }

      aVal = (aVal ?? '').toString().toUpperCase();
      bVal = (bVal ?? '').toString().toUpperCase();
      return this.sortDirection === 'asc'
        ? aVal.localeCompare(bVal)
        : bVal.localeCompare(aVal);
    });

    this.familias = sorted;
    this.page = 0;
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

  get paginatedFamilias(): any[] {
    if (!this.familias || this.familias.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.familias.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.familias?.length ?? 0) / this.pageSize));
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

  //detail grid functions
  selectedFamilias: any = null;
  showDetails(familia: any) {
    this.limpiarMessages();
    this.subfamilias = [];
    this.selectedFamilias = familia;
    this.tempFamilia = familia;
    this.SubfamiliaGrid(this.selectedFamilias.afacod);
  }

  closeDetails() {
    this.selectedFamilias = null;
    this.subfamilias = [];
    this.tempFamilia = [];
    this.showSubfamiliasGrid = false;
    this.activeDetailTab = null;
    this.limpiarMessages();
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  afacodError:string = '';
  subfamilias: any[] = [];
  mta:any[] = [];
  activeDetailTab: 'subfamilia' | null = null;
  showSubfamiliasGrid: boolean = false;
  SubfamiliaGrid(afacod: String){
    this.showSubfamiliasGrid = true;
    this.FetchSubfamilias(afacod);
  }
  
  FetchSubfamilias(afacod: String) {
    if (!afacod) {
      this.afacodError = 'Familia requerido'
    }

    this.http.get<any>(`${environment.backendUrl}/api/asu/by-ent-afacod/${this.entcod}/${afacod}`).subscribe({
      next: (Response) => {
        if(Response.length === 0) {
          this.afacodError = 'No se encontraron Subfamilias'
        } else {
          this.subfamilias = Array.isArray(Response) ? [...Response] : [];
          this.subfamilias.forEach((item, idx) => {
            this.http.get<any[]>(`${environment.backendUrl}/api/mta/all-mta/${this.entcod}`).subscribe({
              next: (mtas) => {
                const almacenajeArray = Array.isArray(mtas) ? mtas : [];
                this.subfamilias[idx].mta = almacenajeArray
                const mtaDescription = almacenajeArray[0]?.mtades ?? '';
                this.subfamilias[idx].mtades = mtaDescription;
              }, error: () => {
                this.subfamilias[idx].mtas = [];
              },
            })
          })
        }
      }, error: (err) => {
        this.afacodError = err.error.error ?? err.error;
      }
    })
  }

  updateMtades(subfamilia: any, value: string): void {
    if (subfamilia.mta?.length) {
      subfamilia.mta[0].mtades = value;
    }
  }

  onMtaChange(subfamilia: any, selectedMtacod: number): void {
    subfamilia.mtacod = selectedMtacod;
    const match = (subfamilia.mta ?? []).find((m: any) => m.mtacod === selectedMtacod);
    if (match) {
      subfamilia.mtades = match.mtades;
    }
  }

  tempFamilia: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedFamilias ? { ...this.selectedFamilias } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempFamilia = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed(afacod: string, afades: string) {
    if (this.allowToUpdate) {
      this.updateFamilia(afacod, afades);
    } else {
      return;
    }
  }

  familiaErrorMessage:string = '';
  familiaSucessMessage: string = '';
  isUpdating: boolean = false;
  updateFamilia(afacod: string, afades: string): void {
    this.limpiarMessages();
    this.isUpdating = true;
    if (!afacod) { return; }
    const payload = {
      "AFADES" : afades
    }

    Object.assign(this.selectedFamilias, this.tempFamilia);

    this.http.patch<any>(`${environment.backendUrl}/api/afa/update-familia/${this.entcod}/${afacod}`, payload).subscribe({
      next: (response) => {
        this.updateSuccess();
        this.familiaSucessMessage = 'Familia actualizada con éxito';
        this.isUpdating = false;
        this.fetchFamilias();
      }, error: (err) => {
        const message = err.error.error ?? err.error;
        this.familiaErrorMessage = message;
        this.isUpdating = false;
      }
    })
  }

  showDeleteConfirm: boolean = false;
  familiaToDelete: any = null;
  openDeleteConfirm(afacod: string) {
    this.familiaToDelete = afacod;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.familiaToDelete = null;
    this.showDeleteConfirm = false;
  }

  confirmDelete(): void {
    if (this.familiaToDelete) {
      this.deleteFamilia(this.familiaToDelete);
    }
  }

  isDeleting: boolean = false;
  delErr: string = '';
  deleteFamilia(afacod: string): void {
    this.isDeleting = true;
    this.limpiarMessages();
    this.http.delete(`${environment.backendUrl}/api/art/delete-familia/${this.entcod}/${afacod}`).subscribe({
      next: () => {
        this.familiaSucessMessage = 'Familia eliminado correctamente';
        this.familias = this.familias.filter(f => f.afacod !== afacod);
        this.backupFamilias = this.backupFamilias.filter(f => f.afacod !== afacod);
        this.closeDeleteConfirm();
        this.isDeleting = false;
        this.closeDetails();
      }, error: (err) => {
        this.delErr = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    })
  }

  //subfamilia grid inside detail grid functions
  successUpdateMEssage: string = '';
  updateSubfamilia(afacod:string, asucod: string, Description:string, Economica: string, almacenaje: any) {
    this.limpiarMessages();
    this.isUpdating = true;
    const almacenajeNum = Number(almacenaje);
    if (!afacod || !asucod || !Description || !Economica || !almacenajeNum) {
      this.afacodError = 'datos faltantes';
    }

    const payload = { ASUDES: Description, ASUECO: Economica, MTACOD: almacenajeNum}

    this.http.patch<any>(`${environment.backendUrl}/api/asu/update-subfamilia/${this.entcod}/${afacod}/${asucod}`, payload).subscribe({
      next: (res) => {
        this.successUpdateMEssage = 'Subfamilia actualizado con éxito';
        this.isUpdating = false;
      }, error: (err) => {
        this.afacodError = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    })
  }

  deleteSubfamilia(afacod:string, asucod: string) {
    this.isDeleting = true;
    if (!afacod || !asucod) {
      this.afacodError = 'datos faltantes';
    }

    this.http.delete<any>(`${environment.backendUrl}/api/art/delete-sub-familia/${this.entcod}/${afacod}/${asucod}`).subscribe({
      next: (res) => {
        this.successUpdateMEssage = 'Subfamilia eliminado exitosamente';
        this.isDeleting = false;
      }, error: (err) => {
        this.afacodError = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    })
  }

  showAddConfirmSub: boolean = false;
  launchAddSubFamilia() {
    if (!this.almacenajes.length) {
      this.fetchAlmacenaje();
    }
    this.showAddConfirmSub = true;
  }

  closeAddConfirmSub() {
    this.showAddConfirmSub = false;
    this.limpiarMessages();
  }

  subAddError: string = '';
  addSub(asucod: string, asudes: string, asueco: string, mtacod: string): void {
    this.limpiarMessages();
    this.launchAddSubFamilia();
    this.fetchAlmacenaje()

    if (!this.selectedFamilias) {
      this.subAddError = 'Seleccione una familia antes de añadir subfamilias.';
      return;
    }

    const trimmedAsucod = (asucod ?? '').trim().toUpperCase();
    const trimmedAsudes = (asudes ?? '').trim();
    const trimmedAsueco = (asueco ?? '').trim();
    const almacenajeNum = Number(mtacod);

    if (!trimmedAsucod || !trimmedAsudes || !trimmedAsueco || Number.isNaN(almacenajeNum)) {
      this.subAddError = 'Complete todos los campos correctamente.';
      return;
    }

    const payload = {
      ent: this.entcod,
      afacod: this.selectedFamilias.afacod,
      asucod: trimmedAsucod,
      asudes: trimmedAsudes,
      asueco: trimmedAsueco,
      mtacod: almacenajeNum
    };

    this.http.post(`${environment.backendUrl}/api/asu/Insert-Subfamilia`, payload).subscribe({
      next: () => {
        this.successUpdateMEssage = 'Subfamilia añadida correctamente.';
        this.closeAddConfirmSub();
        this.FetchSubfamilias(this.selectedFamilias.afacod);
      },
      error: (err) => {
        this.subAddError = err.error.error ?? err.error;
      }
    });
  }

  forceUppercase(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase();
  }

  allowOnlyDigits(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D+/g, '');
  }

  almacenajes: any[] = [];
  fetchAlmacenaje() {
    this.limpiarMessages();
    this.http.get<any[]>(`${environment.backendUrl}/api/mta/all-mta/${this.entcod}`).subscribe({
      next: (mtas) => {
        this.almacenajes = Array.isArray(mtas) ? mtas : [];
      }, error: () => {
        this.almacenajes = [];
      },
    });
  }

  //add familia grid functions
  showAddConfirm: boolean = false;
  launchAddFamilia() {
    this.showAddConfirm = true;
  }

  familiaMessage: string = '';
  familiaAddError: string = '';
  isAdding: boolean = false;
  addFamilia(familia: string, descripcion: string): void {
    this.limpiarMessages();
    this.isAdding = true;

    if (!familia || !descripcion) { 
      this.familiaAddError = 'Se requiere familia y descripción';
      return;
    }

    const ent = this.entcod;
    const payload = { ent, afacod: familia, afades: descripcion };
    this.http.post<any>(`${environment.backendUrl}/api/afa/Insert-familia`, payload).subscribe({
      next: () => {
        this.familiaMessage = 'familia agregada exitosamente'
        this.fetchFamilias();
        this.isAdding = false;
        this.closeAddConfirm();
      }, error: (err) => {
        this.familiaAddError = err.error.error ?? err.error;
        this.isAdding = false;
      }
    });
  }

  closeAddConfirm() {
    this.showAddConfirm = false;
  }

  //misc
  limpiarMessages() {
    this.tableMessage = '';
    this.familiaMessage = '';
    this.familiaErrorMessage = '';
    this.familiaSucessMessage = '';
    this.subAddError = '';
    this.familiaAddError = '';
    this.afacodError = '';
  }
}

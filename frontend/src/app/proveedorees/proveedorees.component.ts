import { Component, HostListener } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-proveedorees',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './proveedorees.component.html',
  styleUrls: ['./proveedorees.component.css']
})
export class ProveedoreesComponent {
  private entcod: number | null = null;
  eje: number | null = null;
  orgCode: string | null = null;
  entidad: string | null = null;

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

  proveedores: any[] = [];
  private backupProveedores: any[] = [];
  error: string | null = null;
  isLoading: boolean = false;
  ngOnInit(): void {
    this.limpiarMessages();
    this.error = '';
    const entidad = sessionStorage.getItem('Entidad');
    const session = sessionStorage.getItem('EJERCICIO');
    const orgnizacion = sessionStorage.getItem('WSORG');
    const entcod = sessionStorage.getItem('WSENT');

    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (session) { const parsed = JSON.parse(session); this.eje = parsed.eje;}
    if (orgnizacion) {const parsed = JSON.parse(orgnizacion); this.orgCode = parsed.WSORG;}
    if (entcod) {const parsed = JSON.parse(entcod); this.entidad = parsed.WSENT};

    if (!entidad || this.entcod === null || this.eje == null || this.orgCode === '' || this.entidad === '') {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchProveedores()
  }

  fetchProveedores() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/ter/by-ent/${this.entcod}`).subscribe({
      next: (response) => {
        if (response.error) {
          this.error = `Error:  ${response.error}`;
        } else {
          this.proveedores = response;
          this.backupProveedores = Array.isArray(response) ? [...response] : [];
          this.defaultProveedores = [...this.backupProveedores];
          this.page = 0;
          this.updatePaginatedProveedores();
          this.isLoading = false;
        }
      },
      error: (err) => {
        this.proveedores = [];
        this.error = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  //sorting main table related function
  sortField: 'tercod' | 'ternom' | 'ternif' | 'terali' | 'tertel' | 'terdom' | 'tercpo' | 'terpob' | 'terayt' | 'terfax' | 'terweb' | 'tercoe' | 'terobs' | null = null;
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
    this.updatePaginatedProveedores();
  }

  private applySort(): void {
    if (!this.sortColumn) return;
    this.proveedores.sort((a, b) => {
      let aValue = a[this.sortColumn];
      let bValue = b[this.sortColumn];

      const aNum = Number(aValue);
      const bNum = Number(bValue);
      if (!isNaN(aNum) && !isNaN(bNum)) {
        return this.sortDirection === 'asc' ? aNum - bNum : bNum - aNum;
      }

      aValue = (aValue ?? '').toString().toUpperCase();
      bValue = (bValue ?? '').toString().toUpperCase();
      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  private updatePaginatedProveedores(): void {
    const total = this.totalPages;
    if (total === 0) {
      this.page = 0;
      return;
    }
    if (this.page >= total) {
      this.page = total - 1;
    }
  }

  page = 0;
  pageSize = 20;
  selectedProveedor: any = null;
  get filteredProveedores() {
    let filtered = this.proveedores;
    return filtered;
  }

  get totalPages(): number {
  return Math.ceil(this.proveedores.length / this.pageSize);
  }

  get paginatedProveedores() {
    const start = this.page * this.pageSize;
   return this.proveedores.slice(start, start + this.pageSize);
  }

  nextPage() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
    }
  }

  goToPage(event: any) {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {
      this.page = inputPage - 1;
    }
  }

  onSearchChange() {
    this.page = 0;
  }
  onFilterChange() {
    this.page = 0;
  }

  showDetails(proveedor: any) {
    this.limpiarMessages();
    this.selectedProveedor = proveedor;
    this.tempProveedor = proveedor;
    this.error = '';
    this.showContactPersons(this.selectedProveedor);
  }

  closeDetails() {
    this.limpiarMessages();
    this.activeDetailTab = null;
    this.selectedProveedor = null;
    this.contactPersons = null;
    this.articulos = null;
    this.showContactPersonsGrid = false;
    this.showArticulosGrid = false;
    this.page = 0;
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  onSearchFormSubmit(event: Event) {
    event.preventDefault();
    this.search();
  }

  //proveedores main related functions
  searchTerm: string = '';
  filterOption: string = 'Nobloqueado';
  isTodos: boolean = true;
  search() {
    this.limpiarMessages();
    this.isLoading = true
    if (this.filterOption != 'todos') {this.isTodos = false;}
    const params = {
      ent: this.entcod || '',
      searchMode: this.filterOption,
      term: this.searchTerm
    };

    this.http.get<any[]>(`${environment.backendUrl}/api/ter/search-proveedores`, {params}).subscribe({
      next: (response) => {
        this.proveedores = response;
        this.page = 0;
        this.isLoading = false;
      },
      error: (err) => {
        this.proveedores = [];
        this.error = err.error.error ?? err.error;
        this.isLoading = false;
      }
    })
  }

  clearSearch() {
    this.limpiarMessages();
    this.proveedores = [...this.backupProveedores];
    this.filterOption = 'Nobloqueado';
    this.page = 0;
    this.searchTerm = '';
    this.isTodos = true;
  }
  
  DownloadPDF() {
    this.limpiarMessages();
    const doc = new jsPDF({orientation: 'landscape', unit: 'mm', format: 'a4'});
    const columns = [
      { header: 'Código', dataKey: 'tercod' },
      { header: 'Nombre', dataKey: 'ternom' },
      { header: 'NIF', dataKey: 'ternif' },
      { header: 'Alias', dataKey: 'terali' },
      { header: 'Teléfono', dataKey: 'tertel' },
      { header: 'Domicilio', dataKey: 'terdom' },
      { header: 'Código Postal', dataKey: 'tercpo' },
      { header: 'Municipio', dataKey: 'terpob' },
      { header: 'Código contable', dataKey: 'terayt' },
      { header: 'Fax', dataKey: 'terfax' },
      { header: 'Web', dataKey: 'terweb' },
      { header: 'Correo electronico', dataKey: 'tercoe' },
      { header: 'Observaciones', dataKey: 'terobs' }
    ];
    const data = this.proveedores;

    autoTable(doc, {
      columns,
      body: data,
      styles: { fontSize: 8 },
      tableWidth: 'wrap',
      headStyles: {
        fillColor: [240, 240, 240],
        textColor: [33, 53, 71],
        fontStyle: 'bold',
        halign: 'left'
      },
      columnStyles: {
        tercod: { cellWidth: 12 },         
        ternom: { cellWidth: 25 },  
        ternif: { cellWidth: 18 },         
        terali: { cellWidth: 25 },          
        tertel: { cellWidth: 18 },          
        terdom: { cellWidth: 25 },        
        tercpo: { cellWidth: 18 },          
        terpob: { cellWidth: 25 },          
        terayt: { cellWidth: 18 },       
        terfax: { cellWidth: 18 },
        terweb: { cellWidth: 20 },
        tercoe: { cellWidth: 25 },
        terobs: { cellWidth: 25 }
      },
      didDrawPage: (dataArg) => {
        doc.setFontSize(10);
        doc.text('Lista de Proveedores', 14, 10);
      }
    });

    doc.save('proveedores.pdf');
  }

  downloadExcel() {
    this.limpiarMessages();
    const rows = this.proveedores;
    if (!rows || rows.length === 0) {
      this.error = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map(row => ({
      tercod: row.tercod ?? '',
      ternom: row.ternom ?? '',
      ternif: row.ternif ?? '',
      terali: row.terali ?? '',
      tertel: row.tertel ?? '',
      terdom: row.terdom ?? '',
      tercpo: row.tercpo ?? '',
      terpob: row.terpob ?? '',
      terayt: row.terayt ?? '',
      terfax: row.terfax ?? '',
      terweb: row.terweb ?? '',
      tercoe: row.tercoe ?? '',
      terobs: row.terobs ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Listado de proveedores']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Código', 'Nombre', 'NIF', 'Alias', 'Teléfono', 'Domicilio', 'Código Postal', 'Municipio', 'Código contable', 'Fax', 'Web', 'Correo electrónico', 'Observaciones']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 8 },
      { wch: 40 },
      { wch: 20 },
      { wch: 40 },
      { wch: 20 },
      { wch: 40 },
      { wch: 20 },
      { wch: 40 },
      { wch: 10 },
      { wch: 20 },
      { wch: 30 },
      { wch: 30 },
      { wch: 40 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Proveedores');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Provedores.xlsx'
    );
  }

  tempProveedor: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.tempProveedor ? { ...this.tempProveedor } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempProveedor = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed() {
    if (this.allowToUpdate) {
      this.saveChanges();
    } else {
      return;
    }
  }

  messageSuccess: string = '';
  messageError: string = '';
  saveChanges() {
    this.isUpdating = true;
    this.limpiarMessages();

    Object.assign(this.selectProveedor, this.tempProveedor);

    const payload ={
      TERWEB : this.selectedProveedor.terweb,
      TEROBS : this.selectedProveedor.terobs,
      TERBLO : this.selectedProveedor.terblo,
      TERACU : this.selectedProveedor.teracu,
      TERTEL : this.selectedProveedor.tertel,
      TERCOE : this.selectedProveedor.tercoe
    }

    this.http.put(`${environment.backendUrl}/api/ter/updateFields/${this.entcod}/${this.selectedProveedor.tercod}`, payload, { responseType: 'text' }
    ).subscribe({
      next: (res) => {
        this.updateSuccess();
        this.messageSuccess = 'Proveedor actualizado correctamente';
        this.isUpdating = false;
      },
      error: (err) => {
        this.messageError = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    });
  }

  onCheckboxGeneric(event: Event, field: string) {
    const checked = (event.target as HTMLInputElement).checked;
    if (this.tempProveedor && field in this.tempProveedor) {
      (this.tempProveedor as any)[field] = checked ? 1 : 0;
    }
    if (this.articulos && field in this.articulos) {
      (this.articulos as any)[field] = checked ? 1 : 0;
    }
  }

  onApracuChange(articulo: any, event: Event) {
    const input = event.target as HTMLInputElement;
    articulo.apracu = input.checked ? 1 : 0;
  }

  //persona related functions
  showPersonasGrid = false;
  personaInfo: any = [];
  showPersonas() {
    this.limpiarMessages();
    this.showPersonasGrid = true;
    this.personaInfo = this.selectedProveedor;
  }

  hidePersonas() {
    this.showPersonasGrid = false;
  }

  showContactPersonsGrid = false;
  contactPersons: any = null;
  activeDetailTab: 'contact' | 'articulo' | null = null;
  personaError: string = '';
  isLoadingPersonas: boolean = false;
  showContactPersons(proveedore: any){
    this.limpiarMessages();
    this.showContactPersonsGrid = true;
    this.activeDetailTab = 'contact';
    this.showArticulosGrid = false;
    this.selectedProveedor = proveedore;
    const tercod = proveedore.tercod;

    this.isLoadingPersonas = true;
    this.http.get<any[]>(`${environment.backendUrl}/api/more/by-tpe/${this.entcod}/${tercod}`)
      .subscribe({ next: (response) => {
        this.contactPersons = response;
        this.isLoadingPersonas = false;
        this.pagePersona = 0;
      },
      error: (err) => {
        this.personaError = err.error.error ?? err.error;
        this.contactPersons = [];
        this.pagePersona = 0;
        this.isLoadingPersonas = false;
      } 
    });
  }
  pagePersona = 0;
  get filteredPersonas() { let filtered = this.contactPersons; return filtered;}
  get totalPagesPersonas(): number { return Math.ceil(this.contactPersons.length / this.pageSize);}
  get paginatedPersonas() {const start = this.pagePersona * this.pageSize; return this.contactPersons.slice(start, start + this.pageSize);}
  nextPagePersona() {if (this.pagePersona + 1 < this.totalPagesPersonas) {this.pagePersona++;}}
  prevPagePersona() {if (this.pagePersona > 0) {this.pagePersona--;}}
  goToPagePersona(event: any) { const inputPage = Number(event.target.value); if (inputPage >= 1 && inputPage <= this.totalPagesPersonas) {this.pagePersona = inputPage - 1;}}

  personasContactoErrorMessage: string = '';
  personasContactoSuccessMessage: string = '';
  personaContactoaddError: string = '';
  isAdding: boolean = false;
  addPersonas(tpenom: string, tpetel: string, tpetmo: string, tpecoe: string, tpeobs: string) {
    this.limpiarMessages();
    this.isAdding = true;
    const tercod = this.personaInfo.tercod;

    if(!tpenom) {
      this.personaContactoaddError = 'Nombre requirido'
      return;
    }

    const payload = {
      "tpenom" : tpenom,
      "tpetel" : tpetel,
      "tpetmo" : tpetmo,
      "tpecoe" : tpecoe,
      "tpeobs" : tpeobs
    }

    this.http.post(`${environment.backendUrl}/api/more/add/${this.entcod}/${tercod}`, payload, { responseType: 'text' }).subscribe({
      next: (res) => {
        this.personasContactoSuccessMessage = 'Persona Agregado exitosamente';
        this.hidePersonas();
        this.reloadContactPersons();
        this.isAdding = false;
      },
      error: (err) => {
        this.personaContactoaddError = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }

  reloadContactPersons() {
    this.limpiarMessages();
    if (!this.selectedProveedor) return;
    this.showContactPersons(this.selectedProveedor);
  }

  isUpdating: boolean = false;
  updatepersonas(tpecod: number, nom: string, telefon: string, movil: string, email: string, obs: string){
    this.isUpdating = true;
    this.limpiarMessages();
    if (!nom) {
      this.personasContactoErrorMessage = 'El nombre de la persona no debe estar vacío';
      return;
    }
    const updateFields = {
      tpenom : nom,
      tpetel : telefon,
      tpetmo : movil,
      tpecoe : email,
      tpeobs : obs
    }
    this.http.put(
      `${environment.backendUrl}/api/more/modify/${this.entcod}/${this.selectedProveedor.tercod}/${tpecod}`,
      updateFields,
      { responseType: 'text' }
    ).subscribe({
      next: (res) => {
        this.personasContactoSuccessMessage = 'Persona de contacto actualizada correctamente';
        this.isUpdating = false;
      },
      error: (err) => {
        this.personasContactoErrorMessage = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    });
  }

  deletepersona(persona: any){
    this.limpiarMessages();
    this.isDeleting = true;
    const tercod = persona.tercod;
    const tpecod = persona.tpecod;
    this.http.delete(
      `${environment.backendUrl}/api/more/delete/${this.entcod}/${tercod}/${tpecod}`,
      { responseType: 'text' }).subscribe({
      next: (res) => {
        this.personasContactoSuccessMessage = 'Persona de contacto eliminada correctamente';
        this.reloadContactPersons();
        this.isDeleting = false;
        this.closeDeletePersonas();
      },
      error: (err) => {
        this.personasContactoErrorMessage = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    });
  }

  showDeletePersona = false;
  personaToDelete: any = null;
  openDeletePersonas(persona: any) {
    this.limpiarMessages();
    this.personaToDelete = persona;
    this.showDeletePersona = true;
  }

  closeDeletePersonas() {
    this.showDeletePersona = false;
    this.personaToDelete = null;
    this.limpiarMessages();
  }

  confirmDeletePersona() {
    if (this.showDeletePersona) {
      this.deletepersona(this.personaToDelete);
    }
  }

  //articulos related functions
  articuloSuccessMessage: string = '';
  showHelloGrid = false;
  selectedFamilia: string = '';
  selectedSubfamilia: string = '';
  selectedArticulo: string = '';
  afas: any[] = [];
  asus: any[] = [];
  arts: any[] = [];
  showHello() {
    this.limpiarMessages();
    this.searchValue = '';
    this.searchResults = [];
    this.searchType = 'familia';
    this.showHelloGrid = true;
    this.selectedFamilia = '';
    this.selectedSubfamilia = '';
    this.selectedArticulo = '';
    this.asus = [];
    this.arts = [];
  }

  hideHello() {
    this.limpiarMessages();
    this.showHelloGrid = false;
    this.searchValue = '';
    this.searchResults = [];
    this.searchType = 'familia';
    this.searchPage;
    this.selectedFamilia = '';
    this.selectedSubfamilia = '';
    this.selectedArticulo = '';
    this.afas = [];
    this.asus = [];
    this.arts = [];
    this.selectedArticulosAdd = [];
  }

  showArticulosGrid = false;
  articulos: any = null;
  articuloError: string = '';
  articulosShowError: string = '';
  isLoadingArticulosDetails: boolean = false;
  showArticulos(proveedore: any){
    this.limpiarMessages();
    this.showArticulosGrid = true;
    this.activeDetailTab = 'articulo';
    this.showContactPersonsGrid = false;
    this.selectedProveedor = proveedore;
    const tercod = proveedore.tercod;
    
    this.isLoadingArticulosDetails = true;
    this.http.get<any[]>(`${environment.backendUrl}/api/more/by-apr/${this.entcod}/${tercod}`)
      .subscribe({ next: (response) => {
        this.articulos = Array.isArray(response) ? response : (response ? [response] : []);

        this.articulos.forEach((row: any, index: number) => {
          const artcod = String(row?.artcod ?? '').trim();
          const afacod = String(row?.afacod ?? '').trim();
          const asucod = String(row?.asucod?? '').trim();
          this.getDescription(row, index, artcod, afacod, asucod);
        });
        this.isLoadingArticulosDetails = false;
        this.pageArticulo = 0;
      },
      error: (err) => {
        this.articulos = [];
        this.pageArticulo = 0;
        this.articulosShowError = err.error.error ?? err.error;
        this.isLoadingArticulosDetails = false;
      } 
    });
  }
  pageArticulo = 0;
  get filteredArticulo() { let filtered = this.articulos; return filtered;}
  get totalPagesArticulos(): number { return Math.ceil(this.articulos.length / this.pageSize);}
  get paginatedArticulos() {const start = this.pageArticulo * this.pageSize; return this.articulos.slice(start, start + this.pageSize);}
  nextPageArticulos() {if (this.pageArticulo + 1 < this.totalPagesArticulos) {this.pageArticulo++;}}
  prevPageArticulos() {if (this.pageArticulo > 0) {this.pageArticulo--;}}
  goToPageArticulos(event: any) { const inputPage = Number(event.target.value); if (inputPage >= 1 && inputPage <= this.totalPagesArticulos) {this.pageArticulo = inputPage - 1;}}

  updatearticulos(articulo: any){
    this.limpiarMessages();
    this.isUpdating = true; 

    const tercod = this.selectedProveedor.tercod;
    const afacod = articulo.afacod;
    const asucod = articulo.asucod;
    const artcud = articulo.artcod;

    const updateFields = {
      aprref : articulo.aprref,
      apruem : articulo.apruem,
      aprobs : articulo.aprobs,
      apracu : articulo.apracu,
      aprpre : articulo.aprpre
    }

    this.http.patch(`${environment.backendUrl}/api/more/update-apr/${this.entcod}/${tercod}/${afacod}/${asucod}/${artcud}`,updateFields,{ responseType: 'text' }).subscribe({
      next: (res) => {
        this.articuloError = '';
        this.articuloSuccessMessage = 'Artículo actualizado correctamente';
        this.isUpdating = false;
      },
      error: (err) => {
        this.articuloSuccessMessage = '';
        this.articuloError = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    });
  }

  isDeleting: boolean = false;
  deletearticulo(articulo: any){
    this.isDeleting = true;
    this.limpiarMessages();
    const params = [
      `ent=${articulo.ent}`,
      `tercod=${articulo.tercod}`,
      `afacod=${articulo.afacod}`,
      `asucod=${articulo.asucod}`,
      `artcod=${articulo.artcod}`
    ].join('&');

    this.http.delete(`${environment.backendUrl}/api/more/delete-apr?${ params}`, { responseType: 'text' }).subscribe({
      next: (res) => {
        this.articuloSuccessMessage = 'Artículo eliminado correctamente';
        this.showArticulos(this.selectedProveedor);
        this.isDeleting = false;
        this.closeDeleteConfirm();
      },
      error: (err) => {
        this.articuloError = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    });
  }

  showDeleteConfirm = false;
  articuloToDelete: any = null;
  openDeleteConfirm(articulo: any) {
    this.articuloToDelete = articulo;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.articuloToDelete = null;
    this.limpiarMessages();
  }

  confirmDelete() {
    if (this.articuloToDelete) {
      this.deletearticulo(this.articuloToDelete);
    }
  }

  anadirErrorMessage: string = '';
  addArticulo(){
    this.limpiarMessages();

    const payload = this.selectedArticulosAdd.map((a: any) => {
      return {
        ent: this.entcod,
        tercod: this.selectedProveedor.tercod,
        afacod: a.afacod || '*',
        asucod: a.asucod || '*',
        artcod: a.artcod || '*'
      }
    });
    if ((this.articulos ?? []).some((a: any) =>
      (a.afacod ?? '*') === payload.afacod &&
      (a.asucod ?? '*') === payload.asucod &&
      (a.artcod ?? '*') === payload.artcod)) {
      this.anadirErrorMessage = 'El artículo ya está en la lista.';
      this.isAdding = false;
      return;
    }

    this.isAdding = true;
    this.http.post<any>(`${environment.backendUrl}/api/more/add-apr`, payload).subscribe({
      next: (res) => {
        this.isAdding = false;
        this.savedNamesArticulo = res?.savedArticulos;
        this.unsavedNamesArticulo = res?.unsavedArticulos;
        this.openArticuloMessagesAdd();
      },
      error: (err) => {
        this.messageGridError = err.error.error ?? err.error;
        this.isAdding = false;
      }
    });
  }

  messageGridError: string = '';
  savedNamesArticulo: any = [];
  unsavedNamesArticulo: any = [];
  articulosMessagesGrid: boolean = false;
  openArticuloMessagesAdd() {
    this.articulosMessagesGrid = true;
  }

  closeArtoculosMessages() {
    this.savedNamesArticulo = [];
    this.unsavedNamesArticulo = [];
    this.articulosMessagesGrid = false;
    this.hideHello();
    this.showArticulos(this.selectedProveedor);
  }

  selectedArticulosAdd: any = [];
  selectArticulo(articulo: any) {
    if (this.selectedArticulosAdd.includes(articulo)) {
      const index = this.selectedArticulosAdd.indexOf(articulo);
      if (index !== -1) {
        this.selectedArticulosAdd.splice(index, 1);
      }
    } else {
      this.selectedArticulosAdd = [...this.selectedArticulosAdd, articulo];
    }
  }

  isArticuloSelected(articulo: any) {
    if (this.selectedArticulosAdd.includes(articulo)) {
      return true;
    } else {
      return false;
    }
  }

  searchType: string = 'familia';
  searchValue: string = '';
  searchResults: any[] = [];
  isLoadingArticulo: boolean = false;
  articuloErrorFetch: string = '';
  onSearch() {
    this.limpiarMessages();
    this.selectedArticulosAdd = [];

    if (!this.searchValue || !this.searchType) {
      this.articuloErrorFetch = 'Introduce algo para buscar';
      return;
    };

    this.isLoadingArticulo = true;
    this.http.get<any[]>(`${environment.backendUrl}/api/afa/fetch-articulos-proveedor/${this.entcod}/${this.searchType}/${this.searchValue}`, { withCredentials: true }).subscribe({
      next: (data) => {
        this.searchResults = Array.isArray(data) ? data : (data ? [data] : []);
        this.isLoadingArticulo = false;
        this.isLoading = false;
        this.searchPage = 0;
      },
      error: (err) => {
        this.isLoadingArticulo = false;
        this.searchResults = [];
        this.articuloErrorFetch = err.error.error ?? err.error;
      }
    });
  }
  searchPage: number = 0;
  searchPageSize: number = 5;
  get paginatedSearchResults() {const start = this.searchPage * this.searchPageSize; return this.searchResults.slice(start, start + this.searchPageSize);}
  get searchTotalPages() {return Math.ceil(this.searchResults.length / this.searchPageSize);}

  limpiarSearchArticulo() {
    this.limpiarMessages();
    this.searchResults = [];
    this.searchType = 'familia';
    this.searchValue = '';
    this.selectedArticulosAdd = [];
  }

  getDescription(row: any, index: number, artcod: string, afacod: string, asucod: string){
    if (artcod === '*' || '') {
      if (asucod === '*' || '') {
        this.http.get<any[]>(`${environment.backendUrl}/api/afa/by-ent/${this.entcod}/${afacod}`).subscribe({
          next: (response) => {
            const respArray = Array.isArray(response) ? response : response ? [response] : [];
            const afades = respArray[0]?.afades;
            row.description = String(afades ?? '').trim();
          },
        });
      } else {
        this.http.get<any[]>(`${environment.backendUrl}/api/asu/art-name/${this.entcod}/${afacod}/${asucod}`).subscribe({
          next: (response) => {
            const respArray = Array.isArray(response) ? response : response ? [response] : [];
            const asudes = respArray[0]?.asudes;
            row.description = String(asudes ?? '').trim();
          },
        });
      }
    } else {
      this.http.get<any[]>(`${environment.backendUrl}/api/art/art-name/${this.entcod}/${afacod}/${asucod}/${artcod}`).subscribe({
        next: (response) => {
          const respArray = Array.isArray(response) ? response : response ? [response] : [];
          const artdes = respArray[0]?.artdes;
          row.description = String(artdes ?? '').trim();
        },
      });
    }
  }

  //adding from webservice proveedores related functions
  showProveedorModal = false;
  anadirProveedorErrorMessage: string = '';
  openProveedorModal(): void {
    this.showProveedorModal = true;
    this.limpiarMessages();
    this.resetProveedorModalState();
  }

  closeProveedorModal(): void {
    this.proveedoresSearchResults = [];
    this.fullProveedoresSearchResults = [];
    this.showProveedorModal = false;
    this.limpiarMessages();
    this.resetProveedorModalState();
    this.clearSelectedProveedores();
  }

  private resetProveedorModalState(): void {
    this.limpiarMessages();
    this.proveedoresSearchPage = 0;

    if (this.fullProveedoresSearchResults && this.fullProveedoresSearchResults.length) {
      this.proveedoresSearchResults = [...this.fullProveedoresSearchResults];
    } else {
      this.proveedoresSearchResults = [];
    }
  }

  searchAdd: string = 'nif';
  searchProveedor: string = '';
  isSearching: boolean = false;
  onProveedorSearchChange(): void {
    const q = (this.searchProveedor || '').toString().trim();
    if (q.length === 0) {
      this.proveedoresSearchResults = [...(this.fullProveedoresSearchResults || [])];
      this.proveedoresSearchPage = 0;
      this.anadirProveedorErrorMessage = '';
    }
  }

  guardarProveedorIssuccess = false;
  proveedoresSearchResults: any[] = [];
  fullProveedoresSearchResults: any[] = [];
  selectedProveediresFromResults: any[] = [];
  onProveedorSearch(){
    this.selectedProveediresFromResults = [];
    this.limpiarMessages();

    const q = this.searchProveedor.toString().trim();
    
    if (!this.searchAdd) {
      this.anadirProveedorErrorMessage = 'Selecciona tipo de búsqueda';
      return;
    }

    if (q.length === 0) {
      this.proveedoresSearchResults = [];
      this.fullProveedoresSearchResults = [];
      this.proveedoresSearchPage = 0;
      this.anadirProveedorErrorMessage = '';
      return;
    }

    this.callProveedorSearchApi(q);
  }

  private callProveedorSearchApi(searchTerm: string): void {
    let nif: string | null = null;
    let nom: string | null = null;
    let codigo: string | null = null;

    if (this.searchAdd === 'nif') {
      nif = searchTerm;
    } else if (this.searchAdd === 'nom') {
      nom = searchTerm;
    } else if (this.searchAdd === 'codigo') {
      codigo = searchTerm;
    }

    this.isSearching = true;
    let apiUrl = `${environment.backendUrl}/api/sical/terceros?`;
    const params: string[] = [];
    if (nif) params.push(`nif=${encodeURIComponent(nif)}`);
    if (nom) params.push(`nom=${encodeURIComponent(nom)}`);
    if (codigo) params.push(`codigo=${encodeURIComponent(codigo)}`);
    params.push(`orgCode=${this.orgCode}`);
    params.push(`entidad=${this.entidad}`);
    params.push(`eje=${this.eje}`);
    apiUrl += params.join('&');

    this.http.get<any[]>(apiUrl).subscribe({
      next: (response) => {
        const mappedResults = (response || []).map(d => ({
          ENT: this.entcod?.toString() || '',
          TERNOM: d.nomTercero ?? '',
          TERALI: d.apellTercero ?? '',
          NIF: d.niftercero ?? d.ternif ?? '',
          TERDOM: d.domicilio ?? '',
          TERCPO: d.codigoPostal ?? '',
          TERTEL: d.telefono ?? '',
          TERFAX: d.fax ?? '',
          TERWEB: d.web ?? '',
          TERCOE: d.email ?? '',
          TEROBS: d.observaciones ?? '',
          PROCOD: d.provincia ?? '',
          TERPOB: d.poblacion ?? '',
          TERAYT: d.idenTercero ?? '',
          TERACU: 0,
          TERBLO: 0,
          __raw: d
        }));
        this.fullProveedoresSearchResults = mappedResults;
        this.proveedoresSearchResults = [...this.fullProveedoresSearchResults];
        this.proveedoresSearchPage = 0;
        this.isSearching = false;
      },
      error: (err) => {
        this.anadirProveedorErrorMessage = err.error.error ?? err.error;
        this.proveedoresSearchResults = [];
        this.fullProveedoresSearchResults = [];
        this.proveedoresSearchPage = 0;
        this.isSearching = false;
      }
    });
  }

  private proveedorKey(p: any): string {
    const ent = (p?.ENT ?? '').toString();
    const ternom = (p?.TERNOM ?? p?.nomTercero ?? '').toString().trim();
    const ternif = (p?.NIF ?? p?.TERNIF ?? p?.niftercero ?? '').toString().trim();
    return `${ent}|${ternom}|${ternif}`;
  }

  isProveedorSelected(p: any): boolean {
    if (!this.selectedProveediresFromResults?.length) return false;
    const k = this.proveedorKey(p);
    return this.selectedProveediresFromResults.some(s => this.proveedorKey(s) === k);
   }

  selectProveedor(item: any){
    const k = this.proveedorKey(item);
    const idx = this.selectedProveediresFromResults.findIndex(s => this.proveedorKey(s) === k);
    if (idx === -1) {
      this.selectedProveediresFromResults.push(item);
    } else {
      this.selectedProveediresFromResults.splice(idx, 1);
    }
  }

  clearSelectedProveedores() {
    this.selectedProveediresFromResults = [];
    this.proveedoresSearchResults = [];
    this.searchProveedor = '';
    this.searchAdd = 'nif';
    this.limpiarMessages();
  }

  guardarMessageProveedor: string = '';
  cargarProveedoresError: string = '';
  saveProveedorees() {
    this.limpiarMessages();
    const ent = this.entcod;

    if (!this.selectedProveediresFromResults || this.selectedProveediresFromResults.length === 0) {
      this.anadirProveedorErrorMessage = 'No hay proveedores seleccionados';
      return;
    }

    const payload = this.selectedProveediresFromResults.map(p => ({
      ENT: ent,
      TERNOM: p.TERNOM ?? '',
      TERALI: p.TERALI ?? '',
      TERNIF: p.NIF ?? '',
      TERDOM: p.TERDOM ?? '',
      TERCPO: p.TERCPO ?? '',
      TERTEL: p.TERTEL ?? '',
      TERFAX: p.TERFAX ?? '',
      TERWEB: p.TERWEB ?? '',
      TERCOE: p.TERCOE ?? '',
      TEROBS: p.TEROBS ?? '',
      PROCOD: (p.TERCPO ?? '').slice(0, 2),
      TERPOB: p.TERPOB ?? '',
      TERAYT: p.TERAYT ?? ''
    }));

    const token = sessionStorage.getItem('JWT');
    const headers = token
      ? new HttpHeaders({ 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' })
      : new HttpHeaders({ 'Content-Type': 'application/json' });

    this.isSaving = true;
    this.limpiarMessages();
    this.http.post<any>(`${environment.backendUrl}/api/ter/save-proveedores/${ent}`, payload, {headers}).subscribe({
      next: (res) => {
        this.isSaving = false;
        this.savedNames = res.savedNames.join(', ') ?? '';
        this.unsavedNames = res?.unsavedNames.join(', ') ?? '';
        this.openProveedoresMessages();
      },
      error: (err) => {
        this.isSaving = false;
        this.cargarProveedoresError = err.error.error ?? err.error;
      }
    })
  }

  savedNames: string[] = [];
  unsavedNames: string[] = [];
  proveedoresMessage: boolean = false;
  openProveedoresMessages() {
    this.proveedoresMessage = true;
  }

  closeProveedoresMessages() {
    this.limpiarMessages();
    this.proveedoresMessage = false;
    this.clearSelectedProveedores();
    this.closeProveedorModal();
  }

  isSaving = false;
  proveedoresSearchPage: number = 0;
  proveedoresSearchPageSize: number = 10;
  get paginatedProveedoresSearchResults() {
    const start = this.proveedoresSearchPage * this.proveedoresSearchPageSize;
    return this.proveedoresSearchResults.slice(start, start + this.proveedoresSearchPageSize);
  }

  get proveedoresSearchTotalPages() {
    return Math.ceil(this.proveedoresSearchResults.length / this.proveedoresSearchPageSize) || 1;
  }

  proveedoresSearchPrev() {
    if (this.proveedoresSearchPage > 0) this.proveedoresSearchPage--;
  }

  proveedoresSearchNext() {
    if (this.proveedoresSearchPage + 1 < this.proveedoresSearchTotalPages) this.proveedoresSearchPage++;
  }

  //main table width adjustment related functions
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

  //updating a proveedor
  updateProveedor: boolean = false;
  updateProveedorMessage: string = '';
  openUpdateProveedor() {
    this.updateProveedor = true;
  }

  closeUpdateProvedor() {
    this.updateProveedor = false;
    this.proveedorInfo = [];
  }

  updateProveedorMessageSuccess: string = '';
  proveedorInfo: any = []
  isUpdatingProveedor: boolean = false;
  getUpdatedInfo() {
    this.limpiarMessages();
    const terayt = this.selectedProveedor.terayt;
    if (terayt === null || terayt === '' || terayt == 0) {
      this.updateProveedorMessage = 'Falta código de contabilidad';
      this.updatingAnimation = true;
      return;
    }
    
    this.isUpdatingProveedor = true;
    this.updatingAnimation = true;
    this.http.get<any>(`${environment.backendUrl}/api/sical/terceros?codigo=${terayt}&orgCode=${this.orgCode}&entidad=${this.entidad}&eje=${this.eje}`).subscribe({
      next: (res) => {
        if (res.length === 0) {
          this.updateProveedorMessage = 'No se encuentra el código en de contabilidad';
          this.isUpdatingProveedor = false;
          return;
        }
        this.proveedorInfo = res;
        this.actualizarProveedor();
      },
      error: (err) => {
        this.proveedorInfo = [];
        this.isUpdatingProveedor = false;
        this.updateProveedorMessage = err.error.error ?? err.error;
      }
    })
  }

  actualizarProveedor() {
    const proveedor = this.selectedProveedor.tercod;
    const procod = this.proveedorInfo[0]?.codigoPostal?.slice(0, 2);
    const payload = {
      "TERNOM": this.proveedorInfo[0].nomTercero,
      "TERALI": this.proveedorInfo[0].nombreCompleto,
      "TERNIF":	this.proveedorInfo[0].niftercero,
      "TERDOM": this.proveedorInfo[0].domicilio,
      "TERCPO": this.proveedorInfo[0].codigoPostal,
      "TERFAX": this .proveedorInfo[0].fax,
      "PROCOD": procod,
      "TERPOB": this.proveedorInfo[0].poblacion,
      "TERAYT": Number(this.proveedorInfo[0].idenTercero)
    }

    this.http.post<any>(`${environment.backendUrl}/api/ter/actualizar-proveedor/${this.entcod}/${proveedor}`, payload).subscribe({
      next: (res) => {
        this.closeUpdateProvedor();
        this.updatingProveedorDetail();
        this.isUpdatingProveedor = false;
      },
      error: (err) => {
        this.updateProveedorMessage = err.error.error ?? err.error;
        this.isUpdatingProveedor = false;
      }
    })
  }

  updatingAnimation: boolean = false;
  newProveedorDetails: any = [];
  updatingProveedorDetail() {
    this.limpiarMessages();
    const tercod = this.selectedProveedor.tercod;

    this.http.get(`${environment.backendUrl}/api/ter/proveedorDetail/${this.entcod}/${tercod}`).subscribe({
      next: (res) => {
        this.updateProveedorMessageSuccess = 'El proveedor actualizó correctamente';
        this.newProveedorDetails = res;
        const index = this.proveedores.findIndex(
          p => p.tercod === this.newProveedorDetails.tercod
        );

        if (index !== -1) {
          this.proveedores[index] = this.newProveedorDetails;
        }
        this.tempProveedor = res;
        this.selectedProveedor = res;
        this.paginatedProveedores;
      },
      error: (err) => {
        this.messageError = err.error.error ?? err.error;
      }
    })
  }

  cerrarMessageActualizar() {
    this.limpiarMessages();
    this.closeUpdateProvedor()
    this.updatingAnimation = false;
  }

  //mist
  limpiarMessages() {
    this.error = '';
    this.messageError = '';
    this.messageSuccess = '';
    this.personasContactoErrorMessage = '';
    this.personasContactoSuccessMessage = '';
    this.articuloError = '';
    this.articuloSuccessMessage = '';
    this.personaContactoaddError = '';
    this.anadirErrorMessage = '';
    this.guardarMessageProveedor = '';
    this.anadirProveedorErrorMessage = '';
    this.articulosShowError = '';
    this.personaError = '';
    this.cargarProveedoresError = '';
    this.articuloErrorFetch = '';
    this.messageGridError = '';
    this.updateProveedorMessage = '';
    this.updateProveedorMessageSuccess = '';
  }
}
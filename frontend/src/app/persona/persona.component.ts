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
  selector: 'app-persona',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent ],
  templateUrl: './persona.component.html',
  styleUrls: ['./persona.component.css']
})
export class PersonaComponent {
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
  private perfil: string | null = null;
  personas: any[] = [];
  private backuppersonas: any[] = [];
  private defaultpersonas: any[] = [];
  personasMessageSuccess: string = '';
  personasMessageError: string = '';
  page = 0;
  pageSize = 20;
  isLoading: boolean = false;

  ngOnInit(): void {
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const percod = sessionStorage.getItem('Perfil');
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}
    if (percod) {const parsed = JSON.parse(percod); this.perfil = parsed.perfil}

    if (!entidad || this.entcod === null || !eje || this.eje === null || this.perfil === null ) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }
    this.fetchPersonas();
  }

  //main table functions
  fetchPersonas() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/Per/fetch-all`).subscribe({
      next: (res) => {
        this.personas = res;
        this.backuppersonas = Array.isArray(res) ? [...res] : [];
        this.defaultpersonas = [...this.backuppersonas];
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.personasMessageError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  toggleSort(field: 'percod' | 'pernom' | 'percoe' | 'pertel' | 'pertmo' | 'percar' | 'perobs'): void {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDirection = 'asc';
    } else if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortField = null;
      this.sortDirection = 'asc';
      this.personas = [...this.defaultpersonas];
      this.page = 0;
      this.updatePagination();
      return;
    }

    this.applySort();
  }

  sortField: 'percod' | 'pernom' | 'percoe' | 'pertel' | 'pertmo' | 'percar' | 'perobs' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const sorted = [...this.personas].sort((a, b) => {
      const extract = (item: any, prop: string) =>
        (item?.[prop] ?? item?.[prop.toUpperCase()] ?? '').toString();


      const aVal = extract(a, field);
      const bVal = extract(b, field);
      return this.sortDirection === 'asc'
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
    });

    this.personas = sorted;
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

  get paginatedPersonas(): any[] {
    if (!this.personas || this.personas.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.personas.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.personas?.length ?? 0) / this.pageSize));
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

  excelDownload() {
    this.limpiarMessages(); 
    const rows = this.personas;
    if (!rows || rows.length === 0) {
      this.personasMessageError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Código: row.percod ?? '',
      Nombre: row.pernom ?? '',
      Correo_Electrónico: row.percoe ?? '',
      Teléfono: row.pertel ?? '',
      Cargo: row.percar ?? '',
      Observaciones: row.perobs ?? '',
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de Personas']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Código', 'Nombre', 'Correo_Electrónico', 'Teléfono', 'Cargo', 'Observaciones']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 30 },
      { wch: 40 },
      { wch: 35 },
      { wch: 20 },
      { wch: 40 },
      { wch: 45 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Personas');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Personas.xlsx'
    );
  }

  exportPdf() {
    this.limpiarMessages();
    const source = this.personas;
    if (!source?.length) {
      this.personasMessageError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      percod: row.percod ?? '',
      pernom: row.pernom ?? '',
      percoe: row.percoe ?? '',
      pertel: row.pertel ?? '',
      pertmo: row.pertmo ?? '',
      percar: row.percar ?? '',
      perobs: row.perobs ?? ''
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de Personas', 40, 40);

    const columns = [
      { header: 'Código', dataKey: 'percod' },
      { header: 'Nombre', dataKey: 'pernom' },
      { header: 'Correo electrónico', dataKey: 'percoe' },
      { header: 'Teléfono', dataKey: 'pertel' },
      { header: 'Móvil', dataKey: 'pertmo' },
      { header: 'Cargo', dataKey: 'percar' },
      { header: 'Observaciones', dataKey: 'perobs' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('personas.pdf');
  }

  searchPersonas: string = '';
  search() {
    this.isLoading = true;
    this.limpiarMessages();
    if(this.searchPersonas === '') {
      this.fetchPersonas();
      this.isLoading = false;
      return 
    }

    if(this.searchPersonas.length <= 20) {
      this.http.get<any>(`${environment.backendUrl}/api/Per/search-cod-nom/${this.searchPersonas}`).subscribe({
        next: (res) => {
          this.personas = res;
          this.backuppersonas = Array.isArray(res) ? [...res] : [];
          this.defaultpersonas = [...this.backuppersonas];
          this.page = 0;
          this.updatePagination();
          this.isLoading = false;
          if (!res.length) {
            this.personasMessageError = 'No se encontraron servicios con los filtros dados.';
          }
        },
        error: (err) => {
          this.personas = [];
          this.backuppersonas = [];
          this.defaultpersonas = [];
          this.page = 0;
          this.updatePagination();
          this.personasMessageError = err.error.error ?? err.error;
          this.isLoading = false;
        }
      });
    } else {
      this.http.get<any>(`${environment.backendUrl}/api/Per/search-nom/{this.searchPersonas}`).subscribe({
        next: (res) => {
          this.personas = res;
          this.backuppersonas = Array.isArray(res) ? [...res] : [];
          this.defaultpersonas = [...this.backuppersonas];
          this.page = 0;
          this.updatePagination();
          this.isLoading = false;
          if (!res.length) {
            this.personasMessageError = 'No se encontraron servicios con los filtros dados.';
          }
        },
        error: (err) => {
          this.personas = [];
          this.backuppersonas = [];
          this.defaultpersonas = [];
          this.page = 0;
          this.updatePagination();
          this.personasMessageError = err.error.error ?? err.error;
          this.isLoading = false;
        }
      });
    }
  }

  limpiarSearch() {
    this.searchPersonas = '';
    this.fetchPersonas();
    this.limpiarMessages();
  }

  //detail grid functions
  selectedPersona: any = null;
  detailMessageSuccess: string = '';
  detailMessageError: string = '';
  showDetails(persona: any) {
    this.limpiarMessages();
    this.selectedPersona = persona;
    this.tempPersonas = persona;
    this.showServices(persona);
  }

  closeDetails() {
    this.selectedPersona = null
    this.personServicesOrigin = [];
    this.showSerivcesGrid = false;
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }

  tempPersonas: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
      this.backupData = this.selectedPersona ? { ...this.selectedPersona } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempPersonas = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed(pernom: string, percoe: string, pertel: string, pertmo: string, percar: string, perobs: string) {
    if (this.allowToUpdate) {
      this.updatePersona(pernom, percoe, pertel, pertmo, percar, perobs);
    } else {
      return;
    }
  }

  isUpdating: boolean = false;
  updatePersona(pernom: string, percoe: string, pertel: string, pertmo: string, percar: string, perobs: string) {
    this.isUpdating = true;
    this.limpiarMessages();
    Object.assign(this.selectedPersona, this.tempPersonas);

    if(!pernom) {this.detailMessageError = 'Nombre requerido'; return;}

    const payload = {
      "PERNOM" : pernom,
      "PERCOE" : percoe,
      "PERTEL" : pertel,
      "PERTMO" : pertmo,
      "PERCAR" : percar,
      "PEROBS" : perobs,
      "PERCOD" : this.selectedPersona.percod,
    }

    this.http.patch(`${environment.backendUrl}/api/Per/update-persona`, payload).subscribe({
      next: (res) => {
        this.detailMessageSuccess = 'La persona se ha actualizado correctamente';
        this.isUpdating = false;
        this.updateSuccess();
      },
      error: (err) => {
        this.detailMessageError = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    })
  }

  //services grid functions
  showSerivcesGrid = false;
  personServicesOrigin: any = [];
  serviceErrorMessage: string = '';
  serviceSuccessMessage: string = '';
  showServices(persona: any) {
    this.limpiarMessages();
    this.showSerivcesGrid = true;
    const percod = persona.percod;
    this.fetchServices(percod);
  }

  pageServices: number = 0;
  fetchServices(percod: string) {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/depe/fetch-persona-service/${this.entcod}/${this.eje}/${percod}`).subscribe({
      next: (res) => {
        this.personServicesOrigin = res;
        this.pageServices = 0;
        this.isLoading = false;
      },
      error: (err) => {
        this.personServicesOrigin = [];     
        this.pageServices = 0;              
        this.serviceErrorMessage = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  get paginatedServices(): any[] {
    if (!this.personServicesOrigin || this.personServicesOrigin.length === 0) return [];
    const start = this.pageServices * this.pageSize;
    return this.personServicesOrigin.slice(start, start + this.pageSize);
  }
  get totalPagesServices(): number {
    return Math.max(1, Math.ceil((this.personServicesOrigin?.length ?? 0) / this.pageSize));
  }
  prevPageService(): void {
    if (this.pageServices > 0) this.pageServices--;
  }
  nextPageService(): void {
    if (this.pageServices < this.totalPages - 1) this.pageServices++;
  }
  goToPageService(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {
      this.pageServices = inputPage - 1;
    }
  }

  serviceToDelete: any = [];
  showDeleteGrid: boolean = false;
  showDelete(service: any) {
    this.limpiarMessages();
    this.serviceToDelete = service;
    this.showDeleteGrid = true;
  }

  closeDelete() {
    this.showDeleteGrid = false;
  }

  ErrorDelete: string = '';
  isDeleting: boolean = false;
  deleteService(depcod: string) {
    this.isDeleting = true;
    if(!depcod || !this.selectedPersona.percod) { return;}
    const persona = this.selectedPersona.percod;
    const service = depcod

    this.http.delete(`${environment.backendUrl}/api/depe/delete-service-persona/${this.entcod}/${this.eje}/${service}/${persona}`).subscribe({
      next: (res) => {
        this.serviceSuccessMessage = 'El servicio se ha eliminado correctamente'
        this.fetchServices(persona);
        this.closeDelete();
        this.isDeleting = false;
      },
      error: (err) => {
        this.ErrorDelete = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    })
  }

  addService: boolean = false;
  showAddService() {
    this.limpiarMessages();
    this.addService = true;
    this.servicesAddError = 'No hay servicios'
  }

  closeAddService() {
    this.addService = false;
    this.clearSearch();
  }

  services: any = [];
  servicesAddError: string = '';
  isLoadingServices: boolean = false;

  pageAdd: number = 0;
  get paginatedServicesAdd(): any[] {
    if (!this.services || this.services.length === 0) return [];
    const start = this.pageAdd * this.pageSize;
    return this.services.slice(start, start + this.pageSize);
  }
  get totalPagesAdd(): number {
    return Math.max(1, Math.ceil((this.services?.length ?? 0) / this.pageSize));
  }
  prevPageAdd(): void {
    if (this.pageAdd > 0) this.pageAdd--;
  }
  nextPageAdd(): void {
    if (this.pageAdd < this.totalPages - 1) this.pageAdd++;
  }
  goToPageAdd(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {
      this.page = inputPage - 1;
    }
  }

  setInputToUpper(event: Event):void {
    const target = event.target as HTMLTextAreaElement;
    const upper = (target.value ?? '').toUpperCase();
    target.value = upper;
  }

  searchServicio: string = '';
  searchCentroGestor: string = '';
  searchPerfil: string = 'todos';
  searchServices() {
    this.limpiarMessages();
    const params: any = {
      ent: this.entcod,
      eje: this.eje,
      percod: this.perfil
    };
    if (this.searchServicio && this.searchServicio.trim() !== '') {
      params.search = this.searchServicio;
    }
    if (this.searchCentroGestor && this.searchCentroGestor.trim() !== '') {
      params.cgecod = this.searchCentroGestor;
    }
    if (this.searchPerfil && this.searchPerfil !== 'todos') {
      params.perfil = this.searchPerfil;
    }

    this.isLoadingServices = true;
    this.http.get<any>(`${environment.backendUrl}/api/dep/search`, { params }).subscribe({
      next: (res) => {
        this.services = res;
        this.page = 0;
        this.isLoadingServices = false;
        this.updatePagination();
        if (!res.length) {
          this.servicesAddError = 'No se encontraron servicios con los filtros dados.';
        }
      },
      error: (err) => {
        this.isLoadingServices = false;
        this.services = [];
        this.pageAdd = 0;
        this.servicesAddError = err.error.error ?? err.error;
      }
    });
  }

  clearSearch() {
    this.searchServicio = '';
    this.searchCentroGestor = '';
    this.searchPerfil = 'todos';
    this.limpiarMessages();
    this.services = []
    this.linesSelected = [];
    this.count = 0;
     this.servicesAddError = 'No hay servicios'
  }

  linesSelected: string[] = [];
  selectService(service: string) {
    if(this.linesSelected.includes(service)) {
      this.linesSelected = this.linesSelected.filter((s) => s !== service);
    } else {
      this.linesSelected = [...this.linesSelected, service];
    }
    
    this.getLinesAdded()
  }

  count: number = 0;
  getLinesAdded(){
    this.count = this.linesSelected.length;
  }
  
  isAddingservices: boolean = false;
  cargarServicesError: string = '';
  addServicePersona(services: string[]) {
    if(!services) {this.servicesAddError = 'Debe seleccionar al menos un servicio.'; return;}

    const payload = {
      "ent": this.entcod,
      "eje": this.eje,
      "percod": this.selectedPersona.percod,
      "services": services
    }

    this.isAddingservices = true;
    this.http.post<any>(`${environment.backendUrl}/api/depe/add-persona-services`, payload).subscribe({
      next: (res) => {
        this.isAddingservices = false;
        this.savedNames = res.savedNames.join(', ') ?? '';
        this.unsavedNames = res?.unsavedNames.join(', ') ?? '';
        this.openServicesMessages();
      },
      error: (err) => {
        this.isAddingservices = false;
        this.cargarServicesError = err.error.error ?? err.error;
      }
    })
  }

  savedNames: string[] = [];
  unsavedNames: string[] = [];
  servicesMessages: boolean = false;
  openServicesMessages() {
    this.servicesMessages = true;
  }

  closeServicesMessages() {
    this.limpiarMessages();
    this.fetchServices(this.selectedPersona.percod);
    this.servicesMessages = false;
    this.closeAddService();
  }

  //add personas grid functions
  showAddConfirm: boolean = false;
  PersonaErrorMessage: string = '';
  showAdd() {
    this.limpiarMessages();
    this.showAddConfirm = true;
  }

  closeAddConfirm() {
    this.showAddConfirm = false;
  }

  isAdding: boolean = false;
  addPersona(code: string, name: string, email: string, phone: string, movil: string, trans: string, obs: string) {
    this.isAdding = true;
    if(!code || !name) {
      this.PersonaErrorMessage = 'Código y Nombre requerido'
      return;
    }

    const payload = {
      "PERCOD" : code,
      "PERNOM" : name,
      "PERCOE" : email,
      "PERTEL" : phone,
      "PERTMO" : movil,
      "PERCAR" : trans,
      "PEROBS" : obs
    }

    this.http.post(`${environment.backendUrl}/api/Per/Insert-persona`, payload).subscribe({
      next: (res) => {
        this.personasMessageSuccess = 'La persona se agregó correctamente';
        this.fetchPersonas();
        this.closeAddConfirm();
        this.isAdding = false;
      },
      error: (err) => {
        this.PersonaErrorMessage = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }

  forceUppercase(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase();
  }

  //compiar grid functions
  compiarPersona: boolean = false;
  gridMessag: string = '';
  personServices: any = [];
  showCopiar(percod: string) {
    this.limpiarMessages();
    this.gridMessag = 'La persona activa tiene servicios asignados. Se borrarán para añadir los de la persona seleccionada. ¿Quiere seguir?';
    this.compiarPersona = true;
  }

  closeCopiar() {
    this.compiarPersona = false;
    this.personServices = [];
  }

  compiarPersonaGrid: boolean = false;
  showCopiarPersonas() {
    this.limpiarMessages();
    this.compiarPersonaGrid = true;
    this.closeCopiar();
    this.errorCopy = 'No hay personas';
  }
  closeShowCopiarPersonas() {
    this.compiarPersonaGrid = false;
    this.limpiarSearcCopy();
  }

  pageCopy: number = 0;
  pesonasCopy: any[] = [];
  errorCopy: string = '';

  get paginatedPersonasCopy(): any[] {
    if (!this.pesonasCopy || this.pesonasCopy.length === 0) return [];
    const start = this.pageCopy * this.pageSize;
    return this.pesonasCopy.slice(start, start + this.pageSize);
  }
  get totalPagesCopy(): number {
    return Math.max(1, Math.ceil((this.pesonasCopy?.length ?? 0) / this.pageSize));
  }
  prevPageCopy(): void {
    if (this.pageCopy > 0) this.pageCopy--;
  }
  nextPageCopy(): void {
    if (this.pageCopy < this.totalPages - 1) this.pageCopy++;
  }
  goToPageCopy(event: any): void {
    const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.totalPages) {
      this.pageCopy = inputPage - 1;
    }
  }

  searchPersonasCopy: string = '';
  isLoadingPersonasAdd: boolean = false;
  searchCopy() {
    this.limpiarMessages();
    if(this.searchPersonasCopy === '') {return}

    if(this.searchPersonasCopy.length <= 20) {
      this.isLoadingPersonasAdd = true;
      this.http.get<any>(`${environment.backendUrl}/api/Per/search-cod-nom/${this.searchPersonasCopy}`).subscribe({
        next: (res) => {
          this.isLoadingPersonasAdd = false;
          this.pesonasCopy = res;
          this.pageCopy = 0;
        },
        error: (err) => {
          this.isLoadingPersonasAdd = false;
          this.pesonasCopy = [];
          this.pageCopy = 0;
          this.errorCopy = err.error.error ?? err.error;
        }
      });
    } else {
      this.isLoadingPersonasAdd = true;
      this.http.get<any>(`${environment.backendUrl}/api/Per/search-nom/{this.searchPersonasCopy}`).subscribe({
        next: (res) => {
          this.isLoadingPersonasAdd = false;
          this.pesonasCopy = res;
          this.pageCopy = 0;
        },
        error: (err) => {
          this.isLoadingPersonasAdd = false;
          this.pesonasCopy = [];
          this.pageCopy = 0;
          this.errorCopy = err.error.error ?? err.error;
        }
      });
    }
  }

  limpiarSearcCopy() {
    this.searchPersonasCopy = '';
    this.pesonasCopy = [];
    this.errorCopy = 'No hay personas';
  }

  continueAdCheckGrid: boolean = false;
  continueMessag: string = '';
  isAvailable: boolean = false;
  percodCopyTo: string = '';
  percodOrigin: string = ''
  showcontinueAdCheckGrid(info: any) {
    this.limpiarMessages();
    this.continueAdCheckGrid = true;
    this.closeShowCopiarPersonas()

    this.percodCopyTo = info.percod;
    this.percodOrigin = this.selectedPersona.percod;
    if (this.percodCopyTo === this.percodOrigin) {
      this.isAvailable = true;
      this.continueMessag = 'Ha seleccionado la misma persona que está editando'
      return;
    }

    this.isAvailable = false;
    this.continueMessag = 'Se van a borrar los servicios asignados y añadir los de la persona seleccionada. ¿Quiere seguir?'
    this.fetchAndStoreServiceCodes(this.percodOrigin);
  }

  closecontinueAdCheckGrid() {
    this.continueAdCheckGrid = false;
  }

  copiedServiceCodes: string[] = [];
  fetchAndStoreServiceCodes(percod: string) {
    this.http.get<any>(`${environment.backendUrl}/api/depe/fetch-persona-service/${this.entcod}/${this.eje}/${percod}`).subscribe({
      next: (res) => {
        this.copiedServiceCodes = Array.isArray(res) ? res.map((s: any) => s.depcod) : [];
      },
      error: (err) => {
        this.copiedServiceCodes = [];
        this.detailMessageError = err.error.error ?? err.error;
      }
    });
  }

  copyPerfil() {
    this.isAdding = true;
    this.limpiarMessages();
    if (!this.percodOrigin || !this.percodCopyTo) {this.detailMessageError = 'datos faltantes'; return;}
  
    const payload = {
      "ent": this.entcod,
      "eje": this.eje,
      "percod": this.percodCopyTo,
      "services": this.copiedServiceCodes
    }

    this.http.post(`${environment.backendUrl}/api/depe/add-persona-services`, payload).subscribe({
      next: (res) => {
        this.personasMessageSuccess = 'Los servicios se han añadido correctamente';

        this.http.delete(`${environment.backendUrl}/api/depe/delete-persona-Allservice/${this.entcod}/${this.eje}/${this.percodOrigin}`).subscribe({
          next: (res) => {
          this.fetchPersonas();
          this.closecontinueAdCheckGrid();
          this.closeDetails();
          this.isAdding = false;
          },
          error: (err) => {
            this.detailMessageError = err?.error;
            this.closecontinueAdCheckGrid();
            this.isAdding = false;
            return;
          }
        })
      },
      error: (err) => {
        this.detailMessageError = err.error.error ?? err.error;
        this.closecontinueAdCheckGrid()
        this.closeDetails();
        return;
      }
    })
  }

  //misc
  limpiarMessages() {
    this.personasMessageSuccess = '';
    this.personasMessageError = '';
    this.detailMessageSuccess = '';
    this.detailMessageError = '';
    this.serviceErrorMessage = '';
    this.serviceSuccessMessage = '';
    this.PersonaErrorMessage = '';
    this.ErrorDelete = '';
    this.servicesAddError = '';
    this.errorCopy = '';
  }
}
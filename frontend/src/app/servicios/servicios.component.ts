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
  selector: 'app-servicios',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent ],
  templateUrl: './servicios.component.html',
  styleUrls: ['./servicios.component.css']
})
export class ServiciosComponent {
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
  usucod: string | null = null;
  services: any[] = [];
  private backupServices: any[] = [];
  private defaultServices: any[] = [];
  servicesMessageSuccess: string = '';
  servicessMessageError: string = '';
  page = 0;
  pageSize = 20;
  isLoading: boolean = false;

  ngOnInit() {
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const percod = sessionStorage.getItem('Perfil');
    const user = sessionStorage.getItem('USUCOD');

    if (entidad) {const parsed = JSON.parse(entidad);this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje);this.eje = parsed.eje;}
    if(percod) {const parsed = JSON.parse(percod);this.perfil = parsed.PERCOD;}
    if (user) { this.usucod = user;}

    if (!entidad || this.entcod === null || !eje || this.eje === null || !percod) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }
    this.fetchServices();
  }

  //main table functions
  private fetchServices(): void {
    this.isLoading = true;
    if (this.entcod === null || this.eje === null) return;
    this.http.get<any>(`${environment.backendUrl}/api/dep/fetch-services/${this.entcod}/${this.eje}`).subscribe({
      next: (res) => {
        this.services = res;
        this.backupServices = Array.isArray(res) ? [...res] : [];
        this.defaultServices = [...this.backupServices];
        this.page = 0;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (err) => {
        this.servicessMessageError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  toggleSort(field: 'depcod' | 'depdes' | 'cgecod' | 'ccocod' | 'depalm' | 'depcom' | 'depint'): void {
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

  sortField: 'depcod' | 'depdes' | 'cgecod' | 'ccocod' | 'depalm' | 'depcom' | 'depint' | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  private applySort(): void {
    if (!this.sortField) {
      return;
    }

    const field = this.sortField;
    const collator = new Intl.Collator('es', { numeric: true, sensitivity: 'base' });

    const sorted = [...this.services].sort((a, b) => {
      const extract = (item: any, prop: string) =>
        (item?.[prop] ?? item?.[prop.toUpperCase()] ?? '').toString();

      if (field === 'depcod') {
        const aVal = extract(a, 'depcod');
        const bVal = extract(b, 'depcod');
        return this.sortDirection === 'asc'
          ? collator.compare(aVal, bVal)
          : collator.compare(bVal, aVal);
      }

      const aVal = extract(a, field);
      const bVal = extract(b, field);
      return this.sortDirection === 'asc'
        ? collator.compare(aVal, bVal)
        : collator.compare(bVal, aVal);
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

  get paginatedServices(): any[] {
    if (!this.services || this.services.length === 0) return [];
    const start = this.page * this.pageSize;
    return this.services.slice(start, start + this.pageSize);
  }
  get totalPages(): number {
    return Math.max(1, Math.ceil((this.services?.length ?? 0) / this.pageSize));
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
      this.servicessMessageError = 'No hay datos para exportar.';
      return;
    }
  
    const exportRows = rows.map((row, index) => ({
      Servicio: row.depcod ?? '',
      Descripción: row.depdes ?? '',
      centro_gestor: row.cgecod ?? '',
      Centro_de_Coste: row.ccocod ?? '',
      Almacén: this.checkIfChecked(row.depalm) ?? '',
      Comprador_Or_Farmacia: this.checkIfChecked(row.depcom) ?? '',
      Contabilidad: this.checkIfChecked(row.depint) ?? ''
    }));
  
    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['listas de servicios']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Servicio', 'Descripción', 'Cód. C.G.', 'Centro de Coste', 'Almacén', 'Comprador/Farmacia', 'Contabilidad']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 15 },
      { wch: 45 },
      { wch: 12 },
      { wch: 15 },
      { wch: 16 },
      { wch: 20 },
      { wch: 16 }
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Servicios');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'servicios.xlsx'
    );
  }

  exportPdf() {
    this.limpiarMessages();
    const source = this.services;
    if (!source?.length) {
      this.servicessMessageError = 'No hay datos para exportar.';
      return;
    }

    const rows = source.map((row: any, index: number) => ({
      depcod: row.depcod ?? '',
      depdes: row.depdes ?? '',
      cgecod: row.cgecod ?? '',
      ccocod: row.ccocod ?? '',
      depalm: this.checkIfChecked(row.depalm),
      depcom: this.checkIfChecked(row.depcom),
      depint: this.checkIfChecked(row.depint)
    }));

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Listado de servicios', 40, 40);

    const columns = [
      { header: 'Servicio', dataKey: 'depcod' },
      { header: 'Descripción', dataKey: 'depdes' },
      { header: 'Cód. C.G.', dataKey: 'cgecod' },
      { header: 'Centro de Coste', dataKey: 'ccocod' },
      { header: 'Almacén', dataKey: 'depalm' },
      { header: 'Comprador/Farmacia', dataKey: 'depcom' },
      { header: 'Contabilidad', dataKey: 'depint' }
    ];

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map(row => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' }
    });

    doc.save('servicios.pdf');
  }

  searchServicio: string = '';
  searchCentroGestor: string = '';
  searchPerfil: string = 'todos';
  search() {
    this.limpiarMessages();
    this.isLoading = true;

    if (this.searchServicio === '' && this.searchCentroGestor === '' && this.searchPerfil === 'todos') {
      this.fetchServices();
      this.isLoading = false;
      return;
    }

    const params: any = {
      ent: this.entcod,
      eje: this.eje,
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

    this.http.get<any>(`${environment.backendUrl}/api/dep/search`, { params }).subscribe({
      next: (res) => {
        this.services = res;
        this.backupServices = Array.isArray(res) ? [...res] : [];
        this.defaultServices = [...this.backupServices];
        this.page = 0;
        this.updatePagination();
        if (!res.length) {
          this.servicessMessageError = 'No se encontraron servicios con los filtros dados.';
          this.isLoading = false;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.services = [];
        this.backupServices = [];
        this.defaultServices = [];
        this.page = 0;
        this.updatePagination();
        this.servicessMessageError = err.error.error ?? err.error;
        this.isLoading = false;
      }
    });
  }

  clearSearch() {
    this.searchServicio = '';
    this.searchCentroGestor = '';
    this.searchPerfil = 'todos';
    this.limpiarMessages();
    this.fetchServices();
  }

  //detail grid functions
  selectedService: any = null;
  servicesDetailError: string = '';
  servicesDetailSuccess: string = '';
  detailFlags = { depalm: false, depcom: false, depint: false };
  showDetails(services: any) {
    this.limpiarMessages();
    this.selectedService = services;
    this.tempServicio = services;
    
    this.detailFlags = {
      depalm: services?.depalm === 1,
      depcom: services?.depcom === 1,
      depint: services?.depint === 1
    };

    const cgecod = this.selectedService.cgecod;
    this.fetchServiceDescription(cgecod);

    this.option = 'personas';
    this.fetchPersonas(services.depcod);
  }

  fetchServiceDescription(cgecod: string) {
    this.http.get(`${environment.backendUrl}/api/cge/fetch-description-services/${this.entcod}/${this.eje}/${cgecod}`, { responseType: 'text' }
    ).subscribe({
      next: (res) => {
        const description = res;
        this.selectedService = {... this.selectedService, cgedes: description};
        this.tempServicio = {... this.tempServicio, cgedes: description};
      },
      error: (err) => {
        this.servicesDetailError = err.error.error ?? err.error;
      }
    })
  }

  isAccesible: boolean = false;
  isAlmacenAccesible(depalm: number) {
    if (depalm != 1) {
      this.isAccesible = this.isAccesible;
    } else if (depalm = 1) {
      this.isAccesible = !this.isAccesible;
    }

    return this.isAccesible;
  }

  toggleDetailFlag(field: 'depalm' | 'depcom' | 'depint', value: boolean): void {
    this.detailFlags = { ...this.detailFlags, [field]: value };
  }

  closeDetails() {
    this.selectedService = null;
    this.tempServicio = null;
    this.limpiarMessages();
    this.personas = [];
    this.almacenArray = [];
    this.almacenDatosArray = [];
  }

  closeDetailsSure() {if (this.isUpdate) {return;} 
    else {this.closeDetails();}
  }
  
  option: 'personas' | 'almacen' = 'personas';
  setOption(next: 'personas' | 'almacen'): void {
    this.option = next;
    this.fetchPersonas(this.selectedService.depcod);
  }

  tempServicio: any = {};
  isUpdate: boolean = false;
  backupData: any = [];
  modificar() {
    this.isUpdate = true;
    this.backupData = this.selectedService ? { ...this.selectedService } : {};
  }

  cancelar() {
    this.isUpdate = false;
    this.tempServicio = { ...this.backupData };
  }

  updateSuccess() {
    this.isUpdate = false;
    this.allowToUpdate = false;
  }

  allowToUpdate: boolean = false;
  isUpdateAllowed(cod:string, des: string) {
    if (this.allowToUpdate) {
      this.updateService(cod, des);
    } else {
      return;
    }
  }

  updateServiceSuccessMessage: string = '';
  updateServiceErrorMessage: string = ''
  isUpdating: boolean = false;
  updateService(cod:string, des: string) {
    this.isUpdating = true;
    this.limpiarMessages();

    Object.assign(this.selectedService, this.tempServicio);

    if (cod === '' || des === '') {
      this.updateServiceErrorMessage = 'Todos los campos son obligatorios.'
      return;
    }

    const payload = {
      depdes: des,
      depalm: this.detailFlags.depalm ? 1 : 0,
      depcom: this.detailFlags.depcom ? 1 : 0,
      depint: this.detailFlags.depint ? 1 : 0
    };

    this.http.patch(`${environment.backendUrl}/api/dep/update-service/${this.entcod}/${this.eje}/${cod}`, payload).subscribe({
      next: (res) => {
        this.updateSuccess()
        this.updateServiceSuccessMessage = 'servicio actualizado exitosamente'
        this.fetchServices();
        this.isUpdating = false;
      },
      error: (err) => {
        this.updateServiceErrorMessage = err.error.error ?? err.error;
        this.isUpdating = false;
      }
    })
  }

  personasError: string = '';
  personas: any[] = [];
  personasPage = 0;
  personasPageSize = 10;
  isLoadingPersonas: boolean = false;
  fetchPersonas(depcod: string): void {
    this.limpiarMessages();
    this.isLoadingPersonas = true;
    if (!depcod) {
      this.personasError = 'codigo extraviado'
      return;
    }

    this.http.get<any>(`${environment.backendUrl}/api/depe/fetch-service-personas/${this.entcod}/${this.eje}/${depcod}`).subscribe({
      next: (res) => {
        this.personas = res;
        this.personasPage = 0;
        this.isLoadingPersonas = false;
      },
      error: (err) => {
        this.personasPage=0;
        this.personas = [];
        this.personasError = err.error.error ?? err.error;
        this.isLoadingPersonas = false;
      }
    })
  }

  get paginatedPersonas(): any[] {const start = this.personasPage * this.personasPageSize;
    return this.personas.slice(start, start + this.personasPageSize);
  }
  get personasTotalPages(): number {return Math.max(1, Math.ceil(this.personas.length / this.personasPageSize));}
  personasPrevPage(): void {if (this.personasPage > 0) this.personasPage--;}
  personasNextPage(): void {if (this.personasPage < this.personasTotalPages - 1) this.personasPage++;}
  personasGoToPage(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.personasTotalPages) {this.personasPage = inputPage - 1;}
  }

  almacenErro: string = '';
  almacenSecondError: string = '';
  almacenArray: any = null;
  almacenDatosArray: any[] = [];
  isLoadingAlmacen: boolean = false;
  almacenDatos(depcod: string) {
    this.limpiarMessages();
    this.isLoadingAlmacen = true;
    if (!depcod) {
      this.almacenErro = 'codigo extraviato';
      return;
    }

    this.http.get<any>(`${environment.backendUrl}/api/mag/fetch-almacen-nombre/${this.entcod}/${depcod}`).subscribe({
      next: (res) => {
        this.almacenArray = res;
      },
      error: (err) => {
        this.almacenErro = err.error.error ?? err.error;
      }
    })

    this.http.get<any>(`${environment.backendUrl}/api/mat/fetch-almacenajes/${this.entcod}/${depcod}`).subscribe({
      next: (res) => {
        this.almacenDatosArray = res;
        this.isLoadingAlmacen = false;
        this.almacenPage = 0;
      },
      error: (err) => {
        this.almacenDatosArray = [];
        this.almacenSecondError = err.error.error ?? err.error;
        this.isLoadingAlmacen = false;
      }
    })
  }
  almacenPage = 0;
  get paginatedAlmacen(): any[] {const start = this.almacenPage * this.personasPageSize;
    return this.almacenDatosArray.slice(start, start + this.personasPageSize);
  }
  get almacenTotalPages(): number {return Math.max(1, Math.ceil(this.almacenDatosArray.length / this.personasPageSize));}
  almacenPrevPage(): void {if (this.almacenPage > 0) this.almacenPage--;}
  almacenNextPage(): void {if (this.almacenPage < this.almacenTotalPages - 1) this.almacenPage++;}
  almacenGoToPage(event: any): void {const inputPage = Number(event.target.value);
    if (inputPage >= 1 && inputPage <= this.almacenTotalPages) {this.almacenPage = inputPage - 1;}
  }

  updateServiceSecondError: string = '';
  updateServiceSecondSuccess: string = '';
  updateServiceSecond(depcod: string, depd1c:string, depd1d:string, depd2c:string, depd2d:string, depd3c:string, depd3d:string, depdco:string, depden:string) {
    this.limpiarMessages();
    const payload = {
      "depd1c": depd1c,
      "depd1d": depd1d,
      "depd2c": depd2c,
      "depd2d": depd2d,
      "depd3c": depd3c,
      "depd3d": depd3d,
      "depdco": depdco,
      "depden": depden
    }

    this.http.patch(`${environment.backendUrl}/api/dep/update-service-second/${this.entcod}/${this.eje}/${depcod}`, payload).subscribe({
      next: (res) => {
        this.updateServiceSecondSuccess = 'Se ha actualizado la pestaña Detalles del pedido';
      },
      error: (err) => {
        this.updateServiceSecondError =  err.error.error ?? err.error;
      }
    })
  }

  //add grid functions
  showAddConfirm: boolean = false;
  launchAddService() {
    this.limpiarMessages();
    this.showAddConfirm = true;
  }

  closeAddConfirm() {
    this.showAddConfirm = false;
  }

  newCge = '';
  setInputToUpper(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    let upper = (target.value ?? '').toUpperCase();
    if(upper.length > 4) {
      upper = upper.slice(0, 4);
    }
    target.value = upper;
    this.newCge = upper;
  }

  servicio = '';
  setInputToLimit(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    let service = (target.value ?? '').toUpperCase();
    if(service.length > 8) {
      service = service.slice(0, 8);
    }
    target.value = service;
    this.servicio = service;
  }

  addDepalm: boolean = false;
  addDepcom: boolean = false;
  addDepint: boolean = false;
  addServiceErrorMessage: string = '';
  addService(cod: string, des:string, cco:string, cge: string, d1c_add:string, d1d_add:string, d2c_add:string, d2d_add:String, d3c_add:string, d3d_add:string, den_add:string, dco_add:string) {
    this.isAdding = true;
    this.limpiarMessages();
    if (cod === '' || des === '' || cco === '' || cge === '') {
      this.addServiceErrorMessage = 'Se requieren Código, Descripción, Centro de Coste y Centro de Gestor'
      this.isAdding = false;
      return;
    }

    const payload = {
      "ent": this.entcod,
      "eje": this.eje,
      "depcod": cod,
      "depdes": des,
      "depalm": this.addDepalm ? 1 : 0,
      "depcom": this.addDepcom ? 1 : 0,
      "depint": this.addDepint ? 1 : 0,
      "ccocod": cco,
      "cgecod": cge,
      "percod": this.usucod,
      "depd1c": d1c_add,
      "depd1d": d1d_add,
      "depd2c": d2c_add,
      "depd2d": d2d_add,
      "depd3c": d3c_add,
      "depd3d": d3d_add,
      "depdco": dco_add,
      "depden": den_add
    }

    this.http.post(`${environment.backendUrl}/api/dep/Insert-service`, payload).subscribe({
      next: (res) => {
        this.servicesMessageSuccess = 'Servicio añadido con éxito';
        this.fetchServices();
        this.closeAddConfirm();
        this.isAdding = false;
      },
      error: (err) => {
        this.addServiceErrorMessage = err.error.error ?? err.error;
        this.isAdding = false;
      }
    })
  }

  //persona grid's functions
  showDeleteConfirm: boolean = false;
  isDeleting: boolean = false;
  personaTodelete: any;
  deleErr: string = '';
  showDelete(persona: any) {
    this.showDeleteConfirm = true;
    this.personaTodelete = persona;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
  }

  personasSuccess: string = '';
  confirmDelete(persona: string) {
    const depcod = this.selectedService.depcod;
    const percod = persona;
    this.isDeleting = true;

    this.http.delete(`${environment.backendUrl}/api/depe/delete-persona-service/${this.entcod}/${this.eje}/${depcod}/${percod}`).subscribe({
      next: (res) => {
        this.personasSuccess = 'Persona eliminada exitosamente';
        this.closeDeleteConfirm()
        this.fetchPersonas(depcod);
        this.isDeleting = false;
      },
      error: (err) => {
        this.deleErr = err.error.error ?? err.error;
        this.isDeleting = false;
      }
    })
  }

  //add personas grid
  addPersonas: boolean = false;
  errorCopy: string = '';
  pesonasCopy: any = [];
  showAddPersonas() {
    this.addPersonas = true;
    this.errorCopy = 'No hay personas';
  }

  closeAddPersonas() {
    this.limpiarMessages();
    this.limpiarSearcCopy();
    this.addPersonas = false;
  }

  isAdding: boolean = false;

  pageCopy: number = 0;
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
    if(this.searchPersonasCopy === '') {
      this.errorCopy = 'Ingrese algo para buscar';
      return 
    }

    if(this.searchPersonasCopy.length <= 20) {
      this.isLoadingPersonasAdd = true;
      this.http.get<any>(`${environment.backendUrl}/api/Per/search-cod-nom/${this.searchPersonasCopy}`).subscribe({
        next: (res) => {
          this.pesonasCopy = res;
          this.pageCopy = 0;
          this.isLoadingPersonasAdd = false;
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
          this.pesonasCopy = res;
          this.pageCopy = 0;
          this.isLoadingPersonasAdd = false;
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
    this.limpiarMessages();
    this.searchPersonasCopy = '';
    this.pesonasCopy = [];
    this.linesSelected = [];
    this.count = 0;
    this.errorCopy = 'No hay personas';
  }
  
  linesSelected: string[] = [];
  selectLines(persona: string) {
    if(this.linesSelected.includes(persona)) {
      this.linesSelected = this.linesSelected.filter((p) => p !== persona);
    } else {
      this.linesSelected = [...this.linesSelected, persona];
    }

    this.getLinesAdded()
  }

  count: number = 0;
  getLinesAdded(){
    this.count = this.linesSelected.length;
  }

  isAddingPersonnas: boolean = false;
  cargarPersonasError: string = '';
  addPersonasToService(personas: string[]) {
    if(!personas) {this.errorCopy = 'Debes seleccionar al menos una persona'; return;}

    const payload = {
      "ent": this.entcod,
      "eje": this.eje,
      "depcod": this.selectedService.depcod,
      "personas": personas
    }

    this.isAddingPersonnas = true;
    this.http.post<any>(`${environment.backendUrl}/api/depe/add-services-persona`, payload).subscribe({
      next: (res) => {
        this.isAddingPersonnas = false;
        this.savedNames = res.savedNames.join(', ') ?? '';
        this.unsavedNames = res?.unsavedNames.join(', ') ?? '';
        this.option = 'personas';
        this.openPersonasMessages();
      },
      error: (err) => {
        this.isAddingPersonnas = false;
        this.cargarPersonasError = err.error.error ?? err.error;
      }
    })
  }

  savedNames: string[] = [];
  unsavedNames: string[] = [];
  perosanasMessages: boolean = false;
  openPersonasMessages() {
    this.perosanasMessages = true;
  }

  closePersonasMessages() {
    this.limpiarMessages();
    this.perosanasMessages = false;
    this.closeAddPersonas();
  }

  //misc
  limpiarMessages() {
    this.servicesMessageSuccess = '';
    this.servicessMessageError = '';
    this.updateServiceSuccessMessage = '';
    this.updateServiceErrorMessage = '';
    this.personasError = '';
    this.updateServiceSecondError = '';
    this.updateServiceSecondSuccess = '';
    this.almacenErro = '';
    this.addServiceErrorMessage = '';
    this.personasSuccess = '';
    this.deleErr = '';
    this.almacenSecondError = '';
    this.cargarPersonasError = '';
    this.errorCopy = '';
  }
}
import { Component, HostListener} from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { environment } from '../../environments/environment';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-consulta-existencias-almacen',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-existencias-almacen.component.html',
  styleUrls: ['./consulta-existencias-almacen.component.css']
})

export class ConsultaExistenciasAlmacenComponent {
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

  //global variables
  private entcod: number | null = null;
  cge: string = '';
  percod: string = '';
  eje: string = '';
  page = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  existenciasSuccess: string = '';
  existenciasError: string = '';
  ngOnInit(): void{
    this.limpiarMessages();
    const entidad = sessionStorage.getItem('Entidad');
    const centroGestor = sessionStorage.getItem('CENTROGESTOR');
    const percodNam = sessionStorage.getItem('USUCOD');
    const ejercicio = sessionStorage.getItem('EJERCICIO');

    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (centroGestor) {const parsed = JSON.parse(centroGestor); this.cge = parsed.value;}
    if (percodNam) {this.percod = percodNam;}
    if (ejercicio) {const parsed = JSON.parse(ejercicio); this.eje = parsed.eje;}

    if (!entidad || this.entcod === null || this.cge === '' || this.percod === '' || this.eje === '') {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchExistencias();
  }

  existencias: any[] = [];
  almacenes: any[] = [];
  isLoading: boolean = false;
  magcod_main: string = '';
  selectedAlmacenNombreSearch: string = '';
  is_main_fetch: boolean = true;
  fetchExistencias() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/mea/existencias-almacen/${this.entcod}?cge=${this.cge}&percod=${this.percod}&eje=${this.eje}&page=${this.page}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.existencias = res.existencias;
        this.almacenes = res.almacenes;
        this.magcod_main = this.almacenes[0]?.depcod;
        this.selectedAlmacenNombreSearch = this.almacenes[0]?.dep_DEPDES;
        this.fetchTotalPages();
      },
      error: (err) => {
        this.isLoading = false;
        this.existencias = [];
        this.almacenes = [];
        this.existenciasError = err.error.error ?? err.error;
      }
    })
  }
  totalPagesMain = 0;
  fetchTotalPages() {
    this.http.get(`${environment.backendUrl}/api/mea/get-pag/${this.entcod}/${this.magcod_main}`).subscribe({
      next: (res) => {
        this.totalPagesMain = Math.ceil(2650 / 20);
      },
      error: (err) => {
        this.totalPagesMain = 0;
        console.warn(err.error.error ?? err.error);
      }
    })
  }
  almacenChange() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/mea/existencias-almacen/${this.entcod}?cge=${this.cge}&percod=${this.percod}&eje=${this.eje}&magcod_main=${this.magcod_main}`).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.existencias = res.existencias;
      },
      error: (err) => {
        this.isLoading = false;
        this.existencias = [];
        this.existenciasError = err.error.error ?? err.error;
      }
    })
  }

  main_search: string = '';
  afacod: string = '';
  asucod: string = '';
  is_search: boolean = false;
  search() {
    this.limpiarMessages();
    if (this.main_search === '' && this.afacod === '' && this.asucod === '') {

      this.almacenChange();
      this.fetchTotalPages();
      return;
    }

    this.is_main_fetch = false;
    this.is_search = true;

    let params = new HttpParams();
    
    if (this.main_search?.trim()) {
      params = params.set('main_search', this.main_search.trim());
    }
    if (this.afacod?.trim()) {
      params = params.set('afacod', this.afacod.trim());
    }
    if (this.asucod?.trim()) {
      params = params.set('asucod', this.asucod.trim());
    }
    
    this.isLoading = true;
    params = params.set('page', this.page.toString());
    this.http.get<any>(`${environment.backendUrl}/api/mea/existencias-almacen/${this.entcod}?cge=${this.cge}&percod=${this.percod}&eje=${this.eje}&magcod_main=${this.magcod_main}`, { params }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.existencias = res.existencias;
        this.page = 0;
        this.totalPagesMain = Math.ceil(this.existencias.length / this.pageSize);
      },
      error: (err) => {
        this.isLoading = false;
        this.existencias = [];
        this.almacenes = [];
        this.existenciasError = err.error.error ?? err.error;
      }
    })
  }
  get paginatedExistencias(): any[] {
    if (this.is_main_fetch) {
      return this.existencias ?? [];
    }
    if (this.is_search) {
      if (this.existencias.length > 20) {
        const start = this.page * this.pageSize;

        return this.existencias.slice(start, start + this.pageSize);
      } else {
        return this.existencias ?? [];
      }
    }
    return [];
  }
  prevPage(): void {
    if (this.is_main_fetch) {
      this.page--;
      this.fetchExistencias();
    }
    if (this.is_search) {
      if (this.page > 0) this.page--;
    }
  }
  nextPage(): void {
    if (this.is_main_fetch) {
      this.page++;
      this.fetchExistencias();
    }
    if (this.is_search) {
      if (this.page < this.totalPagesMain - 1) this.page++;
    }
  }
  goToPage(event: any): void {
    if (this.is_main_fetch) {
      const inputPage = Number(event.target.value);
      if (inputPage >= 1 && inputPage <= this.totalPagesMain) {
        this.page = inputPage - 1;
        this.fetchExistencias();
      }
    }
    if (this.is_search) {
      const inputPage = Number(event.target.value);
      if (inputPage >= 1 && inputPage <= this.totalPagesMain) {
        this.page = inputPage - 1;
      }
    }
  }

  clearSearch() {
    this.limpiarMessages();
    this.main_search = '';
    this.afacod = '';
    this.asucod= '';
    this.page = 0;
    this.is_main_fetch = true;
    this.is_search = false;
    this.fetchExistencias();
  }

  //main functions
  sortField: 'art_AFACOD' | 'art_ASUCOD' | 'art_ARTCOD' | 'art_ARTDES' | 'art_ARTREF' | 'selectedAlmacenNombreSearch' | 'meauni' | 'measol' | 'mearec' | 'calculateKEstVir' | 'mealoc' | null = null;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  toggleSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applySort();
    this.page = 0;
  }

  private applySort(): void {
    if (!this.sortColumn) return;
    
    const parseValue = (val: string) => {
      return val.replace(/(\d+)/g, (match) => {
        return match.padStart(20, '0');
      });
    };

    this.existencias.sort((a, b) => {
      const aValue = parseValue((a[this.sortColumn] ?? '').toString().toUpperCase());
      const bValue = parseValue((b[this.sortColumn] ?? '').toString().toUpperCase());
      
      const comparison = aValue.localeCompare(bValue);
      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
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

  kestvir: number | null = null;
  calculateKEstVir(artuni: number, artsol: number, artrec: number) {
    this.kestvir = artuni - artsol + artrec;
    return this.kestvir;
  }

  exportExistencias: any = [];
  isPdf: boolean = false;
  isExcel: boolean = false;
  getExportData() {
    this.http.get(`${environment.backendUrl}/api/mea/export/${this.entcod}/${this.magcod_main}`).subscribe({
      next: (res) => {
        this.exportExistencias = res;
        if (this.exportExistencias.length === 0) {
          this.existenciasError = 'No hay datos para exportar.';
          return;
        }

        if (this.isPdf) {
          this.preparePDF();
          return;
        }
        if (this.isExcel) {
          this.prepareExcel();
          return;
        }
      },
      error: (err) => {
        console.warn(err.error.error ?? err.error);
      }
    })
  }

  DownloadPDF() {
    this.limpiarMessages();

    this.isPdf = true;
    if (this.exportExistencias.length === 0) {
      this.getExportData();
    } else {
      this.preparePDF();
    }
  }

  preparePDF() {
    const rows = this.exportExistencias.map((row: any) => ({
      afacod: row.art_AFACOD ?? '',
      asucod: row.art_ASUCOD ?? '',
      artcod: row.art_ARTCOD ?? '',
      artdes: row.art_ARTDES ?? '',
      artref: row.art_ARTREF ?? '',
      depdes: this.selectedAlmacenNombreSearch,
      meauni: row.meauni,
      measol: row.measol,
      mearec: row.mearec,
      kestvir: this.calculateKEstVir(row.meauni, row.measol, row.mearec) ?? '',
      mealoc: row.mealoc ?? ''
    }));

    const columns = [
      { header: 'Familia', dataKey: 'afacod' },
      { header: 'Subfamilia', dataKey: 'asucod'},
      { header: 'Código', dataKey: 'artcod'},
      { header: 'Descripción', dataKey: 'artdes'},
      { header: 'Referencia universal', dataKey: 'artref'},
      { header: 'almacén', dataKey: 'depdes'},
      { header: 'Existencias', dataKey: 'meauni'},
      { header: 'Pte. Servir', dataKey: 'measol'},
      { header: 'Pte. Entrada', dataKey: 'mearec'},
      { header: 'Estocaje virtual', dataKey: 'kestvir'},
      { header: 'Ubicación', dataKey: 'mealoc'}
    ];

    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(14);
    doc.text('Consulta de existencias por almacén', 40, 40);

    autoTable(doc, {
      startY: 60,
      head: [columns.map(col => col.header)],
      body: rows.map((row: any) => columns.map(col => row[col.dataKey as keyof typeof row] ?? '')),
      styles: { font: 'helvetica', fontSize: 10, cellPadding: 6 },
      headStyles: { fillColor: [240, 240, 240], textColor: 33, fontStyle: 'bold' },
      columnStyles: {
        afacod: { cellWidth: 10 },
        asucod: { cellWidth: 10 },
        artcod: { cellWidth: 10 },
        artdes: { cellWidth: 30 },
        artref: { cellWidth: 25 },
        depdes: { cellWidth: 25 },
        artuni: { cellWidth: 10 },
        measol: { cellWidth: 10 },
        mearec: { cellWidth: 10 },
        kestvir: { cellWidth: 10 },
        mealoc: { cellWidth: 25 },
      }
    });

    doc.save('Consulta_de_existencias_por_almacen.pdf');
    this.isPdf = false;
  }

  downloadExcel() {
    this.limpiarMessages();

    this.isExcel = true;
    if (this.exportExistencias.length === 0) {
      this.getExportData();
    } else {
      this.prepareExcel();
    }
  }

  prepareExcel() {
    const exportRows = this.exportExistencias.map((row: any) => ({
      afacod: row.art_AFACOD ?? '',
      asucod: row.art_ASUCOD ?? '',
      artcod: row.art_ARTCOD ?? '',
      artdes: row.art_ARTDES ?? '',
      artref: row.art_ARTREF ?? '',
      depdes: this.selectedAlmacenNombreSearch ?? '',
      meauni: row.meauni ?? '',
      measol: row.measol ?? '',
      mearec: row.mearec ?? '',
      kestvir: this.calculateKEstVir(row.meauni, row.measol, row.mearec) ?? '',
      mealoc: row.mealoc ?? ''
    }));

    const worksheet = XLSX.utils.aoa_to_sheet([]);
    XLSX.utils.sheet_add_aoa(worksheet, [['Consulta de existencias por almacén']], { origin: 'A1' });
    worksheet['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 3 } }];
    XLSX.utils.sheet_add_aoa(worksheet, [['Familias', 'Subfamilia', 'Código', 'Descripción', 'Referencia universal', 'almacén',  'Existencias', 'Pte. Servir', 'Pte. Entrada', 'Estocaje virtual', 'Ubicación']], { origin: 'A2' });
    XLSX.utils.sheet_add_json(worksheet, exportRows, { origin: 'A3', skipHeader: true });

    worksheet['!cols'] = [
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 30 },
      { wch: 25 },
      { wch: 25 },
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 10 },
      { wch: 25 },
    ];
  
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Existencias');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    saveAs(
      new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      'Consulta_de_existencias_por_almacen.xlsx'
    );
    this.isExcel = false;
  }

  //misc
  limpiarMessages() {
    this.existenciasSuccess = '';
    this.existenciasError = '';
  }
}

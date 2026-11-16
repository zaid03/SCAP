import { Component, HostListener} from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  selector: 'app-consulta-albaranes',
  standalone: true,
  imports: [ CommonModule ,FormsModule, SidebarComponent],
  templateUrl: './consulta-albaranes.component.html',
  styleUrls: ['./consulta-albaranes.component.css']
})
export class ConsultaAlbaranesComponent {
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
  eje: number | null = null;
  centroGestor: string = '';
  pageSize = 20;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void{
    this.limpiarMEssages();
    const entidad = sessionStorage.getItem('Entidad');
    const eje = sessionStorage.getItem('EJERCICIO');
    const cge = sessionStorage.getItem('CENTROGESTOR');

    if (cge){const parsed = JSON.parse(cge); this.centroGestor = parsed.value}
    if (entidad) {const parsed = JSON.parse(entidad); this.entcod = parsed.ENTCOD;}
    if (eje) {const parsed = JSON.parse(eje); this.eje = parsed.eje;}

    if (!entidad || this.entcod === null || !eje || this.eje === null || !cge || cge === null) {
      sessionStorage.clear();
      alert('Debes iniciar sesión para acceder a esta página.');
      this.router.navigate(['/login']);
      return;
    }

    this.fetchAlbaranes();
    this.fetchItemsAmount()
  }

  albaranes: any[] = [];
  isLoading: boolean = false;
  albaranesSuccess: string = '';
  albaranesError: string = '';
  page = 0;
  fetchAlbaranes() {
    let url =
    `${environment.backendUrl}/api/alb/fetch-C-albaranes/${this.entcod}` +
    `?page=${this.page}`;

    if (this.sortColumn) {
      url += `&sort=${encodeURIComponent(this.sortColumn)}`;
      url += `&direction=${this.sortDirection}`;
    }

    this.isLoading = true;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.albaranes = res;
      },
      error: (err) => {
        this.albaranes = [];
        this.isLoading = false;
        this.albaranesError = err.error.error || err.error;
      }
    })
  }
  totalPages: number = 0;
  fetchItemsAmount() {
    this.http.get<any>(`${environment.backendUrl}/api/alb/fetch-rows-albaranes/${this.entcod}`).subscribe({
      next: (res) => {
        this.totalPages = Math.ceil(res / 20);
      },
      error: (err) => {
        console.warn(err.error.error || err.error);
      }
    })
  }
  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.fetchAlbaranes();
    } 
  }
  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.fetchAlbaranes();
    }
  }
  goToPage(event: any): void {
    this.page = Number(event.target.value) - 1;
    if (this.page > 0 && this.page < this.totalPages - 1) {
      this.fetchAlbaranes();
    }
  }

  //main table functions
  sortField: 'albnum' | 'ter.tercod' | 'ter.ternom' | 'albref' | 'albdat' | 'albbim' | 'albfre' | 'depcod' | 'albcom' | 'solnum' | 'facnum' | null = null;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  toggleSort(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection =
        this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    this.page = 0;
    this.fetchAlbaranes();
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

  main_search: string = '';
  centroDep: string = '';
  comprador: string = '';
  ejeSearch: stirng = '';
  search() {

  }

  limpiarSearch() {
    this.page = 0;
    this.sortColumn = '';
    this.fetchAlbaranes();
    this.fetchItemsAmount();
  }

  //details functions
  showDetails(albaran: any) {

  }

  //misc
  limpiarMEssages() {
  this.albaranesSuccess = '';
  this.albaranesError = '';
  }
}
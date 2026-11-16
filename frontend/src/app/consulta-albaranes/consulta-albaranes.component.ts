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
  public Math = Math;

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
  backupAlbaranes: any[] = [];
  isLoading: boolean = false;
  albaranesSuccess: string = '';
  albaranesError: string = '';
  page = 0;
  fetchAlbaranes() {
    this.isLoading = true;
    this.http.get<any>(`${environment.backendUrl}/api/alb/fetch-C-albaranes/${this.entcod}?page=${this.page}`).subscribe({
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
        this.totalPages = this.Math.round(res / 20);
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
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    this.applySort();
  }

  private getSortValue(row: any, column: string): string | number {
    switch (column) {
      case 'albnum':
        return row.albnum ?? 0;

      case 'ter.tercod':
        return row.ter?.tercod ?? 0;

      case 'ter.ternom':
        return row.ter?.ternom ?? '';

      case 'albref':
        return row.albref ?? '';

      case 'albdat':
        return row.albdat ? new Date(row.albdat).getTime() : 0;

      case 'albbim':
        return Number(row.albbim ?? 0);

      case 'albfre':
        return row.albfre ? new Date(row.albfre).getTime() : 0;

      case 'depcod':
        return row.depcod ?? '';

      case 'albcom':
        return row.albcom ?? '';

      case 'solnum':
        return row.solnum ?? 0;

      case 'facnum':
        return row.facnum ?? 0;

      default:
        return '';
    }
  }

  private applySort(): void {
    const direction = this.sortDirection === 'asc' ? 1 : -1;

    this.albaranes = [...this.albaranes].sort((a, b) => {
      const aValue = this.getSortValue(a, this.sortColumn);
      const bValue = this.getSortValue(b, this.sortColumn);

      if (typeof aValue === 'number' && typeof bValue === 'number') {
        return (aValue - bValue) * direction;
      }

      return String(aValue)
        .localeCompare(String(bValue), 'es', {
          numeric: true,
          sensitivity: 'base'
        }) * direction;
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

  //details functions
  showDetails(albaran: any) {

  }

  //misc
  limpiarMEssages() {
  this.albaranesSuccess = '';
  this.albaranesError = '';
  }
}
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaArticulosAlmacenComponent } from './consulta-articulos-almacen.component';

describe('ConsultaArticulosAlmacenComponent', () => {
  let component: ConsultaArticulosAlmacenComponent;
  let fixture: ComponentFixture<ConsultaArticulosAlmacenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaArticulosAlmacenComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaArticulosAlmacenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

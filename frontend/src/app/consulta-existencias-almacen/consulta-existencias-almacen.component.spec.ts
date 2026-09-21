import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaExistenciasAlmacenComponent } from './consulta-existencias-almacen.component';

describe('ConsultaExistenciasAlmacenComponent', () => {
  let component: ConsultaExistenciasAlmacenComponent;
  let fixture: ComponentFixture<ConsultaExistenciasAlmacenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaExistenciasAlmacenComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaExistenciasAlmacenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

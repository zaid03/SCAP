import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaGeneralExistenciasComponent } from './consulta-general-existencias.component';

describe('ConsultaGeneralExistenciasComponent', () => {
  let component: ConsultaGeneralExistenciasComponent;
  let fixture: ComponentFixture<ConsultaGeneralExistenciasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaGeneralExistenciasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaGeneralExistenciasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

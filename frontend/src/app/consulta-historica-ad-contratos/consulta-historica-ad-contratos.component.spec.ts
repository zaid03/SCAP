import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaHistoricaAdContratosComponent } from './consulta-historica-ad-contratos.component';

describe('ConsultaHistoricaAdContratosComponent', () => {
  let component: ConsultaHistoricaAdContratosComponent;
  let fixture: ComponentFixture<ConsultaHistoricaAdContratosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaHistoricaAdContratosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaHistoricaAdContratosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

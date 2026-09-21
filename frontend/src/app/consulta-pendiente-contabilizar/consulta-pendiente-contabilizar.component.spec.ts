import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaPendienteContabilizarComponent } from './consulta-pendiente-contabilizar.component';

describe('ConsultaPendienteContabilizarComponent', () => {
  let component: ConsultaPendienteContabilizarComponent;
  let fixture: ComponentFixture<ConsultaPendienteContabilizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaPendienteContabilizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaPendienteContabilizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

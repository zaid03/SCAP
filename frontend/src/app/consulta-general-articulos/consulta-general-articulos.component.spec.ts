import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaGeneralArticulosComponent } from './consulta-general-articulos.component';

describe('ConsultaGeneralArticulosComponent', () => {
  let component: ConsultaGeneralArticulosComponent;
  let fixture: ComponentFixture<ConsultaGeneralArticulosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaGeneralArticulosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaGeneralArticulosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

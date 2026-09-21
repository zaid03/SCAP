import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaAnalticaDeArticulosComponent } from './consulta-analtica-de-articulos.component';

describe('ConsultaAnalticaDeArticulosComponent', () => {
  let component: ConsultaAnalticaDeArticulosComponent;
  let fixture: ComponentFixture<ConsultaAnalticaDeArticulosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaAnalticaDeArticulosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaAnalticaDeArticulosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

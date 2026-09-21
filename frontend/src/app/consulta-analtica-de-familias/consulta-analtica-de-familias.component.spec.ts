import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaAnalticaDeFamiliasComponent } from './consulta-analtica-de-familias.component';

describe('ConsultaAnalticaDeFamiliasComponent', () => {
  let component: ConsultaAnalticaDeFamiliasComponent;
  let fixture: ComponentFixture<ConsultaAnalticaDeFamiliasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaAnalticaDeFamiliasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaAnalticaDeFamiliasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

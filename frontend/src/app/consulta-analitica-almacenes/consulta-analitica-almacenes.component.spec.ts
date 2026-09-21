import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaAnaliticaAlmacenesComponent } from './consulta-analitica-almacenes.component';

describe('ConsultaAnaliticaAlmacenesComponent', () => {
  let component: ConsultaAnaliticaAlmacenesComponent;
  let fixture: ComponentFixture<ConsultaAnaliticaAlmacenesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaAnaliticaAlmacenesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaAnaliticaAlmacenesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

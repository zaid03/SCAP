import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaAlbaranesComponent } from './consulta-albaranes.component';

describe('ConsultaAlbaranesComponent', () => {
  let component: ConsultaAlbaranesComponent;
  let fixture: ComponentFixture<ConsultaAlbaranesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaAlbaranesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaAlbaranesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

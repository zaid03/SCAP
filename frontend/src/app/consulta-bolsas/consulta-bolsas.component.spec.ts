import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaBolsasComponent } from './consulta-bolsas.component';

describe('ConsultaBolsasComponent', () => {
  let component: ConsultaBolsasComponent;
  let fixture: ComponentFixture<ConsultaBolsasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaBolsasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaBolsasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

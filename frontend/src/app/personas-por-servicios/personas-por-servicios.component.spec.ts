import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonasPorServiciosComponent } from './personas-por-servicios.component';

describe('PersonasPorServiciosComponent', () => {
  let component: PersonasPorServiciosComponent;
  let fixture: ComponentFixture<PersonasPorServiciosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonasPorServiciosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PersonasPorServiciosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

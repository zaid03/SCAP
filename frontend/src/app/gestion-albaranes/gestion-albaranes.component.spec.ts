import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionAlbaranesComponent } from './gestion-albaranes.component';

describe('GestionAlbaranesComponent', () => {
  let component: GestionAlbaranesComponent;
  let fixture: ComponentFixture<GestionAlbaranesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionAlbaranesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionAlbaranesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

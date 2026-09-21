import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MantenimientoGeneralArticulosComponent } from './mantenimiento-general-articulos.component';

describe('MantenimientoGeneralArticulosComponent', () => {
  let component: MantenimientoGeneralArticulosComponent;
  let fixture: ComponentFixture<MantenimientoGeneralArticulosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MantenimientoGeneralArticulosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MantenimientoGeneralArticulosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

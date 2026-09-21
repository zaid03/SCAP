import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MantinimientoArticulosAlmacenComponent } from './mantinimiento-articulos-almacen.component';

describe('MantinimientoArticulosAlmacenComponent', () => {
  let component: MantinimientoArticulosAlmacenComponent;
  let fixture: ComponentFixture<MantinimientoArticulosAlmacenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MantinimientoArticulosAlmacenComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MantinimientoArticulosAlmacenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

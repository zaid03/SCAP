import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TipoAlmacenajeComponent } from './tipo-almacenaje.component';

describe('TipoAlmacenajeComponent', () => {
  let component: TipoAlmacenajeComponent;
  let fixture: ComponentFixture<TipoAlmacenajeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TipoAlmacenajeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TipoAlmacenajeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TiposUnidadesComponent } from './tipos-unidades.component';

describe('TiposUnidadesComponent', () => {
  let component: TiposUnidadesComponent;
  let fixture: ComponentFixture<TiposUnidadesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TiposUnidadesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TiposUnidadesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

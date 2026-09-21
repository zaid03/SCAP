import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitorContabilizacionComponent } from './monitor-contabilizacion.component';

describe('MonitorContabilizacionComponent', () => {
  let component: MonitorContabilizacionComponent;
  let fixture: ComponentFixture<MonitorContabilizacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonitorContabilizacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonitorContabilizacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

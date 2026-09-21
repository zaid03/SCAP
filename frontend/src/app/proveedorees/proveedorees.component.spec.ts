import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProveedoreesComponent } from './proveedorees.component';

describe('ProveedoreesComponent', () => {
  let component: ProveedoreesComponent;
  let fixture: ComponentFixture<ProveedoreesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProveedoreesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProveedoreesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

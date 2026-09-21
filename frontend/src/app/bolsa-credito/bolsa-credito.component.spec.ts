import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BolsaCreditoComponent } from './bolsa-credito.component';

describe('BolsaCreditoComponent', () => {
  let component: BolsaCreditoComponent;
  let fixture: ComponentFixture<BolsaCreditoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BolsaCreditoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BolsaCreditoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

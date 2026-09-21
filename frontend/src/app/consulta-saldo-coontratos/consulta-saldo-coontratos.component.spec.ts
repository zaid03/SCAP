import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaSaldoCoontratosComponent } from './consulta-saldo-coontratos.component';

describe('ConsultaSaldoCoontratosComponent', () => {
  let component: ConsultaSaldoCoontratosComponent;
  let fixture: ComponentFixture<ConsultaSaldoCoontratosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaSaldoCoontratosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaSaldoCoontratosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

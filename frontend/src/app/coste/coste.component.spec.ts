import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CosteComponent } from './coste.component';

describe('CosteComponent', () => {
  let component: CosteComponent;
  let fixture: ComponentFixture<CosteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CosteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CosteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

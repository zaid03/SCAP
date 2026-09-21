import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CgeComponent } from './cge.component';

describe('CgeComponent', () => {
  let component: CgeComponent;
  let fixture: ComponentFixture<CgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CgeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

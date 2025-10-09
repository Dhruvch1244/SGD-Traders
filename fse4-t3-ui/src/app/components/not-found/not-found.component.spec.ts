import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotFoundComponent } from './not-found.component';

describe('NotFoundComponent', () => {
  let component: NotFoundComponent;
  let fixture: ComponentFixture<NotFoundComponent>;

beforeEach(async () => {
  await TestBed.resetTestingModule();
  await TestBed.configureTestingModule({
    imports: [NotFoundComponent]
  })
  .compileComponents();

  fixture = TestBed.createComponent(NotFoundComponent);
  component = fixture.componentInstance;
  fixture.detectChanges();
});

afterEach(() => {
  TestBed.resetTestingModule();
});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

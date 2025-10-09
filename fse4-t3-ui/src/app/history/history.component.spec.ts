import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HistoryComponent } from './history.component';

describe('HistoryComponent', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;


 beforeEach(async () => {
  await TestBed.resetTestingModule();
  await TestBed.configureTestingModule({
    imports: [HistoryComponent, HttpClientTestingModule]
  })
  .compileComponents();

  fixture = TestBed.createComponent(HistoryComponent);
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

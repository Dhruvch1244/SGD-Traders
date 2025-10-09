import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TradeComponent } from './trade.component';
import { ActivatedRoute } from '@angular/router';
import { HeaderComponent } from '../components/header/header.component';
import { SidebarComponent } from '../components/sidebar/sidebar.component';
import { TradePageComponent } from '../components/trade-page/trade-page.component';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('TradeComponent', () => {
  let component: TradeComponent;
  let fixture: ComponentFixture<TradeComponent>;

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [
        TradeComponent,
        HeaderComponent,
        SidebarComponent,
        TradePageComponent,
        HttpClientTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ mode: 'buy' }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TradeComponent);
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
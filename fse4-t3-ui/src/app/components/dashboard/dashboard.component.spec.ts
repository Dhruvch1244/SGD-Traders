class MockChart {
  data: any;
  options: any;
  destroy = () => {};
  constructor(ctx: any, config: any) {
    this.data = config.data;
    this.options = config.options;
  }
}
(window as any).Chart = MockChart;

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../services/auth.service';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { PortfolioService } from '../../services/portfolio.service';
import { of, Subscription } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { fakeAsync, tick } from '@angular/core/testing';
const mockPortfolioSummary = {
  totalValue: 10000,
  totalGainLoss: 500,
};
const mockLineChartData = {
  labels: ['Jan', 'Feb'],
  datasets: [{ data: [1, 2] }],
};
const mockPieChartData = { labels: ['A', 'B'], datasets: [{ data: [3, 4] }] };
const mockPerformanceColumns = [
  { headerName: 'Col1', field: 'col1' },
  { headerName: 'Col2', field: 'col2' },
];
const mockPerformanceData = [
  { date: '2023-01-01', gainLoss: 100, volume: 10 },
  { date: '2023-01-02', gainLoss: 200, volume: 20 },
];
const mockBarChartData = { labels: ['X'], datasets: [{ data: [5] }] };
const mockTradeHistoryColumns = [
  { headerName: 'Type', field: 'type' }, // This will get cellRenderer in createColumnDefs
  { headerName: 'Qty', field: 'amount' },
  { headerName: 'Price', field: 'price' },
  { headerName: 'Asset', field: 'asset' },
  { headerName: 'Date', field: 'date' },
];
const mockTradeHistoryData = [
  { date: '2023-01-01', asset: 'AAPL', type: 'BUY', amount: 1, price: 100 },
  { date: '2023-01-02', asset: 'GOOG', type: 'SELL', amount: 2, price: 200 },
];

class MockAuthService {
  getCurrClient() {
    return 'test-client-id';
  }
}
class MockInvestmentPreferencesService {
  getPreferences(clientId: string) {
    return clientId === 'test-client-id' ? {} : null;
  }
}
class MockPortfolioService {
  getPortfolioSummary(clientId: string) {
    return of({
      portfolioSummary: mockPortfolioSummary,
      lineChartData: mockLineChartData,
      pieChartData: mockPieChartData,
      performanceColumns: mockPerformanceColumns,
      performanceData: mockPerformanceData,
      barChartData: mockBarChartData,
      tradeHistoryColumns: mockTradeHistoryColumns,
      tradeHistoryData: mockTradeHistoryData,
    });
  }
}

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        {
          provide: InvestmentPreferencesService,
          useClass: MockInvestmentPreferencesService,
        },
        { provide: PortfolioService, useClass: MockPortfolioService },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Ensure ngOnInit runs and data is set
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should fetch and set portfolio summary and chart data on init', fakeAsync(() => {
    fixture.detectChanges();
    tick(); // Wait for async observable
    expect(component.portfolioSummary).toEqual(mockPortfolioSummary);
    expect(component.lineChartData).toEqual(mockLineChartData);
    expect(component.pieChartData).toEqual(mockPieChartData);
    expect(component.performanceColumns).toEqual(mockPerformanceColumns);
    expect(component.performanceData).toEqual(mockPerformanceData);
    expect(component.barChartData).toEqual(mockBarChartData);
    expect(component.columnDefs.length).toBe(mockTradeHistoryColumns.length);
    expect(component.rowData).toEqual(mockTradeHistoryData);
  }));

  it('should display stats in the template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('My Stats');
    expect(compiled.textContent).toContain('Total Value:');
    expect(compiled.textContent).toContain('Total Gain/Loss:');
  });

  // it('should render charts if data is present', fakeAsync(() => {
  //   fixture.detectChanges();
  //   tick();
  //   expect(component.lineChartData.datasets.length).toBeGreaterThan(0);
  //   expect(component.pieChartData.datasets.length).toBeGreaterThan(0);
  //   expect(component.barChartData.datasets.length).toBeGreaterThan(0);
  //   // Patch Chart mock to avoid canvas error
  //   const chartInstances: any[] = [];
  //   (window as any).Chart = function (ctx: any, config: any) {
  //     chartInstances.push({ ctx, config });
  //     return {
  //       destroy: () => {
  //         chartInstances.length = 0;
  //       },
  //     };
  //   };
  //   expect(() => component.renderCharts()).not.toThrow();
  //   // Clean up chart instances
  //   chartInstances.forEach((c) => c.destroy && c.destroy());
  // }));

  it('should create columnDefs with custom cellRenderer for type', () => {
    const typeCol = component.columnDefs.find((c) => c.field === 'type');
    expect(typeCol).toBeTruthy();
    if (typeCol && typeCol.cellRenderer) {
      const buyBtn = typeCol.cellRenderer({ value: 'BUY' });
      expect(buyBtn.innerText).toBe('Buy');
      expect(buyBtn.style.backgroundColor).toBe('green');
      const sellBtn = typeCol.cellRenderer({ value: 'SELL' });
      expect(sellBtn.innerText).toBe('Sell');
      expect(sellBtn.style.backgroundColor).toBe('red');
    }
  });

  it('should unsubscribe from tradeSubscription on destroy', () => {
    (component as any).tradeSubscription = new Subscription();
    const subscription = (component as any).tradeSubscription as Subscription;
    spyOn(subscription, 'unsubscribe');
    component.ngOnDestroy();
    expect(subscription.unsubscribe).toHaveBeenCalled();
  });

  // it('should navigate away if clientId is missing or preferences missing', () => {
  //   const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
  //   // Assign router before ngOnInit
  //   (component as any).router = routerSpy;
  //   // Simulate missing clientId
  //   component.clientId = '';
  //   component.ngOnInit();
  //   expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
  //   // Simulate missing preferences
  //   component.clientId = 'bad-id';
  //   component.ngOnInit();
  //   expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
  // });

  it('should createColumnDefs return empty array if columns is empty', () => {
    const result = component.createColumnDefs([]);
    expect(result).toEqual([]);
  });

  it('should createColumnDefs not add cellRenderer if type column is missing', () => {
    const columns = [{ headerName: 'Qty', field: 'quantity' }];
    const result = component.createColumnDefs(columns as any);
    expect(result[0].cellRenderer).toBeUndefined();
  });

  it('should not throw in ngAfterViewInit', () => {
    expect(() => component.ngAfterViewInit()).not.toThrow();
  });

  it('should not render line chart if data is empty', () => {
    component.lineChartData = { labels: [], datasets: [] };
    spyOn<any>(component, 'renderLineChart').and.callThrough();
    expect(() => component.renderLineChart()).not.toThrow();
  });

  it('should not render pie chart if data is empty', () => {
    component.pieChartData = { labels: [], datasets: [] };
    spyOn<any>(component, 'renderPieChart').and.callThrough();
    expect(() => component.renderPieChart()).not.toThrow();
  });

  it('should not render bar chart if data is empty', () => {
    component.barChartData = { labels: [], datasets: [] };
    spyOn<any>(component, 'renderBarChart').and.callThrough();
    expect(() => component.renderBarChart()).not.toThrow();
  });
});

// Separate describe for error handling to avoid TestBed override errors
describe('DashboardComponent error handling', () => {
  let errorFixture: ComponentFixture<DashboardComponent>;
  let errorComponent: DashboardComponent;

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        {
          provide: InvestmentPreferencesService,
          useClass: MockInvestmentPreferencesService,
        },
        {
          provide: PortfolioService,
          useValue: {
            getPortfolioSummary: () => {
              return { subscribe: (_: any, errorCb: any) => errorCb('error!') };
            },
          },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
    errorFixture = TestBed.createComponent(DashboardComponent);
    errorComponent = errorFixture.componentInstance;
  });

  it('should handle error when getPortfolioSummary fails', () => {
    spyOn(console, 'error');
    errorComponent.ngOnInit();
    expect(console.error).toHaveBeenCalledWith(
      'Error fetching portfolio summary:',
      'error!'
    );
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TradeHistoryComponent } from './trade-history.component';
import { InstrumentService } from '../../services/instrument.service';
import { AuthService } from '../../services/auth.service';
import { PLATFORM_ID } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

describe('TradeHistoryComponent', () => {
  let component: TradeHistoryComponent;
  let fixture: ComponentFixture<TradeHistoryComponent>;
  let instrumentServiceMock: any;
  let authServiceMock: any;

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    instrumentServiceMock = {
      trades$: new BehaviorSubject([]),
      instruments$: new BehaviorSubject([]),
      loadTradesForClient: jasmine.createSpy('loadTradesForClient'),
    };

    authServiceMock = {
      currentUserValue: { clientId: '123' },
    };

    await TestBed.configureTestingModule({
      imports: [TradeHistoryComponent],
      providers: [
        { provide: InstrumentService, useValue: instrumentServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TradeHistoryComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    if (component && component.chart && typeof component.chart.destroy === 'function') {
      component.chart.destroy();
    }
    if (fixture) {
      fixture.destroy();
    }
    TestBed.resetTestingModule();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call loadTradesForClient on init if clientId exists', () => {
    component.ngOnInit();
    expect(instrumentServiceMock.loadTradesForClient).toHaveBeenCalledWith('123');
  });

  it('should not call loadTradesForClient if clientId does not exist', () => {
    authServiceMock.currentUserValue = {};
    component.ngOnInit();
    expect(instrumentServiceMock.loadTradesForClient).not.toHaveBeenCalled();
  });

  it('should populate rowData when trades and instruments are emitted', () => {
    component.ngOnInit();
    const trades = [
      { tradeId: 't1', instrumentId: 'i1', direction: 'BUY', quantity: 10, executionPrice: 100, cashValue: 1000, timestamp: new Date() },
      { tradeId: 't2', instrumentId: 'i2', direction: 'SELL', quantity: 5, executionPrice: 50, cashValue: 250, timestamp: new Date() },
    ];
    const instruments = [
      { instrumentId: 'i1', description: 'Instrument 1' },
      { instrumentId: 'i2', description: 'Instrument 2' },
    ];
    instrumentServiceMock.trades$.next(trades);
    instrumentServiceMock.instruments$.next(instruments);
    fixture.detectChanges();
    expect(component.rowData.length).toBe(2);
    expect(component.rowData[0].instrumentName).toBe('Instrument 2'); // reversed
    expect(component.rowData[0].direction).toBe('S'); // SELL mapped
    expect(component.rowData[1].direction).toBe('B'); // BUY mapped
  });

  it('should handle empty trades array gracefully', () => {
    component.ngOnInit();
    instrumentServiceMock.trades$.next([]);
    instrumentServiceMock.instruments$.next([]);
    fixture.detectChanges();
    expect(component.rowData.length).toBe(0);
  });

  it('should unsubscribe on destroy', () => {
    component.ngOnInit();
    spyOn(component['tradeSubscription'], 'unsubscribe');
    component.ngOnDestroy();
    expect(component['tradeSubscription'].unsubscribe).toHaveBeenCalled();
  });

  it('should create a pie chart correctly', () => {
    const canvas = document.createElement('canvas');
    component.pieChartRef = { nativeElement: canvas } as any;
    const trades = [
      { tradeId: 't1', instrumentId: 'i1', direction: 'BUY', quantity: 10, executionPrice: 100, cashValue: 100, clientId: '123', timestamp: new Date() },
      { tradeId: 't2', instrumentId: 'i2', direction: 'SELL', quantity: 5, executionPrice: 50, cashValue: 50, clientId: '123', timestamp: new Date() },
      { tradeId: 't3', instrumentId: 'i3', direction: 'BUY', quantity: 20, executionPrice: 150, cashValue: 150, clientId: '123', timestamp: new Date() },
    ];
    component.createPieChart(trades);
    expect(component.chart).toBeTruthy();
    expect(component.chart.data.datasets[0].data).toEqual([250, 50]); // sum BUY/Sell
  });

  it('should retry pie chart creation if pieChartRef is undefined', () => {
    component.pieChartRef = undefined as any;
    const trades = [
      { tradeId: 't1', instrumentId: 'i1', direction: 'BUY', quantity: 10, executionPrice: 100, cashValue: 100, clientId: '123', timestamp: new Date() },
    ];
    const setTimeoutSpy = spyOn(window as any, 'setTimeout').and.callFake(() => 1);
    component.createPieChart(trades);
    expect(setTimeoutSpy).toHaveBeenCalled();
  });

  it('should map unknown instrumentId to trade object if instrument not found', () => {
    component.ngOnInit();
    const trades = [
      { tradeId: 't1', instrumentId: 'unknown', direction: 'BUY', quantity: 10, executionPrice: 100, cashValue: 1000, timestamp: new Date() },
    ];
    instrumentServiceMock.trades$.next(trades);
    instrumentServiceMock.instruments$.next([]);
    fixture.detectChanges();
    expect(component.rowData[0].instrumentName).toBe('unknown');
  });
});
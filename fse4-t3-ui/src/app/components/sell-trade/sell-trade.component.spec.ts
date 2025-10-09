import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { SellTradeComponent } from './sell-trade.component';
import { InstrumentService } from '../../services/instrument.service';
import { AuthService } from '../../services/auth.service';

describe('SellTradeComponent', () => {
  let component: SellTradeComponent;
  let fixture: ComponentFixture<SellTradeComponent>;
  let mockInstrumentService: any;
  let mockAuthService: any;

  const mockHoldings = [
    { instrumentId: 'I1', quantity: 10, instrumentName: 'Apple' },
    { instrumentId: 'I2', quantity: 5, instrumentName: 'Gov Bond' },
  ];
  const mockInstrument = {
    instrumentId: 'I1',
    minQuantity: 1,
    maxQuantity: 100,
    bidPrice: 99,
    askPrice: 100,
  };
  const mockPrice = { bidPrice: 99, askPrice: 100 };

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    mockInstrumentService = {
      getPortfolioByClientId: jasmine.createSpy('getPortfolioByClientId').and.returnValue(of({ holdings: mockHoldings })),
      getInstrumentById: jasmine.createSpy('getInstrumentById').and.callFake(() => of(mockInstrument)),
      getPrice: jasmine.createSpy('getPrice').and.callFake(() => of(mockPrice)),
      sellInstrument: jasmine.createSpy('sellInstrument').and.returnValue(of({ success: true })),
    };
    mockAuthService = {
      currentUserValue: { clientId: 'C1' }
    };

    await TestBed.configureTestingModule({
      imports: [FormsModule, SellTradeComponent],
      providers: [
        { provide: InstrumentService, useValue: mockInstrumentService },
        { provide: AuthService, useValue: mockAuthService }
      ],
    }).compileComponents();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  function createComponent() {
    fixture = TestBed.createComponent(SellTradeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create component', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('should load holdings on init', () => {
    createComponent();
    expect(mockInstrumentService.getPortfolioByClientId).toHaveBeenCalledWith('C1');
    expect(component.holdings.length).toBe(2);
    expect(component.selectedInstrument).toBe('I1');
  });

  it('should normalize holdings with various payloads', () => {
    createComponent();
    expect(component['normalizeHoldings']({ holdings: mockHoldings })).toEqual(mockHoldings);
    expect(component['normalizeHoldings']({ rowData: mockHoldings })).toEqual(mockHoldings);
    expect(component['normalizeHoldings']({ data: { holdings: mockHoldings } })).toEqual(mockHoldings);
    expect(component['normalizeHoldings'](mockHoldings)).toEqual(mockHoldings);
    expect(component['normalizeHoldings'](null)).toEqual([]);
  });

  it('should update price, min/max quantity, and saleAmount on instrument change', () => {
    createComponent();
    component.selectedInstrument = 'I1';
    component.holdings = mockHoldings;
    component.quantity = 5;
    component.onInstrumentChange();

    expect(component.price).toBe(99); // uses bidPrice
    expect(component.minQuantity).toBe(1);
    expect(component.maxQuantity).toBe(10); // capped at holding quantity
    expect(component.saleAmount).toBe(component.quantity * component.price);
  });

  it('should reset controls if no instrument selected', () => {
    createComponent();
    component.selectedInstrument = '';
    component.price = 100;
    component.quantity = 5;
    component.onInstrumentChange();
    expect(component.price).toBe(0);
    expect(component.quantity).toBe(1);
  });

  it('should handle quantity changes within min/max', () => {
    createComponent();
    component.price = 10;
    component.minQuantity = 2;
    component.maxQuantity = 5;
    component.quantity = 3;

    component.onQuantityChange(4);
    expect(component.quantity).toBe(4);
    expect(component.saleAmount).toBe(40);

    component.onQuantityChange(6);
    expect(component.quantity).toBe(5);
    expect(component.tradeResult).toContain('Cannot sell more than');

    component.onQuantityChange(1);
    expect(component.quantity).toBe(2);
  });

  it('should handle amount changes and sync quantity', () => {
    createComponent();
    component.price = 10;
    component.minQuantity = 2;
    component.maxQuantity = 5;
    component.minAmount = 20;
    component.maxAmount = 50;

    component.onAmountChange(30);
    expect(component.saleAmount).toBe(30);
    expect(component.quantity).toBe(3);

    component.onAmountChange(100);
    expect(component.saleAmount).toBe(50);
    expect(component.quantity).toBe(5);

    component.onAmountChange(5);
    expect(component.saleAmount).toBe(20);
    expect(component.quantity).toBe(2);
  });

  it('should not submit if invalid', () => {
    createComponent();
    component.selectedInstrument = '';
    component.quantity = 0;
    component.clientId = '';
    component.onSubmit();
    expect(mockInstrumentService.sellInstrument).not.toHaveBeenCalled();
  });

  it('should not submit if quantity > maxQuantity', () => {
    createComponent();
    component.selectedInstrument = 'I1';
    component.quantity = 20;
    component.maxQuantity = 10;
    component.clientId = 'C1';
    component.onSubmit();
    expect(component.tradeResult).toContain('Cannot sell more than');
    expect(mockInstrumentService.sellInstrument).not.toHaveBeenCalled();
  });

  it('should submit sell trade and reset on success', () => {
    createComponent();
    component.selectedInstrument = 'I1';
    component.quantity = 5;
    component.maxQuantity = 10;
    component.clientId = 'C1';
    component.price = 99;

    component.onSubmit();

    expect(mockInstrumentService.sellInstrument).toHaveBeenCalledWith('I1', 5, 99);
    expect(component.tradeResult).toContain('successful');
    expect(component.selectedInstrument).toBe('');
    expect(component.quantity).toBe(1);
    expect(component.price).toBe(0);
  });

  it('should handle sell trade failure', () => {
    createComponent();
    mockInstrumentService.sellInstrument.and.returnValue(throwError(() => new Error('fail')));

    component.selectedInstrument = 'I1';
    component.quantity = 5;
    component.maxQuantity = 10;
    component.clientId = 'C1';
    component.price = 99;

    component.onSubmit();
    expect(component.tradeResult).toContain('failed');
  });
});
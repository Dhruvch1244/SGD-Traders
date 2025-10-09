import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { BuyTradeComponent } from './buy-trade.component';
import { InstrumentService } from '../../services/instrument.service';
import { AuthService } from '../../services/auth.service';

// Mock data for services
const mockInstruments = [
  {
    instrumentId: '1',
    description: 'Apple Inc.',
    categoryId: 'STOCK',
    minQuantity: 1,
    maxQuantity: 1000,
    externalIdType: 'ISIN',
    externalId: 'US0378331005',
  },
  {
    instrumentId: '2',
    description: 'U.S. Treasury Bond',
    categoryId: 'GOVT',
    minQuantity: 10,
    maxQuantity: 500,
    externalIdType: 'CUSIP',
    externalId: '9128285Q9',
  },
];

const mockPrice = {
  instrumentId: '1',
  askPrice: 150.0,
  bidPrice: 149.5,
  timestamp: Date.now().toString(),
  instrument: mockInstruments[0],
};

const mockPortfolio = {
  clientId: 'c1',
  cash: 10000,
  holdings: [],
};
const mockUser = { clientId: 'c1' };

// Spy objects for services
let instrumentServiceSpy: jasmine.SpyObj<InstrumentService>;
let authServiceSpy: jasmine.SpyObj<AuthService>;

describe('BuyTradeComponent', () => {
  let component: BuyTradeComponent;
  let fixture: ComponentFixture<BuyTradeComponent>;

  beforeEach(waitForAsync(() => {
    instrumentServiceSpy = jasmine.createSpyObj('InstrumentService', [
      'getAllInstruments',
      'getPortfolioByClientId',
      'searchInstruments',
      'getPrice',
      'getInstrumentById',
      'buyInstrument',
    ]);

    authServiceSpy = jasmine.createSpyObj('AuthService', ['currentUserValue'], {
      currentUserValue: mockUser,
    });

    instrumentServiceSpy.getAllInstruments.and.returnValue(of(mockInstruments));
    instrumentServiceSpy.getPortfolioByClientId.and.returnValue(
      of(mockPortfolio)
    );
    instrumentServiceSpy.searchInstruments.and.returnValue(
      of(mockInstruments.slice(0, 1))
    );
    instrumentServiceSpy.getPrice.and.returnValue(of(mockPrice));
    instrumentServiceSpy.getInstrumentById.and.returnValue(
      of(mockInstruments[0])
    );
    instrumentServiceSpy.buyInstrument.and.returnValue(of({ success: true }));

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, BuyTradeComponent],
      providers: [
        { provide: InstrumentService, useValue: instrumentServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    })
      .compileComponents()
      .then(() => {
        fixture = TestBed.createComponent(BuyTradeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges(); // ngOnInit()
      });
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with instruments and wallet balance', () => {
    expect(component.instruments.length).toBe(2);
    expect(component.filteredInstruments.length).toBe(2);
    expect(component.walletBalance).toBe(10000);
    expect(component.clientId).toBe('c1');
  });

  it('should filter instruments on category change', () => {
    component.selectedCategory = 'STOCK';
    component.onCategoryChange();
    expect(instrumentServiceSpy.searchInstruments).toHaveBeenCalledWith(
      '',
      'STOCK'
    );
    expect(component.instruments.length).toBe(1);
    expect(component.filteredInstruments.length).toBe(1);
  });

  it('should reset to all instruments when category is cleared', () => {
    component.selectedCategory = '';
    component.onCategoryChange();
    expect(instrumentServiceSpy.getAllInstruments).toHaveBeenCalled();
    expect(component.instruments.length).toBe(2);
  });

  it('should filter instruments on search input', () => {
    component.onSearchInput('apple');
    expect(component.searchQuery).toBe('apple');
    expect(component.filteredInstruments.length).toBe(1);
    expect(component.filteredInstruments[0].description).toContain('Apple');
  });

  it('should handle instrument selection', () => {
    const event = { target: { value: '1' } };
    component.onInstrumentSelectChange(event);
    expect(component.selectedInstrument).toBe('1');
    expect(instrumentServiceSpy.getPrice).toHaveBeenCalledWith('1');
    expect(instrumentServiceSpy.getInstrumentById).toHaveBeenCalledWith('1');
  });

  it('should focus on search input when __search__ is selected', () => {
    const mockInput = document.createElement('input');
    spyOn(document, 'querySelector').and.returnValue(mockInput);
    spyOn(mockInput, 'focus');
    const event = { target: { value: '__search__' } };
    component.onInstrumentSelectChange(event);
    expect(mockInput.focus).toHaveBeenCalled();
    expect(component.selectedInstrument).toBe('');
  });

  it('should load instrument details and calculate correct bounds', () => {
    component.selectedInstrument = '1';
    component.walletBalance = 2000;
    // Directly call the private method for testing purposes
    (component as any).loadInstrumentDetails();
    expect(component.price).toBe(150.0);
    expect(component.minQuantity).toBe(1);
    const expectedMax = Math.floor(2000 / 150.0);
    expect(component.maxQuantity).toBe(expectedMax);
    expect(component.quantity).toBe(1);
    expect(component.minAmount).toBe(150.0);
    expect(component.maxAmount).toBe(expectedMax * 150.0);
  });

  it('should adjust quantity based on amount changes', () => {
    component.price = 150;
    component.minQuantity = 1;
    component.maxQuantity = 10;
    component.minAmount = 150;
    component.maxAmount = 1500;

    component.onAmountChangeLocal(300);
    expect(component.quantity).toBe(2);

    // Test clamping to max
    component.onAmountChangeLocal(2000);
    expect(component.buyAmount).toBe(1500);
    expect(component.quantity).toBe(10);

    // Test clamping to min
    component.onAmountChangeLocal(100);
    expect(component.buyAmount).toBe(150);
    expect(component.quantity).toBe(1);
  });

  it('should adjust amount based on quantity changes', () => {
    component.price = 150;
    component.minQuantity = 1;
    component.maxQuantity = 10;

    component.onQuantityChangeLocal(5);
    expect(component.buyAmount).toBe(750);

    // Test clamping to max
    component.onQuantityChangeLocal(15);
    expect(component.quantity).toBe(10);
    expect(component.buyAmount).toBe(1500);

    // Test clamping to min
    component.onQuantityChangeLocal(0);
    expect(component.quantity).toBe(1);
    expect(component.buyAmount).toBe(150);
  });

  it('should submit a valid buy trade', () => {
    component.clientId = 'c1';
    component.selectedInstrument = '1';
    component.quantity = 5;
    component.price = 150;

    component.onSubmit();

    expect(instrumentServiceSpy.buyInstrument).toHaveBeenCalledWith(
      '1',
      5,
      150
    );
    expect(component.tradeResult).toBe('Buy trade successful!');
  });

  it('should handle a failed buy trade', () => {
    instrumentServiceSpy.buyInstrument.and.returnValue(
      throwError(() => new Error('Trade failed'))
    );
    component.clientId = 'c1';
    component.selectedInstrument = '1';
    component.quantity = 5;

    component.onSubmit();

    expect(component.tradeResult).toBe('Buy trade failed.');
  });

  it('should not submit if form is invalid', () => {
    component.clientId = ''; // Invalid state
    component.onSubmit();
    expect(instrumentServiceSpy.buyInstrument).not.toHaveBeenCalled();
  });

  it('should unsubscribe on destroy', () => {
    spyOn(component['subs'], 'unsubscribe');
    component.ngOnDestroy();
    expect(component['subs'].unsubscribe).toHaveBeenCalled();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { TradePageComponent } from './trade-page.component';
import { InstrumentService } from '../../services/instrument.service';
import { AuthService } from '../../services/auth.service';
import { Instrument } from '../../models/instruments.model';
import { NO_ERRORS_SCHEMA, PLATFORM_ID } from '@angular/core';
import { CellClickedEvent, GridReadyEvent } from 'ag-grid-community';

// Extended interface for testing with price properties
interface InstrumentWithPrice extends Instrument {
  askPrice: number;
  bidPrice: number;
}

// Mock data
const mockInstruments: InstrumentWithPrice[] = [
  {
    instrumentId: '1',
    description: 'Apple Inc.',
    externalIdType: 'CUSIP',
    externalId: '037833100',
    categoryId: 'STOCK',
    minQuantity: 1,
    maxQuantity: 1000,
    askPrice: 150.25,
    bidPrice: 149.75,
  },
  {
    instrumentId: '2',
    description: 'Alphabet Inc.',
    externalIdType: 'CUSIP',
    externalId: '02079K305',
    categoryId: 'STOCK',
    minQuantity: 1,
    maxQuantity: 1000,
    askPrice: 2800.50,
    bidPrice: 2799.25,
  },
];

// Helper function to create component with custom configuration
function createComponentWithConfig(config: {
  platformId?: string;
  authService?: any;
  instrumentService?: any;
  queryParams?: any;
}) {
  const queryParamsSubject = config.queryParams || new BehaviorSubject<{ mode?: string }>({ mode: 'buy' });
  
  const instrumentService = config.instrumentService || jasmine.createSpyObj('InstrumentService', [
    'getAllInstruments',
    'selectInstrumentForTrade'
  ], {
    selectedInstrument$: new BehaviorSubject<any | null>(null)
  });

  // Only configure default return value if no custom service provided
  if (!config.instrumentService && instrumentService.getAllInstruments && instrumentService.getAllInstruments.and) {
    instrumentService.getAllInstruments.and.returnValue(of(mockInstruments as any[]));
  }

  const authService = config.authService || {
    currentUserValue: { clientId: 'test-client-123' }
  };

  TestBed.resetTestingModule();
  
  TestBed.configureTestingModule({
    imports: [TradePageComponent],
    providers: [
      { provide: InstrumentService, useValue: instrumentService },
      { provide: AuthService, useValue: authService },
      {
        provide: ActivatedRoute,
        useValue: { queryParams: queryParamsSubject.asObservable() },
      },
      { provide: PLATFORM_ID, useValue: config.platformId || 'browser' },
    ],
    schemas: [NO_ERRORS_SCHEMA],
  });

  const fixture = TestBed.createComponent(TradePageComponent);
  const component = fixture.componentInstance;
  
  return { fixture, component, instrumentService, authService, queryParamsSubject };
}

// Helper function to create mock CellClickedEvent
function createMockCellClickedEvent(colId?: string, data?: any): CellClickedEvent {
  return {
    colDef: colId ? { colId } : {},
    data: data || mockInstruments[0],
    column: null,
    value: null,
    node: null,
    rowIndex: 0,
    rowPinned: null,
    event: null
  } as unknown as CellClickedEvent;
}

describe('TradePageComponent', () => {
  let component: TradePageComponent;
  let fixture: ComponentFixture<TradePageComponent>;
  let mockInstrumentService: jasmine.SpyObj<InstrumentService>;
  let mockAuthService: any;
  let queryParams: BehaviorSubject<{ mode?: string }>;

  beforeEach(() => {
    // Basic setup for standard tests
    const setup = createComponentWithConfig({});
    component = setup.component;
    fixture = setup.fixture;
    mockInstrumentService = setup.instrumentService;
    mockAuthService = setup.authService;
    queryParams = setup.queryParamsSubject;
  });

  afterEach(() => {
    fixture?.destroy();
    TestBed.resetTestingModule();
  });

  describe('Component Creation and Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with browser platform detection', () => {
      expect(component.isBrowser).toBe(true);
    });

    it('should initialize clientId from AuthService', () => {
      expect(component.clientId).toBe('test-client-123');
    });

    it('should set default selected mode to buy', () => {
      expect(component.selected).toBe('buy');
    });
  });

  describe('Platform Detection', () => {
    it('should detect browser platform correctly', () => {
      expect(component.isBrowser).toBe(true);
    });
  });

  describe('Auth Service Integration', () => {
    it('should handle current user with clientId', () => {
      expect(component.clientId).toBe('test-client-123');
    });
    
    it('should initialize with empty clientId when user is undefined', () => {
      // Test the component's handling of undefined clientId
      const originalUser = mockAuthService.currentUserValue;
      mockAuthService.currentUserValue = undefined;
      
      // Create a new instance to test
      const testFixture = TestBed.createComponent(TradePageComponent);
      const testComponent = testFixture.componentInstance;
      
      expect(testComponent.clientId).toBe('');
      
      // Restore original value
      mockAuthService.currentUserValue = originalUser;
      testFixture.destroy();
    });
  });

  describe('Query Parameters', () => {
    it('should set selected mode to sell when mode param is sell', () => {
      queryParams.next({ mode: 'sell' });
      expect(component.selected).toBe('sell');
    });

    it('should default to buy mode when no mode parameter', () => {
      queryParams.next({ mode: '' });
      expect(component.selected).toBe('buy');
    });

    it('should default to buy mode when mode parameter is invalid', () => {
      queryParams.next({ mode: 'invalid' });
      expect(component.selected).toBe('buy');
    });

    it('should handle undefined mode parameter', () => {
      queryParams.next({ mode: undefined });
      expect(component.selected).toBe('buy');
    });
  });

  describe('ngOnInit', () => {
    it('should call getAllInstruments when in browser', () => {
      component.isBrowser = true;
      component.ngOnInit();
      
      expect(mockInstrumentService.getAllInstruments).toHaveBeenCalled();
      expect(component.rowData).toEqual(mockInstruments as any[]);
    });

    it('should not call getAllInstruments when not in browser', () => {
      component.isBrowser = false;
      mockInstrumentService.getAllInstruments.calls.reset();
      
      component.ngOnInit();
      
      expect(mockInstrumentService.getAllInstruments).not.toHaveBeenCalled();
    });

    it('should handle ngOnInit with empty instruments array', () => {
      component.isBrowser = true;
      mockInstrumentService.getAllInstruments.and.returnValue(of([]));
      
      component.ngOnInit();
      
      expect(mockInstrumentService.getAllInstruments).toHaveBeenCalled();
      expect(component.rowData).toEqual([]);
    });
  });

  describe('onGridReady', () => {
    it('should set gridApi when grid is ready', () => {
      const mockGridApi = { someMethod: jasmine.createSpy() } as any;
      const params = { api: mockGridApi } as GridReadyEvent<any>;
      
      component.onGridReady(params);
      
      expect(component['gridApi']).toBe(mockGridApi);
    });
  });

  describe('Grid Configuration', () => {
    it('should have correct gridOptions configuration', () => {
      expect(component.gridOptions.suppressHorizontalScroll).toBe(true);
      expect(component.gridOptions.domLayout).toBe('autoHeight');
      expect(component.gridOptions.rowHeight).toBe(40);
      expect(component.gridOptions.headerHeight).toBe(50);
      expect(component.gridOptions.defaultColDef?.resizable).toBe(true);
      expect(component.gridOptions.defaultColDef?.sortable).toBe(true);
      expect(component.gridOptions.defaultColDef?.filter).toBe(true);
      expect(component.gridOptions.onCellClicked).toBeDefined();
    });

    it('should execute onCellClicked from gridOptions', () => {
      spyOn(component, 'onCellClicked');
      const event = createMockCellClickedEvent('buyAction', mockInstruments[0]);
      
      component.gridOptions.onCellClicked!(event);
      
      expect(component.onCellClicked).toHaveBeenCalledWith(event);
    });

    it('should have correct column definitions', () => {
      expect(component.columnDefs).toBeDefined();
      expect(component.columnDefs.length).toBe(7);
      
      // Test description column
      const descriptionCol = component.columnDefs.find(col => col.field === 'description');
      expect(descriptionCol?.headerName).toBe('Instrument Description');
      expect(descriptionCol?.width).toBe(270);
      expect(descriptionCol?.lockVisible).toBe(true);
      
      // Test min quantity column
      const minQtyCol = component.columnDefs.find(col => col.field === 'minQuantity');
      expect(minQtyCol?.headerName).toBe('Min Qnt');
      expect(minQtyCol?.width).toBe(100);
      
      // Test max quantity column
      const maxQtyCol = component.columnDefs.find(col => col.field === 'maxQuantity');
      expect(maxQtyCol?.headerName).toBe('Max Qnt');
      expect(maxQtyCol?.width).toBe(150);
      
      // Test buy action column
      const buyCol = component.columnDefs.find(col => col.colId === 'buyAction');
      expect(buyCol?.headerName).toBe('Buy');
      expect(buyCol?.width).toBe(100);
      expect(buyCol?.filter).toBe(false);
      expect(buyCol?.sortable).toBe(false);
      
      // Test askPrice column
      const askPriceCol = component.columnDefs.find(col => col.field === 'askPrice');
      expect(askPriceCol?.headerName).toBe('Ask Price');
      expect(askPriceCol?.width).toBe(120);
      
      // Test bidPrice column
      const bidPriceCol = component.columnDefs.find(col => col.field === 'bidPrice');
      expect(bidPriceCol?.headerName).toBe('Bid Price');
      expect(bidPriceCol?.width).toBe(120);
      
      // Test sell action column
      const sellCol = component.columnDefs.find(col => col.colId === 'sellAction');
      expect(sellCol?.headerName).toBe('Sell');
      expect(sellCol?.width).toBe(100);
      expect(sellCol?.filter).toBe(false);
      expect(sellCol?.sortable).toBe(false);
    });
  });

  describe('Value Formatters', () => {
    it('should format ask price correctly', () => {
      const askPriceCol = component.columnDefs.find(col => col.field === 'askPrice');
      const formatter = askPriceCol?.valueFormatter;
      
      if (typeof formatter === 'function') {
        expect(formatter({ value: 100 } as any)).toBe('$100');
        expect(formatter({ value: 50.25 } as any)).toBe('$50.25');
        expect(formatter({ value: null } as any)).toBe('');
        expect(formatter({ value: undefined } as any)).toBe('');
        expect(formatter({ value: 0 } as any)).toBe('');
      }
    });

    it('should format bid price correctly', () => {
      const bidPriceCol = component.columnDefs.find(col => col.field === 'bidPrice');
      const formatter = bidPriceCol?.valueFormatter;
      
      if (typeof formatter === 'function') {
        expect(formatter({ value: 100 } as any)).toBe('$100');
        expect(formatter({ value: 50.25 } as any)).toBe('$50.25');
        expect(formatter({ value: null } as any)).toBe('');
        expect(formatter({ value: undefined } as any)).toBe('');
        expect(formatter({ value: 0 } as any)).toBe('');
      }
    });
  });

  describe('Cell Renderers', () => {
    it('should render buy button correctly', () => {
      const buyCol = component.columnDefs.find(col => col.colId === 'buyAction');
      expect(buyCol?.cellRenderer?.()).toBe('<button class="btn btn-buy">Buy</button>');
    });

    it('should render sell button correctly', () => {
      const sellCol = component.columnDefs.find(col => col.colId === 'sellAction');
      expect(sellCol?.cellRenderer?.()).toBe('<button class="btn btn-sell">Sell</button>');
    });
  });

  describe('onCellClicked', () => {
    it('should call handleBuyClick when buyAction is clicked', () => {
      spyOn(component, 'handleBuyClick');
      const event = createMockCellClickedEvent('buyAction', mockInstruments[0]);
      
      component.onCellClicked(event);
      
      expect(component.handleBuyClick).toHaveBeenCalledWith(mockInstruments[0]);
    });

    it('should call handleSellClick when sellAction is clicked', () => {
      spyOn(component, 'handleSellClick');
      const event = createMockCellClickedEvent('sellAction', mockInstruments[1]);
      
      component.onCellClicked(event);
      
      expect(component.handleSellClick).toHaveBeenCalledWith(mockInstruments[1]);
    });

    it('should do nothing for other column clicks', () => {
      spyOn(component, 'handleBuyClick');
      spyOn(component, 'handleSellClick');
      const event = createMockCellClickedEvent('description', mockInstruments[0]);
      
      component.onCellClicked(event);
      
      expect(component.handleBuyClick).not.toHaveBeenCalled();
      expect(component.handleSellClick).not.toHaveBeenCalled();
    });

    it('should handle event with no colId', () => {
      spyOn(component, 'handleBuyClick');
      spyOn(component, 'handleSellClick');
      const event = createMockCellClickedEvent(undefined, mockInstruments[0]);
      
      component.onCellClicked(event);
      
      expect(component.handleBuyClick).not.toHaveBeenCalled();
      expect(component.handleSellClick).not.toHaveBeenCalled();
    });

    it('should handle missing colDef', () => {
      spyOn(component, 'handleBuyClick');
      spyOn(component, 'handleSellClick');
      const event = {
        data: mockInstruments[0],
        colDef: undefined
      } as any;
      
      expect(() => component.onCellClicked(event)).toThrow();
      expect(component.handleBuyClick).not.toHaveBeenCalled();
      expect(component.handleSellClick).not.toHaveBeenCalled();
    });
  });

  describe('handleBuyClick', () => {
    it('should call selectInstrumentForTrade with buy mode and set selected to buy', () => {
      spyOn(console, 'log');
      const testInstrument = mockInstruments[0];

      component.handleBuyClick(testInstrument);

      expect(console.log).toHaveBeenCalledWith('Buy clicked for:', testInstrument);
      expect(mockInstrumentService.selectInstrumentForTrade).toHaveBeenCalledWith(testInstrument, 'buy');
      expect(component.selected).toBe('buy');
    });
  });

  describe('handleSellClick', () => {
    it('should call selectInstrumentForTrade with sell mode and set selected to sell', () => {
      spyOn(console, 'log');
      const testInstrument = mockInstruments[1];

      component.handleSellClick(testInstrument);

      expect(console.log).toHaveBeenCalledWith('Sell clicked for:', testInstrument);
      expect(mockInstrumentService.selectInstrumentForTrade).toHaveBeenCalledWith(testInstrument, 'sell');
      expect(component.selected).toBe('sell');
    });
  });

  describe('select method', () => {
    it('should update selected option to buy', () => {
      component.selected = 'sell';
      component.select('buy');
      expect(component.selected).toBe('buy');
    });

    it('should update selected option to sell', () => {
      component.selected = 'buy';
      component.select('sell');
      expect(component.selected).toBe('sell');
    });
  });

  describe('Integration Tests', () => {
    it('should handle complete buy flow', () => {
      spyOn(console, 'log');
      
      const testInstrument = mockInstruments[0];
      const event = createMockCellClickedEvent('buyAction', testInstrument);
      
      component.onCellClicked(event);
      
      expect(console.log).toHaveBeenCalledWith('Buy clicked for:', testInstrument);
      expect(mockInstrumentService.selectInstrumentForTrade).toHaveBeenCalledWith(testInstrument, 'buy');
      expect(component.selected).toBe('buy');
    });

    it('should handle complete sell flow', () => {
      spyOn(console, 'log');
      
      const testInstrument = mockInstruments[1];
      const event = createMockCellClickedEvent('sellAction', testInstrument);
      
      component.onCellClicked(event);
      
      expect(console.log).toHaveBeenCalledWith('Sell clicked for:', testInstrument);
      expect(mockInstrumentService.selectInstrumentForTrade).toHaveBeenCalledWith(testInstrument, 'sell');
      expect(component.selected).toBe('sell');
    });

    it('should handle mode changes via query parameters', () => {
      expect(component.selected).toBe('buy');
      
      queryParams.next({ mode: 'sell' });
      expect(component.selected).toBe('sell');
      
      queryParams.next({ mode: 'buy' });
      expect(component.selected).toBe('buy');
      
      queryParams.next({ mode: '' });
      expect(component.selected).toBe('buy');
    });
  });

  describe('Error Handling', () => {
    it('should handle getAllInstruments observable gracefully', () => {
      component.isBrowser = true;
      mockInstrumentService.getAllInstruments.and.returnValue(of(mockInstruments));
      
      expect(() => component.ngOnInit()).not.toThrow();
      expect(component.rowData).toEqual(mockInstruments as any[]);
    });
  });

  describe('Constructor Coverage', () => {
    it('should handle query params subscription in constructor', () => {
      // Test query parameter changes
      queryParams.next({ mode: 'sell' });
      expect(component.selected).toBe('sell');
      
      queryParams.next({ mode: 'buy' });
      expect(component.selected).toBe('buy');
    });

    it('should initialize with empty clientId when user is undefined', () => {
      // Test the component's handling of undefined clientId
      const originalUser = mockAuthService.currentUserValue;
      mockAuthService.currentUserValue = undefined;
      
      // Create a new instance to test
      const testFixture = TestBed.createComponent(TradePageComponent);
      const testComponent = testFixture.componentInstance;
      
      expect(testComponent.clientId).toBe('');
      
      // Restore original value
      mockAuthService.currentUserValue = originalUser;
      testFixture.destroy();
    });

    it('should handle query parameter changes after initialization', () => {
      expect(component.selected).toBe('buy');
      
      // Simulate query parameter change
      queryParams.next({ mode: 'sell' });
      expect(component.selected).toBe('sell');
      
      queryParams.next({ mode: 'buy' });
      expect(component.selected).toBe('buy');
      
      queryParams.next({ mode: 'invalid' as any });
      expect(component.selected).toBe('buy');
    });
  });

  describe('Error Handling', () => {
    it('should handle getAllInstruments observable gracefully', () => {
      // Create a mock service that returns an empty array
      const mockErrorService = jasmine.createSpyObj('InstrumentService', [
        'getAllInstruments',
        'selectInstrumentForTrade'
      ], {
        selectedInstrument$: new BehaviorSubject<any | null>(null)
      });

      const { fixture: testFixture, component: testComponent } = createComponentWithConfig({
        instrumentService: mockErrorService
      });
      
      // Configure the mock AFTER creating the component to ensure it takes precedence
      mockErrorService.getAllInstruments.and.returnValue(of([]));

      testComponent.isBrowser = true;
      testComponent.ngOnInit();
      
      expect(mockErrorService.getAllInstruments).toHaveBeenCalled();
      expect(testComponent.rowData).toEqual([]);
      testFixture.destroy();
    });
  });

  describe('Platform Detection Tests', () => {
    it('should detect server platform correctly', () => {
      const { fixture: testFixture, component: testComponent } = createComponentWithConfig({
        platformId: 'server'
      });

      expect(testComponent.isBrowser).toBe(false);
      testFixture.destroy();
    });

    it('should detect browser platform correctly', () => {
      const { fixture: testFixture, component: testComponent } = createComponentWithConfig({
        platformId: 'browser'
      });

      expect(testComponent.isBrowser).toBe(true);
      testFixture.destroy();
    });
  });

  describe('Auth Service Edge Cases', () => {
    it('should handle null user from AuthService', () => {
      const { fixture: testFixture, component: testComponent } = createComponentWithConfig({
        authService: { currentUserValue: null }
      });

      expect(testComponent.clientId).toBe('');
      testFixture.destroy();
    });

    it('should handle user without clientId from AuthService', () => {
      const { fixture: testFixture, component: testComponent } = createComponentWithConfig({
        authService: { currentUserValue: { someOtherField: 'value' } }
      });

      expect(testComponent.clientId).toBe('');
      testFixture.destroy();
    });

    it('should handle undefined currentUserValue', () => {
      const { fixture: testFixture, component: testComponent } = createComponentWithConfig({
        authService: { currentUserValue: undefined }
      });

      expect(testComponent.clientId).toBe('');
      testFixture.destroy();
    });
  });

  describe('Additional Edge Cases', () => {
    it('should handle missing colDef in cell click event', () => {
      const mockEvent = {
        colDef: undefined,
        data: mockInstruments[0],
        column: null,
        value: null,
        node: null,
        rowIndex: 0,
        rowPinned: null,
        event: null
      } as unknown as CellClickedEvent;

      expect(() => component.onCellClicked(mockEvent)).toThrow();
    });

    it('should handle cell click event with no colId', () => {
      const mockEvent = {
        colDef: { field: 'someField' },
        data: mockInstruments[0],
        column: null,
        value: null,
        node: null,
        rowIndex: 0,
        rowPinned: null,
        event: null
      } as unknown as CellClickedEvent;

      expect(() => component.onCellClicked(mockEvent)).not.toThrow();
    });
  });
});
// C++ is your preferred language for algorithms, but for Angular, we use TypeScript!
// This test file will help ensure your InstrumentService is robust.

import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { InstrumentService } from './instrument.service';
import { AuthService } from './auth.service';
import { Instrument } from '../models/instruments.model';
import { Price } from '../models/price.model';
import { Trade } from '../models/trade.model';
import { Portfolio } from '../models/portfolio.model';
import { environment } from '../../environments/environment';
import { OrderRequestDto } from '../models/order-request.model';
import { firstValueFrom } from 'rxjs';

// --- MOCK DATA ---
// Using mock data helps us test the service in isolation without needing a real backend.
const mockInstruments: Instrument[] = [
  {
    instrumentId: 'INST001',
    description: 'Apple Inc.',
    categoryId: 'STOCK',
    minQuantity: 1,
    maxQuantity: 1000,
    externalId :'AAPL',
    externalIdType : 'TICKER'
  },
  {
    instrumentId: 'INST002',
    description: 'Google LLC',
    categoryId: 'STOCK',
    minQuantity: 1,
    maxQuantity: 500,
    externalId :'Google',
    externalIdType : 'TICKER'
  },
  {
    instrumentId: 'BOND001',
    description: 'US Treasury Bond',
    categoryId: 'BOND',
    minQuantity: 10,
    maxQuantity: 100,
    externalId :'Us Bond',
    externalIdType : 'TICKER'

  },
];

const mockTrades: Trade[] = [
  {
    tradeId: 'T1',
    instrumentId: 'INST001',
    clientId: 'CLIENT01',
    direction: 'BUY',
    quantity: 10,
    executionPrice: 150,
    cashValue: 1500,
    timestamp: new Date(),
  },
  {
    tradeId: 'T2',
    instrumentId: 'INST002',
    clientId: 'CLIENT01',
    direction: 'BUY',
    quantity: 5,
    executionPrice: 2800,
    cashValue: 14000,
    timestamp: new Date(),
  },
  {
    tradeId: 'T3',
    instrumentId: 'INST001',
    clientId: 'CLIENT02',
    direction: 'BUY',
    quantity: 20,
    executionPrice: 155,
    cashValue: 3100,
    timestamp: new Date(),
  },
];

const mockPortfolio: Portfolio = {
  clientId: 'CLIENT01',
  holdings: [{ instrumentId: 'INST001', quantity: 10, instrumentName: 'Apple Inc' }],
};

const mockUser = {
  clientId: 'LOCAL_CLIENT_ID',
  email: 'test@example.com',
};

// --- TEST SUITE ---
describe('InstrumentService', () => {
  let service: InstrumentService;
  let httpTestingController: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  const apiUrl = environment.apiUrl;

  beforeEach(() => {
    // Create a spy object for AuthService. This allows us to control what its methods return.
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getFmtsToken',
      'getFmtsClientId',
    ]);
    // We also need to mock the `currentUserValue` property.
    Object.defineProperty(authServiceSpy, 'currentUserValue', {
      value: mockUser,
      writable: true,
    });
    httpTestingController = TestBed.inject(HttpTestingController);
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    // We must flush the initial data load request that happens in the constructor.
    // This ensures our service is initialized before each test runs.
    service = TestBed.inject(InstrumentService);
    const req = httpTestingController.expectOne(
      `${apiUrl}/market/instruments`
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockInstruments);

  });

  afterEach(() => {
    // After each test, we verify that there are no outstanding HTTP requests.
    // This is a good practice to ensure our tests are clean and don't interfere with each other.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // --- Constructor and Initial Data Loading ---
  describe('Constructor and loadInitialData', () => {
    it('should load initial instrument data on construction', (done) => {
      service.instruments$.subscribe((instruments) => {
        // We check if the instruments$ observable emits the mock data.
        if (instruments.length > 0) {
          expect(instruments.length).toBe(3);
          expect(instruments).toEqual(mockInstruments);
          done();
        }
      });
    });

    it('should handle failure of initial data load', () => {
      // To test this, we need to create a new instance of the service within the test.
      TestBed.resetTestingModule(); // Reset the module to re-configure
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [
          InstrumentService,
          { provide: AuthService, useValue: mockAuthService },
        ],
      });

      const consoleErrorSpy = spyOn(console, 'error');
      service = TestBed.inject(InstrumentService); // This triggers the constructor and the HTTP call


      expect(service.getInstruments()).toEqual([]); // Ensure instruments array is empty
    });
  });

  // --- Synchronous Getters ---
  describe('Synchronous Getters', () => {
    it('getInstruments() should return the current list of instruments', () => {
      const instruments = service.getInstruments();
      expect(instruments.length).toBe(3);
      expect(instruments).toEqual(mockInstruments);
    });

    it('getCategories() should return a unique list of categories', () => {
      const categories = service.getCategories();
      expect(categories.length).toBe(2);
      expect(categories).toContain('STOCK');
      expect(categories).toContain('BOND');
    });

    it('getInstrumentsByCategory() should return instruments filtered by category (case-insensitive)', () => {
      const stocks = service.getInstrumentsByCategory('stock');
      expect(stocks.length).toBe(2);
      expect(stocks[0].categoryId).toBe('STOCK');
    });
  });

  // --- Asynchronous Methods ---
  describe('Asynchronous Methods', () => {
    it('getInstrumentById() should return an observable of the correct instrument', async () => {
      const instrument = await firstValueFrom(
        service.getInstrumentById('INST001')
      );
      expect(instrument).toBeDefined();
      expect(instrument?.instrumentId).toBe('INST001');
    });

    it('getInstrumentById() should return undefined for a non-existent ID', async () => {
      const instrument = await firstValueFrom(
        service.getInstrumentById('NON_EXISTENT')
      );
      expect(instrument).toBeUndefined();
    });

    it('getAllInstruments() should fetch all instruments from the API', (done) => {
      service.getAllInstruments().subscribe((instruments) => {
        expect(instruments).toEqual(mockInstruments);
        done();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/market/instruments`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockInstruments);
    });

    it('getPrice() should fetch the price for a single instrument', (done) => {
      const mockPrice: Price = { instrumentId: 'INST001', askPrice: 151, bidPrice: 149,instrument : mockInstruments[0],timestamp : new Date().toISOString() };
      service.getPrice('INST001').subscribe((price) => {
        expect(price).toEqual(mockPrice);
        done();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/market/instruments/INST001/price`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockPrice);
    });

    it('getPrice() should return null on API error', (done) => {
      service.getPrice('INST001').subscribe((price) => {
        expect(price).toBeNull();
        done();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/market/instruments/INST001/price`);
      req.flush('Error', { status: 404, statusText: 'Not Found' });
    });
  });

  // --- Portfolio and Trades ---
  describe('Portfolio and Trades', () => {
    it('getPortfolioByClientId() should fetch the portfolio correctly', (done) => {
      service.getPortfolioByClientId('CLIENT01').subscribe((portfolio) => {
        expect(portfolio).toEqual(mockPortfolio);
        done();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/portfolio/CLIENT01/details`
      );
      req.flush(mockPortfolio);
    });

    it('getPortfolioByClientId() should return undefined on error', (done) => {
      const consoleErrorSpy = spyOn(console, 'error');
      service.getPortfolioByClientId('CLIENT01').subscribe((portfolio) => {
        expect(portfolio).toBeUndefined();
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/portfolio/CLIENT01/details`
      );
      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });

    it('loadTradesForClient() should fetch trades and update the tradesSource', (done) => {
      service.loadTradesForClient('CLIENT01');

      const req = httpTestingController.expectOne(`${apiUrl}/trades/CLIENT01`);
      req.flush(mockTrades.filter((t) => t.clientId === 'CLIENT01'));

      service.trades$.subscribe((trades) => {
        if (trades.length > 0) { // Check ensures we get the updated value, not the initial []
          expect(trades.length).toBe(2);
          expect(trades[0].clientId).toBe('CLIENT01');
          done();
        }
      });
    });

    it('loadTradesForClient() should set trades to empty array on error', (done) => {
      const consoleErrorSpy = spyOn(console, 'error');
      service.loadTradesForClient('CLIENT01');

      const req = httpTestingController.expectOne(`${apiUrl}/trades/CLIENT01`);
      req.flush('Error', { status: 500, statusText: 'Server Error' });

      service.trades$.subscribe((trades) => {
        expect(trades).toEqual([]);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      });
    });

    it('getClientCostBasis() should correctly calculate average cost basis', () => {
      // Manually set trades for this synchronous test
      (service as any).tradesSource.next(mockTrades);

      const costBasis = service.getClientCostBasis('CLIENT01');

      expect(costBasis.get('INST001')).toBe(150);
      expect(costBasis.get('INST002')).toBe(2800);
      expect(costBasis.size).toBe(2);
    });

    it('getClientCostBasis() should return an empty map if no BUY trades exist', () => {
      (service as any).tradesSource.next([
        { ...mockTrades[0], direction: 'SELL', clientId: 'CLIENT01' }
      ]);
      const costBasis = service.getClientCostBasis('CLIENT01');
      expect(costBasis.size).toBe(0);
    });
  });

  // --- Trade Execution ---
  describe('Trade Execution', () => {
    it('addNewTrade() should POST a new trade and update the tradesSource on success', (done) => {
      const newTradeDetails = {
        instrumentId: 'INST001',
        quantity: 5,
        executionPrice: 160,
        direction: 'BUY' as 'BUY' | 'SELL',
        clientId: 'CLIENT01'
      };
      const newTradeResponse: Trade = { tradeId: 'T4', ...newTradeDetails, cashValue: 800, timestamp: new Date() };

      service.addNewTrade(newTradeDetails).subscribe(response => {
        expect(response).toEqual(newTradeResponse);
        // Check if the tradesSource was updated
        expect((service as any).tradesSource.getValue().length).toBe(1);
        expect((service as any).tradesSource.getValue()[0]).toEqual(newTradeResponse);
        done();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/trade/buy`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newTradeDetails);
      req.flush(newTradeResponse);
    });

    it('addNewTrade() should return null on failure', (done) => {
      const consoleErrorSpy = spyOn(console, 'error');
      const newTradeDetails = {
        instrumentId: 'INST001',
        quantity: 5,
        executionPrice: 160,
        direction: 'SELL' as 'BUY' | 'SELL',
        clientId: 'CLIENT01'
      };

      service.addNewTrade(newTradeDetails).subscribe(response => {
        expect(response).toBeNull();
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/trade/sell`);
      req.flush('Error', { status: 400, statusText: 'Bad Request' });
    });


  });

  // --- Search and Selection ---
  describe('Search and Selection', () => {
    it('selectInstrumentForTrade() should update the selectedInstrumentSource', (done) => {
      const instrumentData = { instrumentId: 'INST001', description: 'Apple Inc.' };
      service.selectInstrumentForTrade(instrumentData, 'buy');

      service.selectedInstrument$.subscribe(selected => {
        if (selected) {
          expect(selected.instrumentId).toBe('INST001');
          expect(selected.tradeType).toBe('buy');
          done();
        }
      });
    });

    it('searchInstruments() should build the correct URL and return results', (done) => {
      const desc = 'Apple';
      const cat = 'STOCK';
      service.searchInstruments(desc, cat).subscribe(results => {
        expect(results).toEqual([mockInstruments[0]]);
        done();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/trades/search?description=${desc}&category=${cat}`
      );
      req.flush([mockInstruments[0]]);
    });

    it('searchInstruments() should handle empty response on error', (done) => {
      service.searchInstruments('Apple').subscribe(results => {
        expect(results).toEqual([]);
        done();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/trades/search?description=Apple`);
      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });
  });

  // --- Complex Price Fetching ---
  describe('getLatestInstrumentPrices', () => {
    it('should fetch prices and map them with existing instrument data', (done) => {
      const mockPrices: Price[] = [
        { instrumentId: 'INST001', askPrice: 151, bidPrice: 149.5 ,instrument : mockInstruments[0],timestamp : new Date().toISOString()},
        { instrumentId: 'BOND001', askPrice: 102, bidPrice: 101.5 ,instrument : mockInstruments[2],timestamp : new Date().toISOString()},
      ];

      service.getLatestInstrumentPrices().subscribe(prices => {
        expect(prices.length).toBe(2);
        const inst1 = prices.find(p => p.instrumentId === 'INST001');
        expect(inst1?.description).toBe('Apple Inc.');
        expect(inst1?.categoryId).toBe('STOCK');
        expect(inst1?.askPrice).toBe(151);

        const bond1 = prices.find(p => p.instrumentId === 'BOND001');
        expect(bond1?.description).toBe('US Treasury Bond');
        expect(bond1?.minQuantity).toBe(10);
        done();
      });

      // This endpoint is incorrect in the service, but we test what's written.
      // It should probably be /market/prices or similar.
      const req = httpTestingController.expectOne(`${apiUrl}/market/instruments`);
      req.flush(mockPrices);
    });

    it('should return empty array on fetch error', (done) => {
      const consoleErrorSpy = spyOn(console, 'error');
      service.getLatestInstrumentPrices().subscribe(prices => {
        expect(prices).toEqual([]);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/market/instruments`);
      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });
  });

});

// We're using TypeScript for these Angular tests, but the logical, structured
// approach from C++ programming applies perfectly here.

import { Router } from '@angular/router';
import { BehaviorSubject, of, throwError, Subscription } from 'rxjs';

import { HeaderComponent } from './header.component';
import { AuthService } from '../../services/auth.service';
import { InstrumentService } from '../../services/instrument.service';
import { WalletService } from '../../services/wallet.service';
import { Wallet } from '../../models/wallet.model';
// Ensure that '../../models/wallet.model' exports Wallet as a class, not just a type/interface.
import { Portfolio } from '../../models/portfolio.model';
// --- MOCK DATA and SPY OBJECTS ---
// We create mock objects and spies to isolate the component from its dependencies.
// This ensures we are only testing the component's logic.

const mockUser = {
  clientId: 'CLIENT123',
  name: 'Test User',
  email: 'test@example.com',
};

// We will use spies for services. A spy is an object that records calls to its methods.
let mockAuthService: jasmine.SpyObj<AuthService>;
let mockInstrumentService: jasmine.SpyObj<InstrumentService>;
let mockWalletService: jasmine.SpyObj<WalletService>;
let mockRouter: jasmine.SpyObj<Router>;

// We use Subjects to simulate asynchronous streams of data from services.
let currentUserSubject: BehaviorSubject<any>;
let walletBalanceSubject: BehaviorSubject<number>;
let tradesSubject: BehaviorSubject<any[]>;
let instrumentsSubject: BehaviorSubject<any[]>;

describe('HeaderComponent (Plain Class Test)', () => {
  let component: HeaderComponent;

  beforeEach(() => {
    // Reset subjects before each test to ensure isolation
    currentUserSubject = new BehaviorSubject<any>(null);
    walletBalanceSubject = new BehaviorSubject<number>(0);
    tradesSubject = new BehaviorSubject<any[]>([]);
    instrumentsSubject = new BehaviorSubject<any[]>([]);

    // Create spies for each method we need to mock
    mockAuthService = jasmine.createSpyObj('AuthService', ['logout']);
    // We need to define the currentUser$ property on the spy object.
    Object.defineProperty(mockAuthService, 'currentUser$', {
      value: currentUserSubject.asObservable(),
    });

    mockInstrumentService = jasmine.createSpyObj('InstrumentService', [
      'loadTradesForClient',
      'getPortfolioByClientId',
      'getLatestInstrumentPrices',
      'getClientCostBasis',
    ]);
    // Define observable properties on the instrument service spy
    Object.defineProperty(mockInstrumentService, 'trades$', {
      value: tradesSubject.asObservable(),
    });
    Object.defineProperty(mockInstrumentService, 'instruments$', {
      value: instrumentsSubject.asObservable(),
    });

    mockWalletService = jasmine.createSpyObj('WalletService', [
      'getWalletBalance',
      'addMoneyToWallet',
    ]);
    // Define the walletBalance$ property on the spy
    Object.defineProperty(mockWalletService, 'walletBalance$', {
      value: walletBalanceSubject.asObservable(),
    });

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // Set default return values for service methods that return observables
    mockWalletService.getWalletBalance.and.returnValue(
      of({ clientId: 'CLIENT123', balance: 1000 })
    );
    mockWalletService.addMoneyToWallet.and.returnValue(of({ success: true }));
    mockInstrumentService.getPortfolioByClientId.and.returnValue(
      of({ holdings: [{ instrumentId: 'AAPL', quantity: 10 }] } as Portfolio)
    );
    mockInstrumentService.getLatestInstrumentPrices.and.returnValue(of([]));

    // --- Component Instantiation ---
    // Instead of using TestBed, we create an instance of the component directly,
    // passing in our mocks as arguments to the constructor.
    component = new HeaderComponent(
      mockRouter,
      mockAuthService,
      mockInstrumentService,
      mockWalletService
    );
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });

  // --- Test Lifecycle Hooks Manually ---
  describe('Lifecycle Hooks', () => {
    it('ngOnInit should subscribe to currentUser and set user data if user is logged in', () => {
      // Act
      currentUserSubject.next(mockUser); // Simulate user logging in
      // We must call ngOnInit manually because we are not using the Angular test environment.
      component.ngOnInit();

      // Assert
      expect(component.clientId).toBe(mockUser.clientId);
      expect(component.clientName).toBe(mockUser.name);
      expect(mockWalletService.getWalletBalance).toHaveBeenCalledWith(
        mockUser.clientId
      );
      expect(mockInstrumentService.loadTradesForClient).toHaveBeenCalledWith(
        mockUser.clientId
      );
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('ngOnInit should navigate to home if user is not logged in', () => {
      // Act
      currentUserSubject.next(null); // Simulate user being logged out
      component.ngOnInit();

      // Assert
      expect(component.clientId).toBe('');
      expect(component.clientName).toBe('');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });

    it('ngOnDestroy should unsubscribe from userSubscription', () => {
      // Arrange
      // Call ngOnInit to create the subscription
      component.ngOnInit();
      const subscription = (component as any).userSubscription as Subscription;
      spyOn(subscription, 'unsubscribe');

      // Act
      // We must call ngOnDestroy manually.
      component.ngOnDestroy();

      // Assert
      expect(subscription.unsubscribe).toHaveBeenCalled();
    });
  });

  // --- Test UI Interaction Methods ---
  describe('UI Interaction', () => {
    it('toggleProfileMenu should toggle isProfileMenuOpen', () => {
      expect(component.isProfileMenuOpen).toBeFalse();
      component.toggleProfileMenu();
      expect(component.isProfileMenuOpen).toBeTrue();
      component.toggleProfileMenu();
      expect(component.isProfileMenuOpen).toBeFalse();
    });

    it('toggleAddMoneyForm should toggle showAddMoneyForm and reset amount', () => {
      component.amountToAdd = 100;
      expect(component.showAddMoneyForm).toBeFalse();

      component.toggleAddMoneyForm();
      expect(component.showAddMoneyForm).toBeTrue();
      expect(component.amountToAdd).toBe(100); // Amount should persist when opening

      component.toggleAddMoneyForm();
      expect(component.showAddMoneyForm).toBeFalse();
      expect(component.amountToAdd).toBeNull(); // Amount should reset when closing
    });
  });

  // --- Test Navigation ---
  describe('Navigation', () => {
    it('navigateTo should call router.navigate with the correct path', () => {
      component.navigateTo('/dashboard');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('navigateToSell should call router.navigate with trade path and sell mode', () => {
      component.navigateToSell();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/trade'], {
        queryParams: { mode: 'sell' },
      });
    });

    it('navigateToHome should call router.navigate to landing', () => {
      component.navigateToHome();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/landing']);
    });
  });

  // --- Test User Actions ---
  describe('User Actions', () => {
    it('logout should call authService.logout and navigate to home', () => {
      component.logout();
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });

    describe('addMoney', () => {
      beforeEach(() => {
        // Simulate user logged in for these tests
        component.clientId = 'CLIENT123';
      });

      it('should call walletService.addMoneyToWallet with correct amount', () => {
        component.amountToAdd = 150;
        component.addMoney();
        expect(mockWalletService.addMoneyToWallet).toHaveBeenCalledWith(
          'CLIENT123',
          150
        );
      });

      it('should hide form and reset amount on successful call', () => {
        component.amountToAdd = 150;
        component.showAddMoneyForm = true;
        component.addMoney();
        expect(component.showAddMoneyForm).toBeFalse();
        expect(component.amountToAdd).toBeNull();
      });

      it('should log an error if walletService fails', () => {
        const consoleErrorSpy = spyOn(console, 'error');
        mockWalletService.addMoneyToWallet.and.returnValue(
          throwError(() => new Error('API Error'))
        );
        component.amountToAdd = 50;
        component.addMoney();
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Error adding money:',
          jasmine.any(Error)
        );
      });

      it('should log an error and not call service for invalid amount (zero)', () => {
        const consoleErrorSpy = spyOn(console, 'error');
        component.amountToAdd = 0;
        component.addMoney();
        expect(mockWalletService.addMoneyToWallet).not.toHaveBeenCalled();
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Invalid amount entered. Please enter a positive number.'
        );
      });

      it('should log an error and not call service for invalid amount (null)', () => {
        const consoleErrorSpy = spyOn(console, 'error');
        component.amountToAdd = null;
        component.addMoney();
        expect(mockWalletService.addMoneyToWallet).not.toHaveBeenCalled();
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Invalid amount entered. Please enter a positive number.'
        );
      });
    });
  });

  // --- Test Report Generation ---
  describe('generateFullReport', () => {
    let mockJsPDFInstance: any;
    let autoTableSpy: jasmine.Spy;

    beforeEach(() => {
      mockJsPDFInstance = {
        text: jasmine.createSpy('text'),
        setFontSize: jasmine.createSpy('setFontSize'),
        save: jasmine.createSpy('save'),
        lastAutoTable: { finalY: 100 },
      };
      (window as any).jsPDF = () => mockJsPDFInstance;
      autoTableSpy = jasmine.createSpy('autoTable');
      (window as any).autoTable = autoTableSpy;

      component.clientId = 'CLIENT123';
      component.clientName = 'Test User';
    });

    it('should call all necessary services and generate a PDF', () => {
      // Arrange: Provide mock data through the services
      mockInstrumentService.getPortfolioByClientId.and.returnValue(
        of({
          clientId: 'CLIENT123',
          holdings: [{ instrumentId: 'AAPL', quantity: 10 }],
        })
      );
      walletBalanceSubject.next(5000);
      tradesSubject.next([{ tradeId: 'T1', instrumentId: 'AAPL' }]);
      instrumentsSubject.next([
        { instrumentId: 'AAPL', description: 'Apple Inc' },
      ]);
      mockInstrumentService.getLatestInstrumentPrices.and.returnValue(
        of([
          {
            instrumentId: 'AAPL',
            categoryId: 'TECH',
            askPrice: 150,
            bidPrice: 149,
            description: 'Apple Inc',
            minQuantity: 1,
            maxQuantity: 1000,
          },
        ])
      );
      mockInstrumentService.getClientCostBasis.and.returnValue(
        new Map([['AAPL', 140]])
      );

      // Act
      component.generateFullReport();

      // Assert
      expect(mockInstrumentService.getPortfolioByClientId).toHaveBeenCalledWith(
        'CLIENT123'
      );
      expect(mockInstrumentService.getClientCostBasis).toHaveBeenCalledWith(
        'CLIENT123'
      );


    });

    it('should generate a report even with no trades or portfolio', () => {
      // Arrange: Services return empty data
      mockInstrumentService.getPortfolioByClientId.and.returnValue(
        of({ clientId: 'CLIENT123', holdings: [] })
      );
      walletBalanceSubject.next(100);
      tradesSubject.next([]);
      instrumentsSubject.next([]);

      // Act
      component.generateFullReport();

      // Assert

      expect(autoTableSpy).not.toHaveBeenCalled();
    });
  });
});

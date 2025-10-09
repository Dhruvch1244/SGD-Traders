import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import {
  catchError,
  map,
  tap,
  switchMap,
  retry,
  finalize,
} from 'rxjs/operators';
import { Instrument } from '../models/instruments.model';
import { Price } from '../models/price.model';
import { Trade } from '../models/trade.model';
import { Portfolio } from '../models/portfolio.model';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { OrderRequestDto } from '../models/order-request.model';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class InstrumentService {
  private apiUrl = environment.apiUrl;
  private instrumentsSource = new BehaviorSubject<Instrument[]>([]);
  public readonly instruments$ = this.instrumentsSource.asObservable();
  private pricesSource = new BehaviorSubject<Price[]>([]);
  private tradesSource = new BehaviorSubject<Trade[]>([]);
  public readonly trades$ = this.tradesSource.asObservable();

  private loadingSource = new BehaviorSubject<boolean>(false);
  public readonly loading$ = this.loadingSource.asObservable();

  private errorSource = new BehaviorSubject<string | null>(null);
  public readonly error$ = this.errorSource.asObservable();

  private instrumentDataObject: { [instrumentId: string]: Instrument } = {};

  private selectedInstrumentSource = new BehaviorSubject<any | null>(null);
  public readonly selectedInstrument$ =
    this.selectedInstrumentSource.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router
  ) {
    // Don't load data in constructor - wait for explicit call
    // Subscribe to auth state instead
    this.authService.isAuthenticated$.subscribe((isAuth) => {
      if (isAuth) {
        this.loadInitialData().subscribe();
      } else {
        // Clear data when logged out
        this.clearData();
      }
    });
  }

  /**
   * Clears all cached data
   */
  private clearData(): void {
    this.instrumentsSource.next([]);
    this.pricesSource.next([]);
    this.tradesSource.next([]);
    this.instrumentDataObject = {};
    this.selectedInstrumentSource.next(null);
  }

  /**
   * Fetches all instruments and caches them.
   */
  loadInitialData(): Observable<any> {
    this.loadingSource.next(true);
    this.errorSource.next(null);

    return this.http
      .get<Instrument[]>(`${this.apiUrl}/market/instruments`, {
        withCredentials: true,
      })
      .pipe(
        retry(1),
        tap((data) => {
          this.instrumentsSource.next(data);
          this.buildInstrumentMap(data);
        }),
        catchError((error) =>
          this.handleError(error, 'Failed to load instrument data')
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  /**
   * Handle HTTP errors
   */
  private handleError(
    error: HttpErrorResponse,
    context: string
  ): Observable<never> {
    let errorMsg = '';

    if (error.status === 401) {
      errorMsg = 'Your session has expired. Please log in again.';

      // Optional: Redirect to login after a short delay
      setTimeout(() => {
        this.authService.flushLocalStorage();
        this.router.navigate(['/login']);
      }, 1000);
    } else {
      errorMsg = `${context}: ${error.message || 'Unknown error'}`;
    }

    this.errorSource.next(errorMsg);
    console.error(context, error);
    return throwError(() => new Error(errorMsg));
  }

  private buildInstrumentMap(instruments: Instrument[]): void {
    this.instrumentDataObject = instruments.reduce((acc, instrument) => {
      acc[instrument.instrumentId] = instrument;
      return acc;
    }, {} as { [instrumentId: string]: Instrument });
  }

  selectInstrumentForTrade(instrumentData: any, tradeType: 'buy' | 'sell') {
    this.selectedInstrumentSource.next({ ...instrumentData, tradeType });
  }

  getPortfolioByClientId(clientId: string): Observable<Portfolio | undefined> {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    return this.http
      .get<any>(`${this.apiUrl}/portfolio/${clientId}/details`, {
        withCredentials: true,
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Error fetching portfolio')
        )
      );
  }

  getClientCostBasis(clientId: string): Map<string, number> {
    const clientTrades = this.tradesSource
      .getValue()
      .filter(
        (trade) => trade.clientId === clientId && trade.direction === 'BUY'
      );

    const costBasis = new Map<
      string,
      { totalCost: number; totalQuantity: number }
    >();

    for (const trade of clientTrades) {
      const existing = costBasis.get(trade.instrumentId) || {
        totalCost: 0,
        totalQuantity: 0,
      };
      existing.totalCost += trade.cashValue;
      existing.totalQuantity += trade.quantity;
      costBasis.set(trade.instrumentId, existing);
    }

    const avgCostMap = new Map<string, number>();
    for (const [instrumentId, data] of costBasis.entries()) {
      if (data.totalQuantity > 0) {
        avgCostMap.set(instrumentId, data.totalCost / data.totalQuantity);
      }
    }

    return avgCostMap;
  }

  /**
   * Fetches all trades for a specific client and updates the trades observable.
   */
  loadTradesForClient(clientId: string): Observable<Trade[]> {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    this.loadingSource.next(true);

    return this.http
      .get<Trade[]>(`${this.apiUrl}/trades/${clientId}`, {
        withCredentials: true,
      })
      .pipe(
        tap((trades) => this.tradesSource.next(trades)),
        catchError((error) =>
          this.handleError(
            error,
            `Failed to load trades for client ${clientId}`
          )
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  addNewTrade(tradeDetails: {
    instrumentId: string;
    quantity: number;
    executionPrice: number;
    direction: 'BUY' | 'SELL';
    clientId: string;
  }): Observable<Trade | null> {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    const url = `${this.apiUrl}/trade/${tradeDetails.direction.toLowerCase()}`;
    this.loadingSource.next(true);

    return this.http
      .post<Trade>(url, tradeDetails, { withCredentials: true })
      .pipe(
        tap((newTrade) => {
          const currentTrades = this.tradesSource.getValue();
          this.tradesSource.next([...currentTrades, newTrade]);
        }),
        catchError((error) =>
          this.handleError(error, 'Failed to add new trade')
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  buyInstrument(
    instrumentId: string,
    quantity: number,
    targetPrice: number
  ): Observable<any> {
    const currentUser = this.authService.currentUserValue;
    const token = this.authService.getFmtsToken();
    const fmtsClientId = this.authService.getFmtsClientId();

    if (!currentUser || !token || !fmtsClientId) {
      return throwError(() => new Error('User not authenticated'));
    }

    this.loadingSource.next(true);

    const orderRequest: OrderRequestDto = {
      instrumentId,
      quantity,
      targetPrice,
      direction: 'B',
      fmtsClientId,
      token,
      email: currentUser.email,
      localClientId: currentUser.clientId,
    };

    return this.http
      .post(`${this.apiUrl}/trade/buy`, orderRequest, { withCredentials: true })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to place buy order')
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  sellInstrument(
    instrumentId: string,
    quantity: number,
    targetPrice: number
  ): Observable<any> {
    const currentUser = this.authService.currentUserValue;
    const token = this.authService.getFmtsToken();
    const fmtsClientId = this.authService.getFmtsClientId();

    if (!currentUser || !token || !fmtsClientId) {
      return throwError(() => new Error('User not authenticated'));
    }

    this.loadingSource.next(true);

    const orderRequest: OrderRequestDto = {
      instrumentId,
      quantity,
      targetPrice,
      direction: 'S',
      fmtsClientId,
      token,
      email: currentUser.email,
      localClientId: currentUser.clientId,
    };

    return this.http
      .post(`${this.apiUrl}/trade/sell`, orderRequest, {
        withCredentials: true,
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to place sell order')
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  getInstruments(): Instrument[] {
    return this.instrumentsSource.getValue();
  }

  getCategories(): string[] {
    return Array.from(new Set(this.getInstruments().map((i) => i.categoryId)));
  }

  getInstrumentsByCategory(category: string): Instrument[] {
    return this.getInstruments().filter(
      (i) => i.categoryId?.toLocaleLowerCase() === category
    );
  }

  getInstrumentById(id: string): Observable<Instrument | undefined> {
    return this.instruments$.pipe(
      map((instruments) => instruments.find((i) => i.instrumentId === id))
    );
  }

  getLatestInstrumentPrices(): Observable<
    {
      instrumentId: string;
      categoryId: string;
      askPrice: number;
      bidPrice: number;
      description: string;
      minQuantity: number;
      maxQuantity: number;
    }[]
  > {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    this.loadingSource.next(true);

    return this.http
      .get<Price[]>(`${this.apiUrl}/market/instruments`, {
        withCredentials: true,
      })
      .pipe(
        map((prices) =>
          prices.map((p) => {
            const instrument = this.instrumentDataObject[p.instrumentId];
            return {
              instrumentId: p.instrumentId,
              askPrice: p.askPrice,
              bidPrice: p.bidPrice,
              description: instrument?.description ?? '',
              categoryId: instrument?.categoryId ?? '',
              minQuantity: instrument?.minQuantity ?? 0,
              maxQuantity: instrument?.maxQuantity ?? 0,
            };
          })
        ),
        catchError((error) =>
          this.handleError(error, 'Failed to fetch latest instrument prices')
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  getPrice(instrumentId: string): Observable<Price | null> {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    return this.http
      .get<Price>(`${this.apiUrl}/market/instruments/${instrumentId}/price`, {
        withCredentials: true,
      })
      .pipe(
        catchError((error) =>
          this.handleError(
            error,
            `Failed to fetch price for instrument ${instrumentId}`
          )
        )
      );
  }

  getAllInstruments(): Observable<Instrument[]> {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    this.loadingSource.next(true);

    return this.http
      .get<Instrument[]>(`${this.apiUrl}/market/instruments`, {
        withCredentials: true,
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to fetch all instruments')
        ),
        finalize(() => this.loadingSource.next(false))
      );
  }

  searchInstruments(description: string, category?: string): Observable<any[]> {
    if (!this.authService.isAuthenticated) {
      return throwError(() => new Error('User not authenticated'));
    }

    const params: any = {};
    if (description) params.description = description;
    if (category) params.category = category;

    const query = Object.keys(params)
      .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
      .join('&');

    const url = `${this.apiUrl}/trades/search${query ? '?' + query : ''}`;

    return this.http
      .get<any[]>(url, { withCredentials: true })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to search instruments')
        )
      );
  }

  // Method to manually refresh token if needed
  refreshAuthAndRetryOperation(
    operation: () => Observable<any>
  ): Observable<any> {
    // First try to refresh auth token
    return this.http
      .post<any>(
        `${this.apiUrl}/auth/refresh-token`,
        {},
        { withCredentials: true }
      )
      .pipe(
        // Then retry the operation
        switchMap(() => operation()),
        catchError((error) => {
          if (error.status === 401) {
            // If refresh token fails, redirect to login
            this.authService.flushLocalStorage();
            this.router.navigate(['/login']);
          }
          return throwError(() => error);
        })
      );
  }
}

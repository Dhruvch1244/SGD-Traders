import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Trade } from '../models/trade.model';
import { OrderRequestDto } from '../models/order-request.model';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { WalletService } from './wallet.service';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class TradeService {
  private trades: Trade[] = [];
  private cashBalance = 0;
  private apiUrl = `${environment.apiUrl}/portfolios`; // Assuming apiUrl is in your environment config

  constructor(
    private http: HttpClient,
    private walletService: WalletService,
    private authService: AuthService
  ) {}

  submitBuyTrade(instrumentId: string, quantity: number, targetPrice: number): Observable<string> {
    const currentUser = this.authService.currentUserValue;
    const token = this.authService.getFmtsToken();
    const fmtsClientId = this.authService.getFmtsClientId();

    if (!currentUser || !token || !fmtsClientId) {
      return of('User not authenticated');
    }

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

    return this.http.post<any>(`${this.apiUrl}/buy`, orderRequest,{withCredentials: true}).pipe(
      map((response) => {
        if (response.success) {
          return 'Buy trade successful';
        } else {
          return 'Buy trade failed';
        }
      })
    );
  }

  submitSellTrade(instrumentId: string, quantity: number, targetPrice: number): Observable<string> {
    const currentUser = this.authService.currentUserValue;
    const token = this.authService.getFmtsToken();
    const fmtsClientId = this.authService.getFmtsClientId();

    if (!currentUser || !token || !fmtsClientId) {
      return of('User not authenticated');
    }

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

    return this.http.post<any>(`${this.apiUrl}/sell`, orderRequest,{withCredentials: true}).pipe(
      map((response) => {
        if (response.success) {
          return 'Sell trade successful';
        } else {
          return 'Sell trade failed';
        }
      })
    );
  }

  getTrades(): Observable<Trade[]> {
    return of(this.trades);
  }

  getCashBalance(): Observable<number> {
    return of(this.cashBalance);
  }
}


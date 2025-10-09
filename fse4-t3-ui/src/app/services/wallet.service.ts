import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Wallet } from '../models/wallet.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WalletService {
  private apiUrl = `${environment.apiUrl}/wallets`; // Corrected to /wallets
  private walletBalanceSubject = new BehaviorSubject<number>(0);
  public walletBalance$ = this.walletBalanceSubject.asObservable();

  constructor(private http: HttpClient) {}

  getWalletBalance(clientId: string): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.apiUrl}/${clientId}`,{withCredentials: true}).pipe(
      tap((wallet) => {
        this.walletBalanceSubject.next(wallet.balance);
      }),
      catchError((error) => {
        console.error('Error fetching wallet balance:', error);
        this.walletBalanceSubject.next(0); // Reset or handle error state
        return throwError(() => new Error('Could not fetch wallet balance.'));
      })
    );
  }

  addMoneyToWallet(clientId: string, amount: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${clientId}/add`, { amount },{withCredentials: true}).pipe( // Corrected endpoint and request body
      tap((response) => {
        if (response.success && response.data && response.data.balance !== undefined) {
          this.walletBalanceSubject.next(response.data.balance);
        } else {
          console.error('Failed to add money:', response.message);
        }
      }),
      catchError((error) => {
        console.error('Error adding money to wallet:', error);
        return throwError(() => new Error('Could not add money to wallet.'));
      })
    );
  }

  // Method to manually update the balance if needed (e.g., after a trade)
  updateWalletBalance(newBalance: number) {
    this.walletBalanceSubject.next(newBalance);
  }
}

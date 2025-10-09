export const InvestmentPurposeMap: { [key: number]: string } = {
  1: 'RETIREMENT',
  2: 'WEALTH_CREATION',
  3: 'EDUCATION'
};
export const InvestmentDurationMap: { [key: number]: string } = {
  1: 'ZERO_TO_FIVE_YEARS',
  2: 'FIVE_TO_SEVEN_YEARS',
  3: 'SEVEN_TO_TEN_YEARS',
  4: 'TEN_TO_FIFTEEN_YEARS'
};
// Maps for id-to-label lookups
export const RiskToleranceMap: { [key: number]: string } = {
  1: 'CONSERVATIVE',
  2: 'BELOW_AVERAGE',
  3: 'AVERAGE',
  4: 'ABOVE_AVERAGE',
  5: 'AGGRESSIVE'
};

export const IncomeCategoryMap: { [key: number]: string } = {
  1: 'RANGE_0_20000',
  2: 'RANGE_20001_40000',
  3: 'RANGE_40001_60000',
  4: 'RANGE_60001_80000',
  5: 'RANGE_80001_100000',
  6: 'RANGE_100001_150000',
  7: 'RANGE_150000_PLUS'
};
  /**
   * Utility: Get a field value from an InvestmentPreferences object by key (string).
   */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InvestmentPreferences } from '../models/investment-preferences';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InvestmentPreferencesService {
  /**
   * Utility: Get a field value from an InvestmentPreferences object by key (string).
   */
  /**
   * Get the display value for a field from an InvestmentPreferences object using the id and the correct map.
   * Returns the value in Proper Case (Title Case).
   */
  getPreferenceField(pref: InvestmentPreferences, field: string): string | undefined {
    if (!pref) return undefined;
    let id = (pref as any)[field];
    let value: string | undefined;
    switch (field) {
      case 'riskTolerance':
        value = RiskToleranceMap[Number(id)];
        break;
      case 'incomeCategory':
        value = IncomeCategoryMap[Number(id)];
        break;
      case 'investmentDuration':
        value = InvestmentDurationMap[Number(id)];
        break;
      case 'investmentPurpose':
        value = InvestmentPurposeMap[Number(id)];
        break;
      default:
        value = id;
    }
    // Return backend value (no formatting)
    return value;
  }
  private apiUrl = `${environment.apiUrl}/preferences`;

  constructor(private http: HttpClient) {}

  /**
   * Fetch investment preferences for a given clientId.
   * Returns null if not found (404).
   */
  getPreferences(clientId: string): Observable<InvestmentPreferences | null> {
    return this.http
      .get<InvestmentPreferences | null>(`${this.apiUrl}/${clientId}`,{withCredentials: true})
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 404) {
            return of(null); // No preferences found
          }
          return throwError(() => error);
        })
      );
  }

  /**
   * Save new investment preferences.
   */
  savePreferences(pref: InvestmentPreferences): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, pref,{withCredentials: true}).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => error))
    );
  }

  /**
   * Update existing investment preferences.
   * Could call a dedicated update endpoint if needed.
   */
  updatePreferences(pref: InvestmentPreferences): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, pref,{withCredentials: true}).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => error))
    );
  }
}

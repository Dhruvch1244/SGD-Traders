import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PortfolioSummary } from '../models/portfolio-summary.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PortfolioService {
  private apiUrl = `${environment.apiUrl}/portfolios`; // Assuming apiUrl is in your environment config

  constructor(private http: HttpClient) {}

  getPortfolioSummary(uuid: string): Observable<PortfolioSummary> {
    return this.http.get<PortfolioSummary>(`${this.apiUrl}/${uuid}/summary`,{withCredentials: true});
  }
}

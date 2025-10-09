import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../environments/environment';

export interface RoboAdvisorRecommendation {
  instrumentId: string;
  description: string;
  category: string;
}

@Injectable({ providedIn: 'root' })
export class RoboAdvisorService {
    private baseUrl = environment.apiUrl+'/robo-advisor';

  constructor(private http: HttpClient) {}

  getRecommendations(clientId: string): Observable<RoboAdvisorRecommendation[]> {
    return this.http.get<RoboAdvisorRecommendation[]>(`${this.baseUrl}/recommend/${clientId}`);
  }
}

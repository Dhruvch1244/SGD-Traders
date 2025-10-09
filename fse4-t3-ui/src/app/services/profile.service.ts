// services/profile.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Profile, ProfileUpdateRequest, Identification } from '../models/profile.model';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getProfile(clientId: string): Observable<Profile> {
    return this.http.get<Profile>(`${this.baseUrl}/profile/${clientId}`,{withCredentials: true});
  }

  updateProfile(clientId: string, profileData: ProfileUpdateRequest): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/profile/${clientId}`, profileData,{withCredentials: true});
  }

  updateIdentifications(clientId: string, identifications: Identification[]): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/profile/${clientId}/identification`, identifications);
  }
}

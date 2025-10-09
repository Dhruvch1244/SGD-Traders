import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError, map, finalize } from 'rxjs/operators';
import { Client } from '../models/client.model';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

export interface AuthResult {
  success: boolean;
  message?: string;
  data?: {
    client: any;
    token?: string;
    fmtsToken?: string;
    fmtsClientId?: string;
  };
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private authApiUrl = environment.authApiUrl + '/auth';
  private apiUrl = environment.apiUrl;
  private currentUserSubject: BehaviorSubject<Client | null>;
  public readonly currentUser$: Observable<Client | null>;
  public readonly isAuthenticated$: Observable<boolean>;
  private isBrowser: boolean;
  private fmtsToken: string | null = null;
  private fmtsClientId: string | null = null;

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    let storedUser = null;

    if (this.isBrowser) {
      storedUser = localStorage.getItem('currentUser');
      this.fmtsToken = localStorage.getItem('fmtsToken');
      this.fmtsClientId = localStorage.getItem('fmtsClientId');
    }

    this.currentUserSubject = new BehaviorSubject<Client | null>(
      storedUser ? JSON.parse(storedUser) : null
    );

    this.currentUser$ = this.currentUserSubject.asObservable();
    this.isAuthenticated$ = this.currentUser$.pipe(map((user) => !!user));
  }

  // Map API client to our Client model
  private mapApiClientToClient(apiClient: any): Client {
    return {
      name: apiClient.name || '',
      clientId: apiClient.clientId || '',
      email: apiClient.email || '',
      dateOfBirth: apiClient.dateOfBirth || '',
      country: apiClient.country || '',
      postalCode: apiClient.postalCode || '',
      identification: apiClient.identification || [],
      password: '', // Never store password
    };
  }

  public flushLocalStorage() {
    if (this.isBrowser) {
      localStorage.removeItem('currentUser');
      localStorage.removeItem('fmtsToken');
      localStorage.removeItem('fmtsClientId');
    }
    this.currentUserSubject.next(null);
    this.fmtsToken = null;
    this.fmtsClientId = null;
  }

  public get currentUserValue(): Client | null {
    return this.currentUserSubject.value;
  }

  public getFmtsToken(): string | null {
    return this.fmtsToken;
  }

  public getFmtsClientId(): string | null {
    return this.fmtsClientId;
  }

  public getCurrClient(): string | null {
    const user = this.currentUserValue;
    return user ? user.clientId : null;
  }

  public persistCurrentUser() {
    if (this.isBrowser && this.currentUserValue) {
      localStorage.setItem(
        'currentUser',
        JSON.stringify(this.currentUserValue)
      );
    }
  }

  // Main signin method - using /api/signin endpoint which the backend redirects to /api/auth/signin
  signin(email: string, password: string): Observable<AuthResult> {
    console.log(this.authApiUrl);
    
    return this.http
      .post<AuthResult>(
        `${this.authApiUrl}/signin`,
        { email, password },
        {
          withCredentials: true,
        }
      )
      .pipe(
        map((response) => {
          if (response.success && response.data?.client) {
            const apiClient = response.data.client;
            const client = this.mapApiClientToClient(apiClient);

            this.currentUserSubject.next(client);

            if (this.isBrowser) {
              localStorage.setItem('currentUser', JSON.stringify(client));

              // Handle FMTS token
              const fmtsToken = response.data.fmtsToken || apiClient.fmtsToken;
              if (fmtsToken) {
                localStorage.setItem('fmtsToken', fmtsToken);
                this.fmtsToken = fmtsToken;
              }

              // Handle FMTS client ID
              const fmtsClientId =
                response.data.fmtsClientId || apiClient.fmtsClientId;
              if (fmtsClientId) {
                localStorage.setItem('fmtsClientId', fmtsClientId);
                this.fmtsClientId = fmtsClientId;
              }
            }
          }

          return response;
        }),
        catchError((error) => {
          console.error('Login error:', error);
          return throwError(() => error);
        })
      );
  }

  // For backward compatibility
  login(username: string, password: string): Observable<AuthResult> {
    return this.signin(username, password);
  }

  forgotPassword(email: string, dateOfBirth: string): Observable<any> {
    return this.http.post<any>(
      `${this.authApiUrl}/forgot-password`,
      {
        email,
        dateOfBirth,
      },
      { withCredentials: true }
    );
  }

  changePassword(email: string, newPassword: string): Observable<any> {
    return this.http.post<any>(
      `${this.authApiUrl}/change-password`,
      {
        email,
        newPassword,
      },
      { withCredentials: true }
    );
  }

  // Update to use /api/logout endpoint
  logout(): Observable<any> {
    return this.http.post<any>(`${this.authApiUrl}/logout`, {}).pipe(
      tap(() => {
        this.router.navigate(['/']);
      }),
      catchError((err) => {
        console.error('Logout error:', err);
        this.router.navigate(['/']);
        return throwError(() => err);
      }),
      finalize(() => {
        this.flushLocalStorage();
      })
    );
  }
  get isAuthenticated(): boolean {
    return !!this.currentUserSubject.value;
  }

  // Add method for token refresh
  refreshToken(): Observable<any> {
    return this.http
      .post<any>(
        `${this.authApiUrl}/auth/refresh-token`,
        {},
        {
          withCredentials: true,
        }
      )
      .pipe(
        catchError((error) => {
          console.error('Token refresh failed', error);
          return throwError(() => error);
        })
      );
  }

  register(client: Client): Observable<AuthResult> {
    return this.http
      .post<AuthResult>(`${this.authApiUrl}/register`, client, {
        withCredentials: true,
      })
      .pipe(
        map((response) => {
          if (response.success && response.data?.client) {
            const apiClient = response.data.client;
            const registeredClient = this.mapApiClientToClient(apiClient);
            this.currentUserSubject.next(registeredClient);

            if (this.isBrowser) {
              localStorage.setItem(
                'currentUser',
                JSON.stringify(registeredClient)
              );

              // Handle FMTS tokens
              const fmtsToken = response.data.fmtsToken || apiClient.fmtsToken;
              if (fmtsToken) {
                localStorage.setItem('fmtsToken', fmtsToken);
                this.fmtsToken = fmtsToken;
              }

              const fmtsClientId =
                response.data.fmtsClientId || apiClient.fmtsClientId;
              if (fmtsClientId) {
                localStorage.setItem('fmtsClientId', fmtsClientId);
                this.fmtsClientId = fmtsClientId;
              }
            }
          }

          return response;
        }),
        catchError((error) => {
          console.error('Registration error:', error);
          return throwError(() => error);
        })
      );
  }
}

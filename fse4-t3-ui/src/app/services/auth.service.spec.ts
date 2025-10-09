import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { Client } from '../models/client.model';

const mockClient: Client = {
  clientId: 'CLT1001',
  name: 'Alice',
  email: 'alice@example.com',
  dateOfBirth: '1990-01-01',
  country: 'Wonderland',
  postalCode: '123456',
  identification: [{ type: 'Passport', value: 'A1234567' }],
  password: 'Pass@123',
};
  let service: AuthService;
  let httpMock: HttpTestingController;
  let platformId: Object;
  let localStorageMock: any;

  beforeEach(() => {
    localStorageMock = (function() {
      let store: any = {};
      return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => { store[key] = value; },
        removeItem: (key: string) => { delete store[key]; },
        clear: () => { store = {}; },
      };
    })();
    spyOnProperty(window, 'localStorage', 'get').and.returnValue(localStorageMock);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    platformId = TestBed.inject(PLATFORM_ID);
  });

  afterEach(() => {
    httpMock.verify();
    localStorageMock.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should initialize with null user if no localStorage', () => {
    expect(service.currentUserValue).toBeNull();
  });

  it('should sign in and store user and tokens on success', () => {
    const response = {
      success: true,
      data: { client: mockClient, fmtsToken: 'tok', fmtsClientId: 'cid' },
    };
    service.signin('alice@example.com', 'Pass@123').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/signin`);
    expect(req.request.method).toBe('POST');
    req.flush(response);
    expect(service.currentUserValue).toEqual(mockClient);
    expect(localStorageMock.getItem('currentUser')).toContain('Alice');
    expect(localStorageMock.getItem('fmtsToken')).toBe('tok');
    expect(localStorageMock.getItem('fmtsClientId')).toBe('cid');
    expect(service.getFmtsToken()).toBe('tok');
    expect(service.getFmtsClientId()).toBe('cid');
  });

  it('should not set user if response has no client', () => {
    const response = { success: true, data: {} };
    service.signin('alice@example.com', 'Pass@123').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/signin`);
    req.flush(response);
    expect(service.currentUserValue).toBeNull();
  });

  it('should handle signin error', (done) => {
    service.signin('alice@example.com', 'Pass@123').subscribe({
      error: (err) => {
        expect(err.status).toBe(401);
        done();
      },
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/signin`);
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
  });

  it('should register and store user and tokens on success', () => {
    const response = {
      success: true,
      data: { client: mockClient, fmtsToken: 'tok', fmtsClientId: 'cid' },
    };
    service.register(mockClient).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush(response);
    expect(service.currentUserValue).toEqual(mockClient);
    expect(localStorageMock.getItem('currentUser')).toContain('Alice');
    expect(localStorageMock.getItem('fmtsToken')).toBe('tok');
    expect(localStorageMock.getItem('fmtsClientId')).toBe('cid');
  });

  it('should not set user if register response has no client', () => {
    const response = { success: true, data: {} };
    service.register(mockClient).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    req.flush(response);
    expect(service.currentUserValue).toBeNull();
  });

  it('should handle register error', (done) => {
    service.register(mockClient).subscribe({
      error: (err) => {
        expect(err.status).toBe(400);
        done();
      },
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    req.flush({ message: 'Bad Request' }, { status: 400, statusText: 'Bad Request' });
  });

  it('should clear user and tokens on logout', () => {
    // Set up user and tokens
    localStorageMock.setItem('currentUser', JSON.stringify(mockClient));
    localStorageMock.setItem('fmtsToken', 'tok');
    localStorageMock.setItem('fmtsClientId', 'cid');
    (service as any).fmtsToken = 'tok';
    (service as any).fmtsClientId = 'cid';
    (service as any).currentUserSubject.next(mockClient);
    service.logout();
    expect(localStorageMock.getItem('currentUser')).toBeNull();
    expect(localStorageMock.getItem('fmtsToken')).toBeNull();
    expect(localStorageMock.getItem('fmtsClientId')).toBeNull();
    expect(service.currentUserValue).toBeNull();
    expect(service.getFmtsToken()).toBeNull();
    expect(service.getFmtsClientId()).toBeNull();
  });

  it('should persist current user to localStorage', () => {
    (service as any).currentUserSubject.next(mockClient);
    service.persistCurrentUser();
    expect(localStorageMock.getItem('currentUser')).toContain('Alice');
  });

  it('should flush localStorage and clear tokens', () => {
    localStorageMock.setItem('currentUser', JSON.stringify(mockClient));
    localStorageMock.setItem('fmtsToken', 'tok');
    localStorageMock.setItem('fmtsClientId', 'cid');
    (service as any).fmtsToken = 'tok';
    (service as any).fmtsClientId = 'cid';
    (service as any).currentUserSubject.next(mockClient);
    service.flushLocalStorage();
    expect(localStorageMock.getItem('currentUser')).toBeNull();
    expect(localStorageMock.getItem('fmtsToken')).toBeNull();
    expect(localStorageMock.getItem('fmtsClientId')).toBeNull();
    expect(service.currentUserValue).toBeNull();
    expect(service.getFmtsToken()).toBeNull();
    expect(service.getFmtsClientId()).toBeNull();
  });

  it('should getCurrClient return clientId or null', () => {
    (service as any).currentUserSubject.next(mockClient);
    expect(service.getCurrClient()).toBe('CLT1001');
    (service as any).currentUserSubject.next(null);
    expect(service.getCurrClient()).toBeNull();
  });

  it('should getFmtsToken and getFmtsClientId return correct values', () => {
    (service as any).fmtsToken = 'tok';
    (service as any).fmtsClientId = 'cid';
    expect(service.getFmtsToken()).toBe('tok');
    expect(service.getFmtsClientId()).toBe('cid');
  });

  it('should call forgotPassword endpoint', () => {
    service.forgotPassword('alice@example.com', '1990-01-01').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/forgot-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'alice@example.com', dateOfBirth: '1990-01-01' });
    req.flush({ success: true });
  });

  it('should call changePassword endpoint', () => {
    service.changePassword('alice@example.com', 'newPass').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/change-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'alice@example.com', newPassword: 'newPass' });
    req.flush({ success: true });
  });

  it('should call getProfile endpoint', () => {
    service.getProfile('CLT1001').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/profile/CLT1001`);
    expect(req.request.method).toBe('GET');
    req.flush({ success: true });
  });

  it('should not throw if persistCurrentUser called with null user', () => {
    (service as any).currentUserSubject.next(null);
    expect(() => service.persistCurrentUser()).not.toThrow();
    expect(localStorageMock.getItem('currentUser')).toBeNull();
  });


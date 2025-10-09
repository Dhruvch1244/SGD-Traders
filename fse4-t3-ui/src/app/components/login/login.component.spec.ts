import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let prefServiceSpy: jasmine.SpyObj<InvestmentPreferencesService>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['signin', 'forgotPassword', 'changePassword']);
    Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => null, configurable: true });
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    prefServiceSpy = jasmine.createSpyObj('InvestmentPreferencesService', ['getPreferences']);
    component = new LoginComponent(authServiceSpy, routerSpy, prefServiceSpy);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should navigate to /landing if currentUserValue and preferences exist', () => {
      Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => ({ clientId: '123' }), configurable: true });
      prefServiceSpy.getPreferences.and.returnValue(of({
        clientId: '123',
        investmentPurpose: '',
        riskTolerance: '',
        incomeCategory: '',
        investmentDuration: '',
        acceptedTerms: 1
      }));
      component.ngOnInit();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/landing']);
    });
    it('should navigate to /preferences if currentUserValue exists but no preferences', () => {
      Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => ({ clientId: '123' }), configurable: true });
      prefServiceSpy.getPreferences.and.returnValue(of(null));
      component.ngOnInit();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/preferences']);
    });
    it('should navigate to /landing if error fetching preferences', () => {
      Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => ({ clientId: '123' }), configurable: true });
      prefServiceSpy.getPreferences.and.returnValue(throwError(() => 'err'));
      component.ngOnInit();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/landing']);
    });
    it('should navigate to /landing if currentUserValue exists but no clientId', () => {
      Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => ({}), configurable: true });
      component.ngOnInit();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/landing']);
    });
    it('should do nothing if not logged in', () => {
      Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => null, configurable: true });
      component.ngOnInit();
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });
  });

  describe('login', () => {
    it('should navigate to /landing on successful login', () => {
      authServiceSpy.signin.and.returnValue(of({ success: true }));
      component.email = 'user@example.com';
      component.password = 'pass';
      component.login();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/landing']);
    });
    it('should navigate to /preferences if preferences not set', () => {
      authServiceSpy.signin.and.returnValue(of({ success: false, message: 'Investment preferences not set' }));
      component.email = 'user@example.com';
      component.password = 'pass';
      component.login();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/preferences']);
    });
    it('should show error message for other failed login', () => {
      authServiceSpy.signin.and.returnValue(of({ success: false, message: 'Invalid credentials' }));
      component.email = 'user@example.com';
      component.password = 'pass';
      component.login();
      expect(component.message).toBe('Invalid credentials');
      expect(component.success).toBeFalse();
    });
    it('should show error message on login error', () => {
      authServiceSpy.signin.and.returnValue(throwError(() => ({ error: { message: 'Server error' } })));
      component.email = 'user@example.com';
      component.password = 'pass';
      component.login();
      expect(component.message).toBe('Server error');
      expect(component.success).toBeFalse();
    });
    it('should show default error message if error has no message', () => {
      authServiceSpy.signin.and.returnValue(throwError(() => ({})));
      component.email = 'user@example.com';
      component.password = 'pass';
      component.login();
      expect(component.message).toBe('Invalid email or password.');
      expect(component.success).toBeFalse();
    });
  });

  describe('register', () => {
    it('should navigate to /register', () => {
      component.register();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/register']);
    });
  });

  describe('showForgotPassword', () => {
    it('should set formState to forgotPassword and clear message', () => {
      component.message = 'err';
      component.formState = 'login';
      component.showForgotPassword();
      expect(component.formState).toBe('forgotPassword');
      expect(component.message).toBe('');
    });
  });

  describe('verifyIdentity', () => {
    it('should normalize dd/MM/yyyy to yyyy-MM-dd', () => {
      authServiceSpy.forgotPassword.and.returnValue(of({ success: true, message: 'ok' }));
      component.email = 'user@example.com';
      component.dateOfBirth = '01/02/2000';
      component.verifyIdentity();
      expect(authServiceSpy.forgotPassword).toHaveBeenCalledWith('user@example.com', '2000-02-01');
    });
    it('should set formState to changePassword on success', () => {
      authServiceSpy.forgotPassword.and.returnValue(of({ success: true, message: 'ok' }));
      component.email = 'user@example.com';
      component.dateOfBirth = '2000-02-01';
      component.verifyIdentity();
      expect(component.success).toBeTrue();
      expect(component.message).toBe('ok');
      expect(component.formState).toBe('changePassword');
    });
    it('should show error message on forgotPassword error', () => {
      authServiceSpy.forgotPassword.and.returnValue(throwError(() => ({ error: { message: 'not found' } })));
      component.email = 'user@example.com';
      component.dateOfBirth = '2000-02-01';
      component.verifyIdentity();
      expect(component.success).toBeFalse();
      expect(component.message).toBe('not found');
    });
    it('should show default error if forgotPassword error has no message', () => {
      authServiceSpy.forgotPassword.and.returnValue(throwError(() => ({})));
      component.email = 'user@example.com';
      component.dateOfBirth = '2000-02-01';
      component.verifyIdentity();
      expect(component.success).toBeFalse();
      expect(component.message).toBe('Server error');
    });
  });

  describe('changePassword', () => {
    it('should show error if password is weak', () => {
      component.newPassword = 'weak';
      component.changePassword();
      expect(component.message).toContain('Password must be at least 8 characters');
      expect(component.success).toBeFalse();
    });
    it('should set formState to login on success', () => {
      authServiceSpy.changePassword.and.returnValue(of({ success: true, message: 'changed' }));
      component.newPassword = 'Pass@1234';
      component.email = 'user@example.com';
      component.changePassword();
      expect(component.success).toBeTrue();
      expect(component.message).toBe('changed');
      expect(component.formState).toBe('login');
    });
    it('should show error message from error object', () => {
      authServiceSpy.changePassword.and.returnValue(throwError(() => ({ error: { password: 'bad' } })));
      component.newPassword = 'Pass@1234';
      component.email = 'user@example.com';
      component.changePassword();
      expect(component.success).toBeFalse();
      expect(component.message).toBe('bad');
    });
    it('should show error message from error.message', () => {
      authServiceSpy.changePassword.and.returnValue(throwError(() => ({ error: { message: 'fail' } })));
      component.newPassword = 'Pass@1234';
      component.email = 'user@example.com';
      component.changePassword();
      expect(component.success).toBeFalse();
      expect(component.message).toBe('fail');
    });
    it('should show default error if no error object', () => {
      authServiceSpy.changePassword.and.returnValue(throwError(() => ({})));
      component.newPassword = 'Pass@1234';
      component.email = 'user@example.com';
      component.changePassword();
      expect(component.success).toBeFalse();
      expect(component.message).toBe('Server error');
    });
  });

  describe('cancel', () => {
    it('should set formState to login and clear message', () => {
      component.formState = 'forgotPassword';
      component.message = 'err';
      component.cancel();
      expect(component.formState).toBe('login');
      expect(component.message).toBe('');
    });
  });
});

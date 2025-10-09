
import { RegisterComponent } from './register.component';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);
    component = new RegisterComponent(routerSpy, authServiceSpy);
    localStorage.clear();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should show error if name is empty', () => {
    component.name = '';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';

    component.register();

    expect(component.errorMessages['name']).toBe('Name is required.');
    expect(component.success).toBeFalse();
  });

  it('should show error for invalid email', () => {
    component.name = 'John';
    component.client.email = 'invalid-email';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';

    component.register();

    expect(component.errorMessages['email']).toBe('Please enter a valid email address.');
  });

  it('should show error if passwords do not match', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Mismatch123';

    component.register();

    expect(component.errorMessages['confirmPassword']).toBe('Passwords do not match.');
  });

  it('should show error if underage', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    const today = new Date();
    const year = today.getFullYear() - 10;
    component.client.dateOfBirth = `${year}-01-01`;
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';

    component.register();
    expect(component.errorMessages['dateOfBirth']).toBe('You must be at least 18 years old.');
  });

  it('should show error if id number is wrong length', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';

    component.register();
    expect(component.errorMessages['idValue']).toContain('ID Number must be 12 digits long.');
  });

  it('should show error if password is weak', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'password';
    component.confirmPassword = 'password';

    component.register();
    expect(component.errorMessages['password']).toContain('Password must be at least 8 characters');
  });

  it('should show error if required fields are missing', () => {
    component.name = '';
    component.client.email = '';
    component.client.dateOfBirth = '';
    component.client.country = '';
    component.client.postalCode = '';
    component.client.identification = [{ type: '', value: '' }];
    component.client.password = '';
    component.confirmPassword = '';

    component.register();
    expect(component.errorMessages['name']).toBe('Name is required.');
    expect(component.errorMessages['email']).toBe('Please enter a valid email address.');
    expect(component.errorMessages['dateOfBirth']).toBe('Date of Birth is required.');
    expect(component.errorMessages['country']).toBe('Country is required.');
    expect(component.errorMessages['postalCode']).toBe('Postal Code is required.');
    expect(component.errorMessages['idType']).toBe('ID Type is required.');
    expect(component.errorMessages['idValue']).toBe('ID Number is required.');
    expect(component.errorMessages['password']).toContain('Password must be at least 8 characters');
  });

  it('should call AuthService.register and navigate on success', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';
    const response = { success: true, message: 'Registered!', data: {} };
    authServiceSpy.register.and.returnValue(of(response));

    component.register();
    expect(authServiceSpy.register).toHaveBeenCalled();
    expect(component.success).toBeTrue();
    expect(component.message).toBe('Registered!');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/preferences']);
  });

  it('should handle AuthService.register error with error object', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';
    const error = { error: { email: 'Email already exists.' } };
    authServiceSpy.register.and.returnValue(throwError(() => error));

    component.register();
    expect(component.success).toBeFalse();
    expect(component.errorMessages['email']).toBe('Email already exists.');
    expect(component.message).toBe('Please correct the highlighted errors.');
  });

  it('should handle AuthService.register error with no error object', () => {
    component.name = 'John';
    component.client.email = 'test@example.com';
    component.client.dateOfBirth = '1990-01-01';
    component.client.country = 'India';
    component.client.postalCode = '123456';
    component.client.identification = [{ type: 'Aadhar', value: '123456789012' }];
    component.client.password = 'Pass@1234';
    component.confirmPassword = 'Pass@1234';
    authServiceSpy.register.and.returnValue(throwError(() => ({})));

    component.register();
    expect(component.success).toBeFalse();
    expect(component.message).toBe('An unexpected error occurred.');
  });

  it('should set selectedFlag and idType on country change', () => {
    component.client.country = 'India';
    component.onCountryChange();
    expect(component.client.identification[0].type).toBe('Aadhar');
    expect(component.selectedFlag).toBe('🇮🇳');
  });

  it('should call router.navigate on goBack', () => {
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
  });
});

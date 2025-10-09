import { ReactiveFormsModule, FormBuilder, FormArray, FormGroup } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { ProfileContentComponent } from './profile-content.component';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

describe('ProfileContentComponent', () => {
  let component: ProfileContentComponent;
  let profileServiceSpy: jasmine.SpyObj<ProfileService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let formBuilder: FormBuilder;

  const mockProfile = {
    clientId: 'C001',
    name: 'Alice Johnson',
    email: 'alice.j@example.com',
    dateOfBirth: '1985-05-20',
    country: 'USA',
    postalCode: '10001',
    identification: [
      {
        id: '1963febb-0892-4f23-b4f7-cec7d2559eb1',
        clientId: 'C001',
        type: 'Driver License',
        value: 'D1234157',
        new: true
      }
    ]
  };

  // Disable HTTP requests verification
  beforeAll(() => {
    jasmine.getEnv().configure({ random: false });
    // Mock any global HTTP verifier
    spyOn(window, 'addEventListener').and.callFake(() => {});
  });

  beforeEach(() => {
    // Create dependencies with spies
    profileServiceSpy = jasmine.createSpyObj('ProfileService', [
      'getProfile',
      'updateProfile',
      'updateIdentifications'
    ]);

    authServiceSpy = jasmine.createSpyObj('AuthService', [], {
      currentUserValue: { clientId: 'C001' }
    });

    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    formBuilder = new FormBuilder();

    // Configure spy return values
    profileServiceSpy.getProfile.and.returnValue(of(mockProfile));
    profileServiceSpy.updateProfile.and.returnValue(of({success: true}));
    profileServiceSpy.updateIdentifications.and.returnValue(of({success: true}));

    // Create component manually with proper constructor parameters
    // Check your component's constructor to ensure this order is correct
    component = new ProfileContentComponent(
      profileServiceSpy,  // First parameter (not FormBuilder)
      formBuilder,
      authServiceSpy,
      routerSpy,
    );

    // Skip automatic ngOnInit to avoid HTTP requests
    // We'll call specific methods manually in tests
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect if no client ID is available', () => {
    // Set up the test
    Object.defineProperty(authServiceSpy, 'currentUserValue', {
      get: () => null
    });

    // Call the method directly
    component.ngOnInit();

    // Check the result
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should load profile data when loadProfile is called', () => {
    // Call method directly
    component.loadProfile();
    // Verify method calls and state
    expect(profileServiceSpy.getProfile).toHaveBeenCalledWith('');
    expect(component.profile).toEqual(mockProfile);
  });

  it('should patch form values when patchFormValues is called', () => {
    // Setup
    component.profile = mockProfile;

    // Action
    component.patchFormValues();

    // Assert
    expect(component.profileForm.get('name')?.value).toBe('Alice Johnson');
    expect(component.profileForm.get('country')?.value).toBe('USA');
  });

  it('should add identification when addIdentification is called', () => {
    // Setup
    const initialLength = component.identificationArray.length;

    // Action
    component.addIdentification();

    // Assert
    expect(component.identificationArray.length).toBe(initialLength + 1);
  });

  it('should remove identification when removeIdentification is called', () => {
    // Setup
    component.addIdentification();
    const initialLength = component.identificationArray.length;

    // Action
    component.removeIdentification(0);

    // Assert
    expect(component.identificationArray.length).toBe(initialLength - 1);
  });

  it('should toggle edit mode', () => {
    // Setup
    component.isEditing = false;

    // Action & Assert
    component.toggleEdit();
    expect(component.isEditing).toBe(true);

    component.toggleEdit();
    expect(component.isEditing).toBe(false);
  });

  it('should validate form correctly', () => {
    // Setup - use patchValue instead of setValue
    component.profileForm.patchValue({
      name: 'Valid Name',
      email: 'valid@email.com',
      dateOfBirth: '1990-01-01',
      country: 'USA',
      postalCode: '12345'
    });
    // Action & Assert
    expect(component.validateForm()).toBe(true);

    // Test invalid case
    component.profileForm.get('name')?.setValue('');
    expect(component.validateForm()).toBe(false);
  });

  // Fix 5: Updated test to use patchValue instead of setValue
  it('should save profile when form is valid', () => {
    // Setup with patchValue
    component.profileForm.patchValue({
      name: 'Alice Updated',
      email: 'valid@email.com',
      dateOfBirth: '1990-01-01',
      country: 'USA',
      postalCode: '10001'
    });
    spyOn(component, 'validateForm').and.returnValue(true);

    // Action
    component.saveProfile();

    // Assert
    expect(profileServiceSpy.updateProfile).toHaveBeenCalledWith('', jasmine.any(Object));
  });

  it('should handle errors when loading profile', () => {
    // Setup
    profileServiceSpy.getProfile.and.returnValue(throwError(() => new Error('Failed to load')));
    spyOn(console, 'error');

    // Action
    component.loadProfile();

    // Assert
    expect(console.error).toHaveBeenCalled();
  });
});

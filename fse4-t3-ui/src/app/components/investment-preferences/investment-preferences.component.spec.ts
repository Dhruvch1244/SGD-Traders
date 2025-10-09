import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InvestmentPreferencesComponent } from './investment-preferences.component';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { of, Subject } from 'rxjs';

describe('InvestmentPreferencesComponent', () => {
  let component: InvestmentPreferencesComponent;
  let fixture: ComponentFixture<InvestmentPreferencesComponent>;
  let prefServiceSpy: jasmine.SpyObj<InvestmentPreferencesService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userSubject: Subject<any>;

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    prefServiceSpy = jasmine.createSpyObj('InvestmentPreferencesService', ['savePreferences', 'getPreferences']);
    prefServiceSpy.getPreferences.and.returnValue(of(null)); // Mock getPreferences method
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    userSubject = new Subject();
    authServiceSpy = jasmine.createSpyObj('AuthService', [], {
      currentUser$: userSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      imports: [InvestmentPreferencesComponent],
      providers: [
        { provide: InvestmentPreferencesService, useValue: prefServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InvestmentPreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    if (component && typeof component.ngOnDestroy === 'function') {
      component.ngOnDestroy();
    }
    TestBed.resetTestingModule();
  });

  it('should set clientId from user observable and navigate if user is null', () => {
    userSubject.next({ clientId: 'testId' });
    expect(component.preferences.clientId).toBe('testId');
    userSubject.next(null);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/landing']);
  });

  it('should clean up subscription on destroy', () => {
    userSubject.next({ clientId: 'abc123' }); // Ensure subscription is set
    const spy = spyOn(component['userSubscription']!, 'unsubscribe');
    component.ngOnDestroy();
    expect(spy).toHaveBeenCalled();
  });

  it('should validate form correctly', () => {
    component.preferences = {
      clientId: 'abc',
      investmentPurpose: 'RETIREMENT',
      riskTolerance: 'CONSERVATIVE',
      incomeCategory: 'RANGE_0_20000',
      investmentDuration: 'ZERO_TO_FIVE_YEARS',
      acceptedTerms: 1,
    };
    expect(component.isFormValid()).toBeTrue();
    component.preferences.acceptedTerms = 0;
    expect(component.isFormValid()).toBeFalse();
  });

  it('should call savePreferences and navigate on valid save, not on invalid', () => {
    spyOn(component, 'isFormValid').and.returnValue(false);
    component.save();
    expect(prefServiceSpy.savePreferences).not.toHaveBeenCalled();

    (component.isFormValid as jasmine.Spy).and.returnValue(true);
    prefServiceSpy.savePreferences.and.returnValue(of({}));
    component.save();
    expect(prefServiceSpy.savePreferences).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/landing']);
  });

  it('should return correct options from getOptions', () => {
    const map = { A: 'A', B: 'B' };
    const options = component.getOptions(map);
    expect(options.length).toBe(3);
    expect(options[0].label).toBe('Select an option');
  });

  it('should split camelCase strings', () => {
    expect(component.splitCamelCase('riskTolerance')).toBe('risk Tolerance');
    expect(component.splitCamelCase('Risk')).toBe('Risk');
  });

  it('should set acceptedTerms based on checkbox', () => {
    component.onAcceptedTermsChange({ target: { checked: true } } as any);
    expect(component.preferences.acceptedTerms).toBe(1);
    component.onAcceptedTermsChange({ target: { checked: false } } as any);
    expect(component.preferences.acceptedTerms).toBe(0);
  });
});
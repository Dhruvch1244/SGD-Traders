
// import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
// import { FormBuilder } from '@angular/forms';
// import { of, throwError } from 'rxjs';
// import { InvestmentPreferencesUpdateFormComponent } from './investment-preferences-update-form.component';
// import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
// import { AuthService } from '../../services/auth.service';
// import { Router } from '@angular/router';


// describe('InvestmentPreferencesUpdateFormComponent', () => {
//   let component: InvestmentPreferencesUpdateFormComponent;
//   let fixture: ComponentFixture<InvestmentPreferencesUpdateFormComponent>;
//   let prefServiceSpy: jasmine.SpyObj<InvestmentPreferencesService>;
//   let authServiceSpy: jasmine.SpyObj<AuthService>;
//   let routerSpy: jasmine.SpyObj<Router>;

//   const mockPref = {
//     clientId: '12345',
//     investmentPurpose: 'RETIREMENT' as const,
//     riskTolerance: 'MEDIUM' as const,
//     incomeCategory: 'MIDDLE' as const,
//     investmentDuration: 'FIVE_TO_SEVEN_YEARS' as const,
//     acceptedTerms: true,
//   };
//     it('should not call savePreferences if clientId is missing', () => {
//       component.form = new FormBuilder().group({
//         investmentPurpose: ['RETIREMENT'],
//         riskTolerance: ['MEDIUM'],
//         incomeCategory: ['MIDDLE'],
//         investmentDuration: ['FIVE_TO_SEVEN_YEARS'],
//         acceptedTerms: [true],
//       });
//       component.clientId = null;
//       component.save();
//       expect(prefServiceSpy.savePreferences).not.toHaveBeenCalled();
//     });

//   beforeEach(async () => {
//     prefServiceSpy = jasmine.createSpyObj('InvestmentPreferencesService', ['getPreferences', 'savePreferences']);
//     authServiceSpy = jasmine.createSpyObj('AuthService', [], { currentUserValue: { clientId: '12345' } });
//     routerSpy = jasmine.createSpyObj('Router', ['navigate']);
//     await TestBed.configureTestingModule({
//       imports: [InvestmentPreferencesUpdateFormComponent],
//       providers: [
//         FormBuilder,
//         { provide: InvestmentPreferencesService, useValue: prefServiceSpy },
//         { provide: AuthService, useValue: authServiceSpy },
//         { provide: Router, useValue: routerSpy },
//       ],
//     }).compileComponents();

//     fixture = TestBed.createComponent(InvestmentPreferencesUpdateFormComponent);
//     component = fixture.componentInstance;
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });

//   describe('ngOnInit', () => {
//     it('should fetch preferences and initialize form', () => {
//       prefServiceSpy.getPreferences.and.returnValue(of(mockPref));
//       component.ngOnInit();
//       expect(prefServiceSpy.getPreferences).toHaveBeenCalledWith('12345');
//       expect(component.preferences).toEqual(mockPref);
//       expect(component.form.value.investmentPurpose).toBe('RETIREMENT');
//     });

//     it('should initialize form with defaults if no preferences', () => {
//       prefServiceSpy.getPreferences.and.returnValue(of(null));
//       component.ngOnInit();
//       expect(component.preferences).toBeNull();
//       expect(component.form.value.investmentPurpose).toBe('');
//     });

//     it('should navigate to root if no clientId', () => {
//       // Patch the getter to simulate no clientId
//       Object.defineProperty(authServiceSpy, 'currentUserValue', { get: () => null });
//       component.ngOnInit();
//       expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
//     });
//   });

//   describe('initForm', () => {
//     it('should initialize form with given preferences', () => {
//       component.initForm(mockPref);
//       expect(component.form.value.investmentPurpose).toBe('RETIREMENT');
//       expect(component.form.value.acceptedTerms).toBeTrue();
//     });

//     it('should initialize form with defaults if null', () => {
//       component.initForm(null);
//       expect(component.form.value.investmentPurpose).toBe('');
//       expect(component.form.value.acceptedTerms).toBeFalse();
//     });
//   });

//   describe('editField', () => {
//     it('should set editingField', () => {
//       component.editField('riskTolerance');
//       expect(component.editingField).toBe('riskTolerance');
//     });
//   });

//   describe('save', () => {
//     it('should call savePreferences and navigate, then reset editingField', fakeAsync(() => {
//       component.form = new FormBuilder().group({
//         investmentPurpose: ['RETIREMENT'],
//         riskTolerance: ['MEDIUM'],
//         incomeCategory: ['MIDDLE'],
//         investmentDuration: ['FIVE_TO_SEVEN_YEARS'],
//         acceptedTerms: [true],
//       });
//       component.clientId = '12345';
//       prefServiceSpy.savePreferences.and.returnValue(of('Saved'));
//       component.editingField = 'riskTolerance';
//       component.save();
//       tick();
//       expect(prefServiceSpy.savePreferences).toHaveBeenCalledWith({
//         clientId: '12345',
//         investmentPurpose: 'RETIREMENT',
//         riskTolerance: 'MEDIUM',
//         incomeCategory: 'MIDDLE',
//         investmentDuration: 'FIVE_TO_SEVEN_YEARS',
//         acceptedTerms: true,
//       });
//       expect(component.editingField).toBeNull();
//       expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
//     }));

//     it('should not call savePreferences if form is invalid', () => {
//       component.form = new FormBuilder().group({
//         investmentPurpose: [''],
//         riskTolerance: [''],
//         incomeCategory: [''],
//         investmentDuration: [''],
//         acceptedTerms: [false],
//       });
//       component.clientId = '12345';
//       component.save();
//       expect(prefServiceSpy.savePreferences).not.toHaveBeenCalled();
//     });

//     it('should handle error from savePreferences', fakeAsync(() => {
//       component.form = new FormBuilder().group({
//         investmentPurpose: ['RETIREMENT'],
//         riskTolerance: ['MEDIUM'],
//         incomeCategory: ['MIDDLE'],
//         investmentDuration: ['FIVE_TO_SEVEN_YEARS'],
//         acceptedTerms: [true],
//       });
//       component.clientId = '12345';
//       prefServiceSpy.savePreferences.and.returnValue(throwError(() => new Error('error!')));
//       component.editingField = 'riskTolerance';
//       expect(() => component.save()).not.toThrow();
//     }));
//   });

//   describe('getOptions', () => {
//     it('should return correct options for InvestmentPurposeMap', () => {
//       const map = { RETIREMENT: 'Retirement', EDUCATION: 'Education' };
//       const options = component.getOptions(map);
//       expect(options).toEqual([
//         { value: 'RETIREMENT', label: 'Retirement' },
//         { value: 'EDUCATION', label: 'Education' },
//       ]);
//     });
//     it('should return empty array for empty map', () => {
//       expect(component.getOptions({})).toEqual([]);
//     });
//   });

//   describe('getMapForField', () => {
//     it('should return correct map for each field', () => {
//       expect(component.getMapForField('investmentPurpose')).toBe(component.InvestmentPurposeMap);
//       expect(component.getMapForField('riskTolerance')).toBe(component.RiskToleranceMap);
//       expect(component.getMapForField('incomeCategory')).toBe(component.IncomeCategoryMap);
//       expect(component.getMapForField('investmentDuration')).toBe(component.InvestmentDurationMap);
//     });
//     it('should return empty object for unknown field', () => {
//       expect(component.getMapForField('unknown')).toEqual({});
//     });
//   });

//   describe('splitCamelCase', () => {
//     it('should split camelCase words', () => {
//       expect(component.splitCamelCase('riskTolerance')).toBe('risk Tolerance');
//       expect(component.splitCamelCase('investmentPurpose')).toBe('investment Purpose');
//       expect(component.splitCamelCase('acceptedTerms')).toBe('accepted Terms');
//     });
//     it('should return string unchanged if no camelCase', () => {
//       expect(component.splitCamelCase('Risk')).toBe('Risk');
//     });
//   });
// });

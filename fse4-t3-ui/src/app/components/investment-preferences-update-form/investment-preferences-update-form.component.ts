
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { InvestmentPreferences } from '../../models/investment-preferences';
import {
  InvestmentPurposeMap,
  RiskToleranceMap,
  IncomeCategoryMap,
  InvestmentDurationMap,
} from '../../models/investment-preferences.enums';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-investment-preferences-update-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './investment-preferences-update-form.component.html',
  styleUrls: ['./investment-preferences-update-form.component.scss'],
})
export class InvestmentPreferencesUpdateFormComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  clientId: string | null = null;
  preferences: InvestmentPreferences | null = null;
  editingField: string | null = null;
  private userSubscription: Subscription | undefined;
  isLoading = true;

  InvestmentPurposeMap = InvestmentPurposeMap;
  RiskToleranceMap = RiskToleranceMap;
  IncomeCategoryMap = IncomeCategoryMap;
  InvestmentDurationMap = InvestmentDurationMap;

  constructor(
    private fb: FormBuilder,
    private prefService: InvestmentPreferencesService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
  // Initialize form immediately to avoid NG01052
  this.form = this.fb.group({
    investmentPurpose: [''],
    riskTolerance: [''],
    incomeCategory: [''],
    investmentDuration: [''],
    acceptedTerms: [false, Validators.requiredTrue],
  });

  this.userSubscription = this.authService.currentUser$.subscribe((user) => {
    this.clientId = user?.clientId || null;

    if (!this.clientId) {
      this.isLoading = false;
      this.router.navigate(['/landing']);
      return;
    }

    this.prefService.getPreferences(this.clientId).subscribe({
      next: (pref) => {
        this.isLoading = false;

        if (pref && Object.keys(pref).length > 0) {
          // Map backend keys to frontend-friendly keys
         const backendPref = pref as any; // bypass TS check
this.preferences = {
  clientId: backendPref.clientId,
  investmentPurpose: backendPref.investmentPurposeId,
  riskTolerance: backendPref.riskToleranceId,
  incomeCategory: backendPref.incomeCategoryId,
  investmentDuration: backendPref.investmentDurationId,
  acceptedTerms: backendPref.acceptedTerms
};


          // Patch form with mapped values
          this.form.patchValue({
            investmentPurpose: this.preferences.investmentPurpose,
            riskTolerance: this.preferences.riskTolerance,
            incomeCategory: this.preferences.incomeCategory,
            investmentDuration: this.preferences.investmentDuration,
            acceptedTerms: false // Always unchecked on load
          });
        } else {
          // No preferences → redirect
          this.router.navigate(['/preferences']);
        }
      },
      error: () => {
        this.isLoading = false;
        this.router.navigate(['/preferences']);
      },
    });
  });
}


  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }

  editField(field: string): void {
    this.editingField = field;
    // Optionally, focus/select the field here
  }


  getLabelForValue(field: string, value: any): string {
    console.log('Fetched preferences:', this.preferences);
    // Use the service's getPreferenceField for proper case and id-to-label mapping
    if (this.preferences) {
      const id = (this.preferences as any)[field];
      const label = this.prefService.getPreferenceField(this.preferences, field);
      console.log(`[getLabelForValue] field:`, field, '| id:', id, '| label:', label);
      return label || '-';
    }
    console.log(`[getLabelForValue] No preferences loaded for field:`, field);
    return '-';
  }

  save(): void {
    console.log('Save button clicked. Form valid:', this.form.valid, this.form.value);
    if (this.form.invalid) {
      alert('Please fill all required fields and accept the terms.');
      return;
    }

    const formValues = this.form.value;

    // Helper to get backend key from display value
    const getBackendKey = (field: string, map: { [key: string]: string }) => {
      const formVal = formValues[field];
      if (formVal) {
        // If formVal is a key, return it; if it's a value, find the key
        if (map[formVal]) return formVal;
        const foundKey = Object.keys(map).find(key => map[key] === formVal);
        if (foundKey) return foundKey;
      }
      // fallback to preferences if form value is empty
      const prefVal = this.preferences ? (this.preferences as any)[field] : undefined;
      if (prefVal) {
        if (map[prefVal]) return prefVal;
        const foundKey = Object.keys(map).find(key => map[key] === prefVal);
        if (foundKey) return foundKey;
      }
      return '';
    };


    // Build payload with only non-empty fields
    const updatedPref: any = { clientId: this.clientId!, acceptedTerms: 1 };
    const fields = ['investmentPurpose', 'riskTolerance', 'incomeCategory', 'investmentDuration'];
    // Service maps: id (number) -> enum key (string)
    const serviceMaps: any = {
      investmentPurpose: { 1: 'RETIREMENT', 2: 'WEALTH_CREATION', 3: 'EDUCATION' },
      riskTolerance: { 21: 'CONSERVATIVE', 22: 'BELOW_AVERAGE', 23: 'AVERAGE', 24: 'ABOVE_AVERAGE', 25: 'AGGRESSIVE' },
      incomeCategory: { 1: 'RANGE_0_20000', 2: 'RANGE_20001_40000', 3: 'RANGE_40001_60000', 4: 'RANGE_60001_80000', 5: 'RANGE_80001_100000', 6: 'RANGE_100001_150000', 7: 'RANGE_150000_PLUS' },
      investmentDuration: { 1: 'ZERO_TO_FIVE_YEARS', 2: 'FIVE_TO_SEVEN_YEARS', 3: 'SEVEN_TO_TEN_YEARS', 4: 'TEN_TO_FIFTEEN_YEARS' }
    };
    for (const field of fields) {
      let enumKey: string | undefined = undefined;
      if (field === this.editingField) {
        // Always include the edited field, using the current form value (even if empty)
        const formValue = this.form.get(field)?.value;
        if (serviceMaps[field][formValue]) {
          enumKey = serviceMaps[field][formValue];
        } else if (Object.keys(serviceMaps[field]).includes(formValue)) {
          enumKey = formValue;
        } else {
          enumKey = Object.keys(this.InvestmentPurposeMap).find(key => this.InvestmentPurposeMap[key] === formValue)
            || Object.keys(this.RiskToleranceMap).find(key => this.RiskToleranceMap[key] === formValue)
            || Object.keys(this.IncomeCategoryMap).find(key => this.IncomeCategoryMap[key] === formValue)
            || Object.keys(this.InvestmentDurationMap).find(key => this.InvestmentDurationMap[key] === formValue);
        }
        // Always include the edited field, even if enumKey is undefined or empty
        updatedPref[field] = enumKey !== undefined ? enumKey : formValue;
      } else {
        // For unedited fields, use the existing value from preferences
        const originalValue = this.preferences ? (this.preferences as any)[field] : '';
        if (serviceMaps[field][originalValue]) {
          enumKey = serviceMaps[field][originalValue];
        } else if (Object.keys(serviceMaps[field]).includes(originalValue)) {
          enumKey = originalValue;
        } else {
          enumKey = Object.keys(this.InvestmentPurposeMap).find(key => this.InvestmentPurposeMap[key] === originalValue)
            || Object.keys(this.RiskToleranceMap).find(key => this.RiskToleranceMap[key] === originalValue)
            || Object.keys(this.IncomeCategoryMap).find(key => this.IncomeCategoryMap[key] === originalValue)
            || Object.keys(this.InvestmentDurationMap).find(key => this.InvestmentDurationMap[key] === originalValue);
        }
        if (enumKey) {
          updatedPref[field] = enumKey;
        }
      }
    }

    // First call savePreferences, then updatePreferences, then close window
    this.prefService.savePreferences(updatedPref).subscribe({
      next: () => {
        this.prefService.updatePreferences(updatedPref).subscribe({
          next: () => {
            window.close();
          },
          error: (err) => alert('Update failed: ' + (err?.message || err)),
        });
      },
      error: (err) => alert('Save failed: ' + (err?.message || err)),
    });
    this.router.navigate(['/landing']);
  }

  getOptions(map: { [key: string]: string }): { value: string; label: string }[] {
    return Object.keys(map).map((key) => ({ value: key, label: map[key] }));
  }

  getMapForField(field: string): { [key: string]: string } {
    switch (field) {
      case 'investmentPurpose':
        return this.InvestmentPurposeMap;
      case 'riskTolerance':
        return this.RiskToleranceMap;
      case 'incomeCategory':
        return this.IncomeCategoryMap;
      case 'investmentDuration':
        return this.InvestmentDurationMap;
      default:
        return {};
    }
  }


  getPreferenceField(field: string): string | undefined {
    return this.preferences ? (this.preferences as any)[field] : undefined;
  }

  splitCamelCase(str: string): string {
    return str.replace(/([a-z])([A-Z])/g, '$1 $2');
  }
}

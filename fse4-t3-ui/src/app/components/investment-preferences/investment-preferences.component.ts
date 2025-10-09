  // ...existing code...
// ...existing code...
import { Component, OnInit, OnDestroy } from '@angular/core'; // Import OnDestroy
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs'; // Import Subscription to manage it

import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { AuthService } from '../../services/auth.service';
import { InvestmentPreferences } from '../../models/investment-preferences';
import {
  InvestmentPurposeMap,
  RiskToleranceMap,
  IncomeCategoryMap,
  InvestmentDurationMap,
} from '../../models/investment-preferences.enums';

// Helper type to allow empty strings for the initial form state, which is good practice.
type InvestmentPreferencesForm = {
  [K in Exclude<keyof InvestmentPreferences, 'acceptedTerms'>]:
    | InvestmentPreferences[K]
    | '';
} & { clientId: string; acceptedTerms: number }; // Ensure clientId is always a string

@Component({
  selector: 'app-investment-preferences',
  templateUrl: './investment-preferences.component.html',
  styleUrls: ['./investment-preferences.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
// Implement OnDestroy to clean up the subscription and prevent memory leaks
export class InvestmentPreferencesComponent implements OnInit, OnDestroy {
  // Use the helper type for the form model
  preferences: InvestmentPreferencesForm = {
    clientId: '',
    investmentPurpose: '',
    riskTolerance: '',
    incomeCategory: '',
    investmentDuration: '',
    acceptedTerms: 1,
  };

  // This will hold our subscription so we can clean it up later
  private userSubscription: Subscription | undefined;
  hasExistingPreferences = false;

  InvestmentPurposeMap = InvestmentPurposeMap;
  RiskToleranceMap = RiskToleranceMap;
  IncomeCategoryMap = IncomeCategoryMap;
  InvestmentDurationMap = InvestmentDurationMap;

  constructor(
    private readonly prefService: InvestmentPreferencesService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userSubscription = this.authService.currentUser$.subscribe((user) => {
      if (user?.clientId) {
        this.preferences.clientId = user.clientId;
        // Fetch existing preferences and pre-fill if present
          this.prefService.getPreferences(user.clientId).subscribe((pref) => {
            if (pref) {
              this.preferences = { ...pref };
              this.hasExistingPreferences = true;
            } else {
              this.hasExistingPreferences = false;
            }
          });
      } else {
        this.router.navigate(['/landing']);
      }
    });
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }

  isFormValid(): boolean {
    return (
      !!this.preferences.clientId &&
      !!this.preferences.investmentPurpose &&
      !!this.preferences.riskTolerance &&
      !!this.preferences.incomeCategory &&
      !!this.preferences.investmentDuration &&
      this.preferences.acceptedTerms === 1
    );
  }

  save(): void {
    if (!this.isFormValid()) {
      return;
    }
    // Transform riskTolerance to lowercase for backend
    const finalPreferences = {
      ...this.preferences,
      riskTolerance: (this.preferences.riskTolerance as string).toLowerCase(),
    } as InvestmentPreferences;
      const saveOrUpdate$ = this.hasExistingPreferences
        ? this.prefService.updatePreferences(finalPreferences)
        : this.prefService.savePreferences(finalPreferences);
      saveOrUpdate$.subscribe(() => {
        this.router.navigate(['/']);
      });
  }


  getOptions(map: {
    [key: string]: string;
  }): { value: string; label: string }[] {
    const options = [{ value: '', label: 'Select an option' }];
    const mapOptions = Object.keys(map).map((key) => ({
      value: key,
      label: map[key],
    }));
    return options.concat(mapOptions);
  }

  splitCamelCase(str: string): string {
    return str.replace(/([a-z])([A-Z])/g, '$1 $2');
  }

  onAcceptedTermsChange(event: Event): void {
  const input = event.target as HTMLInputElement;
  this.preferences.acceptedTerms = input?.checked ? 1 : 0;
  }
}

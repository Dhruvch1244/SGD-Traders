import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { take } from 'rxjs/operators';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  dateOfBirth = '';
  newPassword = '';
  message = '';
  success = false;
  formState: 'login' | 'forgotPassword' | 'changePassword' = 'login';

  constructor(
    private authService: AuthService,
    private router: Router,
    private investmentPreferencesService: InvestmentPreferencesService
  ) {}

  ngOnInit(): void {
    if (this.authService.currentUserValue) {
      const clientId = this.authService.currentUserValue.clientId;
      if (clientId) {
        this.investmentPreferencesService
          .getPreferences(clientId)
          .pipe(take(1))
          .subscribe(
            (preferences) => {
              if (preferences) {
                this.router.navigate(['/landing']);
              } else {
                this.router.navigate(['/preferences']);
              }
            },
            (error) => {
              console.error('Error fetching investment preferences:', error);
              // If there's an error fetching preferences, default to landing or handle as needed
              this.router.navigate(['/landing']);
            }
          );
      } else {
        // If somehow currentUserValue exists but clientId doesn't, navigate to landing
        this.router.navigate(['/landing']);
      }
    }
  }

  login() {
    this.message = '';
    this.success = false;
    console.log("Login Was Called!")
    this.authService
      .signin(this.email, this.password)
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.router.navigate(['/landing']);
          } else if (response.message === 'Investment preferences not set') {
            this.router.navigate(['/preferences']);
          } else {
            this.message = response.message || 'An unknown error occurred.';
            this.success = false;
          }
        },
        error: (err) => {
          console.error('Login failed:', err);
          this.message = err.error?.message || 'Invalid email or password.';
          this.success = false;
        },
      });
  }

  register() {
    this.router.navigate(['/register']);
  }

  showForgotPassword() {
    this.formState = 'forgotPassword';
    this.message = '';
  }
  verifyIdentity() {
    this.message = '';
    this.success = false;

    // Normalize DOB to yyyy-MM-dd
    let normalizedDob = this.dateOfBirth?.trim() || '';
    if (normalizedDob.includes('/')) {
      // Assume UI input like dd/MM/yyyy or MM/dd/yyyy; your UI uses dd/MM/yyyy
      const [p1, p2, p3] = normalizedDob.split('/');
      const dd = p1.padStart(2, '0');
      const mm = p2.padStart(2, '0');
      const yyyy = p3;
      normalizedDob = `${yyyy}-${mm}-${dd}`;
    }
    // If already yyyy-MM-dd, leave as-is

    this.authService
      .forgotPassword(this.email, normalizedDob)
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          this.success = response.success;
          this.message = response.message;
          if (response.success) {
            this.formState = 'changePassword';
          }
        },
        error: (err) => {
          this.success = false;
          this.message = err.error?.message || 'Server error';
        },
      });
  }

  changePassword() {
    this.message = '';
    this.success = false;

    const passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    if (!passwordRegex.test(this.newPassword)) {
      this.message =
        'Password must be at least 8 characters long, and contain at least one number and one special character.';
      this.success = false;
      return;
    }

    this.authService
      .changePassword(this.email, this.newPassword)
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          this.success = response.success;
          this.message = response.message;
          if (response.success) {
            this.formState = 'login';
          }
        },
        error: (err) => {
          this.success = false;
          if (err.error && err.error.password) {
            this.message = err.error.password;
          } else {
            this.message = err.error?.message || 'Server error';
          }
        },
      });
  }

  cancel() {
    this.formState = 'login';
    this.message = '';
  }
}

import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Client } from '../../models/client.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule],
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  client: Client = {
    name: '',
    email: '',
    dateOfBirth: '',
    country: '',
    postalCode: '',
    identification: [{ type: '', value: '' }],
    password: '',
    clientId: '',
  };

  name = '';
  confirmPassword = '';
  message = '';
  success = false;
  selectedFlag = '';
  errorMessages: { [key: string]: string } = {};

  countryConfig: {
    [key: string]: { idType: string; digits: number[]; flag: string };
  } = {
    India: { idType: 'Aadhar', digits: [12], flag: '🇮🇳' },
    'United States': { idType: 'SSN', digits: [9], flag: '🇺🇸' },
    'United Kingdom': { idType: 'CitizenCard', digits: [16], flag: '🇬🇧' },
    Ireland: { idType: 'PPS', digits: [8, 9], flag: '🇮🇪' },
    Canada: { idType: 'SIN', digits: [9], flag: '🇨🇦' },
  };

  constructor(private router: Router, private authService: AuthService) {}

  get countryList(): string[] {
    return Object.keys(this.countryConfig);
  }

  onCountryChange() {
    const config = this.countryConfig[this.client.country];
    if (config) {
      this.client.identification[0].type = config.idType;
      this.selectedFlag = config.flag;
    }
  }

  validateAge() {
    const dob = new Date(this.client.dateOfBirth);
    const today = new Date();

    const age = today.getFullYear() - dob.getFullYear();
    const hasHadBirthdayThisYear =
      today.getMonth() > dob.getMonth() ||
      (today.getMonth() === dob.getMonth() && today.getDate() >= dob.getDate());

    const actualAge = hasHadBirthdayThisYear ? age : age - 1;

    if (actualAge < 18) {
      this.errorMessages['dateOfBirth'] = 'You must be at least 18 years old.';
    } else {
      this.errorMessages['dateOfBirth'] = '';
    }
  }

  validateIdNumber() {
    const config = this.countryConfig[this.client.country];
    const idValue = this.client.identification[0].value;
    if (config && idValue) {
      const isValidLength = config.digits.includes(idValue.length);
      if (!isValidLength) {
        this.errorMessages['idValue'] = `ID Number must be ${config.digits.join(
          ' or '
        )} digits long.`;
      } else {
        this.errorMessages['idValue'] = '';
      }
    }
  }

  register() {
    this.errorMessages = {};
    this.message = '';

    const {
      email,
      dateOfBirth,
      country,
      postalCode,
      identification,
      password,
    } = this.client;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    const nameRegex = /^[A-Za-z\s]+$/;

    if (!this.name.trim()) {
      this.errorMessages['name'] = 'Name is required.';
    } else if (!nameRegex.test(this.name)) {
      this.errorMessages['name'] = 'Name must contain only letters and spaces.';
    }

    if (!emailRegex.test(email)) {
      this.errorMessages['email'] = 'Please enter a valid email address.';
    }

    if (!dateOfBirth) {
      this.errorMessages['dateOfBirth'] = 'Date of Birth is required.';
    } else {
      this.validateAge();
    }

    if (!country?.trim()) {
      this.errorMessages['country'] = 'Country is required.';
    }

    if (!postalCode?.trim()) {
      this.errorMessages['postalCode'] = 'Postal Code is required.';
    }

    if (!identification[0]?.type?.trim()) {
      this.errorMessages['idType'] = 'ID Type is required.';
    }

    if (!identification[0]?.value?.trim()) {
      this.errorMessages['idValue'] = 'ID Number is required.';
    } else {
      this.validateIdNumber();
    }

    if (!passwordRegex.test(password)) {
      this.errorMessages['password'] =
        'Password must be at least 8 characters, with 1 number and 1 special character.';
    }

    if (password !== this.confirmPassword) {
      this.errorMessages['confirmPassword'] = 'Passwords do not match.';
    }

    if (
      Object.keys(this.errorMessages).some((key) => this.errorMessages[key])
    ) {
      this.message = 'Please correct the highlighted errors.';
      this.success = false;
      return;
    }

    const [year, month, day] = dateOfBirth.split('-');
    const formattedDate = `${month}-${day}-${year}`;
    const registrationRequest = {
      ...this.client,
      name: this.name,
      dateOfBirth: formattedDate,
    };

    this.authService.register(registrationRequest).subscribe({
      next: (response) => {
        console.log('RegisterComponent: Registration successful', response);
        this.success = response.success;
        this.message = <string>response.message?.toString();
        if (this.success) {
          this.router.navigate(['/preferences']);
        }
      },
      error: (err) => {
        console.error('RegisterComponent: Registration failed', err);
        this.success = false;
        if (err.error) {
          if (
            err.error.message ===
            'Failed to save client data: Registration failed: Client details already exist with another user'
          ) {
            this.errorMessages['idValue'] =
              'Client details already exist with another user';
          } else {
            this.errorMessages = err.error;
            this.message = 'Please correct the highlighted errors.';
          }
        } else {
          this.message = 'An unexpected error occurred.';
        }
      },
    });
  }

  goBack() {
    this.router.navigate(['']);
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormGroup, FormBuilder, FormArray, Validators } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { Profile, Identification } from '../../models/profile.model';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-profile-content',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './profile-content.component.html',
  styleUrl: './profile-content.component.scss'
})
export class ProfileContentComponent implements OnInit {
  profile: Profile | null = null;
  isEditing = false;
  profileForm: FormGroup = new FormGroup({});
  errorMessages: { [key: string]: string } = {};
  idErrorMessages: { [key: string]: { [key: string]: string } } = {};

  // Hardcoded for this example - in a real app, get this from auth service
  clientId: string = '';

  constructor(
    private profileService: ProfileService,
    private fb: FormBuilder,
    private authService : AuthService,
    private router : Router
  ) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(/^[A-Za-z\s]+$/)]],
      email: [{value: '', disabled: true}],
      dateOfBirth: [{value: '', disabled: true}],
      country: ['', Validators.required],
      postalCode: ['', Validators.required],
      identification: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.clientId =  this.authService.currentUserValue?.clientId || '';
    if(this.clientId == ''){
      this.router.navigate(['/']);
    }
    this.loadProfile();

  }

  loadProfile(): void {
    this.profileService.getProfile(this.clientId).subscribe({
      next: (data) => {
        this.profile = data;
        this.patchFormValues();
      },
      error: (err) => {
        console.error('Error fetching profile:', err);
        // Display error notification
      }
    });
  }

  patchFormValues(): void {
    if (!this.profile) return;

    this.profileForm.patchValue({
      name: this.profile.name,
      email: this.profile.email,
      dateOfBirth: this.profile.dateOfBirth,
      country: this.profile.country,
      postalCode: this.profile.postalCode
    });

    // Clear existing identifications and add from profile
    this.identificationArray.clear();
    this.profile.identification.forEach(id => {
      this.identificationArray.push(this.createIdentificationGroup(id));
    });
  }

  get identificationArray(): FormArray {
    return this.profileForm.get('identification') as FormArray;
  }

  createIdentificationGroup(id?: Identification): FormGroup {
    return this.fb.group({
      id: [id?.id || null],
      type: [id?.type || '', Validators.required],
      value: [id?.value || '', Validators.required]
    });
  }

  addIdentification(): void {
    this.identificationArray.push(this.createIdentificationGroup());
  }

  removeIdentification(index: number): void {
    this.identificationArray.removeAt(index);
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      this.patchFormValues();
      this.errorMessages = {};
      this.idErrorMessages = {};
    }
  }

  getIdErrorMessage(index: number, field: string): string {
    return this.idErrorMessages[index]?.[field] || '';
  }

  validateForm(): boolean {
    this.errorMessages = {};
    this.idErrorMessages = {};

    const formValues = this.profileForm.value;
    let isValid = true;

    // Validate name
    if (!formValues.name.trim()) {
      this.errorMessages['name'] = 'Name is required.';
      isValid = false;
    } else if (!/^[A-Za-z\s]+$/.test(formValues.name)) {
      this.errorMessages['name'] = 'Name must contain only letters and spaces.';
      isValid = false;
    }

    // Validate country
    if (!formValues.country.trim()) {
      this.errorMessages['country'] = 'Country is required.';
      isValid = false;
    }

    // Validate postal code
    if (!formValues.postalCode.trim()) {
      this.errorMessages['postalCode'] = 'Postal Code is required.';
      isValid = false;
    }

    // Validate identifications
    formValues.identification.forEach((id: any, index: number) => {
      if (!this.idErrorMessages[index]) {
        this.idErrorMessages[index] = {};
      }

      if (!id.type.trim()) {
        this.idErrorMessages[index]['type'] = 'ID Type is required.';
        isValid = false;
      }

      if (!id.value.trim()) {
        this.idErrorMessages[index]['value'] = 'ID Number is required.';
        isValid = false;
      }
    });

    return isValid;
  }

  saveProfile(): void {
    if (!this.validateForm()) {
      return;
    }

    // Update basic profile info
    const profileUpdate = {
      name: this.profileForm.value.name,
      country: this.profileForm.value.country,
      postalCode: this.profileForm.value.postalCode
    };

    this.profileService.updateProfile(this.clientId, profileUpdate).subscribe({
      next: () => {
        // Update identifications separately
        const identifications = this.identificationArray.value;
        this.profileService.updateIdentifications(this.clientId, identifications).subscribe({
          next: () => {
            this.isEditing = false;
            this.loadProfile(); // Reload the profile

            // Show success message (you can use a custom notification system)
            this.showNotification('Profile updated successfully', 'success');
          },
          error: (err) => {
            console.error('Error updating identifications:', err);
            this.showNotification('Error updating identifications', 'error');
          }
        });
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        this.showNotification('Error updating profile', 'error');
      }
    });
  }

  showNotification(message: string, type: 'success' | 'error'): void {
    // Create a notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    // Add to document
    document.body.appendChild(notification);

    // Remove after 3 seconds
    setTimeout(() => {
      notification.classList.add('fade-out');
      setTimeout(() => {
        document.body.removeChild(notification);
      }, 300);
    }, 3000);
  }
}

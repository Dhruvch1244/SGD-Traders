// models/profile.model.ts
export interface Identification {
  id?: string;
  clientId?: string;
  type: string;
  value: string;
  new?: boolean;
}

export interface Profile {
  clientId: string;
  name: string;
  email: string;
  dateOfBirth: string;
  country: string;
  postalCode: string;
  identification: Identification[];
}

export interface ProfileUpdateRequest {
  name?: string;
  country?: string;
  postalCode?: string;
}

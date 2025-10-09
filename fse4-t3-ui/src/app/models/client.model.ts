export interface ClientIdentification {
  type: string;
  value: string;
}

export interface Client {
  name: string;
  clientId: string;
  email: string;
  dateOfBirth: string;
  country: string;
  postalCode: string;
  identification: ClientIdentification[];
  password: string;
}


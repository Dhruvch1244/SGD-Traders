
export interface ClientIdentification {
  type: string;
  value: string;
}

export interface ClientDTO {
  id: string;
  clientId: string;
  email: string;
  name: string;
  roles?: string[];
  fmtsToken?: string;
  fmtsClientId?: string;
  dateOfBirth?: string;
  country?: string;
  postalCode?: string;
  identification?: ClientIdentification[];
}

export interface AuthResponseDTO {
  success: boolean;
  message?: string;
  data?: {
    client: ClientDTO;
    token?: string;
    fmtsToken?: string;
    fmtsClientId?: string;
  };
}

// src/app/models/auth-result.model.ts
import { Client } from './client.model';

export interface AuthResult {
  success: boolean;
  message?: string;
  client?: Client;
}

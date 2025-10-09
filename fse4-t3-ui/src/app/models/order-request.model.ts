export interface OrderRequestDto {
  quantity: number;
  instrumentId: string;
  targetPrice: number;
  direction: 'B' | 'S';
  fmtsClientId: string | null;
  email: string;
  token: string | null;
  localClientId: string;
}

export interface Instrument {
  instrumentId: string;
  description: string;
  externalIdType: string;
  externalId: string;
  categoryId: string;
  minQuantity: number;
  maxQuantity: number;
}

import { Instrument } from './instruments.model';
export interface Price {
  instrumentId: string;
  bidPrice: number;
  askPrice: number;
  timestamp: string;
  instrument: Instrument;
}

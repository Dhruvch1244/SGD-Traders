import { TestBed } from '@angular/core/testing';
import { TradeService } from './trade.service';
import { Order } from '../models/order.model';
import { Trade } from '../models/trade.model';
import { take } from 'rxjs/operators';

describe('TradeService', () => {
  let service: TradeService;

  beforeAll(() => {
    TestBed.configureTestingModule({
      providers: [TradeService],
    });
  });

  beforeEach(() => {
    service = TestBed.inject(TradeService);
    // Reset internal trades array if it exists
    if ((service as any).trades) {
      (service as any).trades.length = 0;
    }
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

//   describe('submitBuyTrade', () => {
//     it('should execute a buy trade if funds are sufficient', (done) => {
//       const order: Order = {
//         instrumentId: 'I1',
//         quantity: 100,
//         targetPrice: 500,
//         clientId: 'C1',
//         direction: 'B',
//         orderId: 'O1',
//       };

//       service.submitBuyTrade(order).pipe(take(1)).subscribe((result) => {
//         expect(result).toBe('Buy trade successful');

//         const trades: Trade[] = (service as any).trades;
//         expect(trades.length).toBe(1);
//         expect(trades[0].direction).toBe('B');
//         expect(trades[0].cashValue).toBe(order.quantity * order.targetPrice);

//         done();
//       });
//     });

//     it('should fail if funds are insufficient', (done) => {
//       const order: Order = {
//         instrumentId: 'I2',
//         quantity: 1000,
//         targetPrice: 2000, // total cost exceeds 100000
//         clientId: 'C2',
//         direction: 'B',
//         orderId: 'O2',
//       };

//       service.submitBuyTrade(order).pipe(take(1)).subscribe((result) => {
//         expect(result).toBe('Insufficient funds');
//         expect((service as any).trades.length).toBe(0);
//         done();
//       });
//     });
//   });

//   describe('submitSellTrade', () => {
//     it('should execute a sell trade if holdings are sufficient', (done) => {
//       const order: Order = {
//         instrumentId: 'I3',
//         quantity: 50,
//         targetPrice: 1000,
//         clientId: 'C3',
//         direction: 'S',
//         orderId: 'O3',
//       };

//       service.submitSellTrade(order).pipe(take(1)).subscribe((result) => {
//         expect(result).toBe('Sell trade successful');

//         const trades: Trade[] = (service as any).trades;
//         expect(trades.length).toBe(1);
//         expect(trades[0].direction).toBe('S');
//         expect(trades[0].cashValue).toBe(order.quantity * order.targetPrice);

//         done();
//       });
//     });

//     it('should fail if holdings are insufficient', (done) => {
//       const order: Order = {
//         instrumentId: 'I4',
//         quantity: 200, // more than mock owned quantity 100
//         targetPrice: 500,
//         clientId: 'C4',
//         direction: 'S',
//         orderId: 'O4',
//       };

//       service.submitSellTrade(order).pipe(take(1)).subscribe((result) => {
//         expect(result).toBe('Insufficient holdings');
//         expect((service as any).trades.length).toBe(0);
//         done();
//       });
//     });
//   });

//   it('should correctly update trades array on multiple trades', (done) => {
//     const buyOrder: Order = { instrumentId: 'I1', quantity: 10, targetPrice: 1000, clientId: 'C1', direction: 'B', orderId: 'O5' };
//     const sellOrder: Order = { instrumentId: 'I1', quantity: 5, targetPrice: 1200, clientId: 'C1', direction: 'S', orderId: 'O6' };

//     service.submitBuyTrade(buyOrder).pipe(take(1)).subscribe(() => {
//       service.submitSellTrade(sellOrder).pipe(take(1)).subscribe(() => {
//         const trades: Trade[] = (service as any).trades;
//         expect(trades.length).toBe(2);
//         expect(trades[0].direction).toBe('B');
//         expect(trades[1].direction).toBe('S');
//         done();
//       });
//     });
//   });
 });

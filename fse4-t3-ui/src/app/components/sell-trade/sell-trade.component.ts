import { Component, OnInit, OnDestroy } from '@angular/core';
import { InstrumentService } from '../../services/instrument.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, of } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { PortfolioHolding } from '../../models/portfolio.model';
import { switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-sell-trade',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sell-trade.component.html',
  styleUrls: ['./sell-trade.component.scss'],
})
export class SellTradeComponent implements OnInit, OnDestroy {
  holdings: PortfolioHolding[] = [];
  selectedInstrument = '';
  quantity = 1;
  price = 0;

  // Amount-based slider values (currency)
  saleAmount = 0;
  minAmount = 0;
  maxAmount = 0;

  minQuantity = 1;
  maxQuantity = 1;
  clientId = '';
  tradeResult = '';

  private subscription = new Subscription();

  constructor(
    private instrumentService: InstrumentService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const user = this.authService.currentUserValue;
    this.clientId = user?.clientId || '';
    if (!this.clientId) return;

    const sub = this.instrumentService
      .getPortfolioByClientId(this.clientId)
      .pipe(
        catchError((err) => {
          console.error('Portfolio fetch error', err);
          return of(undefined);
        })
      )
      .subscribe((portfolio) => {
        const normalized = this.normalizeHoldings(portfolio as any);
        this.holdings = normalized.filter((h) => h.quantity > 0);

        if (!this.selectedInstrument && this.holdings.length > 0) {
          this.selectedInstrument = this.holdings[0].instrumentId;
        }

        this.onInstrumentChange();
      });

    this.subscription.add(sub);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private normalizeHoldings(payload: any): PortfolioHolding[] {
    if (!payload) return [];

    const mk = (h: any) => ({
      instrumentId: String(h.instrumentId),
      quantity: Number(h.quantity) || 0,
      instrumentName:
        h.instrumentName ||
        h.name ||
        h.description ||
        h.instrument?.name ||
        String(h.instrumentId),
    });

    if (Array.isArray(payload.holdings)) return payload.holdings.map(mk);
    if (Array.isArray(payload.rowData)) return payload.rowData.map(mk);
    if (payload.data && Array.isArray(payload.data.holdings))
      return payload.data.holdings.map(mk);
    if (Array.isArray(payload)) return payload.map(mk);
    return [];
  }

  onInstrumentChange() {
    if (!this.selectedInstrument) {
      this.resetControls();
      return;
    }

    const holding = this.holdings.find(
      (h) => h.instrumentId === this.selectedInstrument
    );

    const instSub = this.instrumentService
      .getInstrumentById(this.selectedInstrument)
      .pipe(
        switchMap((instrumentDetails: any) => {
          if (!instrumentDetails) return of(null);

          this.minQuantity = instrumentDetails.minQuantity ?? 1;
          this.maxQuantity = Math.min(
            holding?.quantity ?? 0,
            instrumentDetails.maxQuantity ?? Number.MAX_SAFE_INTEGER
          );

          if (this.maxQuantity < this.minQuantity)
            this.maxQuantity = this.minQuantity;
          if (this.quantity < this.minQuantity)
            this.quantity = this.minQuantity;
          if (this.quantity > this.maxQuantity)
            this.quantity = this.maxQuantity;

          return this.instrumentService.getPrice(this.selectedInstrument);
        }),
        catchError((err) => {
          console.error('Error loading instrument/price', err);
          return of(null);
        })
      )
      .subscribe((priceObj: any) => {
        this.price =
          (priceObj && (priceObj.bidPrice ?? priceObj.askPrice)) ?? 0;

        // compute amount bounds and sync saleAmount/quantity
        this.minAmount = this.minQuantity * this.price;
        this.maxAmount = this.maxQuantity * this.price;

        if (
          this.quantity >= this.minQuantity &&
          this.quantity <= this.maxQuantity
        ) {
          this.saleAmount = this.quantity * this.price;
        } else {
          this.saleAmount = this.minAmount;
          this.quantity = Math.max(
            this.minQuantity,
            Math.min(
              this.maxQuantity,
              Math.floor(this.saleAmount / (this.price || 1))
            )
          );
        }
      });

    this.subscription.add(instSub);
  }

  onQuantityChange(val: number) {
    if (typeof val !== 'number' || isNaN(val)) return;
    if (val > this.maxQuantity) {
      this.quantity = this.maxQuantity;
      this.tradeResult = `Cannot sell more than ${this.maxQuantity}`;
    } else if (val < this.minQuantity) {
      this.quantity = this.minQuantity;
    } else {
      this.quantity = val;
      this.tradeResult = '';
    }

    // sync saleAmount
    this.saleAmount = this.quantity * this.price;
  }

  onAmountChange(amount: number) {
    if (typeof amount !== 'number' || isNaN(amount)) return;
    if (amount > this.maxAmount) amount = this.maxAmount;
    if (amount < this.minAmount) amount = this.minAmount;

    this.saleAmount = amount;
    const approxQty = Math.floor(
      this.saleAmount / (this.price || 1) + 0.000001
    );
    this.quantity = Math.max(
      this.minQuantity,
      Math.min(this.maxQuantity, approxQty)
    );
  }

  onSubmit() {
    if (!this.selectedInstrument || this.quantity <= 0 || !this.clientId)
      return;
    if (this.quantity > this.maxQuantity) {
      this.tradeResult = `Cannot sell more than ${this.maxQuantity}`;
      return;
    }

    this.instrumentService
      .sellInstrument(this.selectedInstrument, this.quantity, this.price)
      .pipe(
        catchError((err) => {
          this.tradeResult = 'Sell trade failed.';
          console.error('Sell failed', err);
          return of(null);
        })
      )
      .subscribe((res) => {
        if (res) {
          this.tradeResult = 'Sell trade successful!';
          this.selectedInstrument = '';
          this.quantity = 1;
          this.price = 0;
          this.minQuantity = 1;
          this.maxQuantity = 1;
          this.saleAmount = 0;
          this.minAmount = 0;
          this.maxAmount = 0;
        }
      });
  }

  private resetControls() {
    this.price = 0;
    this.quantity = 1;
    this.minQuantity = 1;
    this.maxQuantity = 1;
    this.saleAmount = 0;
    this.minAmount = 0;
    this.maxAmount = 0;
    this.tradeResult = '';
  }
}

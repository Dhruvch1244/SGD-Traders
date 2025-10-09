import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, combineLatest } from 'rxjs';
import { InstrumentService } from '../../services/instrument.service';
import { AuthService } from '../../services/auth.service';
import { Instrument } from '../../models/instruments.model';
import { switchMap, map } from 'rxjs/operators';
@Component({
  selector: 'app-buy-trade',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './buy-trade.component.html',
  styleUrls: ['./buy-trade.component.scss'],
})
export class BuyTradeComponent implements OnInit, OnDestroy {
  // UI lists
  categories: string[] = ['STOCK', 'GOVT', 'CUSIP'];
  instruments: Instrument[] = [];
  filteredInstruments: Instrument[] = [];

  // search/filter
  searchQuery = '';
  selectedCategory = '';

  // selection / trade fields
  selectedInstrument = '';
  quantity = 1;
  price = 0;
  minQuantity = 1;
  maxQuantity = 1000;

  // wallet & amount slider
  walletBalance = 0;
  buyAmount = 0;
  minAmount = 0;
  maxAmount = 0;

  clientId = '';
  tradeResult = '';

  private subs = new Subscription();

  constructor(
    private instrumentService: InstrumentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // load client info / wallet
    const user = this.authService.currentUserValue;
    this.clientId = user?.clientId || '';
    if (this.clientId) {
      const p = this.instrumentService
        .getPortfolioByClientId(this.clientId)
        .subscribe((p: any) => {
          const cash = p?.cash || p?.wallet || p?.accountBalance || p?.balance;
          this.walletBalance = Number(cash) || 0;
        });
      this.subs.add(p);
    }

    // load instruments (local cache)
    const iSub = this.instrumentService
      .getAllInstruments()
      .subscribe((items) => {
        this.instruments = items || [];
        this.updateFilteredInstruments();
      });
    this.subs.add(iSub);
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // Category changed -> update instrument source list and reset selection
onCategoryChange(): void {
  if (this.selectedCategory) {
    this.instrumentService
  .searchInstruments('', this.selectedCategory)
  .pipe(
    switchMap((filtered: any[]) => {
      return this.instrumentService.getAllInstruments().pipe(
        map((all: any[]) => {
          const filteredIds = new Set(filtered.map((f: any) => f.instrumentId));
          const result = all.filter((inst: any) => filteredIds.has(inst.instrumentId));
          return result;
        })
      );
    })
  )
  .subscribe((final: any[]) => {
    this.instruments = final;
   this.filteredInstruments = final; 
  });

  } else {
    this.instrumentService.getAllInstruments().subscribe((items) => {
      this.instruments = items;
      this.updateFilteredInstruments();
    });
  }

  // reset form values
  this.selectedInstrument = '';
  this.price = 0;
  this.quantity = 1;
  this.minQuantity = 1;
  this.maxQuantity = 1000;
}


  // Search input handler (optional field) -> updates filtered dropdown
  onSearchInput(val: string): void {
    this.searchQuery = val || '';
    this.updateFilteredInstruments();
  }

  // Called when user selects an option from the instrument select
  onInstrumentSelectChange(event: any): void {
    const value = event?.target?.value;
    if (value === '__search__') {
      const el = document.querySelector(
        '.buy-trade-form .search-row input'
      ) as HTMLInputElement;
      if (el) el.focus();
      // reset select back
      this.selectedInstrument = '';
      return;
    }
    this.selectedInstrument = value || '';
    this.loadInstrumentDetails();
  }

  // Load price and instrument metadata and compute bounds
  private loadInstrumentDetails(): void {
    if (!this.selectedInstrument) return;
    const sub = combineLatest([
      this.instrumentService.getPrice(this.selectedInstrument),
      this.instrumentService.getInstrumentById(this.selectedInstrument),
    ]).subscribe(([priceDetails, instrumentDetails]) => {
      if (!instrumentDetails) return;
      const unitPrice = (priceDetails?.askPrice ?? priceDetails?.bidPrice) || 0;
      this.price = unitPrice;
      this.minQuantity = instrumentDetails.minQuantity ?? 1;
      const instrMax = instrumentDetails.maxQuantity ?? Number.MAX_SAFE_INTEGER;
      // wallet-limited max quantity
      const walletLimit =
        unitPrice > 0 ? Math.floor(this.walletBalance / unitPrice) : instrMax;
      this.maxQuantity = Math.min(instrMax, walletLimit || instrMax);
      if (this.maxQuantity < this.minQuantity)
        this.maxQuantity = this.minQuantity;
      this.quantity = this.minQuantity;
      // amount bounds
      this.minAmount = this.minQuantity * unitPrice;
      this.maxAmount = this.maxQuantity * unitPrice;
      this.buyAmount = this.minAmount;
    });
    this.subs.add(sub);
  }

  // Filter instrument source by category + search term
  private updateFilteredInstruments(): void {
    const term = (this.searchQuery || '').trim().toLowerCase();
    const cat = this.selectedCategory || '';
    const source = this.instruments || [];
    this.filteredInstruments = source.filter((inst: any) => {
      if (cat && cat !== '') {
        if (inst.categoryId !== cat) return false;
      }
      if (!term) return true;
      const desc = (inst.description || inst.name || '').toLowerCase();
      const id = (inst.instrumentId || inst.id || '').toString().toLowerCase();
      return desc.includes(term) || id.includes(term);
    });
  }

  // quantity / amount sync
  onQuantityChangeLocal(val: number): void {
    if (typeof val !== 'number' || isNaN(val)) return;
    if (val > this.maxQuantity) this.quantity = this.maxQuantity;
    else if (val < this.minQuantity) this.quantity = this.minQuantity;
    else this.quantity = val;
    this.buyAmount = this.quantity * (this.price || 0);
  }

  onAmountChangeLocal(amount: number): void {
    if (typeof amount !== 'number' || isNaN(amount)) return;
    if (amount > this.maxAmount) amount = this.maxAmount;
    if (amount < this.minAmount) amount = this.minAmount;
    this.buyAmount = amount;
    const approxQty = Math.floor(this.buyAmount / (this.price || 1) + 0.000001);
    this.quantity = Math.max(
      this.minQuantity,
      Math.min(this.maxQuantity, approxQty)
    );
  }

  // Submit buy trade
  onSubmit(): void {
    if (!this.clientId || !this.selectedInstrument || this.quantity <= 0)
      return;
    const sub = this.instrumentService
      .buyInstrument(this.selectedInstrument, this.quantity, this.price)
      .subscribe({
        next: () => (this.tradeResult = 'Buy trade successful!'),
        error: () => (this.tradeResult = 'Buy trade failed.'),
      });
    this.subs.add(sub);
  }
}

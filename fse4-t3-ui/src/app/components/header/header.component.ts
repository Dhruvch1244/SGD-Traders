import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterModule } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../services/auth.service';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import { InstrumentService } from '../../services/instrument.service';
import { FormsModule } from '@angular/forms';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { Observable, Subscription, combineLatest, take } from 'rxjs';
import { WalletService } from '../../services/wallet.service'; // Import WalletService

// PDF imports
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { Wallet } from '../../models/wallet.model';
import { Instrument } from '../../models/instruments.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    CommonModule,
    RouterModule,
    FormsModule,
    MatFormField,
    MatLabel,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent implements OnInit, OnDestroy {
  navigateTo(path: string) {
    this.router.navigate([path]);
  }

  navigateToSell() {
    this.router.navigate(['/trade'], { queryParams: { mode: 'sell' } });
  }

  navigateToHome() {
    this.router.navigate(['/landing']);
  }

  toggleProfileMenu() {
    this.isProfileMenuOpen = !this.isProfileMenuOpen;
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }
  private userSubscription: Subscription | undefined;
  clientId: string = '';
  clientName: string = '';
  walletBalance$: Observable<number>; // Use Observable for wallet balance
  showAddMoneyForm = false;
  amountToAdd: number | null = null;
  isProfileMenuOpen = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private instrumentService: InstrumentService,
    private walletService: WalletService,
    private preferencesService: InvestmentPreferencesService
  ) {
    this.walletBalance$ = this.walletService.walletBalance$;
  }

  ngOnInit(): void {
    this.userSubscription = this.authService.currentUser$.subscribe((user) => {
      if (user) {
        this.clientId = user.clientId;
        this.clientName = user.name;
        // Fetch initial wallet balance when user logs in
        this.walletService.getWalletBalance(this.clientId).subscribe();
        this.instrumentService.loadTradesForClient(this.clientId);
      } else {
        this.router.navigate(['']);
      }
    });
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.flushLocalStorage();
        this.router.navigate(['']);
      },
      error: (err) => {
        console.error('Logout failed:', err);
      },
    });
  }

  toggleAddMoneyForm() {
    this.showAddMoneyForm = !this.showAddMoneyForm;
    if (!this.showAddMoneyForm) {
      this.amountToAdd = null;
    }
  }

  addMoney() {
    if (this.amountToAdd && this.amountToAdd > 0) {
      this.walletService
        .addMoneyToWallet(this.clientId, this.amountToAdd)
        .subscribe({
          next: () => {
            this.showAddMoneyForm = false;
            this.amountToAdd = null;
          },
          error: (err) => {
            console.error('Error adding money:', err);
            // Handle error, e.g., show a message to the user
          },
        });
    } else {
      console.error('Invalid amount entered. Please enter a positive number.');
    }
  }

  //   changePass() {
  //   // this.authService.changePassword();
  //   this.router.navigate(['']);
  // }

  generateFullReport() {
    combineLatest([
      this.instrumentService.getPortfolioByClientId(this.clientId),
      this.walletService.walletBalance$,
      this.instrumentService.trades$,
      this.instrumentService.instruments$, // Get all instruments
      this.instrumentService.getLatestInstrumentPrices(), // Add prices observable here
    ])
      .pipe(take(1))
      .subscribe(
        ([
          portfolio,
          currentWalletBalance,
          trades,
          instruments,
          latestPrices,
        ]) => {
          const instrumentMap = new Map<string, Instrument>(
            instruments.map((inst: Instrument) => [inst.instrumentId, inst])
          );

          const doc = new jsPDF();
          let yOffset = 15;

          // ===== Header =====
          doc.setFontSize(18);
          doc.text(`Client: ${this.clientName}`, 14, yOffset);
          // Add current date and time to the header
          const now = new Date();
          const dateTimeStr = now.toLocaleString();
          doc.setFontSize(12);
          doc.text(`Report generated: ${dateTimeStr}`, 120, yOffset); // right side
          yOffset += 8;
          doc.setFontSize(14);
          doc.text(
            `Wallet Balance: ${Number(currentWalletBalance).toFixed(2)}`,
            14,
            yOffset
          );
          yOffset += 10;

          // ===== Trade History Table =====
          if (trades.length > 0) {
            doc.setFontSize(16);
            doc.text('Trade History', 14, yOffset);
            yOffset += 6;

            const tradeBody = trades.map((t: any) => {
              const instrument = instrumentMap.get(t.instrumentId);
              const description = instrument?.description || t.instrumentId;
              // Format timestamp if present
              const timestamp = t.timestamp
                ? new Date(t.timestamp).toLocaleString()
                : '';
              const direction =
                t.direction === 'B' || t.direction === 'BUY'
                  ? 'Buy'
                  : t.direction === 'S' || t.direction === 'SELL'
                  ? 'Sell'
                  : t.direction;
              return [
                t.tradeId,
                description,
                direction,
                t.quantity,
                `${Number(t.executionPrice).toFixed(2)}`,
                `${Number(t.cashValue).toFixed(2)}`,
                timestamp,
              ];
            });

            autoTable(doc, {
              startY: yOffset,
              head: [
                [
                  'Trade ID',
                  'Instrument',
                  'Direction',
                  'Quantity',
                  'Exec Price',
                  'Cash Value',
                  'Timestamp',
                ],
              ],
              body: tradeBody,
              styles: { fontSize: 8 },
            });

            yOffset = (doc as any).lastAutoTable.finalY + 10;
          }

          // ===== Portfolio Table =====
          if (
            portfolio &&
            portfolio.holdings &&
            portfolio.holdings.length > 0
          ) {
            const priceMap = new Map<string, number>(
              latestPrices.map(
                (p: { instrumentId: string; askPrice: number }) => [
                  p.instrumentId,
                  p.askPrice,
                ]
              )
            );
            const costBasisMap = this.instrumentService.getClientCostBasis(
              this.clientId
            );

            const portfolioBody = portfolio.holdings.map((holding: any) => {
              const instrument = instrumentMap.get(holding.instrumentId);
              const currentPrice =
                Number(priceMap.get(holding.instrumentId)) || 0;
              const costBasis =
                Number(costBasisMap.get(holding.instrumentId)) || 0;
              const totalCost = costBasis * holding.quantity;
              const marketValue = currentPrice * holding.quantity;
              const unrealizedPl = marketValue - totalCost;
              const unrealizedPlPercent =
                totalCost > 0 ? (unrealizedPl / totalCost) * 100 : 0;

              return [
                instrument?.description || 'Unknown',
                holding.quantity,
                instrument?.categoryId || 'Unknown',
                `${costBasis.toFixed(2)}`,
                `${totalCost.toFixed(2)}`,
                `${marketValue.toFixed(2)}`,
                `${unrealizedPl.toFixed(2)}`,
                `${unrealizedPlPercent.toFixed(2)}%`,
              ];
            });

            doc.setFontSize(16);
            doc.text('Portfolio Holdings', 14, yOffset);
            yOffset += 6;

            autoTable(doc, {
              startY: yOffset,
              head: [
                [
                  'Instrument',
                  'Quantity',
                  'Category',
                  'Avg. Cost',
                  'Total Cost',
                  'Market Value',
                  'Unrealized P/L',
                  'Unrealized P/L %',
                ],
              ],
              body: portfolioBody,
              styles: { fontSize: 8 },
            });
          }

          // ===== Save PDF =====
          doc.save(`Client_${this.clientName}_FullReport.pdf`);
        }
      );
  }
}

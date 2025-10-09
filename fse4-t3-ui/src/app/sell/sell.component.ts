import { Component } from '@angular/core';
import { HeaderComponent } from '../components/header/header.component';
import { BuyTradeComponent } from '../components/buy-trade/buy-trade.component';
import { SidebarComponent } from '../components/sidebar/sidebar.component';
import { SellTradeComponent } from '../components/sell-trade/sell-trade.component';

@Component({
  selector: 'app-sell',
  standalone: true,
  imports: [HeaderComponent, SidebarComponent, SellTradeComponent],
  templateUrl: './sell.component.html',
  styleUrl: './sell.component.scss',
})
export class SellComponent {}

import { Component } from '@angular/core';
import { HeaderComponent } from '../components/header/header.component';
import { SidebarComponent } from '../components/sidebar/sidebar.component';
import { BuyTradeComponent } from '../components/buy-trade/buy-trade.component';

@Component({
  selector: 'app-buy',
  standalone: true,
  imports: [HeaderComponent, SidebarComponent, BuyTradeComponent],
  templateUrl: './buy.component.html',
  styleUrl: './buy.component.scss',
})
export class BuyComponent {}

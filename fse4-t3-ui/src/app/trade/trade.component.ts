import { Component } from '@angular/core';
import { HeaderComponent } from '../components/header/header.component';
import { SidebarComponent } from '../components/sidebar/sidebar.component';
import { TradePageComponent } from '../components/trade-page/trade-page.component';

@Component({
  selector: 'app-trade',
  standalone: true,
  imports: [HeaderComponent, SidebarComponent, TradePageComponent],
  templateUrl: './trade.component.html',
  styleUrl: './trade.component.scss',
})
export class TradeComponent {}

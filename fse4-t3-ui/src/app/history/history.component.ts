import { Component } from '@angular/core';
import { HeaderComponent } from '../components/header/header.component';
import { SidebarComponent } from '../components/sidebar/sidebar.component';
import { TradeHistoryComponent } from '../components/trade-history/trade-history.component';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [HeaderComponent, SidebarComponent, TradeHistoryComponent],
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss',
})
export class HistoryComponent {}

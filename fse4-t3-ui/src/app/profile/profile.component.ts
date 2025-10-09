import { Component } from '@angular/core';
import {HeaderComponent} from '../components/header/header.component';
import {SidebarComponent} from '../components/sidebar/sidebar.component';
import {ProfileContentComponent} from '../components/profile-content/profile-content.component';
import {TradeHistoryComponent} from '../components/trade-history/trade-history.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [HeaderComponent, SidebarComponent, ProfileContentComponent, TradeHistoryComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {

}

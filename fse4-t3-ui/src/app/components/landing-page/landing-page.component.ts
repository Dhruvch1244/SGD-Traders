import { Component } from '@angular/core';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [SidebarComponent, DashboardComponent, HeaderComponent],
  templateUrl: './landing-page.component.html',
  styleUrl: './landing-page.component.scss',
})
export class LandingPageComponent {}

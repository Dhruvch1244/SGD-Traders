import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { PortfolioContentComponent } from '../../portfolio-content/portfolio-content.component';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [HeaderComponent, SidebarComponent, PortfolioContentComponent],
  templateUrl: './portfolio.component.html',
  styleUrl: './portfolio.component.scss',
})
export class PortfolioComponent {}

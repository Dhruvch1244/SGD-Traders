import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { RoboAdvisorComponent } from './components/robo-advisor/robo-advisor.component';
import { CommonModule} from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RoboAdvisorComponent,
    RouterOutlet,
    ReactiveFormsModule,
  ],
  templateUrl: './app.component.html',
 styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'trading-app';
  showRoboAdvisor = true;
  constructor(private router: Router) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects || event.url;
        this.showRoboAdvisor = !(
          url === '/' ||
          url.startsWith('/register') ||
          url.startsWith('/preferences')
        );
      }
    });
  }
}

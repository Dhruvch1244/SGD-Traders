import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { PortfolioComponent } from './components/portfolio/portfolio.component';
import { BuyTradeComponent } from './components/buy-trade/buy-trade.component';
import { SellTradeComponent } from './components/sell-trade/sell-trade.component';
import { TradeHistoryComponent } from './components/trade-history/trade-history.component';
import { InvestmentPreferencesComponent } from './components/investment-preferences/investment-preferences.component';
import { HistoryComponent } from './history/history.component';
import { BuyComponent } from './buy/buy.component';
import { SellComponent } from './sell/sell.component';
import { TradePageComponent } from './components/trade-page/trade-page.component';
import { PortfolioContentComponent } from './portfolio-content/portfolio-content.component';
import { TradeComponent } from './trade/trade.component';
import { InvestmentPreferencesUpdateFormComponent } from './components/investment-preferences-update-form/investment-preferences-update-form.component';
import { RoboAdvisorComponent } from './components/robo-advisor/robo-advisor.component';
import { AuthGuard } from './services/auth.guard';
import {ProfileComponent} from './profile/profile.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'landing', component: LandingPageComponent },
  { path: 'portfolio', component: PortfolioComponent },
  { path: 'trade', component: TradeComponent },
  { path: 'sell', component: SellComponent },
  { path: 'history', component: HistoryComponent },
  {
    path: 'preferences',
    component: InvestmentPreferencesComponent,
  },
  {
    path: 'profile',
    component:ProfileComponent,
    // canActivate: [AuthGuard],
  },
  {
    path: 'preferences/update',
    component: InvestmentPreferencesUpdateFormComponent,
  },
  { path: 'robo-advisor', component: RoboAdvisorComponent },
];

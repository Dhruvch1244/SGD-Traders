// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { Router, NavigationEnd } from '@angular/router';
// import { AppComponent } from './app.component';
// import { RoboAdvisorComponent } from './components/robo-advisor/robo-advisor.component';
// import { CommonModule } from '@angular/common';
// import { RouterTestingModule } from '@angular/router/testing';
// import { of, Subject } from 'rxjs';

// describe('AppComponent', () => {
//   let component: AppComponent;
//   let fixture: ComponentFixture<AppComponent>;
//   let routerEvents$: Subject<any>;

//   beforeEach(async () => {
//     routerEvents$ = new Subject<any>();

//     await TestBed.configureTestingModule({
//       imports: [AppComponent, CommonModule, RouterTestingModule.withRoutes([])],
//       providers: [
//         {
//           provide: Router,
//           useValue: {
//             events: routerEvents$.asObservable(),
//           },
//         },
//       ],
//     }).compileComponents();

//     fixture = TestBed.createComponent(AppComponent);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });

//   it('should create the AppComponent', () => {
//     expect(component).toBeTruthy();
//   });

//   it('should show RoboAdvisor by default', () => {
//     expect(component.showRoboAdvisor).toBeTrue();
//   });

//   it('should hide RoboAdvisor for root "/" route', () => {
//     routerEvents$.next(new NavigationEnd(1, '/', '/'));
//     expect(component.showRoboAdvisor).toBeFalse();
//   });

//   it('should hide RoboAdvisor for "/register" route', () => {
//     routerEvents$.next(new NavigationEnd(1, '/register', '/register'));
//     expect(component.showRoboAdvisor).toBeFalse();
//   });

//   it('should hide RoboAdvisor for "/preferences" route', () => {
//     routerEvents$.next(new NavigationEnd(1, '/preferences', '/preferences'));
//     expect(component.showRoboAdvisor).toBeFalse();
//   });

//   it('should show RoboAdvisor for other routes', () => {
//     routerEvents$.next(new NavigationEnd(1, '/dashboard', '/dashboard'));
//     expect(component.showRoboAdvisor).toBeTrue();

//     routerEvents$.next(new NavigationEnd(1, '/profile/settings', '/profile/settings'));
//     expect(component.showRoboAdvisor).toBeTrue();
//   });
// });

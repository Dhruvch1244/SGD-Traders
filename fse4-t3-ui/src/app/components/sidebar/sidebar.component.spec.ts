import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SidebarComponent } from './sidebar.component';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let router: Router;

  beforeAll(() => {
    // NEVER configure TestBed inside individual test cases
    TestBed.configureTestingModule({
      imports: [
        SidebarComponent,
        RouterTestingModule.withRoutes([]),
        MatListModule,
        MatIconModule,
        CommonModule,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create the sidebar component', () => {
    expect(component).toBeTruthy();
  });

  it('should expand sidebar on mouseenter and collapse on mouseleave', () => {
    const sidebarDiv = fixture.debugElement.query(By.css('.sidebar'));

    sidebarDiv.triggerEventHandler('mouseenter', {});
    fixture.detectChanges();
    expect(component.isCollapsed).toBeFalse();

    sidebarDiv.triggerEventHandler('mouseleave', {});
    fixture.detectChanges();
    expect(component.isCollapsed).toBeTrue();
  });

  it('should conditionally render label span based on isCollapsed', () => {
    component.isCollapsed = false;
    fixture.detectChanges();
    const labels = fixture.debugElement.queryAll(By.css('.label'));
    expect(labels.length).toBe(4); // One for each menu item

    component.isCollapsed = true;
    fixture.detectChanges();
    const hiddenLabels = fixture.debugElement.queryAll(By.css('.label'));
    expect(hiddenLabels.length).toBe(0);
  });

  it('should call router.navigate when navigateTo is called', () => {
    const spy = spyOn(router, 'navigate');
    component.navigateTo('/portfolio');
    expect(spy).toHaveBeenCalledWith(['/portfolio']);
  });

  it('should navigate when list item is clicked', () => {
    const spy = spyOn(router, 'navigate');
    const listItems = fixture.debugElement.queryAll(By.css('mat-list-item'));

    listItems[2].triggerEventHandler('click', null); // Trade item
    expect(spy).toHaveBeenCalledWith(['/trade']);
  });
});

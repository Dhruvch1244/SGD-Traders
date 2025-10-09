

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoboAdvisorComponent } from './robo-advisor.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RoboAdvisorService, RoboAdvisorRecommendation } from '../../services/robo-advisor.service';
import { AuthService } from '../../services/auth.service';
import { BehaviorSubject, of, throwError } from 'rxjs';

describe('RoboAdvisorComponent', () => {
	let component: RoboAdvisorComponent;
	let fixture: ComponentFixture<RoboAdvisorComponent>;
	let roboAdvisorServiceSpy: jasmine.SpyObj<RoboAdvisorService>;
	let authServiceMock: any;
	let currentUserSubject: BehaviorSubject<any>;

	const mockRecommendations: RoboAdvisorRecommendation[] = [
		{ instrumentId: '1', description: 'Apple Inc.', category: 'STOCK' },
		{ instrumentId: '2', description: 'Government Bond', category: 'GOVT' },
		{ instrumentId: '3', description: 'Corporate Bond', category: 'CORP' },
	];

	beforeEach(async () => {
		await TestBed.resetTestingModule();
		
		currentUserSubject = new BehaviorSubject({ clientId: 'test-client-123' });
		
		const roboSpy = jasmine.createSpyObj('RoboAdvisorService', ['getRecommendations']);
		roboSpy.getRecommendations.and.returnValue(of(mockRecommendations));

		authServiceMock = {
			currentUser$: currentUserSubject.asObservable()
		};

		await TestBed.configureTestingModule({
			imports: [RoboAdvisorComponent, HttpClientTestingModule],
			providers: [
				{ provide: RoboAdvisorService, useValue: roboSpy },
				{ provide: AuthService, useValue: authServiceMock }
			],
		}).compileComponents();

		fixture = TestBed.createComponent(RoboAdvisorComponent);
		component = fixture.componentInstance;
		roboAdvisorServiceSpy = TestBed.inject(RoboAdvisorService) as jasmine.SpyObj<RoboAdvisorService>;
		fixture.detectChanges();
	});

	afterEach(() => {
		TestBed.resetTestingModule();
	});

	describe('Component Creation and Initialization', () => {
		it('should create the component', () => {
			expect(component).toBeTruthy();
		});

		it('should initialize with correct default values', () => {
			expect(component.recommendations).toEqual([]);
			expect(component.showRecommendations).toBeFalse();
			expect(component.isOpen).toBeFalse();
			expect(component.loading).toBeFalse();
			expect(component.error).toBeNull();
		});

		it('should have initial bot message', () => {
			expect(component.chatMessages.length).toBe(1);
			expect(component.chatMessages[0].type).toBe('bot');
			expect(component.chatMessages[0].text).toBe('Hi! I am your Robo Advisor. Click Recommend to get your personalized stock recommendations.');
		});

		it('should set clientId on init when user exists', () => {
			expect(component.clientId).toBe('test-client-123');
		});

		it('should handle null user on init', () => {
			currentUserSubject.next(null);
			expect(component.clientId).toBeNull();
		});

		it('should handle user without clientId', () => {
			currentUserSubject.next({});
			expect(component.clientId).toBeNull();
		});

		it('should handle undefined user', () => {
			currentUserSubject.next(undefined);
			expect(component.clientId).toBeNull();
		});
	});

	describe('Chat Functionality', () => {
		it('should toggle chat visibility', () => {
			expect(component.isOpen).toBeFalse();
			
			component.toggleChat();
			expect(component.isOpen).toBeTrue();
			
			component.toggleChat();
			expect(component.isOpen).toBeFalse();
		});

		it('should toggle chat multiple times correctly', () => {
			for (let i = 0; i < 5; i++) {
				component.toggleChat();
				expect(component.isOpen).toBe(i % 2 === 0);
			}
		});
	});

	describe('Recommendation Functionality', () => {
		it('should get recommendations successfully', () => {
			component.onRecommend();

			expect(roboAdvisorServiceSpy.getRecommendations).toHaveBeenCalledWith('test-client-123');
			expect(component.loading).toBeFalse();
			expect(component.showRecommendations).toBeTrue();
			expect(component.recommendations).toEqual(mockRecommendations);
			expect(component.error).toBeNull();
		});

		it('should add user and bot messages when recommending', () => {
			const initialMessageCount = component.chatMessages.length;
			
			component.onRecommend();

			expect(component.chatMessages.length).toBe(initialMessageCount + 2);
			
			const userMessage = component.chatMessages[component.chatMessages.length - 2];
			const botMessage = component.chatMessages[component.chatMessages.length - 1];

			expect(userMessage.type).toBe('user');
			expect(userMessage.text).toBe('Recommend');
			expect(botMessage.type).toBe('bot');
			expect(botMessage.text).toBe('Here are your recommended instruments:');
		});

		it('should handle recommendation error', () => {
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(throwError(() => new Error('Service error')));
			
			component.onRecommend();

			expect(component.loading).toBeFalse();
			expect(component.error).toBe('Failed to fetch recommendations.');
			expect(component.showRecommendations).toBeFalse();
		});

		it('should not call service when clientId is null', () => {
			component.clientId = null;
			
			component.onRecommend();

			expect(roboAdvisorServiceSpy.getRecommendations).not.toHaveBeenCalled();
			expect(component.error).toBe('No client ID found.');
			expect(component.loading).toBeFalse();
		});

		it('should not call service when clientId is empty string', () => {
			component.clientId = '';
			
			component.onRecommend();

			expect(roboAdvisorServiceSpy.getRecommendations).not.toHaveBeenCalled();
			expect(component.error).toBe('No client ID found.');
			expect(component.loading).toBeFalse();
		});

		it('should reset state before making recommendation request', () => {
			// Set some initial state
			component.error = 'Previous error';
			component.showRecommendations = true;
			
			component.onRecommend();

			expect(component.error).toBeNull();
			expect(component.showRecommendations).toBeTrue(); // Will be true after successful response
			expect(component.loading).toBeFalse(); // Will be false after successful response
		});

		it('should set loading state during request', () => {
			// Create a delayed observable to test loading state
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(
				new BehaviorSubject(mockRecommendations).asObservable()
			);
			
			component.onRecommend();
			
			// Loading should be false after immediate resolution
			expect(component.loading).toBeFalse();
		});
	});

	describe('Error Handling', () => {
		it('should handle service unavailable error', () => {
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(
				throwError(() => ({ status: 503, message: 'Service Unavailable' }))
			);
			
			component.onRecommend();

			expect(component.error).toBe('Failed to fetch recommendations.');
			expect(component.loading).toBeFalse();
		});

		it('should handle network error', () => {
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(
				throwError(() => new Error('Network error'))
			);
			
			component.onRecommend();

			expect(component.error).toBe('Failed to fetch recommendations.');
			expect(component.loading).toBeFalse();
		});

		it('should handle empty recommendations response', () => {
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(of([]));
			
			component.onRecommend();

			expect(component.recommendations).toEqual([]);
			expect(component.showRecommendations).toBeTrue();
			expect(component.error).toBeNull();
		});
	});

	describe('Authentication Integration', () => {
		it('should update clientId when user changes', () => {
			expect(component.clientId).toBe('test-client-123');
			
			currentUserSubject.next({ clientId: 'new-client-456' });
			
			expect(component.clientId).toBe('new-client-456');
		});

		it('should handle user logout scenario', () => {
			expect(component.clientId).toBe('test-client-123');
			
			currentUserSubject.next(null);
			
			expect(component.clientId).toBeNull();
		});

		it('should prevent recommendations when user is not authenticated', () => {
			currentUserSubject.next(null);
			
			component.onRecommend();

			expect(roboAdvisorServiceSpy.getRecommendations).not.toHaveBeenCalled();
			expect(component.error).toBe('No client ID found.');
		});
	});

	describe('Integration Tests', () => {
		it('should handle complete recommendation flow', () => {
			// Initial state
			expect(component.recommendations).toEqual([]);
			expect(component.showRecommendations).toBeFalse();
			
			// Toggle chat open
			component.toggleChat();
			expect(component.isOpen).toBeTrue();
			
			// Get recommendations
			component.onRecommend();
			
			// Verify final state
			expect(component.recommendations).toEqual(mockRecommendations);
			expect(component.showRecommendations).toBeTrue();
			expect(component.chatMessages.length).toBe(3); // Initial + user + bot
			expect(roboAdvisorServiceSpy.getRecommendations).toHaveBeenCalledWith('test-client-123');
		});

		it('should handle error recovery flow', () => {
			// First request fails
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(
				throwError(() => new Error('First error'))
			);
			
			component.onRecommend();
			expect(component.error).toBe('Failed to fetch recommendations.');
			
			// Second request succeeds
			roboAdvisorServiceSpy.getRecommendations.and.returnValue(of(mockRecommendations));
			
			component.onRecommend();
			expect(component.error).toBeNull();
			expect(component.recommendations).toEqual(mockRecommendations);
			expect(component.showRecommendations).toBeTrue();
		});

		it('should handle multiple recommendation requests', () => {
			// First request
			component.onRecommend();
			expect(roboAdvisorServiceSpy.getRecommendations).toHaveBeenCalledTimes(1);
			
			// Second request
			component.onRecommend();
			expect(roboAdvisorServiceSpy.getRecommendations).toHaveBeenCalledTimes(2);
			
			// Messages should accumulate
			expect(component.chatMessages.length).toBe(5); // Initial + 2*(user + bot)
		});
	});
});

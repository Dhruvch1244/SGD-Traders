import { Component, OnInit } from '@angular/core';
import { RoboAdvisorService, RoboAdvisorRecommendation } from '../../services/robo-advisor.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-robo-advisor',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './robo-advisor.component.html',
  styleUrls: ['./robo-advisor.component.scss'],
})
export class RoboAdvisorComponent implements OnInit {
  recommendations: RoboAdvisorRecommendation[] = [];
  chatMessages: { type: 'bot' | 'user'; text: string }[] = [
    {
      type: 'bot',
      text: 'Hi! I am your Robo Advisor. Click Recommend to get your personalized stock recommendations.',
    },
  ];
  showRecommendations = false;
  isOpen = false;
  clientId: string | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private roboAdvisorService: RoboAdvisorService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.clientId = user?.clientId || null;
    });
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  onRecommend() {
    if (!this.clientId) {
      this.error = 'No client ID found.';
      return;
    }
    this.loading = true;
    this.error = null;
    this.showRecommendations = false;
    this.chatMessages.push({ type: 'user', text: 'Recommend' });
    this.roboAdvisorService.getRecommendations(this.clientId).subscribe({
      next: (recs) => {
        this.recommendations = recs;
        this.showRecommendations = true;
        this.chatMessages.push({
          type: 'bot',
          text: `Here are your recommended instruments:`,
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to fetch recommendations.';
        this.loading = false;
      }
    });
  }
}

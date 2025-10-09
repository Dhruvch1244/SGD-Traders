import { Component, AfterViewInit, OnDestroy, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { AgGridModule } from 'ag-grid-angular';
import {
  ColDef,
  GridOptions,
  ICellRendererParams,
  ModuleRegistry,
} from 'ag-grid-community';
import { AllCommunityModule } from 'ag-grid-community';
import Chart from 'chart.js/auto';
import { CurrencyPipe, CommonModule } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { InvestmentPreferencesService } from '../../services/investment-preferences.service';
import {
  PortfolioSummary,
  TradeHistoryData,
  ColumnDefinition,
  ChartData,
  PerformanceData,
} from '../../models/portfolio-summary.model';
import { PortfolioService } from '../../services/portfolio.service';
ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    AgGridModule,
    CurrencyPipe,
    CommonModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements AfterViewInit, OnInit, OnDestroy {
  isBrowser = false;
  portfolioSummary: PortfolioSummary['portfolioSummary'] | null = null;
  lineChartData: ChartData = { labels: [], datasets: [] };
  pieChartData: ChartData = { labels: [], datasets: [] };
  performanceColumns: ColDef[] = [];
  performanceData: PerformanceData[] = [];
  barChartData: ChartData = { labels: [], datasets: [] };
  clientId: string = '';

  rowData: TradeHistoryData[] = [];
  private tradeSubscription!: Subscription;

  columnDefs: ColDef<TradeHistoryData>[] = [];

  defaultColDef: ColDef = {
    sortable: true,
    filter: true,
    resizable: true,
    width: 150,
  };

  gridOptions: GridOptions<TradeHistoryData> = {
    suppressHorizontalScroll: true,
    rowHeight: 30,
    headerHeight: 50,
  };

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private clientService: AuthService,
    private investmentPreferencesService: InvestmentPreferencesService,
    private router: Router,
    private portfolioService: PortfolioService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    this.clientId = this.clientService.getCurrClient() || '';
    if (
      !this.clientId ||
      this.investmentPreferencesService.getPreferences(this.clientId) == null
    ) {
      this.router.navigate(['']);
      return;
    }
    this.portfolioService.getPortfolioSummary(this.clientId).subscribe(
      (data: PortfolioSummary) => {
        this.portfolioSummary = data.portfolioSummary;
        this.lineChartData = data.lineChartData;
        this.pieChartData = data.pieChartData;
        this.performanceColumns = data.performanceColumns;
        this.performanceData = data.performanceData;
        this.barChartData = data.barChartData;
        this.columnDefs = this.createColumnDefs(data.tradeHistoryColumns);
        this.rowData = data.tradeHistoryData;

        if (isPlatformBrowser(this.platformId)) {
          this.renderCharts();
        }
      },
      (error) => {
        console.error('Error fetching portfolio summary:', error);
      }
    );
  }

  createColumnDefs(columns: ColumnDefinition[]): ColDef<TradeHistoryData>[] {
    return columns.map((col) => {
      const colDef: ColDef<TradeHistoryData> = {
        headerName: col.headerName,
        field: col.field as any,
      };
      if (col.field === 'type') {
        colDef.cellRenderer = (params: ICellRendererParams) => {
          const button = document.createElement('button');
          button.innerText = params.value === 'BUY' ? 'Buy' : 'Sell';
          button.style.backgroundColor =
            params.value === 'BUY' ? 'green' : 'red';
          button.style.color = 'white';
          button.style.border = 'none';
          button.style.padding = '4px 10px';
          button.style.borderRadius = '4px';
          return button;
        };
      }
      return colDef;
    });
  }

  ngAfterViewInit(): void {
    // Charts are now rendered after data is fetched in ngOnInit
  }

  ngOnDestroy(): void {
    if (this.tradeSubscription) {
      this.tradeSubscription.unsubscribe();
    }
  }

  renderCharts(): void {
    this.renderLineChart();
    this.renderPieChart();
    this.renderBarChart();
  }

  renderLineChart(): void {
    const lineCtx = document.getElementById('lineChart') as HTMLCanvasElement;
    if (lineCtx && this.lineChartData && this.lineChartData.datasets.length > 0) {
      new Chart(lineCtx, {
        type: 'line',
        data: this.lineChartData,
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { labels: { color: 'white' } } },
          scales: {
            x: { ticks: { color: 'white' } },
            y: { ticks: { color: 'white' } },
          },
        },
      });
    }
  }

  renderPieChart(): void {
    const pieCtx = document.getElementById('pieChart') as HTMLCanvasElement;
    if (pieCtx && this.pieChartData && this.pieChartData.datasets.length > 0) {
      new Chart(pieCtx, {
        type: 'pie',
        data: this.pieChartData,
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { labels: { color: 'white' } } },
        },
      });
    }
  }

  renderBarChart(): void {
    const barCtx = document.getElementById('barChart') as HTMLCanvasElement;
    if (barCtx && this.barChartData && this.barChartData.datasets.length > 0) {
      new Chart(barCtx, {
        type: 'bar',
        data: this.barChartData,
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { labels: { color: 'white' } } },
          scales: {
            x: { ticks: { color: 'white' } },
            y: { ticks: { color: 'white' } },
          },
        },
      });
    }
  }
}

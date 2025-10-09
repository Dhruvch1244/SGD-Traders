import {
  Component,
  OnInit,
  Inject,
  PLATFORM_ID,
  ViewChild,
  ElementRef,
  OnDestroy,
} from '@angular/core';
import { CommonModule, isPlatformBrowser, CurrencyPipe } from '@angular/common';
import { AgGridModule } from 'ag-grid-angular';
import { ColDef, GridOptions } from 'ag-grid-community';
import Chart from 'chart.js/auto';
import { InstrumentService } from '../services/instrument.service';
import { AuthService } from '../services/auth.service';
import { PortfolioHolding } from '../models/portfolio.model';
import { Instrument } from '../models/instruments.model';
import { combineLatest } from 'rxjs';

interface PortfolioRow {
  instrumentName: string;
  quantity: number;
  categoryId: string;
  marketValue: number;
  costBasis: number;
  totalCost: number;
  unrealizedPl: number;
  unrealizedPlPercent: number;
}

@Component({
  selector: 'app-portfolio-content',
  standalone: true,
  imports: [CommonModule, AgGridModule],
  templateUrl: './portfolio-content.component.html',
  styleUrls: ['./portfolio-content.component.scss'],
})
export class PortfolioContentComponent implements OnInit, OnDestroy {
  rowData: PortfolioRow[] = [];
  isBrowser: boolean;
  gridApi: any;
  columnApi: any;
  chart: any;
  instrumentChart: any;

  @ViewChild('portfolioPieChart') pieChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('instrumentPieChart')
  instrumentPieChartRef!: ElementRef<HTMLCanvasElement>;

  columnDefs: ColDef<PortfolioRow>[] = [
    {
      field: 'instrumentName',
      headerName: 'Instrument',
      flex: 2,
      minWidth: 200,
    },
    { field: 'quantity', headerName: 'Quantity', flex: 1, minWidth: 100 },
    { field: 'categoryId', headerName: 'Category', flex: 1, minWidth: 100 },
    {
      field: 'costBasis',
      headerName: 'Avg. Cost',
      flex: 1,
      minWidth: 120,
      valueFormatter: this.currencyFormatter,
    },
    {
      field: 'totalCost',
      headerName: 'Total Cost',
      flex: 1,
      minWidth: 120,
      valueFormatter: this.currencyFormatter,
    },
    {
      field: 'marketValue',
      headerName: 'Market Value',
      flex: 1,
      minWidth: 120,
      valueFormatter: this.currencyFormatter,
    },
    {
      field: 'unrealizedPl',
      headerName: 'Unrealized P/L $',
      flex: 1,
      minWidth: 150,
      valueFormatter: this.currencyFormatter,
      cellStyle: this.plCellStyle,
    },
    {
      field: 'unrealizedPlPercent',
      headerName: 'Unrealized P/L %',
      flex: 1,
      minWidth: 150,
      valueFormatter: (params) => {
        if (params.value == null) return '';
        return `${params.value.toFixed(2)}%`;
      },
      cellStyle: this.plCellStyle,
    },
  ];

  defaultColDef: ColDef = {
    sortable: true,
    filter: true,
    resizable: true,
  };

  gridOptions: GridOptions<PortfolioRow> = {
    suppressHorizontalScroll: true,
    rowHeight: 40,
    headerHeight: 50,
  };

  constructor(
    @Inject(PLATFORM_ID) platformId: Object,
    private instrumentService: InstrumentService,
    private authService: AuthService
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  onGridReady(params: any): void {
    this.gridApi = params.api;
    this.columnApi = params.columnApi;
    this.gridApi.sizeColumnsToFit();
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      const clientId = this.authService.currentUserValue?.clientId;
      if (clientId) {
        this.loadPortfolioData(clientId);
      }
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
    if (this.instrumentChart) {
      this.instrumentChart.destroy();
    }
  }

  loadPortfolioData(clientId: string): void {
    this.instrumentService
      .getPortfolioByClientId(clientId)
      .subscribe((portfolio: any) => {
        if (portfolio && portfolio.rowData) {
          this.rowData = portfolio.rowData;
          // Use chart data directly from backend response
          this.createCategoryPieChartFromApi(portfolio.categoryAllocationChart);
          this.createInstrumentPieChartFromApi(
            portfolio.instrumentAllocationChart
          );
        }
      });
  }

  // New chart methods to use backend chart data
  private createCategoryPieChartFromApi(chartData: any): void {
    // Instead of using backend chartData, calculate from rowData
    if (!this.pieChartRef) {
      setTimeout(() => this.createCategoryPieChartFromApi(chartData), 50);
      return;
    }
    // Calculate cash value per category from rowData
    const categoryValues: { [key: string]: number } = {};
    if (this.rowData && this.rowData.length > 0) {
      this.rowData.forEach((row: any) => {
        categoryValues[row.categoryId] =
          (categoryValues[row.categoryId] || 0) + (row.marketValue || 0);
      });
    }
    const labels = Object.keys(categoryValues);
    const values = Object.values(categoryValues);
    if (this.chart) {
      this.chart.destroy();
    }
    this.chart = new Chart(this.pieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: [
              '#FF6384',
              '#36A2EB',
              '#FFCE56',
              '#4BC0C0',
              '#9966FF',
              '#FF9F40',
            ],
            borderColor: '#1a1a2e',
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: 'white',
              font: { size: 14 },
            },
          },
        },
      },
    });
  }

  private createInstrumentPieChartFromApi(chartData: any): void {
    if (!this.instrumentPieChartRef) {
      setTimeout(() => this.createInstrumentPieChartFromApi(chartData), 50);
      return;
    }
    if (this.instrumentChart) {
      this.instrumentChart.destroy();
    }
    this.instrumentChart = new Chart(this.instrumentPieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: chartData.labels,
        datasets: chartData.datasets,
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: 'white',
              font: { size: 14 },
            },
          },
        },
      },
    });
  }

  currencyFormatter(params: any): string {
    if (params.value == null) return '';
    const currencyPipe = new CurrencyPipe('en-US');
    return currencyPipe.transform(params.value, 'USD', 'symbol', '1.2-2') ?? '';
  }

  plCellStyle(params: any) {
    if (params.value == null) return null;
    if (params.value > 0) {
      return { color: '#4caf50' }; // Green for profit
    }
    if (params.value < 0) {
      return { color: '#f44336' }; // Red for loss
    }
    return null;
  }

  createCategoryPieChart(data: PortfolioRow[]): void {
    if (!this.pieChartRef) {
      setTimeout(() => this.createCategoryPieChart(data), 50);
      return;
    }

    const categoryValues = data.reduce((acc, row) => {
      acc[row.categoryId] = (acc[row.categoryId] || 0) + row.marketValue;
      return acc;
    }, {} as { [key: string]: number });

    const labels = Object.keys(categoryValues);
    const values = Object.values(categoryValues);

    if (this.chart) {
      this.chart.destroy();
    }

    this.chart = new Chart(this.pieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: [
              '#FF6384',
              '#36A2EB',
              '#FFCE56',
              '#4BC0C0',
              '#9966FF',
              '#FF9F40',
            ],
            borderColor: '#1a1a2e',
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: 'white',
              font: {
                size: 14,
              },
            },
          },
        },
      },
    });
  }

  createInstrumentPieChart(data: PortfolioRow[]): void {
    if (!this.instrumentPieChartRef) {
      setTimeout(() => this.createInstrumentPieChart(data), 50);
      return;
    }

    const instrumentValues = data.reduce((acc, row) => {
      acc[row.instrumentName] =
        (acc[row.instrumentName] || 0) + row.marketValue;
      return acc;
    }, {} as { [key: string]: number });

    const labels = Object.keys(instrumentValues);
    const values = Object.values(instrumentValues);

    if (this.instrumentChart) {
      this.instrumentChart.destroy();
    }

    this.instrumentChart = new Chart(this.instrumentPieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: [
              '#FF6384',
              '#36A2EB',
              '#FFCE56',
              '#4BC0C0',
              '#9966FF',
              '#FF9F40',
              '#E7E9ED',
              '#8A2BE2',
              '#A52A2A',
              '#DEB887',
              '#5F9EA0',
              '#7FFF00',
            ],
            borderColor: '#1a1a2e',
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: 'white',
              font: { size: 14 },
            },
          },
        },
      },
    });
  }
}

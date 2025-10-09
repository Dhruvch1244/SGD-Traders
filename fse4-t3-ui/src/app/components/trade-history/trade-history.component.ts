/* File: src/app/components/trade-history/trade-history.component.ts
  Description: This component now subscribes to the trades$ observable for real-time updates.
*/
import {
  Component,
  OnInit,
  OnDestroy, // Import OnDestroy
  Inject,
  PLATFORM_ID,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AgGridModule } from 'ag-grid-angular';
import { ColDef, GridOptions, ICellRendererParams } from 'ag-grid-community';
import { Trade } from '../../models/trade.model';
import Chart from 'chart.js/auto';
import { InstrumentService } from '../../services/instrument.service';
import { Subscription, combineLatest } from 'rxjs'; // Import Subscription
import { Instrument } from '../../models/instruments.model';
import { AuthService } from '../../services/auth.service';

interface TradeHistoryRow {
  tradeId: string;
  instrumentName: string;
  direction: string;
  quantity: number;
  executionPrice: number;
  cashValue: number;
  timestamp: Date;
}

@Component({
  selector: 'app-trade-history',
  standalone: true,
  imports: [CommonModule, AgGridModule],
  templateUrl: './trade-history.component.html',
  styleUrls: ['./trade-history.component.scss'],
})
export class TradeHistoryComponent implements OnInit, OnDestroy {
  rowData: TradeHistoryRow[] = [];
  isBrowser: boolean;
  gridApi: any;
  columnApi: any;
  private tradeSubscription!: Subscription; // To hold the subscription

  columnDefs: ColDef<TradeHistoryRow>[] = [
    {
      field: 'tradeId',
      headerName: 'Trade ID',
      lockVisible: true,
      width: 100,
    },
    {
      field: 'instrumentName',
      headerName: 'Instrument Name',
      lockVisible: true,
      width: 350,
    },
    {
      field: 'direction',
      headerName: 'Direction',
      lockVisible: true,
      width: 100,
      cellRenderer: (params: ICellRendererParams) => {
        const button = document.createElement('button');
        button.innerText = params.value === 'B' ? 'Buy' : 'Sell';
        button.style.backgroundColor = params.value === 'B' ? 'green' : 'red';
        button.style.color = 'white';
        button.style.border = 'none';
        button.style.padding = '4px 10px';
        button.style.borderRadius = '4px';
        return button;
      },
    },
    {
      field: 'quantity',
      width: 100,
      headerName: 'Quantity',
      lockVisible: true,
    },
    { field: 'executionPrice', headerName: 'Execution Price' },
    { field: 'cashValue', headerName: 'Cash Value', width: 100 },
    {
      field: 'timestamp',
      headerName: 'Timestamp',
      valueFormatter: (params) => {
        if (params.value) {
          const date = new Date(params.value);
          return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
        }
        return '';
      },
    },
  ];

  defaultColDef: ColDef = {
    sortable: true,
    filter: true,
    resizable: true,
    autoHeight: true,
  };

  @ViewChild('pieChart') pieChartRef!: ElementRef<HTMLCanvasElement>;
  chart: any;

  constructor(
    @Inject(PLATFORM_ID) platformId: Object,
    private instrumentService: InstrumentService,
    private authService: AuthService
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  gridOptions: GridOptions<TradeHistoryRow> = {
    domLayout: 'autoHeight' as const,
    suppressHorizontalScroll: true,
    rowHeight: 30,
    headerHeight: 50,
    defaultColDef: {
      resizable: true,
      sortable: true,
      filter: true,
    },
  };

  onGridReady(params: any): void {
    this.gridApi = params.api;
    this.columnApi = params.columnApi;
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      const user = this.authService.currentUserValue;
      const clientId = user?.clientId;
      const client = clientId ? clientId : '';

      this.tradeSubscription = combineLatest([
        this.instrumentService.loadTradesForClient(client),
        this.instrumentService.instruments$,
      ]).subscribe(([trades, instruments]: [Trade[], Instrument[]]) => {
        const instrumentMap = new Map(
          instruments.map((inst: Instrument) => [inst.instrumentId, inst])
        );

        const enrichedData = trades.map((trade: Trade) => {
          const instrument = instrumentMap.get(trade.instrumentId);
          let directionDisplay = trade.direction;
          if (directionDisplay === 'BUY') directionDisplay = 'B';
          if (directionDisplay === 'SELL') directionDisplay = 'S';
          return {
            ...trade,
            direction: directionDisplay,
            instrumentName: instrument?.description ?? trade.instrumentId,
          };
        });

        this.rowData = enrichedData.reverse();
        this.createPieChart(trades);
      });
    }
  }

  ngOnDestroy(): void {
    // Unsubscribe to prevent memory leaks when the component is destroyed
    if (this.tradeSubscription) {
      this.tradeSubscription.unsubscribe();
    }
  }

  createPieChart(data: Trade[]): void {
    if (!this.pieChartRef) {
      // If the chart canvas isn't ready yet, wait a moment and try again.
      setTimeout(() => this.createPieChart(data), 50);
      return;
    }
    // Sum cashValue for BUY and SELL trades
    const buyCash = data
      .filter((t) => t.direction === 'B' || t.direction === 'BUY')
      .reduce((sum, t) => sum + (t.cashValue || 0), 0);
    const sellCash = data
      .filter((t) => t.direction === 'S' || t.direction === 'SELL')
      .reduce((sum, t) => sum + (t.cashValue || 0), 0);
    if (this.chart) {
      this.chart.destroy();
    }
    this.chart = new Chart(this.pieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: ['Buy Cash Volume', 'Sell Cash Volume'],
        datasets: [
          {
            data: [buyCash, sellCash],
            backgroundColor: ['#36A2EB', '#FF6384'],
            borderColor: ['#fff', '#fff'],
            borderWidth: 1,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'top' } },
      },
    });
  }
}

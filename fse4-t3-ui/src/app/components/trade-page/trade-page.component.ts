import { Component, Inject, PLATFORM_ID, OnInit } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular';
import { Instrument } from '../../models/instruments.model';
import {
  ColDef,
  GridOptions,
  GridReadyEvent,
  GridApi,
  CellClickedEvent,
} from 'ag-grid-community';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { InstrumentService } from '../../services/instrument.service';
import { ActivatedRoute } from '@angular/router';
import { BuyTradeComponent } from '../buy-trade/buy-trade.component';
import { SellTradeComponent } from '../sell-trade/sell-trade.component';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-trade-page',
  standalone: true,
  imports: [AgGridModule, CommonModule,FormsModule, BuyTradeComponent, SellTradeComponent],
  templateUrl: './trade-page.component.html',
  styleUrl: './trade-page.component.scss',
})
export class TradePageComponent implements OnInit {
  rowData: any[] = [];
  isBrowser: boolean;
  selected: 'buy' | 'sell' = 'buy';
  private gridApi!: GridApi<Instrument>;
  clientId: string = '';
  searchText: string = '';
  
  constructor(
    @Inject(PLATFORM_ID) platformId: Object,
    private instrumentService: InstrumentService,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.route.queryParams.subscribe((params) => {
      this.selected = params['mode'] === 'sell' ? 'sell' : 'buy';
    });
    const user = this.authService.currentUserValue;
    this.clientId = user?.clientId || '';
  }

  // GridOptions now includes onCellClicked handler
  gridOptions: GridOptions<Instrument> = {
    suppressHorizontalScroll: true,
    domLayout: 'autoHeight' as const,
    rowHeight: 40,
    headerHeight: 50,
    onCellClicked: (event: CellClickedEvent) => this.onCellClicked(event),
    defaultColDef: {
      resizable: true,
      sortable: true,
      filter: true,
    },
  };

  // Column definitions now use simple functions for cell renderers with custom CSS classes
  columnDefs: ColDef[] = [
    {
      width: 270,
      field: 'description',
      headerName: 'Instrument Description',
      lockVisible: true,
      filter: 'agTextColumnFilter',
    },
    {
      field: 'minQuantity',
      headerName: 'Min Qnt',
      lockVisible: true,
      width: 100,
      filter: 'agNumberColumnFilter', // enables number search
    },
    {
      field: 'maxQuantity',
      headerName: 'Max Qnt',
      lockVisible: true,
      width: 150,
      filter: 'agNumberColumnFilter',
    },
    {
      field: 'askPrice',
      headerName: 'Ask Price',
      lockVisible: true,
      width: 120,
      valueFormatter: (p) => (p.value !== undefined && p.value !== null ? `$${p.value}` : ''),
      filter: 'agNumberColumnFilter',
    },
    {
      field: 'bidPrice',
      headerName: 'Bid Price',
      lockVisible: true,
      width: 120,
      valueFormatter: (p) => (p.value !== undefined && p.value !== null ? `$${p.value}` : ''),
      filter: 'agNumberColumnFilter',
    },
    {
      headerName: 'Buy',
      width: 100,
      filter: false,
      sortable: false,
      cellRenderer: function () {
        const button = document.createElement('button');
        button.className = 'btn btn-buy';
        button.innerText = 'Buy';
        return button;
      },
      colId: 'buyAction',
    },
    {
      headerName: 'Sell',
      width: 100,
      filter: false,
      sortable: false,
      cellRenderer: function () {
        const button = document.createElement('button');
        button.className = 'btn btn-sell';
        button.innerText = 'Sell';
        return button;
      },
      colId: 'sellAction',
    },
  ];

    ngOnInit(): void {
    if (this.isBrowser) {
      this.instrumentService.getAllInstruments().subscribe((instruments) => {
        this.rowData = instruments;
      });
    }
  }

  onGridReady(params: GridReadyEvent<Instrument>): void {
    this.gridApi = params.api;
  }

  // Search functionality
onSearchChange(event: any): void {
    const searchValue = event.target.value;
    if (this.gridApi) {
      // Try setGridOption first (newer ag-Grid versions)
      if (this.gridApi.setGridOption) {
        this.gridApi.setGridOption('quickFilterText', searchValue);
      } else {
        // Fallback to setFilterModel for broader search
        this.gridApi.setFilterModel({
          description: {
            type: 'contains',
            filter: searchValue
          }
        });
      }
    }
  }


  // Clear search
  clearSearch(): void {
    this.searchText = '';
    if (this.gridApi) {
      if (this.gridApi.setGridOption) {
        this.gridApi.setGridOption('quickFilterText', '');
      } else {
        this.gridApi.setFilterModel(null);
      }
    }
  }


  // Alternative method for more complex filtering
  onSearchChangeAdvanced(): void {
    if (this.gridApi) {
      this.gridApi.setFilterModel({
        description: {
          type: 'contains',
          filter: this.searchText
        }
      });
    }
  }
  // Central click handler
  onCellClicked(event: CellClickedEvent): void {
    if (event.colDef.colId === 'buyAction') {
      this.handleBuyClick(event.data);
    } else if (event.colDef.colId === 'sellAction') {
      this.handleSellClick(event.data);
    }
  }

  handleBuyClick(data: Instrument) {
    console.log('Buy clicked for:', data);
    this.instrumentService.selectInstrumentForTrade(data, 'buy');
    this.select('buy');
  }

  handleSellClick(data: Instrument) {
    console.log('Sell clicked for:', data);
    this.instrumentService.selectInstrumentForTrade(data, 'sell');
    this.select('sell');
  }

  select(option: 'buy' | 'sell') {
    this.selected = option;
  }
}

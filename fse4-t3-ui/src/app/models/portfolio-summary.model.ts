export interface PortfolioSummary {
  portfolioSummary: {
    totalValue: number;
    totalGainLoss: number;
  };
  lineChartData: ChartData;
  pieChartData: ChartData;
  performanceColumns: ColumnDefinition[];
  performanceData: PerformanceData[];
  barChartData: ChartData;
  tradeHistoryColumns: ColumnDefinition[];
  tradeHistoryData: TradeHistoryData[];
}

export interface ChartData {
  labels: string[];
  datasets: ChartDataset[];
}

export interface ChartDataset {
  label?: string;
  data: number[];
  backgroundColor?: string | string[];
  borderColor?: string;
  fill?: boolean;
}

export interface ColumnDefinition {
  headerName: string;
  field: string;
}

export interface PerformanceData {
  date: string;
  gainLoss: number;
  volume: number;
}

export interface TradeHistoryData {
  date: string;
  asset: string;
  type: string;
  amount: number;
  price: number;
}
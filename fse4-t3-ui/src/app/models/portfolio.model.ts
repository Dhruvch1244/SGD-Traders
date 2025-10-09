export interface PortfolioHolding {
  instrumentId: string;
  quantity: number;
  instrumentName?: string;
}

export interface Portfolio {
  clientId: string;
  holdings: PortfolioHolding[];
}

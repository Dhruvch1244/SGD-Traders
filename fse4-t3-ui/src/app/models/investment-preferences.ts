export interface InvestmentPreferences {
  clientId: string;
  investmentPurpose: 'RETIREMENT' | 'WEALTH_CREATION' | 'EDUCATION' | '';
  riskTolerance: 'CONSERVATIVE' | 'BELOW_AVERAGE' | 'AVERAGE' | 'ABOVE_AVERAGE' | 'AGGRESSIVE' | '';
  incomeCategory:
    | 'RANGE_0_20000'
    | 'RANGE_20001_40000'
    | 'RANGE_40001_60000'
    | 'RANGE_60001_80000'
    | 'RANGE_80001_100000'
    | 'RANGE_100001_150000'
    | 'RANGE_150000_PLUS'
    | '';
  investmentDuration:
    | 'ZERO_TO_FIVE_YEARS'
    | 'FIVE_TO_SEVEN_YEARS'
    | 'SEVEN_TO_TEN_YEARS'
    | 'TEN_TO_FIFTEEN_YEARS'
    | '';
  acceptedTerms: number;
}

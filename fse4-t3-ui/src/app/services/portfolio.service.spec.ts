import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { PortfolioService } from './portfolio.service';
import { PortfolioSummary } from '../models/portfolio-summary.model';

describe('PortfolioService', () => {
  let service: PortfolioService;
  let httpSpy: jasmine.SpyObj<HttpClient>;

  beforeEach(async () => {
    await TestBed.resetTestingModule();
    httpSpy = jasmine.createSpyObj('HttpClient', ['get']);
    await TestBed.configureTestingModule({
      providers: [
        PortfolioService,
        { provide: HttpClient, useValue: httpSpy },
      ],
    }).compileComponents();
    service = TestBed.inject(PortfolioService);
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call getPortfolioSummary and return expected data', (done) => {
    const uuid = 'test-uuid';
    const mockSummary: PortfolioSummary = {} as PortfolioSummary;
    httpSpy.get.and.returnValue(of(mockSummary));

    service.getPortfolioSummary(uuid).subscribe((result) => {
      expect(result).toBe(mockSummary);
      expect(httpSpy.get).toHaveBeenCalledWith(jasmine.stringMatching(`${uuid}/summary`));
      done();
    });
  });
});
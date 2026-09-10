import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CareermatchApiService } from './careermatch-api.service';

describe('CareermatchApiService', () => {
  let service: CareermatchApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CareermatchApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads jobs with the backend page contract', () => {
    service.listJobs(undefined, undefined, 1).subscribe();
    const request = http.expectOne((req) => req.url === 'http://localhost:8080/api/jobs');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('1');
    request.flush({ jobs: [], page: 1, page_size: 20, total_items: 0, total_pages: 0 });
  });

  it('uploads a PDF as multipart field file', () => {
    const file = new File(['pdf'], 'candidate.pdf', { type: 'application/pdf' });
    service.uploadCv(file).subscribe();
    const request = http.expectOne('http://localhost:8080/api/cv/upload');
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBe(true);
    const uploaded = request.request.body.get('file') as File;
    expect(uploaded).toBeInstanceOf(File);
    expect(uploaded.name).toBe('candidate.pdf');
    expect(uploaded.type).toBe('application/pdf');
    request.flush({ cv_id: 'cv-1', filename: 'candidate.pdf', parsed: true });
  });

  it('sends snake_case IDs to match analysis', () => {
    service.analyzeMatch('cv-1', 'job-1').subscribe();
    const request = http.expectOne('http://localhost:8080/api/match/analyze');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ cv_id: 'cv-1', job_id: 'job-1' });
    request.flush({});
  });

  it('uses the persisted profile and match endpoints', () => {
    service.getCandidateProfile('cv-1').subscribe();
    http.expectOne('http://localhost:8080/api/cv/cv-1/profile').flush({});
    service.getMatch('match-1').subscribe();
    http.expectOne('http://localhost:8080/api/match/match-1').flush({});
  });
});

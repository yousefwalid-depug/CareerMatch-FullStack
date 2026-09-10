import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { App } from './app';
import { CareermatchApiService } from './services/careermatch-api.service';

describe('App', () => {
  const api = {
    listJobs: () => of({
      jobs: [{ job_id: 'job-1', title: 'Junior Backend Engineer', company: 'Northstar', location: 'Cairo', source: 'LOCAL' }],
      page: 1,
      page_size: 20,
      total_items: 1,
      total_pages: 1,
    }),
    getJob: () => of(null),
    uploadCv: () => of(null),
    getCandidateProfile: () => of(null),
    analyzeMatch: () => of(null),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: CareermatchApiService, useValue: api }],
    }).compileComponents();
  });

  it('loads and renders real job response fields', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Junior Backend Engineer');
    expect(fixture.nativeElement.textContent).toContain('Northstar');
  });

  it('rejects a non-PDF before upload', () => {
    const fixture = TestBed.createComponent(App);
    const component = fixture.componentInstance;
    const file = new File(['not a pdf'], 'resume.txt', { type: 'text/plain' });
    component.onFileSelected({ target: { files: [file], value: 'resume.txt' } } as unknown as Event);
    expect(component.uploadState()).toBe('error');
    expect(component.message()).toContain('Choose a PDF');
  });

  it('enables upload after selecting a valid PDF', () => {
    const fixture = TestBed.createComponent(App);
    const component = fixture.componentInstance;
    const file = new File(['pdf'], 'resume.pdf', { type: 'application/pdf' });

    component.onFileSelected({ target: { files: [file], value: 'resume.pdf' } } as unknown as Event);

    expect(component.selectedFileName()).toBe('resume.pdf');
    expect(component.canUpload()).toBe(true);
  });
});

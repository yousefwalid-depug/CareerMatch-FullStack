import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import {
  CandidateProfile,
  JobDetails,
  JobSummary,
  MatchResponse,
  ScoreBreakdown,
  SkillMatch,
} from './models/api.models';
import { CareermatchApiService } from './services/careermatch-api.service';

type LoadState = 'idle' | 'loading' | 'success' | 'empty' | 'error';

@Component({
  selector: 'app-root',
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly api = inject(CareermatchApiService);
  private readonly maxPdfBytes = 5 * 1024 * 1024;

  readonly jobs = signal<JobSummary[]>([]);
  readonly jobsState = signal<LoadState>('loading');
  readonly selectedJob = signal<JobDetails | null>(null);
  readonly jobDetailState = signal<LoadState>('idle');
  readonly selectedFileName = signal('');
  readonly selectedFileSize = signal('');
  readonly uploadState = signal<LoadState>('idle');
  readonly uploadedCvId = signal<string | null>(null);
  readonly profile = signal<CandidateProfile | null>(null);
  readonly analysisState = signal<LoadState>('idle');
  readonly match = signal<MatchResponse | null>(null);
  readonly message = signal('');

  private readonly selectedFile = signal<File | null>(null);

  readonly canUpload = computed(() => !!this.selectedFile() && this.uploadState() !== 'loading');
  readonly canAnalyze = computed(
    () => !!this.selectedJob() && !!this.uploadedCvId() && this.analysisState() !== 'loading',
  );

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.jobsState.set('loading');
    this.message.set('');
    this.api.listJobs().subscribe({
      next: (response) => {
        this.jobs.set(response.jobs ?? []);
        this.jobsState.set(response.jobs?.length ? 'success' : 'empty');
      },
      error: (error) => {
        this.jobsState.set('error');
        this.message.set(this.errorMessage(error, 'Jobs are unavailable right now.'));
      },
    });
  }

  selectJob(job: JobSummary): void {
    if (this.selectedJob()?.job_id === job.job_id || this.jobDetailState() === 'loading') return;
    this.jobDetailState.set('loading');
    this.selectedJob.set(null);
    this.message.set('');
    this.match.set(null);
    this.analysisState.set('idle');
    this.api.getJob(job.job_id).subscribe({
      next: (detail) => {
        this.selectedJob.set(detail);
        this.jobDetailState.set('success');
      },
      error: (error) => {
        this.jobDetailState.set('error');
        this.message.set(this.errorMessage(error, 'This job could not be loaded.'));
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.resetCvSelection();
    if (!file) return;

    const hasPdfExtension = file.name.toLowerCase().endsWith('.pdf');
    const hasPdfType = !file.type || file.type === 'application/pdf';
    if (!hasPdfExtension || !hasPdfType) {
      this.uploadState.set('error');
      this.message.set('Choose a PDF file. Other file types are not supported.');
      input.value = '';
      return;
    }
    if (file.size > this.maxPdfBytes) {
      this.uploadState.set('error');
      this.message.set('The PDF is larger than 5 MB. Choose a smaller file.');
      input.value = '';
      return;
    }

    this.selectedFile.set(file);
    this.selectedFileName.set(file.name);
    this.selectedFileSize.set(this.formatBytes(file.size));
    this.uploadState.set('idle');
    this.message.set('');
  }

  uploadCv(): void {
    const file = this.selectedFile();
    if (!file) return;
    this.uploadState.set('loading');
    this.message.set('');
    this.api.uploadCv(file).subscribe({
      next: (response) => {
        this.uploadedCvId.set(response.cv_id);
        this.uploadState.set('success');
        this.loadProfile(response.cv_id);
      },
      error: (error) => {
        this.uploadState.set('error');
        this.message.set(this.errorMessage(error, 'The CV upload failed. Please try again.'));
      },
    });
  }

  analyzeMatch(): void {
    const job = this.selectedJob();
    const cvId = this.uploadedCvId();
    if (!job || !cvId) return;

    this.analysisState.set('loading');
    this.match.set(null);
    this.message.set('');
    this.api.analyzeMatch(cvId, job.job_id).subscribe({
      next: (response) => {
        this.match.set(response);
        this.analysisState.set('success');
        queueMicrotask(() => document.getElementById('match-result')?.focus());
      },
      error: (error) => {
        this.analysisState.set('error');
        this.message.set(this.errorMessage(error, 'Match analysis failed. Please try again.'));
      },
    });
  }

  scoreRows(score: ScoreBreakdown): { label: string; value: number; weight: string }[] {
    return [
      { label: 'Required skills', value: score.required_skills, weight: '40%' },
      { label: 'Experience', value: score.experience, weight: '20%' },
      { label: 'Projects', value: score.projects, weight: '15%' },
      { label: 'Education', value: score.education, weight: '10%' },
      { label: 'Preferred skills', value: score.preferred_skills, weight: '15%' },
    ];
  }

  skillGroups(result: MatchResponse): { title: string; items: SkillMatch[]; tone: string }[] {
    return [
      { title: 'Matched required skills', items: result.matched_required_skills, tone: 'positive' },
      { title: 'Partial matches', items: result.partial_matches, tone: 'partial' },
      { title: 'Missing required skills', items: result.missing_required_skills, tone: 'negative' },
      { title: 'Matched preferred skills', items: result.matched_preferred_skills, tone: 'positive' },
      { title: 'Missing preferred skills', items: result.missing_preferred_skills, tone: 'neutral' },
    ];
  }

  formatLabel(value: string): string {
    return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  trackByJobId(_index: number, job: JobSummary): string {
    return job.job_id;
  }

  private loadProfile(cvId: string): void {
    this.api.getCandidateProfile(cvId).subscribe({
      next: (profile) => this.profile.set(profile),
      error: () => this.profile.set(null),
    });
  }

  private resetCvSelection(): void {
    this.selectedFile.set(null);
    this.selectedFileName.set('');
    this.selectedFileSize.set('');
    this.uploadedCvId.set(null);
    this.profile.set(null);
    this.match.set(null);
    this.analysisState.set('idle');
    this.uploadState.set('idle');
    this.message.set('');
  }

  private formatBytes(bytes: number): string {
    return `${(bytes / 1024 / 1024).toFixed(2)} MB`;
  }

  private errorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const serverMessage = error.error?.message;
      if (typeof serverMessage === 'string' && serverMessage.trim()) return serverMessage;
      if (error.status === 0) return 'CareerMatch cannot reach the backend. Check that it is running and try again.';
    }
    return fallback;
  }
}

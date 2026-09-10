import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CandidateProfile, CvUploadResponse, JobDetails, JobListResponse, MatchResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class CareermatchApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = (window.CAREERMATCH_CONFIG?.apiBaseUrl || environment.apiBaseUrl).replace(/\/$/, '');

  listJobs(query?: string, location?: string, page = 1): Observable<JobListResponse> {
    let params = new HttpParams().set('page', page);
    if (query?.trim()) params = params.set('query', query.trim());
    if (location?.trim()) params = params.set('location', location.trim());
    return this.http.get<JobListResponse>(`${this.baseUrl}/api/jobs`, { params });
  }
  getJob(jobId: string): Observable<JobDetails> { return this.http.get<JobDetails>(`${this.baseUrl}/api/jobs/${jobId}`); }
  uploadCv(file: File): Observable<CvUploadResponse> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<CvUploadResponse>(`${this.baseUrl}/api/cv/upload`, body);
  }
  getCandidateProfile(cvId: string): Observable<CandidateProfile> { return this.http.get<CandidateProfile>(`${this.baseUrl}/api/cv/${cvId}/profile`); }
  analyzeMatch(cvId: string, jobId: string): Observable<MatchResponse> {
    return this.http.post<MatchResponse>(`${this.baseUrl}/api/match/analyze`, { cv_id: cvId, job_id: jobId });
  }
  getMatch(matchId: string): Observable<MatchResponse> { return this.http.get<MatchResponse>(`${this.baseUrl}/api/match/${matchId}`); }
}

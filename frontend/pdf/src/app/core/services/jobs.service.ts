import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { environment } from '../../../environments';

export interface PageTransformRequest {
  pageIndex: number;
  appliedAngle: number;
}

export interface JobSummary {
  id: string;
  filename: string;
  status: string;
  pageCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PageAngleDto {
  pageIndex: number;
  detectedAngle: number;
  appliedAngle: number | null;
}


@Injectable({ providedIn: 'root' })
export class JobsService {
  private readonly baseUrl = environment.apiBase;

  constructor(private api: ApiService) {}

  createJob(file: File): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    return this.api.post<any>('/jobs', fd);
  }


  saveEdits(jobId: string, edits: PageAngleDto[]): Observable<void> {
  return this.api.post<void>(`/pages/${jobId}/save`, edits, 'text');
}



  buildDownloadUrl(jobId: string) {
    return `${this.baseUrl}/download/${jobId}`;
  }

  downloadCorrected(jobId: string) {
    return this.api.getBlob(`/download/${jobId}`);
  }

  getJobs(): Observable<JobSummary[]> {
    return this.api.get<JobSummary[]>('/jobs');
  }

  getPages(jobId: string): Observable<PageAngleDto[]> {
    return this.api.get<PageAngleDto[]>(`/pages/${jobId}`);
  }

  deleteJob(jobId: string) {
    return this.api.delete(`/jobs/${jobId}`);
  }
  downloadOriginal(jobId: string) {

  return this.api.getBlob(`/download/${jobId}?original=true`);
}

}

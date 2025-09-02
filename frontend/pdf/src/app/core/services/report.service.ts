// src/app/core/services/report.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments';
import { Observable } from 'rxjs';

export interface ReportDto {
  totalPages: number;
  rotations: { pageIndex: number; original: number; applied: number | null }[];
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBase}`;

  // ✅ Corrected endpoint
  getReport(jobId: string): Observable<ReportDto> {
    return this.http.get<ReportDto>(`${this.baseUrl}/report/${jobId}`);
  }

  download(jobId: string) {
    this.http.get(`${this.baseUrl}/download/${jobId}`, {
      responseType: 'blob'
    }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `corrected-${jobId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

}

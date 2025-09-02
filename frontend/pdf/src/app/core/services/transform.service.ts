import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments';

export interface EditInstruction { 
  pageIndex: number;
  appliedAngle: number | null;
}

@Injectable({ providedIn: 'root' })
export class TransformService {
  constructor(private http: HttpClient) {}

  private apiBase = environment.apiBase;

  // Store updated page thumbnails (data URLs)
  private updatedPages = new Map<number, string>();

  // Save edited rotations to backend
  saveEdits(jobId: string, edits: { pageIndex: number, appliedAngle: number }[]): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/transform/${jobId}`, {
      jobId,
      rotations: edits
    });
  }




  // Get the updated page URL (frontend first, fallback to API if not rotated yet)
  getUpdatedPageUrl(pageIndex: number): string | null {
    return this.updatedPages.get(pageIndex) ?? null;
  }

  // Set/update rotated page thumbnail
  setUpdatedPageUrl(pageIndex: number, url: string) {
    this.updatedPages.set(pageIndex, url);
  }

  // Optional: clear cached rotated pages (e.g., when loading a new PDF)
  clearUpdatedPages() {
    this.updatedPages.clear();
  }

  



    /** Download original PDF from backend */
  downloadOriginal(jobId: string): Observable<Blob> {
    return this.http.get(`/api/download/${jobId}?original=true`, { responseType: 'blob' });
  }

  /** Download corrected PDF from backend */
  downloadCorrected(jobId: string): Observable<Blob> {
    return this.http.get(`/api/download/${jobId}?original=false`, { responseType: 'blob' });
  }
}

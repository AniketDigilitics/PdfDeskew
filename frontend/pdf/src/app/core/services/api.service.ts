import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments';
@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  get base() { return environment.apiBase; }

  // JSON GET
  get<T>(path: string, params?: HttpParams) {
    return this.http.get<T>(`${this.base}${path}`, { params, observe: 'body' as const });
  }

  // JSON POST
  post<T = void>(path: string, body: any, responseType: 'json' | 'text' = 'json') {
    return this.http.post<T>(
      `${this.base}${path}`,
      body,
      { observe: 'body' as const, responseType: responseType as 'json' } // TypeScript quirk
    );
  }

  // Blob GET (download)
  getBlob(path: string) {
    return this.http.get(`${this.base}${path}`, { responseType: 'blob' });
  }

  // DELETE
  delete<T>(path: string) {
    return this.http.delete<T>(`${this.base}${path}`, { observe: 'body' as const });
  }
}


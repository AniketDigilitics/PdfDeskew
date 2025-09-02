import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { PageAngleDto } from '../model/job.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PagesService {
  constructor(private api: ApiService) {}

  getPages(jobId: string): Observable<PageAngleDto[]> {
    return this.api.get<PageAngleDto[]>(`/pages/${jobId}`);
  }
}

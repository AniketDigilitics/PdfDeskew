import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ReportService } from '../core/services/report.service';
import { SkeletonComponent } from '../component/skeleton/skeleton.component';

@Component({
  standalone: true,
  selector: 'app-report-page',
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './report-page.component.html',
  styleUrl: './report-page.component.css'
})
export class ReportPageComponent {
  jobId = signal<string>('');
  loading = signal(true);
  data = signal<{ totalPages: number; rotations: { pageIndex:number; original:number; applied:number|null }[] } | null>(null);

    constructor(
    private route: ActivatedRoute,
    private report: ReportService
    ) {
    this.route.paramMap.subscribe(params => {
        const id = params.get('jobId')!;
        this.jobId.set(id);
        this.loading.set(true);
        this.report.getReport(id).subscribe({
        next: d => this.data.set(d),
        complete: () => this.loading.set(false),
        error: () => this.loading.set(false)
        });
    });
    }

}

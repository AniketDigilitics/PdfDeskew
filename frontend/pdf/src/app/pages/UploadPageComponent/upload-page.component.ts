import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { JobsService } from '../../core/services/jobs.service';
import { ToastService } from '../../core/services/toast.service';
import { UploadItem } from '../../core/model/upload.model';

@Component({
  standalone: true,
  selector: 'app-upload-page',
  imports: [CommonModule],
  templateUrl: './upload-page.component.html',
  styleUrls: ['./upload-page.component.css']
})
export class UploadPageComponent implements OnInit {
  files = signal<UploadItem[]>([]);

  constructor(
    public jobs: JobsService,
    private toast: ToastService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadJobs();
  }

  loadJobs() {
    this.jobs.getJobs().subscribe({
      next: jobs => {
        this.files.set(
          jobs
            .map(j => ({
              jobId: j.id,
              filename: j.filename,
              status: j.status.toLowerCase() as any,
              pageCount: j.pageCount,
              createdAt: j.createdAt,
              updatedAt: j.updatedAt
            }))
            .sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? '')) // newest on top
        );
      },
      error: err => console.error(err)
    });
  }

  editJob(jobId: string) {
    this.router.navigate(['/edit', jobId]);
  }

  viewReport(jobId: string) {
    this.router.navigate(['/report', jobId]);
  }

  deleteJob(jobId: string) {
    if (!confirm('Are you sure you want to delete this job?')) return;
    this.jobs.deleteJob(jobId).subscribe({
      next: () => {
        this.files.update(arr => arr.filter(f => f.jobId !== jobId));
        this.toast.success('Deleted successfully');
      },
      error: () => this.toast.error('Failed to delete')
    });
  }

  onFileSelected(e: Event) {
    const input = e.target as HTMLInputElement;
    if (!input.files?.length) return;

    // enqueue files on top
    for (let i = 0; i < input.files.length; i++) {
      const file = input.files.item(i);
      if (!file) continue;
      const now = new Date().toISOString();
      const item: UploadItem = {
        jobId: '', 
        file,
        filename: file.name,
        status: 'queued',
        pageCount: 0,
        createdAt: now,
        updatedAt: now
      };
      this.files.update(arr => [item, ...arr]);
      this.uploadFile(item);
    }
    input.value = '';
  }

  onDrop(ev: DragEvent) {
    ev.preventDefault();
    if (!ev.dataTransfer?.files?.length) return;
    this.onFileSelected({ target: { files: ev.dataTransfer.files } } as any);
  }

  onDragOver(ev: DragEvent) {
    ev.preventDefault();
  }

  uploadFile(item: UploadItem) {
    item.status = 'analyzing';
    if (!item.file) return;

    this.jobs.createJob(item.file).subscribe({
      next: res => {
        item.status = 'ready';
        item.jobId = res.jobId;
        item.pageCount = res.pageCount ?? 0;
        item.updatedAt = new Date().toISOString();
      },
      error: () => {
        item.status = 'failed';
        this.toast.error('Upload failed');
      }
    });
  }
}

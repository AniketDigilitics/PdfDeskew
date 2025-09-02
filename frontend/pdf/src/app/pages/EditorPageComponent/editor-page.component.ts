import { Component, OnInit, signal,effect,computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { JobsService } from '../../core/services/jobs.service';
import { PagesService } from '../../core/services/pages.service';
import { TransformService } from '../../core/services/transform.service';
import { ToastService } from '../../core/services/toast.service';
import { PageInfo } from '../../core/model/page-info.model';
import { ThumbnailGridComponent } from '../../component/thumbnail-grid/thumbnail-grid.component';
import { RotationControlsComponent } from '../../component/rotation-controls/rotation-controls.component';
import { SkeletonComponent } from '../../component/skeleton/skeleton.component'; // <-- import here
import { HttpErrorResponse } from '@angular/common/http';
import { PageAngleDto } from '../../core/services/jobs.service';
import { ApiService } from '../../core/services/api.service';


@Component({
  standalone: true,
  selector: 'app-editor-page',
  imports: [CommonModule, ThumbnailGridComponent, RotationControlsComponent, SkeletonComponent],
  templateUrl: './editor-page.component.html',
  styleUrls: ['./editor-page.component.css']
})
export class EditorPageComponent implements OnInit {
    
  jobId = signal<string | null>(null);
  pageInfos = signal<PageInfo[]>([]);
  loadingPages = signal(false);
  generatingThumbs = signal(false);
  selectedPageIndex = signal<number | null>(null);
  downloadUrl = signal<string | null>(null);

  selectedPage = computed(() => {
    const idx = this.selectedPageIndex();
    if (idx === null) return null;
    const page = this.pageInfos()[idx];
    const clone = { ...page }; 
    return clone;
  });


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private jobs: JobsService,
    private pagesSvc: PagesService,
    private toast: ToastService,
    private transformSvc: TransformService,
    private api: ApiService
  ) {
    effect(() => {
      const id = this.jobId();
      if (id) {
        this.downloadUrl.set(this.jobs.buildDownloadUrl(id));
        
      }
    }, { allowSignalWrites: true });
  }
  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('jobId');
    if (id) {
      this.jobId.set(id);
      this.fetchPages(id);   
    }
  }

  fetchPages(jobId: string) {
    this.loadingPages.set(true);
    this.pagesSvc.getPages(jobId).subscribe({
      next: pages => {
        const merged = pages.map(p => ({
          jobId,
          pageIndex: p.pageIndex,
          detectedAngle: p.detectedAngle,
          appliedAngle: p.appliedAngle ?? p.detectedAngle,

          thumbnailUrl: null
        }));

        this.pageInfos.set(merged);
        if (merged.length) this.selectedPageIndex.set(0); 

        const id = this.jobId(); 
        if (!id) return;
        const totalPages = merged.length;
        this.generateThumbnails();
      },
      error: () => this.toast.warn('Failed to load pages'),
      complete: () => this.loadingPages.set(false)
    });
  }


  async generateThumbnails() {
      const id = this.jobId();
      if (!id) return;

      this.generatingThumbs.set(true);

      try {
        const infos = [...this.pageInfos()];

        for (let i = 0; i < infos.length; i++) {
          const [originalBlob, correctedBlob] = await Promise.all([
            this.api.getBlob(`/pdf/${id}/original/${i}`).toPromise(),
            this.api.getBlob(`/pdf/${id}/corrected/${i}`).toPromise()
          ]);

          if (!originalBlob || !correctedBlob) continue;

          infos[i] = {
            ...infos[i],
            originalThumbnailUrl: URL.createObjectURL(originalBlob as Blob),
            correctedThumbnailUrl: URL.createObjectURL(correctedBlob as Blob),
            thumbnailUrl: URL.createObjectURL(correctedBlob as Blob) // use corrected as main
          };
        }

        this.pageInfos.set(infos);
      } catch (e) {
        console.error("Thumbnail generation failed", e);
        this.toast.warn("Thumbnails unavailable; showing list view.");
      } finally {
        this.generatingThumbs.set(false);
      }
    }

  onSelectPage(idx: number) {
        console.log('EditorPageComponent: onSelectPage called with', idx);
        this.selectedPageIndex.set(idx);

        const page = this.pageInfos()[idx];
        if (page) {
            this.pageInfos.update(arr => {
            const cloneArr = arr.slice();
            cloneArr[idx] = { ...cloneArr[idx] };
            return cloneArr;
            });
        }
    }

  onRotate(delta: number) {
    const idx = this.selectedPageIndex();
    if (idx === null) return;

    // Save reference to the service
    const transformSvc = this.transformSvc;

    this.pageInfos.update(arr => {
        const clone = arr.slice();
        const page = { ...clone[idx], appliedAngle: (clone[idx].appliedAngle ?? 0) + delta };
        clone[idx] = page;

        // Update the rotated thumbnail immediately
        const canvas = document.createElement('canvas');
        const img = new Image();
        img.src = page.thumbnailUrl!;
        img.onload = () => {
        const ctx = canvas.getContext('2d')!;
        canvas.width = img.width;
        canvas.height = img.height;
        ctx.translate(img.width/2, img.height/2);
        ctx.rotate((page.appliedAngle ?? 0) * Math.PI / 180);
        ctx.drawImage(img, -img.width/2, -img.height/2);
        const rotatedUrl = canvas.toDataURL('image/png');

        // stored reference to service 
        transformSvc.setUpdatedPageUrl(idx, rotatedUrl);
        };

        return clone;
    });
    }

  onSetAngle(angle: number) {
    const idx = this.selectedPageIndex();
    if (idx === null) return;
    this.pageInfos.update(arr => {
      const clone = arr.slice();
      clone[idx] = { ...clone[idx], appliedAngle: angle };
      return clone;
    });
  }


  onReset() {
    const idx = this.selectedPageIndex();
    if (idx === null) return;

    this.pageInfos.update(arr => {
      const clone = arr.slice();
      const resetAngle = clone[idx].detectedAngle; 
      clone[idx] = { ...clone[idx], appliedAngle: resetAngle };
      return clone;
    });

    // update in service
    this.transformSvc.setUpdatedPageUrl(idx, '');

    this.toast.success("Page reset to detected angle");
  }



  saveEdits() {
      if (!this.jobId()) return;

      const edits: PageAngleDto[] = this.pageInfos().map(p => ({
        pageIndex: p.pageIndex,
        detectedAngle: p.detectedAngle,
        //  appliedAngle: p.appliedAngle ?? p.detectedAngle
        appliedAngle: p.appliedAngle !== null ? -p.appliedAngle : null
      }));

      console.log('Saving edits payload:', edits);

      this.jobs.saveEdits(this.jobId()!, edits).subscribe({
        next: () => {
          console.log('Edits saved successfully');
          this.toast.success("PDF saved successfully ");
        },
        error: (err) => {
          console.error('Failed to save edits:', err);
          if (err instanceof HttpErrorResponse) {
            console.error('Status:', err.status);
            console.error('StatusText:', err.statusText);
            console.error('URL:', err.url);
            console.error('Headers:', err.headers);
            console.error('Error body:', err.error);
          }
          this.toast.error("Failed to save PDF ");
        }
      });
}




  navReport() {
    const id = this.jobId();
    if (id) this.router.navigate(['/report', id]);
  }
}
import { Component, Inject, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TransformService } from '../../core/services/transform.service';
import { PageInfo } from '../../core/model/page-info.model';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-page-compare-dialog',
  standalone: true,
   imports: [CommonModule, MatIconModule,MatButtonModule], 
  templateUrl: './page-compare-dialog.component.html',
  styleUrls: ['./page-compare-dialog.component.css']
})
export class PageCompareDialogComponent {

  private transformService = inject(TransformService);
  page: PageInfo;

  constructor(
    private dialogRef: MatDialogRef<PageCompareDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { page: PageInfo }
  ) {
    this.page = data.page; 
  }

  get rotatedUrl(): string | null {
    const updated = this.transformService.getUpdatedPageUrl(this.page.pageIndex);
    if (updated) {
      return updated; 
    }

    if (this.page.correctedThumbnailUrl) {
      return this.page.correctedThumbnailUrl;
    }
    return this.page.thumbnailUrl ?? null;
  }


  get originalUrl(): string | null {
    return this.page.originalThumbnailUrl ?? this.page.thumbnailUrl ?? null;
  }

  get correctedUrl(): string | null {
  return this.page.correctedThumbnailUrl ?? this.page.thumbnailUrl ?? null;
}

  close() {
    this.dialogRef.close();
  }
}

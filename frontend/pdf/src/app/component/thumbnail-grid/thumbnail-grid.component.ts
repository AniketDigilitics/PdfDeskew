import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { PageInfo } from '../../core/model/page-info.model';
import { PageCompareDialogComponent } from '../page-compare-dialog/page-compare-dialog.component';
import { SkeletonComponent } from '../skeleton/skeleton.component';

@Component({
  selector: 'app-thumbnail-grid',
  standalone: true,
  imports: [CommonModule,SkeletonComponent],
  templateUrl: './thumbnail-grid.component.html',
  styleUrl: './thumbnail-grid.component.css'
})
export class ThumbnailGridComponent {
  @Input() pages: PageInfo[] = [];
  @Input() loading = false;
  @Output() selectPage = new EventEmitter<number>();

  placeholders = Array(6);

  private dialog = inject(MatDialog);

  onThumbClick(page: PageInfo) {
    console.log('Thumbnail clicked, emitting selectPage:', page.pageIndex);
    this.selectPage.emit(page.pageIndex);
  }

  openDialog(page: PageInfo) {
    this.dialog.open(PageCompareDialogComponent, {
      width: '90vw',
      maxWidth: '1000px',
      autoFocus: false,
      panelClass: 'custom-dialog-container',
      data: { page }   
    });
  }


  trackByIndex(index: number, item: PageInfo) {
    return item.pageIndex;
  }
}

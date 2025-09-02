import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageInfo } from '../../core/model/page-info.model';
@Component({
  standalone: true,
  selector: 'app-rotation-controls',
  imports: [CommonModule],
  templateUrl: './rotation-controls.component.html',
  styleUrl: './rotation-controls.component.css'
})
export class RotationControlsComponent {
  @Input() selectedIndex: number | null = null;
  @Input() page: PageInfo | null = null;
  @Output() rotateBy = new EventEmitter<number>();
  @Output() setAngle = new EventEmitter<number>();
  @Output() resetAngle = new EventEmitter<void>();

  setFromInput(e: Event) {
    const v = Number((e.target as HTMLInputElement).value);
    if (!Number.isFinite(v)) return;
    this.setAngle.emit(v);
  }
  
}

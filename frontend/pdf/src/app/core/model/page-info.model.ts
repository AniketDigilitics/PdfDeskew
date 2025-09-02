export interface PageInfo {
  jobId: string;
  pageIndex: number;
  detectedAngle: number;
  appliedAngle: number | null;
  thumbnailUrl: string | null;      
  originalThumbnailUrl?: string | null; 
  correctedThumbnailUrl?: string; 
  originalUrl?: string;
}

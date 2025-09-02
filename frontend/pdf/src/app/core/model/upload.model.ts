export interface UploadItem {
  jobId: string;
  filename: string;
  file?: File;
  status: 'queued' | 'analyzing' | 'ready' | 'failed';
  pageCount: number;
  createdAt: string;  
  updatedAt: string; 
}

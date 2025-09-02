export interface CreateJobResponse {
  jobId: string;
  detectedAngles: number[];   
}

export interface PageAngleDto {
  pageIndex: number;
  detectedAngle: number;
  appliedAngle: number | null;
}

export interface ReportDto {
  jobId: string;
  totalPages: number;
  rotations: { pageIndex: number; original: number; applied: number | null }[];
}

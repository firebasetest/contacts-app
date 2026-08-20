export type EntityType = 'GENERAL' | 'COMPANY';

export interface ImportErrorLogDTO {
  id: string;
  jobId: string;
  rowNumber: number;
  recordIdentifier: string;
  errorMessage: string;
  timestamp: string;
}

export interface ImportJobDTO {
  jobId: string;
  businessUnitId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  totalRecords: number;
  processedRecords: number;
  errorMessage?: string;
  filePath?: string;
  entityType: EntityType;
  createdAt?: string;
}

export interface ImportSummaryReportDTO {
  jobId: string;
  businessUnitId: string;
  entityType: EntityType;
  status: string;
  totalRecords: number;
  processedRecords: number;
  insertedRecords: number;
  updatedRecords: number;
  failedRecords: number;
  globalErrorMessage?: string;
  errorLogs: ImportErrorLogDTO[];
}
export type BusinessUnitStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING';

export interface BusinessUnitDto {
  businessUnitId: string;       // UUID - Primary Identifier
  name: string;                 // e.g., "European Operations"
  slug: string;                 // URL-friendly identifier (e.g., "eu-operations")
  description: string;          // Full description of the BU's scope
  status: BusinessUnitStatus;   // ACTIVE, INACTIVE, PENDING
  createdAt: string;            // ISO Date String
  updatedAt: string;            // ISO Date String
}

export interface BusinessUnitSearchResponse {
  content: BusinessUnitDto[];
  number: number;
  size: number;
  totalPages: number;
  totalElements: number;
}

// Input DTO used when creating/updating a BU
export interface BusinessUnitInputDto {
  name: string;
  description: string;
  // Status is usually set by the service layer, but we allow override here.
  initialStatus: BusinessUnitStatus;
}

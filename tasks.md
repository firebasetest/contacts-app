# Remaining tasks

## Current status
- [x] Added a backend batch contact creation endpoint at `/api/v1/contacts/batch`.
- [x] Added service support for bulk contact persistence.
- [x] Wired the contact detail form to call the real create/update backend endpoints.
- [x] Verified the backend controller test for batch creation passes with Maven.
- [x] Verified the frontend test command runs from the project directory.

## Priority tasks
- [ ] Add end-to-end tests for the batch endpoint and contact create/update flow.
- [ ] Add a dedicated admin UI for managing attribute definitions and validating dynamic fields.
- [ ] Implement full company hierarchy support (parent/child company relationships and UI views).
- [ ] Add import/export workflows for CSV/Excel/PDF with batch processing and error reporting.
- [ ] Implement role-based access control and delegated-admin management beyond the current UI placeholders.
- [ ] Add stronger temporal-history validation and coverage for AS-OF/AS-AT queries.
- [ ] Connect the application to real authentication/OIDC and tenant identity propagation.
- [ ] Add database migration and seeding scripts for realistic business-unit and sample data.
- [ ] Document deployment and environment configuration for local and cloud runs.

## Implementation notes
- Keep tenant-scoped context flowing from request headers/JWT into the database layer.
- Use the existing `attribute_definitions` model as the source of truth for dynamic metadata fields.
- Preserve audit/history entries for every create, update, and delete action.
- Prefer small, testable backend and frontend changes over large rewrites.

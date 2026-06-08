# API Documentation

## Contacts Management API v1.0.0

Complete API documentation for the Contacts Management Application.

### Base URL

```
https://api.contactsmanager.com
```

### Authentication

All endpoints (except `/auth/register` and `/auth/login`) require JWT Bearer token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Error Response (409 Conflict):**

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists: user@example.com",
  "timestamp": "2026-06-05T10:30:00",
  "path": "/api/auth/register"
}
```

---

### User Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

---

## Contact Endpoints

### Get All Contacts

```http
GET /api/contacts
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phone": "1234567890",
    "notes": "Best friend"
  },
  {
    "id": 2,
    "name": "Bob Smith",
    "email": "bob@example.com",
    "phone": "9876543210",
    "notes": "Colleague"
  }
]
```

---

### Search and Filter Contacts

```http
POST /api/contacts/search
Authorization: Bearer <token>
Content-Type: application/json

{
  "keyword": "jane",
  "sortBy": "name",
  "sortOrder": "asc",
  "page": 0,
  "size": 10
}
```

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Jane Doe",
      "email": "jane@example.com",
      "phone": "1234567890",
      "notes": "Best friend"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### Get Contact by ID

```http
GET /api/contacts/{id}
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "1234567890",
  "notes": "Best friend"
}
```

**Error Response (404 Not Found):**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Contact not found",
  "timestamp": "2026-06-05T10:30:00",
  "path": "/api/contacts/999"
}
```

---

### Create Contact

```http
POST /api/contacts
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "1234567890",
  "notes": "Best friend"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "1234567890",
  "notes": "Best friend"
}
```

**Validation Error (400 Bad Request):**

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "errors": {
    "email": "Email should be valid",
    "phone": "Phone number must be valid (10-13 digits)"
  },
  "timestamp": "2026-06-05T10:30:00",
  "path": "/api/contacts"
}
```

---

### Update Contact

```http
PUT /api/contacts/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Jane Smith",
  "email": "jane.smith@example.com",
  "phone": "1111111111",
  "notes": "Updated notes"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "Jane Smith",
  "email": "jane.smith@example.com",
  "phone": "1111111111",
  "notes": "Updated notes"
}
```

---

### Delete Contact

```http
DELETE /api/contacts/{id}
Authorization: Bearer <token>
```

**Response (204 No Content):**

---

## Export Endpoints

### Export to CSV

```http
POST /api/contacts/export/csv
Authorization: Bearer <token>
Content-Type: application/json

{
  "keyword": "",
  "sortBy": "name",
  "sortOrder": "asc",
  "page": 0,
  "size": 2147483647
}
```

**Response (200 OK):**

Binary CSV file attachment

---

### Export to PDF

```http
POST /api/contacts/export/pdf
Authorization: Bearer <token>
Content-Type: application/json

{
  "keyword": "",
  "sortBy": "name",
  "sortOrder": "asc",
  "page": 0,
  "size": 2147483647
}
```

**Response (200 OK):**

Binary PDF file attachment

---

## Audit Log Endpoints

### Get User Audit Logs

```http
GET /api/audit-logs?page=0&size=20
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "action": "CREATE",
      "entityType": "Contact",
      "entityId": 5,
      "changes": "[...]",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "status": "SUCCESS",
      "timestamp": "2026-06-05T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### Get Entity Audit Logs

```http
GET /api/audit-logs/entity/Contact/5
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "userId": 1,
    "action": "CREATE",
    "entityType": "Contact",
    "entityId": 5,
    "changes": "[...]",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "status": "SUCCESS",
    "timestamp": "2026-06-05T10:30:00"
  },
  {
    "id": 2,
    "userId": 1,
    "action": "UPDATE",
    "entityType": "Contact",
    "entityId": 5,
    "changes": "[...]",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "status": "SUCCESS",
    "timestamp": "2026-06-05T10:35:00"
  }
]
```

---

## Rate Limiting

The API implements rate limiting with the following rules:

- **Limit**: 100 requests per minute per IP address
- **Headers**:
  - `X-Rate-Limit-Remaining`: Remaining requests
  - `X-Rate-Limit-Retry-After-Seconds`: Seconds to wait before retry

**Rate Limit Exceeded Response (429 Too Many Requests):**

```json
{
  "error": "Too many requests. Please retry after 30 seconds"
}
```

---

## Error Responses

### Unauthorized (401)

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing token",
  "timestamp": "2026-06-05T10:30:00"
}
```

### Forbidden (403)

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "timestamp": "2026-06-05T10:30:00"
}
```

### Internal Server Error (500)

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2026-06-05T10:30:00"
}
```

---

## Field Validation

### Email
- Required
- Must be valid email format
- Must be unique for registration

### Password
- Required
- Minimum 6 characters

### Phone
- Required
- 10-13 digits
- Format: `+1234567890` or `1234567890`

### Name
- Required
- Cannot be empty

---

## Pagination

All paginated endpoints support the following parameters:

- `page` (default: 0): Page number (zero-indexed)
- `size` (default: 10): Items per page

---

## Sorting

Search endpoint supports sorting by:

- `name`: Contact name
- `email`: Contact email
- `createdAt`: Creation date
- `updatedAt`: Last update date

Sort order:
- `asc`: Ascending
- `desc`: Descending (default)

---

## Interactive API Documentation

Access Swagger UI at:

```
https://api.contactsmanager.com/swagger-ui.html
```

Access OpenAPI specification at:

```
https://api.contactsmanager.com/api-docs
```

---

## Example cURL Commands

### Register

```bash
curl -X POST https://api.contactsmanager.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@example.com",
    "password":"password123",
    "firstName":"John",
    "lastName":"Doe"
  }'
```

### Login

```bash
curl -X POST https://api.contactsmanager.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@example.com",
    "password":"password123"
  }'
```

### Get All Contacts

```bash
curl -X GET https://api.contactsmanager.com/api/contacts \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create Contact

```bash
curl -X POST https://api.contactsmanager.com/api/contacts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name":"Jane Doe",
    "email":"jane@example.com",
    "phone":"1234567890",
    "notes":"Friend"
  }'
```

### Search Contacts

```bash
curl -X POST https://api.contactsmanager.com/api/contacts/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "keyword":"jane",
    "sortBy":"name",
    "sortOrder":"asc",
    "page":0,
    "size":10
  }'
```

### Export to CSV

```bash
curl -X POST https://api.contactsmanager.com/api/contacts/export/csv \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o contacts.csv
```

---

## Support

For API support and issues, visit:

[GitHub Issues](https://github.com/firebasetest/contacts-management-app/issues)

# Contacts Management Application - Enhanced Setup

## Configuration Guide

### Environment Variables

#### Database Configuration
```env
DB_NAME=contacts_db
DB_USER=contacts_user
DB_PASSWORD=<strong-random-password>
DATABASE_URL=jdbc:postgresql://localhost:5432/contacts_db
```

#### Email Configuration
```env
# Gmail SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Or any other SMTP provider
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<sendgrid-api-key>
```

#### Security
```env
JWT_SECRET=<generate-random-32-char-secret>
JWT_EXPIRATION=86400000
```

#### API Configuration
```env
REACT_APP_API_URL=https://api.yourdomain.com
SPRING_PROFILES_ACTIVE=prod
```

### Production Email Setup

#### Using Gmail

1. Enable 2-factor authentication
2. Create an app password: https://myaccount.google.com/apppasswords
3. Use the app password in `MAIL_PASSWORD`

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx
```

#### Using SendGrid

1. Create SendGrid account
2. Generate API key
3. Configure:

```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.your-api-key
```

### Rate Limiting Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
rate.limit.enabled=true
rate.limit.default=100          # requests per minute
rate.limit.minute=10             # burst limit
```

### Docker Compose with All Features

```bash
# Start with email service
docker-compose up -d

# Check services
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Verify Features

#### 1. Rate Limiting
```bash
# Test with rapid requests
for i in {1..110}; do
  curl -X GET http://localhost:8080/api/contacts \
    -H "Authorization: Bearer $TOKEN"
done
# Should get 429 after 100 requests
```

#### 2. Audit Logging
```bash
# Login to see audit logs
curl -X GET http://localhost:8080/api/audit-logs \
  -H "Authorization: Bearer $TOKEN"
```

#### 3. Email Notifications
- Check email inbox after registration
- Verify login notifications
- Confirm contact modification emails

#### 4. API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/api-docs

---

For detailed deployment instructions, see `DEPLOYMENT.md`

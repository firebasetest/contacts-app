# Deployment Guide

Complete guide for deploying the Contacts Management Application to production.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Docker Deployment](#docker-deployment)
4. [AWS EC2 Deployment](#aws-ec2-deployment)
5. [Heroku Deployment](#heroku-deployment)
6. [DigitalOcean Deployment](#digitalocean-deployment)
7. [CI/CD Pipeline](#cicd-pipeline)
8. [Monitoring and Logging](#monitoring-and-logging)

---

## Prerequisites

- Docker & Docker Compose
- Git
- PostgreSQL 12+ (or use managed service)
- Node.js 14+ (for local development)
- Java 11+ (for local development)

---

## Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/firebasetest/contacts-management-app.git
cd contacts-management-app
```

### 2. Configure Environment Variables

```bash
cp .env.example .env
```

Edit `.env` with production values:

```env
# Database Configuration
DB_NAME=contacts_db_prod
DB_USER=contacts_user
DB_PASSWORD=<strong-password-here>
DATABASE_URL=jdbc:postgresql://db-host:5432/contacts_db_prod

# JWT Configuration
JWT_SECRET=<generate-random-secret>
JWT_EXPIRATION=86400000

# Frontend
REACT_APP_API_URL=https://your-domain.com/api

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

---

## Docker Deployment

### Option 1: Local Docker Compose

```bash
# Build and start containers
docker-compose up --build

# Stop containers
docker-compose down

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Option 2: Production Docker Compose

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  db:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: always

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      DATABASE_URL: ${DATABASE_URL}
      DATABASE_USER: ${DB_USER}
      DATABASE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    restart: always
    depends_on:
      - db

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    restart: always

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./certs:/etc/nginx/certs:ro
    restart: always

volumes:
  postgres_data:
```

Deploy:

```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

## AWS EC2 Deployment

### Step 1: Launch EC2 Instance

```bash
# Create instance with Ubuntu 20.04 LTS
# Type: t3.medium or larger
# Security Group: Allow ports 80, 443, 22
```

### Step 2: SSH into Instance

```bash
ssh -i your-key.pem ubuntu@your-ec2-ip
```

### Step 3: Install Dependencies

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu

# Install Docker Compose
sudo apt install -y docker-compose

# Install Git
sudo apt install -y git

# Install Nginx
sudo apt install -y nginx
```

### Step 4: Deploy Application

```bash
# Clone repo
git clone https://github.com/firebasetest/contacts-management-app.git
cd contacts-management-app

# Create .env
cp .env.example .env
# Edit .env with production values

# Start services
docker-compose up -d
```

### Step 5: Configure Nginx

Create `/etc/nginx/sites-available/contacts`:

```nginx
upstream backend {
    server localhost:8080;
}

upstream frontend {
    server localhost:3000;
}

server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    location /api {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

Enable site:

```bash
sudo ln -s /etc/nginx/sites-available/contacts /etc/nginx/sites-enabled/
sudo systemctl reload nginx
```

### Step 6: Set Up SSL with Let's Encrypt

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot certonly --nginx -d your-domain.com -d www.your-domain.com
```

---

## Heroku Deployment

### Step 1: Create Heroku App

```bash
heroku login
heroku create your-app-name
heroku addons:create heroku-postgresql:standard-0
```

### Step 2: Set Environment Variables

```bash
heroku config:set JWT_SECRET=your-secret
heroku config:set JWT_EXPIRATION=86400000
```

### Step 3: Deploy Backend

Create `Procfile` in backend directory:

```
web: java $JAVA_OPTS -Dspring.profiles.active=prod -jar target/contacts-management-1.0.0.jar
```

```bash
cd backend
heroku git:remote -a your-app-name
git push heroku main:main
```

### Step 4: Deploy Frontend

Create `Procfile` in frontend directory:

```
web: npm run build && npx serve -s build -l $PORT
```

```bash
cd frontend
heroku git:remote -a your-app-name-frontend
git push heroku main:main
```

---

## DigitalOcean Deployment

### Step 1: Create Droplet

```bash
# Use Docker image (Ubuntu 20.04 with Docker pre-installed)
# Size: $12/month or higher
```

### Step 2: SSH into Droplet

```bash
ssh root@your-droplet-ip
```

### Step 3: Install Dependencies

```bash
apt update && apt upgrade -y
apt install -y docker.io docker-compose git nginx certbot python3-certbot-nginx
```

### Step 4: Deploy Application

```bash
git clone https://github.com/firebasetest/contacts-management-app.git
cd contacts-management-app
cp .env.example .env
# Edit .env
docker-compose up -d
```

### Step 5: Configure Nginx & SSL

Same as AWS EC2 setup above.

---

## CI/CD Pipeline

### GitHub Actions Workflow

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Build and test backend
        run: |
          cd backend
          mvn clean test
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '16'
      
      - name: Install and test frontend
        run: |
          cd frontend
          npm install
          npm test

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /home/ubuntu/contacts-management-app
            git pull origin main
            docker-compose up -d --build
```

---

## Monitoring and Logging

### Docker Logging

```bash
# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
```

### Application Monitoring

```bash
# Check container status
docker-compose ps

# Check resource usage
docker stats
```

### Production Monitoring Tools

- **New Relic**: Application Performance Monitoring
- **DataDog**: Infrastructure and Application Monitoring
- **Prometheus**: Metrics collection
- **ELK Stack**: Logging and analysis

---

## Backup and Recovery

### PostgreSQL Backup

```bash
# Create backup
docker-compose exec db pg_dump -U contacts_user contacts_db > backup.sql

# Restore from backup
cat backup.sql | docker-compose exec -T db psql -U contacts_user contacts_db
```

---

## Troubleshooting

### Database Connection Issues

```bash
# Check database logs
docker-compose logs db

# Test database connection
docker-compose exec db psql -U contacts_user -d contacts_db
```

### Port Conflicts

```bash
# Change ports in docker-compose.yml
# Or kill existing process
sudo lsof -ti:8080 | xargs kill -9
```

### Memory Issues

```bash
# Increase Docker memory limit
# Edit docker-compose.yml
services:
  backend:
    mem_limit: 512m
```

---

For additional support, visit the [GitHub repository](https://github.com/firebasetest/contacts-management-app)

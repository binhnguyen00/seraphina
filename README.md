<h1 align="center"> ⚽️ Premier League Reminder </h1>

## Description
- Pull Premier League schedule matches from ESPN
- Sent notification to user via Zalo

## Tech Stack
- Java 21 with Spring Boot
- Gradle
- PostgreSQL 16
- Flask
- Zalo Bot
- Docker

## Requirements
- Docker & Docker Compose
- Java 21 (for local development)
- Python 3.12 (for local development)
- PostgreSQL 16 (for local development)
- Ngrok account (for webhook)

## Environment Setup
```bash
  cp .env.example .env
```
Update the following variables in ```.env```:
  - MICROSERVICE_ZALO_BOT_TOKEN (Contact me)
  - NGROK_AUTHTOKEN (get from ngrok.com)

## Database Setup
```bash
  ./scripts/database.sh initial-user
  ./scripts/database.sh initial-db
```

## Verify Services
- Main Application: http://localhost:8080/health
- Zalo Bot: http://localhost:8081/health

## Author - Binh Nguyen
- **[Github](https://github.com/binhnguyen00)**
- **[Gmail](mailto:jackjack2000.kahp@gmail.com)**

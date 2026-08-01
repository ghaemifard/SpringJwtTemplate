# Spring JWT Authentication

A modern, production-oriented JWT authentication system built with **Spring Boot 4**, **Spring Security 7**, **PostgreSQL**, and **Redis**.

This project demonstrates a clean and secure way to implement stateless authentication using short-lived access tokens and rotating refresh tokens stored in HttpOnly cookies.

## Features

- JWT-based access tokens (short-lived)
- Opaque refresh tokens with rotation
- Refresh tokens stored in **Redis** (with TTL)
- Users stored in **PostgreSQL**
- HttpOnly + SameSite cookies for refresh tokens
- UserDetails caching with Redis
- Stateless security configuration
- Docker Compose support (PostgreSQL + Redis)
- Ready for further extension (roles, registration, rate limiting, etc.)

## Tech Stack

| Technology            | Version / Notes              |
|-----------------------|------------------------------|
| Java                  | 25                           |
| Spring Boot           | 4.1.x                        |
| Spring Security       | 7.x                          |
| PostgreSQL            | via Docker Compose           |
| Redis                 | via Docker Compose           |
| JWT library           | jjwt 0.12.x                  |
| Build tool            | Gradle                       |

## Architecture Overview

- **Access Token** → JWT, sent in `Authorization: Bearer` header
- **Refresh Token** → Opaque token stored in Redis + sent as HttpOnly cookie
- **Token Rotation** → Old refresh token is invalidated when a new one is issued
- **User Cache** → UserDetails are cached in Redis to reduce database load

## Getting Started

### Prerequisites

- Java 25+
- Docker & Docker Compose
- Gradle (or use the wrapper)

### 1. Start the infrastructure

The project uses Spring Boot Docker Compose support.

Just run the application — PostgreSQL and Redis will start automatically via `compose.yaml`.

Alternatively, you can start them manually:

```bash
docker compose up -d
```

### 2. Configuration

Main configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/springai
    username: springai
    password: springai

  data:
    redis:
      host: localhost
      port: 6379

app:
  jwt:
    secret: your-very-long-secret-key-here
    expiration-ms: 900000              # 15 minutes
    refresh-expiration-ms: 604800000   # 7 days
```

### 3. Run the application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

## API Endpoints

| Method | Endpoint              | Description                     | Auth Required |
|--------|-----------------------|---------------------------------|---------------|
| POST   | `/api/auth/login`     | Login and receive tokens        | No            |
| POST   | `/api/auth/refresh`   | Get new access token            | Cookie        |
| POST   | `/api/auth/logout`    | Invalidate refresh token        | Cookie        |
| GET    | `/api/hello`          | Example protected endpoint      | Bearer Token  |

### Example: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' \
  -c cookies.txt -v
```

### Example: Access protected resource

```bash
curl http://localhost:8080/api/hello \
  -H "Authorization: Bearer <access_token>"
```

### Example: Refresh token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b cookies.txt -c cookies.txt -v
```

### Example: Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt -c cookies.txt
```

## Default Users

On first startup the following users are created:

| Username | Password  | Role       |
|----------|-----------|------------|
| user     | password  | ROLE_USER  |
| admin    | admin     | ROLE_ADMIN |



## Testing

Run all tests:

```bash
./gradlew test
```

The project includes unit tests for:


## Security Notes

- Access tokens are short-lived (15 minutes by default)
- Refresh tokens live in Redis and expire automatically via TTL
- Refresh tokens are rotated on every use
- Refresh tokens are stored in `HttpOnly` cookies (`SameSite=Strict`)
- Always use HTTPS in production and set `secure=true` on cookies

## Future Improvements

- User registration endpoint
- Role-based access control examples
- Refresh token family / reuse detection
- Rate limiting on login & refresh endpoints
- Access token blacklist (optional)
- OpenAPI / Swagger documentation


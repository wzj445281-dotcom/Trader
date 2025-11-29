# Trader - Campus Secondhand (With JWT & BCrypt)

## Overview
Full-stack starter: Spring Boot backend + Vue 3 frontend.
Database name: `trader`.

This update adds:
- Password hashing with BCrypt (spring-security-crypto)
- JWT tokens issued on login/register (java-jwt). Frontend stores token in localStorage and sends Authorization header.

## Run backend
1. Create DB: run `backend/sql_init.sql` on MySQL or use docker-compose.
2. Adjust DB creds in `backend/src/main/resources/application.yml`.
3. Build & run:
   - `cd backend`
   - `mvn clean package`
   - `mvn spring-boot:run`
4. Note: The JWT secret is in `JwtUtil.java` — replace it with a secure value or load from environment.

## Run frontend
1. `cd frontend`
2. `npm install`
3. `npm run dev` (port 5173)

## Test
- Register or login via `/login` page. After login token stored in localStorage and used in subsequent requests.

## Security
- This example is for learning/demo. For production, use secure secret storage, HTTPS, token expiry handling, refresh tokens, and stricter CORS.


## New features added
- JWT secret configurable: set via JVM property -Djwt.secret=yoursecret or environment variable JWT_SECRET.
- Use `mvn -q -Dexec.mainClass=com.trader.app.util.SecretGen exec:java` to generate a base64 secret (or run the SecretGen main).
- Spring Security integrated: protected endpoints require valid JWT (publish, fav, favs).
- Refresh token flow: login returns `refresh` token; call `/api/auth/refresh` with JSON `{ "refresh": "<token>" }` to get a new access token. Call `/api/auth/logout` with `{ "refresh": "<token>" }` to invalidate.
- Sample data inserted in `backend/sql_init.sql` (users with bcrypt-hashed password 'password123', sample products).


## Additional completed features
- Private WebSocket chat with persistence (connect to ws://<host>:8080/ws/chat?uid=<yourId>). Send JSON {"from":1, "to":2, "message":"hi"}.
- Admin console and analytics dashboard (frontend routes /admin and /analytics). Admin route requires user.role === 'ADMIN'.
- JUnit test for JwtUtil included: run `mvn test` in backend.

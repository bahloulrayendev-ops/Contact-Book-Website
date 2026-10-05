# Contact Book Website

A full-stack contact directory web app with a React + Vite frontend and a Spring Boot backend.

This project is meant to be run locally for testing and demonstration.

## Features

- Search and browse company and employee records
- Unlock contact details with tokens
- View token wallet and transaction history
- Receive notification updates when a saved contact detail changes
- Company/employee relationship browsing
- Auth, profile, and password management

## Tech stack

- Frontend: React, TypeScript, Vite, TanStack Router, Tailwind
- Backend: Java 25, Spring Boot 4, Spring Security, JPA, PostgreSQL

## Project structure

- frontend/ — React frontend app
- intern/ — Spring Boot backend and Java app
- Database.sql — PostgreSQL schema/data bootstrap script

## Prerequisites

- Node.js 20+
- Java 25
- PostgreSQL 16+
- Git

## Backend setup

1. Open PostgreSQL and create a database named `DataInfoEgypt`.
2. Create a PostgreSQL user with access to that database.
3. In the backend folder, copy the example config:

   ```bash
   cd intern
   copy application-local.properties.example application-local.properties
   ```

4. Update `intern/application-local.properties` with your local database and JWT values.

   Example:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/DataInfoEgypt
   spring.datasource.username=postgres
   spring.datasource.password=your-password
   jwt.secret=replace-with-a-private-random-secret-of-at-least-32-characters
   ```

5. Start the backend:

   ```bash
   cd intern
   ./mvnw spring-boot:run
   ```

   The API will run at:
   - http://localhost:8080

## Frontend setup

1. Install dependencies:

   ```bash
   cd frontend
   npm install
   ```

2. Run the app in development mode:

   ```bash
   npm run dev -- --host 0.0.0.0
   ```

   The frontend will run at:
   - http://localhost:8081

## Default app behavior

- The frontend expects the backend at `http://localhost:8080/api`
- The backend loads local values from `intern/application-local.properties`
- Do not commit local secrets or passwords to the repository

## Database setup

You can initialize the local database with the provided script:

```bash
psql -U postgres -d DataInfoEgypt -f Database.sql
```

If your Postgres user differs, adjust the command accordingly.

## Notes for public repos

The app is designed for local testing and should not be exposed directly to the internet without additional hardening (HTTPS, environment variables, production secrets, and deployment configuration).

## License

This project is provided as-is for learning and demonstration purposes.

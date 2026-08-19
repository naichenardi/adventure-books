# Adventure Books 📖⚔️

An interactive adventure book application built with **Angular** (frontend) and **Java 21 + Spring Boot 4** (backend).

---

## Table of Contents

- [Project Description](#project-description)
- [Game Rules](#game-rules)
- [Book Validity Rules](#book-validity-rules)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Overview](#api-overview)
- [Development Notes](#development-notes)
- [Frontend - Angular](frontend/README.md)

---

## Project Description

Adventure books have been around for decades, immersing the reader-player in a fantastic journey. Each book is composed of small numbered sections; at the end of each section the reader chooses what the character does next, and the book tells them which section to read.

This application lets users:

- **Browse** the available adventure books (title, author, difficulty)
- **Search & filter** books
- **Validate** a book against the game rules before playing
- **Play** a book interactively, navigating section by section
- **Track health** — certain choices reduce (or restore) health points
- **Save & resume** game sessions (pause / resume)
- **Win or lose** — reach an ending section, or die when health hits 0

---

## Game Rules

| Rule | Detail |
|------|--------|
| Starting health | **10 HP** |
| Consequence types | `LOSE_HEALTH` / `GAIN_HEALTH` |
| Death | Health ≤ 0 → game over |
| Victory | Reaching a section of type `END` |

---

## Book Validity Rules

A book is considered **invalid** if any of the following conditions are met:

| # | Rule |
|---|------|
| 1 | The book has **no beginning** section, or **more than one** beginning section |
| 2 | The book has **no ending** section (multiple endings are allowed) |
| 3 | The book contains an option that references an **invalid / non-existent** section id |
| 4 | A **non-ending** section has **no options** |

---

## Tech Stack

### Backend
| Technology        | Version | Purpose                                         |
|-------------------|---------|-------------------------------------------------|
| Java              | 21      | Language                                        |
| Spring Boot       | 4.1.0   | Application framework                           |
| Spring Web        | —       | REST API                                        |
| Spring Data JPA   | —       | Data persistence                                |
| PostgreSQL        | 16      | Application database (via Docker)               |
| H2 Database       | —       | In-memory database (tests, and local dev via `h2` profile) |
| OpenAPI Generator | 7.0.0   | Contract-first API model & interface generation |
| Bean Validation   | —       | Input validation                                |
| Docker Compose    | —       | Runs postgres + backend + frontend together (see `docker-compose.yml`) |

---

## Project Structure

```
adventure-books/
├── src/
│   ├── main/
│   │   ├── java/com/adventurebooks/
│   │   │   ├── AdventureBooksApplication.java
│   │   │   ├── controller/         # REST controllers (BookController, SessionController)
│   │   │   ├── service/            # Business logic (BookService, GameSessionService, BookLoaderService)
│   │   │   ├── repository/         # Spring Data JPA repositories (BookRepository, GameSessionRepository)
│   │   │   ├── model/
│   │   │   │   ├── entity/         # Domain entities (Book, Section, Option, Consequence, GameSession)
│   │   │   │   └── enums/          # SectionType, ConsequenceType, Difficulty
│   │   │   ├── validation/         # BookValidationService + BookValidationResult
│   │   │   └── exception/          # GlobalExceptionHandler + ErrorResponse
│   │   ├── openapi/
│   │   │   └── openapi.yaml        # OpenAPI contract (source of truth for generated code)
│   │   └── resources/
│   │       ├── application.yaml    # PostgreSQL config (default)
│   │       └── application-h2.yaml # H2 config (opt-in via `h2` profile, no Docker needed)
│   └── test/
│       ├── java/
│       └── resources/
│           ├── application.yaml    # H2 config (overrides main config for tests)
│           └── books/              # Sample book JSON fixtures
│               ├── dragon-quest.json        (intentionally empty — tested as empty-file case)
│               ├── crystal-caverns.json
│               ├── pirates-jade-sea.json
│               └── the-prisoner.json
├── target/generated-sources/openapi/  # Auto-generated from openapi.yaml (do not edit)
├── frontend/
│   ├── Dockerfile                   # Multi-stage build: npm build -> nginx
│   └── nginx.conf                   # Serves the SPA, proxies /api/* to the backend container
├── docker-compose.yml               # postgres + backend + frontend
├── Dockerfile                       # Backend multi-stage build: maven -> jre
├── pom.xml
└── README.md
```

---

## Getting Started

### Option A: Run everything with Docker Compose

Prerequisites: Docker only.

```bash
docker compose up -d --build
```

This builds and starts all three services (see `docker-compose.yml`):

| Service    | URL                            | Notes                                              |
|------------|---------------------------------|-----------------------------------------------------|
| `postgres` | `localhost:5432`                | PostgreSQL 16, data persisted in a named volume     |
| `backend`  | `http://localhost:8080`         | Built from the root `Dockerfile`                    |
| `frontend` | `http://localhost:4200`         | Nginx serving the Angular build; proxies `/api/*` to `backend:8080` internally |

Open `http://localhost:4200` — the frontend talks to `/api` on the same origin, so nginx forwards
those requests to the backend container over the compose network (no CORS involved). Rebuild after
code changes with `docker compose up -d --build`; stop everything with `docker compose down` (add
`-v` to also drop the Postgres volume).

### Option B: Run locally without Docker (or Docker for just Postgres)

Prerequisites:

- Java 21+
- Maven 3.9+
- Node 22+ (for the frontend)
- Docker (for PostgreSQL, unless using the `h2` profile below)

#### Start the database

```bash
docker compose up -d postgres
```

This starts a PostgreSQL 16 container on `localhost:5432` with database/user/password all set to
`adventurebooks` (see `docker-compose.yml`). Data persists in a named Docker volume across restarts.

To point the app at a different database, override the connection via environment variables:
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (see `src/main/resources/application.yaml`).

#### Run the backend

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

#### Run the backend with H2 instead of PostgreSQL

To run locally without Docker at all, activate the `h2` profile — it swaps in an in-memory H2
database (`src/main/resources/application-h2.yaml`) and needs nothing else running:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Or, against a packaged jar:

```bash
java -jar target/adventurebooks-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2
```

#### Run the frontend

```bash
cd frontend
npm install
npm start
```

The dev server runs at `http://localhost:4200` and proxies `/api` to `http://localhost:8080`
(see `frontend/proxy.conf.json`) — start the backend first.

### Run tests

```bash
./mvnw test
```

### Run tests with coverage

```bash
./mvnw verify
```

---

## API Overview

The API is defined contract-first in [
`src/main/resources/openapi/adventure-books-v1-spec.yaml`](src/main/resources/openapi/adventure-books-v1-spec.yaml).  
Interfaces and model classes are generated automatically during the build under `com.adventurebooks.generated`.

### Books

| Method | Endpoint                   | Description                                                      |
|--------|----------------------------|------------------------------------------------------------------|
| `GET`  | `/api/books`               | List all books (supports `query` and `difficulty` filter params) |
| `GET`  | `/api/books/{id}`          | Get a single book by id                                          |
| `GET`  | `/api/books/{id}/validate` | Validate book against game rules                                 |

### Game Sessions

| Method | Endpoint                    | Description                                      |
|--------|-----------------------------|--------------------------------------------------|
| `POST` | `/api/sessions`             | Start a new game session (`{ "bookId": "..." }`) |
| `GET`  | `/api/sessions/{id}`        | Get current session state                        |
| `POST` | `/api/sessions/{id}/choose` | Make a section choice (`{ "optionIndex": 0 }`)   |
| `POST` | `/api/sessions/{id}/save`   | Save / pause a session                           |

### Error responses

All errors follow a consistent shape:

```json
{
  "timestamp": "2026-08-18T00:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found: xyz",
  "path": "/api/books/xyz"
}
```

---

## Development Notes

- Book JSON files placed in `src/test/resources/books/` are loaded automatically on startup (configurable via `adventure-books.books-path` in `application.yaml`).
- `dragon-quest.json` is intentionally empty and is used to test empty-file handling.
- API interfaces (`BookApi`, `SessionApi`) and model classes (`BookDto`, `SectionDto`, etc.) are **generated** from
  `adventure-books-v1-spec.yaml` — do not edit files under `target/generated-sources/`.
- The app connects to PostgreSQL (via `docker-compose.yml`) at runtime; `src/test/resources/application.yaml` overrides this with in-memory H2 for tests, since `target/test-classes` takes precedence over `target/classes` on the test classpath.
- `spring.jpa.hibernate.ddl-auto` is `update` for the PostgreSQL config: existing data and tables survive restarts, and Hibernate only adds missing tables/columns it doesn't already know about. It won't drop or rename columns for you, so if you rework an entity's mapping in a way that needs a column dropped/renamed, do that by hand (or move to a migration tool such as Flyway) rather than relying on `update` to reconcile it. The `h2` profile (`application-h2.yaml`) still uses `create-drop`, which is fine there since that database is in-memory and reset on every run anyway.
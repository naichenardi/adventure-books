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
| H2 Database       | —       | In-memory database (dev)                        |
| OpenAPI Generator | 7.0.0   | Contract-first API model & interface generation |
| Bean Validation   | —       | Input validation                                |

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
│   │   │   ├── repository/         # In-memory repository
│   │   │   ├── model/
│   │   │   │   ├── entity/         # Domain entities (Book, Section, Option, Consequence, GameSession)
│   │   │   │   └── enums/          # SectionType, ConsequenceType, Difficulty
│   │   │   ├── validation/         # BookValidationService + BookValidationResult
│   │   │   └── exception/          # GlobalExceptionHandler + ErrorResponse
│   │   ├── openapi/
│   │   │   └── openapi.yaml        # OpenAPI contract (source of truth for generated code)
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       ├── java/
│       └── resources/
│           └── books/              # Sample book JSON fixtures
│               ├── dragon-quest.json        (intentionally empty — tested as empty-file case)
│               ├── crystal-caverns.json
│               ├── pirates-jade-sea.json
│               └── the-prisoner.json
├── target/generated-sources/openapi/  # Auto-generated from openapi.yaml (do not edit)
├── pom.xml
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run the backend

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### H2 Console (development)

While the application is running, access the in-memory database at:

```
http://localhost:8080/h2-console
```

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:adventurebooks` |
| Username | `sa` |
| Password | *(empty)* |

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
- The H2 database uses `create-drop` DDL mode, so data is reset on each restart. A persistent database can be configured by changing `spring.datasource.url` and `spring.jpa.hibernate.ddl-auto`.
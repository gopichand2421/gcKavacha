# GcKavacha Development Guide

## 1. Overview

GcKavacha is an AI-powered distributed incident management and reliability platform.

The platform helps engineering teams:

* Detect production incidents
* Process and correlate alerts
* Manage incident lifecycle
* Collect incident evidence
* Perform AI-assisted investigation
* Analyze probable root causes
* Recommend investigation and mitigation actions
* Generate postmortem information

---

## 2. Technology Stack

### Backend

* Java 17
* Spring Boot 3.x
* Maven
* Spring Web
* Spring Validation
* Spring Actuator
* Spring Data MongoDB
* Spring Data Redis
* Spring Kafka
* Spring AI
* JUnit 5

### Frontend

* React
* TypeScript
* Webpack
* Material UI
* React Router

### Infrastructure

* MongoDB
* Redis
* Apache Kafka
* Docker
* Docker Compose

### Future Platform Components

* Kubernetes
* OpenTelemetry
* Prometheus
* Grafana
* LLM providers
* Vector database

---

# 3. Prerequisites

Install the following tools before starting development.

```text
Java 17
Node.js 20+
npm
Docker
Docker Compose
Git
```

Verify installations:

```bash
java -version
node --version
npm --version
docker --version
docker compose version
git --version
```

---

# 4. Clone the Repository

```bash
git clone <repository-url>

cd gckavacha
```

---

# 5. Repository Structure

```text
gckavacha/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   └── src/
│
├── frontend/
│   └── gckavacha-ui/
│       ├── package.json
│       ├── package-lock.json
│       ├── webpack.config.js
│       ├── public/
│       └── src/
│
├── docs/
│   ├── architecture/
│   ├── domain/
│   ├── events/
│   └── development/
│
├── docker-compose.yml
└── README.md
```

---

# 6. Start Infrastructure

GcKavacha uses Docker Compose for local infrastructure.

Start the infrastructure:

```bash
docker compose up -d
```

Check containers:

```bash
docker compose ps
```

Expected services:

```text
gckavacha-mongodb
gckavacha-redis
gckavacha-kafka
```

---

# 7. MongoDB

MongoDB runs on:

```text
localhost:27017
```

Database:

```text
gckavacha
```

Default local connection:

```text
mongodb://localhost:27017/gckavacha
```

Check MongoDB:

```bash
docker exec -it gckavacha-mongodb mongosh
```

Inside MongoDB:

```javascript
show databases
```

Exit:

```javascript
exit
```

---

# 8. Redis

Redis runs on:

```text
localhost:6379
```

Verify Redis:

```bash
docker exec -it gckavacha-redis redis-cli ping
```

Expected:

```text
PONG
```

---

# 9. Kafka

Kafka runs on:

```text
localhost:9092
```

Applications running on the host machine should use:

```text
localhost:9092
```

Applications running inside Docker should use:

```text
kafka:29092
```

Kafka uses KRaft mode and does not require ZooKeeper.

---

# 10. Backend Configuration

The backend uses Spring profiles.

Current local profile:

```text
local
```

The default Spring profile is configured as:

```yaml
spring:
  profiles:
    default: local
```

Local MongoDB:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/gckavacha}
```

Local Redis:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

Local Kafka:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

Environment variables can override these defaults.

---

# 11. Run Backend

Navigate to the backend:

```bash
cd backend
```

Run tests:

```bash
./mvnw clean test
```

Start Spring Boot:

```bash
./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

# 12. Backend Health Check

Spring Boot Actuator exposes the health endpoint.

Open:

```text
http://localhost:8080/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

---

# 13. Run Frontend

Open another terminal.

```bash
cd frontend/gckavacha-ui
```

Install dependencies:

```bash
npm ci
```

Start development server:

```bash
npm start
```

The frontend runs on:

```text
http://localhost:3000
```

---

# 14. Frontend Build

Create a production build:

```bash
npm run build
```

TypeScript validation:

```bash
npx tsc --noEmit
```

---

# 15. Running the Complete Application

Start infrastructure:

```bash
docker compose up -d
```

Terminal 1:

```bash
cd backend
./mvnw spring-boot:run
```

Terminal 2:

```bash
cd frontend/gckavacha-ui
npm ci
npm start
```

Access:

```text
Frontend:
http://localhost:3000

Backend:
http://localhost:8080

Backend health:
http://localhost:8080/actuator/health
```

---

# 16. Stop Infrastructure

Stop containers:

```bash
docker compose down
```

This stops containers while preserving named volumes.

To remove containers and volumes:

```bash
docker compose down -v
```

Use `down -v` carefully because it removes local persisted infrastructure data.

---

# 17. Git Workflow

Create a feature branch:

```bash
git checkout -b feature/<feature-name>
```

Example:

```bash
git checkout -b feature/incident-model
```

Make changes and run validation:

```bash
./mvnw clean test
```

and:

```bash
npm run build
```

Commit:

```bash
git add .
git commit -m "feat: implement incident model"
```

Push:

```bash
git push -u origin feature/incident-model
```

Create a Pull Request.

---

# 18. Pull Request Expectations

Every Pull Request should:

* Have a clear description
* Reference the related GitHub issue
* Pass CI
* Include tests for new behavior
* Avoid unrelated changes
* Update documentation when architecture or behavior changes
* Keep commits understandable

---

# 19. CI

GitHub Actions validates backend and frontend changes.

Workflow:

```text
Pull Request
     │
     ▼
GitHub Actions
     │
     ├── Backend
     │    ├── Java 17
     │    ├── Maven
     │    └── Tests
     │
     └── Frontend
          ├── Node.js
          ├── TypeScript
          └── Webpack Build
```

CI workflow:

```text
.github/workflows/ci.yml
```

---

# 20. Architecture Documentation

Architecture documentation is maintained under:

```text
docs/architecture/
```

Important documents include:

```text
docs/architecture/README.md

docs/architecture/high-level-architecture.md

docs/architecture/decisions/
```

Architecture decisions should be recorded using ADRs.

ADR naming:

```text
ADR-001-short-name.md
```

---

# 21. Domain Documentation

Domain models are documented under:

```text
docs/domain/
```

Current documents include:

```text
core-domain-model.md
incident-lifecycle.md
alert-model.md
```

---

# 22. Event Documentation

Event definitions are documented under:

```text
docs/events/
```

Current event documentation:

```text
event-model.md
```

Kafka events should follow the documented event envelope and naming conventions.

---

# 23. Development Principles

### Small Changes

Prefer small Pull Requests that solve one problem.

### Test First Where Practical

New business behavior should have automated tests.

### API Contract

Backend APIs should have explicit request and response models.

### Event-Driven Design

Use Kafka for asynchronous workflows where appropriate.

### Idempotency

Consumers must safely handle duplicate events.

### Evidence-Driven AI

AI-generated conclusions must distinguish:

```text
Observed Evidence
       ↓
Analysis
       ↓
Hypothesis
       ↓
Confidence
       ↓
Recommended Investigation
```

AI must not present unsupported assumptions as facts.

### Security

Never commit:

* Passwords
* API keys
* Tokens
* Private certificates
* Production credentials
* Personally identifiable information

Use environment variables or a proper secret-management mechanism.

---

# 24. Local Troubleshooting

## Backend does not start

Check infrastructure:

```bash
docker compose ps
```

Check MongoDB:

```bash
docker exec -it gckavacha-mongodb mongosh
```

Check Redis:

```bash
docker exec -it gckavacha-redis redis-cli ping
```

Check Kafka:

```bash
docker logs gckavacha-kafka
```

---

## Port 8080 already in use

Find the process:

```bash
lsof -i :8080
```

Stop the conflicting process or configure another backend port.

---

## Port 3000 already in use

```bash
lsof -i :3000
```

Stop the conflicting process before starting the frontend.

---

## MongoDB connection failure

Verify:

```text
localhost:27017
```

and:

```bash
docker compose ps
```

---

## Redis connection failure

Verify:

```bash
docker exec -it gckavacha-redis redis-cli ping
```

Expected:

```text
PONG
```

---

## Kafka connection failure

For applications running directly on the host:

```text
localhost:9092
```

For applications running inside Docker:

```text
kafka:29092
```

Do not interchange these addresses.

---

# 25. Development Environment

The standard local development environment is:

```text
┌──────────────────────────────┐
│       Developer Machine      │
│                              │
│  React / Webpack :3000       │
│           │                  │
│           ▼                  │
│  Spring Boot :8080           │
│      │      │      │         │
└──────┼──────┼──────┼─────────┘
       │      │      │
       ▼      ▼      ▼
    MongoDB  Redis  Kafka
    :27017   :6379  :9092
```

---

# 26. Definition of Done

A development environment is considered ready when:

* [ ] Repository cloned
* [ ] Java 17 available
* [ ] Node.js available
* [ ] Docker available
* [ ] MongoDB running
* [ ] Redis running
* [ ] Kafka running
* [ ] Backend starts successfully
* [ ] `/actuator/health` returns `UP`
* [ ] Frontend starts successfully
* [ ] Frontend production build succeeds
* [ ] Backend tests pass
* [ ] GitHub Actions CI passes

---

# 27. Quick Start

For experienced developers:

```bash
git clone <repository-url>

cd gckavacha

docker compose up -d

cd backend
./mvnw spring-boot:run
```

In another terminal:

```bash
cd frontend/gckavacha-ui

npm ci

npm start
```

Open:

```text
http://localhost:3000
```

Backend health:

```text
http://localhost:8080/actuator/health
```

---

# 28. Current Development Phase

Current epic:

```text
E01 — Foundation & Architecture
```

Completed foundation work:

```text
S01 Repository Structure
S02 Backend Skeleton
S03 Frontend Skeleton
S04 Development Profiles
S05 Local MongoDB
S06 Local Redis
S07 Local Kafka
S08 Unified Docker Compose
S09 High-Level Architecture
S10 Core Domain Model
S11 Incident Lifecycle
S12 Alert Model
S13 Event Model
S14 ADR Template
S15 Technology Decisions
S16 GitHub Actions CI
S17 Development Guide
```

This completes the initial Foundation & Architecture phase.

# ECommerceProject (CS4135 Group 3)

Microservices-style e-commerce app: **React (Vite)** frontend, **Spring Cloud Gateway**, **Spring Boot** services (e.g. user, order), **JWT** auth, **database per service**.

## Prerequisites

- **Docker** and Docker Compose
- **Java 21** and **Maven** (to run or test services locally without the full stack in Docker)
- **Node.js** (optional: root `npm run dev` is a shortcut for Compose)

## Run the full stack locally

From the **repository root**:

```bash
npm run dev
```

Same as:

```bash
docker compose up --build
```

- **Frontend:** [http://localhost:5173](http://localhost:5173)
- **API Gateway:** [http://localhost:8080](http://localhost:8080)
- **User service** is reached via the gateway (`/api/auth/`**, `/api/users/**`), not exposed on the host by default in this setup.
- **PostgreSQL** (user data) and **MySQL** (order data) are started as defined in `docker-compose.yml`.

Stop:

```bash
npm run down
```

## Repository layout


| Path                     | Role                       |
| ------------------------ | -------------------------- |
| `frontend/`              | Vite + React               |
| `backend/api-gateway/`   | Spring Cloud Gateway       |
| `backend/user-service/`  | Users, authentication, JWT |
| `backend/Order-Service/` | Order service              |
| `docker-compose.yml`     | Local stack (repo root)    |


## Tests

In each Java service directory:

```bash
mvn test
```

The user-service tests use Testcontainers; **Docker must be running**.

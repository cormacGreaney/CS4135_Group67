# ECommerceProject (CS4135 Group 3)

Microservices e-commerce stack: **React (Vite)** frontend, **Spring Cloud Gateway**, **Spring Boot** services, **JWT** auth, **database per service**, **RabbitMQ** for order/payment events.

## Prerequisites

- **Docker** and Docker Compose (required for `npm run dev` and for user-service tests)
- **Java 21** and **Maven** (to run `mvn test` or a single service outside Compose)
- **Node.js** (optional; root `package.json` only wraps Docker Compose)

## Run the full stack

From the **repository root**:

```bash
npm run dev
```

Equivalent to:

```bash
docker compose up --build
```

Other scripts: `npm run dev:detached` (Compose in the background), `npm run down` (stop), `npm run reset` (remove volumes, rebuild, start — wipes DB data).

### URLs (with default Compose)


| What                   | URL                                                                                 |
| ---------------------- | ----------------------------------------------------------------------------------- |
| Frontend               | [http://localhost:5173](http://localhost:5173)                                      |
| API Gateway            | [http://localhost:8080](http://localhost:8080)                                      |
| RabbitMQ management UI | [http://localhost:15672](http://localhost:15672) (user/pass default `app` / `app` ) |


Call the backend **only through the gateway**, not by port-hopping services. Typical paths:

- `/api/auth/`**, `/api/users/**` - user-service (user-service is not published on the host; gateway only)
- `/api/products/**` - product-service
- `/api/order/**` - order-service
- `/api/payments/**` - payment-service

### Datastores in Compose

- **PostgreSQL** - `user_service` DB (user-service) and `payment_service` DB (payment-service; port **5433** on the host)
- **MySQL** - separate instances for **order-service** (host **3306**) and **product-service** (host **3307**)
- **RabbitMQ** - messaging between order-service and payment-service

### Admin login (Docker Compose)

On startup, user-service can create one **ADMINISTRATOR** if `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` are both set. Compose supplies defaults:

- **Email:** `admin@example.com`
- **Password:** `adminadmin12`

Use **Login** (not Register). Use the returned JWT for admin-only APIs (e.g. product create/update/delete). If that email already exists, bootstrap does nothing (check user-service logs).

## Repository layout


| Path                       | Role                                         |
| -------------------------- | -------------------------------------------- |
| `frontend/`                | Vite + React                                 |
| `backend/api-gateway/`     | Spring Cloud Gateway                         |
| `backend/user-service/`    | Users, JWT issuance, auth endpoints          |
| `backend/product-service/` | Product catalogue                            |
| `backend/order-service/`   | Orders, cart-related flows, RabbitMQ publish |
| `backend/payment-service/` | Simulated payments, RabbitMQ consume         |
| `docker-compose.yml`       | Full local stack                             |


## Tests

In each Java service folder:

```bash
mvn test
```

User-service tests use **Testcontainers** (PostgreSQL); Docker must be running.

## CI / CD

- **CI** — on push and pull requests to `main`: validate Compose and build all stack images (`.github/workflows/ci.yml`).
- **CD** — on push to `main`: build and push service images to **GitHub Container Registry** (`ghcr.io`, see `.github/workflows/cd.yml`).


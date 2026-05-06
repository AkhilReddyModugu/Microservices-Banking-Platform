# BankFlow — Banking Microservices Platform

A production-style distributed banking system built with Spring Boot microservices.
Supports customer onboarding, account management, fund transfers, and real-time
email notifications using asynchronous event-driven communication.

---

## Architecture

```
Client
└── API Gateway (port 8080)
    ├── Customer Service     (port 8002)
    ├── Account Service      (port 8001)
    ├── Transaction Service  (port 8003)
    └── Notification Service (port 8004)

Service Discovery : Eureka Server (port 8761)
Messaging         : RabbitMQ
Database          : MySQL (separate DB per service)
```

Each service is independently deployable with its own database.
Services communicate via **OpenFeign** (synchronous) and **RabbitMQ** (asynchronous).

---

## Services

### Eureka Server — Port 8761

Service registry. Every other service registers here on startup.
API Gateway and Feign clients look up service addresses dynamically — no hardcoded URLs.

---

### API Gateway — Port 8080

Single entry point for all client requests. Routes traffic based on path:

| Path                | Routed To            |
| ------------------- | -------------------- |
| `/customers/**`     | Customer Service     |
| `/accounts/**`      | Account Service      |
| `/transactions/**`  | Transaction Service  |
| `/notifications/**` | Notification Service |

Uses `lb://` (load-balanced) routing via Eureka.

---

### Customer Service — Port 8002

Manages customer profiles.

| Method | Endpoint          | Description         |
| ------ | ----------------- | ------------------- |
| GET    | `/customers`      | Get all customers   |
| GET    | `/customers/{id}` | Get customer by ID  |
| POST   | `/customers`      | Create new customer |
| DELETE | `/customers/{id}` | Delete customer     |

- **On create:** publishes a welcome notification event to RabbitMQ.
- **On delete:** calls Account Service via Feign to delete the associated account, then publishes a deletion notification.

---

### Account Service — Port 8001

Manages bank accounts.

| Method | Endpoint                            | Description                   |
| ------ | ----------------------------------- | ----------------------------- |
| GET    | `/accounts`                         | Get all accounts              |
| GET    | `/accounts/{id}`                    | Get account by ID             |
| GET    | `/accounts/customer/{id}`           | Get account by customer ID    |
| GET    | `/accounts/account/{accountNumber}` | Get account by account number |
| POST   | `/accounts`                         | Create new account            |
| PUT    | `/accounts/update`                  | Update account balance        |
| DELETE | `/accounts/{accountNumber}`         | Delete account                |

- **On create/delete:** calls Customer Service via Feign to get customer details, then publishes a notification event to RabbitMQ.

---

### Transaction Service — Port 8003

Handles all financial operations.

| Method | Endpoint                                | Description                      |
| ------ | --------------------------------------- | -------------------------------- |
| GET    | `/transactions`                         | Get all transactions             |
| GET    | `/transactions/account/{accountNumber}` | Get by account number            |
| GET    | `/transactions/transaction/{type}`      | Get by type (`CREDIT` / `DEBIT`) |
| POST   | `/transactions/credit-transfer`         | Credit an account                |
| POST   | `/transactions/debit-transfer`          | Debit an account                 |
| POST   | `/transactions/transfer`                | Transfer between two accounts    |

**Business rules enforced:**

- Debit and transfer are rejected if balance is insufficient.
- Transfer is rejected if sender and receiver are the same account.
- Transfer enforces a minimum amount of ₹10.
- Every transaction publishes a notification event to RabbitMQ.

---

### Notification Service — Port 8004

Listens to RabbitMQ and sends emails.

| Method | Endpoint              | Description                       |
| ------ | --------------------- | --------------------------------- |
| POST   | `/notifications/send` | Send notification directly (sync) |

- Consumes messages from the RabbitMQ queue automatically.
- Validates email format before sending.
- Saves every notification to its own database for audit.
- Uses `JavaMailSender` with SMTP (configurable — Gmail, SendGrid, etc.).

---

## Tech Stack

| Layer               | Technology                     |
| ------------------- | ------------------------------ |
| Language            | Java 17                        |
| Framework           | Spring Boot 3.3                |
| Service Discovery   | Netflix Eureka                 |
| API Gateway         | Spring Cloud Gateway           |
| Inter-service calls | OpenFeign                      |
| Async messaging     | RabbitMQ                       |
| Database            | MySQL 8                        |
| ORM                 | Spring Data JPA / Hibernate    |
| Build tool          | Maven                          |
| API Docs            | SpringDoc OpenAPI (Swagger UI) |

---

## How Communication Works

### Synchronous (OpenFeign)

Used when a service needs data from another service immediately.

**Example:** When creating an account, Account Service needs the customer's
email to send a welcome notification. It calls Customer Service via Feign:

```
Account Service ──Feign──▶ Customer Service
                           returns CustomerDTO (name, email)
```

### Asynchronous (RabbitMQ)

Used for notifications — fire and forget. The calling service does not wait for the email to be sent.

```
Account Service ──publishes──▶ RabbitMQ Exchange
                                      │
                               routes via binding key
                                      │
                               Notification Queue
                                      │
                          Notification Service consumes
                                      │
                          JavaMailSender sends email
```

> If Notification Service is down, messages stay in the queue and are delivered automatically when it restarts. No notification is lost.

---

## Project Structure

```
BankFlow/
├── Eureka-server/
├── Api-gateway/
├── CustomerServices/
│   └── src/main/java/com/example/customerservices/
│       ├── controller/
│       ├── service/
│       ├── Repo/
│       ├── entity/
│       ├── dto/
│       ├── feignconfig/
│       ├── config/          ← RabbitMQ config
│       └── CustomerUtils/
├── Accountservices/
│   └── src/main/java/com/example/accountservices/
│       ├── controller/
│       ├── service/
│       ├── Repo/
│       ├── entity/
│       ├── dto/
│       ├── feignconfig/
│       ├── config/
│       └── accountUtils/
├── TransactionService/
│   └── src/main/java/com/example/transactionservice/
│       ├── controller/
│       ├── service/
│       ├── Repo/
│       ├── entity/
│       ├── dto/
│       ├── feignconfig/
│       ├── config/
│       └── transactionUtils/
└── Notification-Service/
    └── src/main/java/com/example/notificationservice/
        ├── controller/
        ├── service/
        ├── Repo/
        ├── entity/
        ├── dto/
        └── config/
```

---

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8
- RabbitMQ (running locally on default port `5672`)

---

## Database Setup

Create these databases in MySQL before starting the services:

```sql
CREATE DATABASE customer_db;
CREATE DATABASE accounts_db;
CREATE DATABASE transaction_db;
CREATE DATABASE notification_db;
```

Tables are created automatically by Hibernate on first startup (`ddl-auto: update`).

---

## Environment Variables

Credentials are not hardcoded. Set these environment variables before running the services:

```bash
export DB_PASSWORD=your_mysql_password
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_gmail_app_password
```

To make these permanent on Mac, add them to `~/.zshrc` and run `source ~/.zshrc`.

> For Gmail, generate an **App Password** from Google Account → Security → App Passwords (requires 2FA enabled). Use the 16-character code as `MAIL_PASSWORD`.

---

## Configuration

Each service reads credentials from environment variables. The relevant config entries are:

**Database** (`application.yml` in all services except Eureka and Gateway):

```yaml
spring:
  datasource:
    username: root
    password: ${DB_PASSWORD}
```

**Email** (`application.properties` in Notification Service):

```properties
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

---

## Running the Project

Start services **in this order** — each depends on the one before it:

**1. Eureka Server**

```bash
cd Eureka-server
mvn spring-boot:run
```

Verify: http://localhost:8761

**2. API Gateway**

```bash
cd Api-gateway
mvn spring-boot:run
```

**3. Customer Service**

```bash
cd CustomerServices
mvn spring-boot:run
```

**4. Account Service**

```bash
cd Accountservices
mvn spring-boot:run
```

**5. Transaction Service**

```bash
cd TransactionService
mvn spring-boot:run
```

**6. Notification Service**

```bash
cd Notification-Service
mvn spring-boot:run
```

After all services start, they will all appear registered at: http://localhost:8761

All requests go through the gateway at **http://localhost:8080**

---

## Sample API Calls

All requests go to port `8080` (gateway). Use Postman or curl.

**Create a customer**

```http
POST http://localhost:8080/customers
Content-Type: application/json

{
  "firstName": "Akhil",
  "lastName": "Reddy",
  "gender": "Male",
  "address": "Hyderabad",
  "phoneNumber": "9999999999",
  "email": "akhil@example.com"
}
```

**Create an account**

```http
POST http://localhost:8080/accounts
Content-Type: application/json

{
  "accountType": "SAVINGS",
  "customerId": 1
}
```

**Transfer money**

```http
POST http://localhost:8080/transactions/transfer
Content-Type: application/json

{
  "fromAccount": "ACC1716234567890",
  "toAccount": "ACC1716234567999",
  "amount": 500
}
```

**Get transaction history**

```http
GET http://localhost:8080/transactions/account/ACC1716234567890
```

---

## Swagger UI

Account Service exposes interactive API docs at:
**http://localhost:8001/swagger-ui.html**

---

## Potential Enhancements

- [ ] Spring Security with JWT authentication
- [ ] Input validation on request DTOs
- [ ] Global exception handling across all services
- [ ] Resilience4j circuit breakers on Feign clients
- [ ] Caffeine caching for customer data in Transaction Service
- [ ] Pagination on list endpoints
- [ ] Docker + docker-compose for one-command startup

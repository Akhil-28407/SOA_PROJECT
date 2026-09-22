# CivicVote Technologies — Tamper-Resistant Digital Election & Vote Auditing System

## 🗳️ Project Overview

A secure digital election platform built with a **Spring Boot microservices architecture** for organizational voting. The system guarantees strict voter identity verification, one-vote-per-user integrity, anonymous ballot processing, and transparent result calculation.

---

## 🏗️ Architecture

```
                                Eureka Server :8761
                   (All microservices register here dynamically)
                                         |
     +-------------------+---------------+-------------------+
     |                   |                                   |
     v                   v                                   v
+-----------+   +-----------------+                 +-----------------+
|   Auth    |   |    Election     |                 |     Voting      |
|  Service  |   |     Service     |                 |     Service     |
|   :8081   |   |      :8082      |                 |      :8083      |
+-----------+   +--------+--------+                 +--------+--------+
                         ^                                   |
                         |           OpenFeign               | OpenFeign
                         +───────────────────────────────────+
                         |                                   |
                         v                                   v
                Candidate Lookup                     +---------------+
                         ^                           |    Result     |
                         |                           |    Service     |
                         +───────────────────────────+     :8084     |
                                                     +---------------+
```

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 3.3.4 |
| Database | PostgreSQL 16 (Spring Data JPA) |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Service Discovery | Spring Cloud Netflix Eureka |
| Inter-Service Communication | Spring Cloud OpenFeign |
| Testing | JUnit 5, Mockito, Spring Boot Test |

---

## 📦 Microservices

| Service | Port | Database | Responsibility |
|---|---|---|---|
| **Eureka Server** | 8761 | — | Service Discovery & Registry |
| **Auth Service** | 8081 | `civicvote_auth` | User Registration, Login, JWT Generation (BCrypt) |
| **Election Service** | 8082 | `civicvote_election` | Election & Candidate Management, Admin Role Enforcement |
| **Voting Service** | 8083 | `civicvote_voting` | Ballot Casting, Duplicate Vote Prevention (`UNIQUE(user_id, election_id)`) |
| **Result Service** | 8084 | `civicvote_result` | Anonymous Vote Counting, Result & Winner Computation |

---

## 🚀 Quick Start

### 1. Create PostgreSQL Databases
```sql
CREATE DATABASE civicvote_auth;
CREATE DATABASE civicvote_election;
CREATE DATABASE civicvote_voting;
CREATE DATABASE civicvote_result;
```

### 2. Start Services (in order)
```bash
# Terminal 1 — Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2 — Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 3 — Election Service
cd election-service && mvn spring-boot:run

# Terminal 4 — Voting Service
cd voting-service && mvn spring-boot:run

# Terminal 5 — Result Service
cd result-service && mvn spring-boot:run
```

### 3. Verify
* Eureka Dashboard: `http://localhost:8761`
* Verify that `AUTH-SERVICE`, `ELECTION-SERVICE`, `VOTING-SERVICE`, and `RESULT-SERVICE` appear registered.

---

## 🔐 Security & JWT Authentication

* **Stateless JWT**: Signed using HMAC-SHA256 containing `userId`, `username`, `role`, and expiration timestamp.
* **Direct Microservice Verification**: Each microservice verifies incoming `Authorization: Bearer <token>` requests using its own `JwtUtil` and `JwtAuthenticationFilter`.
* **BCrypt Password Hashing**: Passwords are never stored in plain text.
* **Role-Based Access Control**:
  * `ADMIN`: Can create elections (`POST /elections`), add candidates (`POST /elections/{id}/candidates`), view results.
  * `VOTER`: Can browse elections (`GET /elections`), cast vote (`POST /votes`). Non-admins receive `403 Forbidden` on admin endpoints.
* **Tamper-Resistant Identity**: `VotingController` extracts the voter identity directly from verified JWT claims in the Spring Security context.
* **Anonymous Balloting**: Inter-service communication from Voting Service to Result Service transmits only `{electionId, candidateId}` via OpenFeign without any voter identity.

---

## 📋 API Endpoints

### Auth Service (`:8081`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register new user (BCrypt hashing) |
| POST | `/auth/login` | Public | Authenticate user and receive JWT |

### Election Service (`:8082`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/elections` | ADMIN | Create new election |
| GET | `/elections` | Authenticated | List all elections with candidates |
| GET | `/elections/{id}` | Authenticated | Get election details |
| POST | `/elections/{id}/candidates` | ADMIN | Add candidate to election |
| GET | `/elections/{id}/candidates` | Authenticated | List candidates for election |
| GET | `/elections/{id}/status` | Authenticated | Get dynamic election status |

### Voting Service (`:8083`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/votes` | VOTER / ADMIN | Cast ballot (prevents duplicate vote) |
| GET | `/votes/status/{electionId}` | VOTER / ADMIN | Check if authenticated user has voted |

### Result Service (`:8084`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/results/vote` | Internal | Record vote tally from Voting Service |
| GET | `/results/{electionId}` | Authenticated | View vote counts and percentages |
| GET | `/results/{electionId}/winner` | Authenticated | Determine winning candidate |

---

## 🧪 Demonstration Guide

For detailed step-by-step review instructions, curl commands, and expected JSON outputs for Rubrics 1, 2, and 3, see [`docs/rubric-demo-guide.md`](docs/rubric-demo-guide.md).

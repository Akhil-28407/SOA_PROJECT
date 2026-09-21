# CivicVote Technologies — Tamper-Resistant Digital Election & Vote Auditing System

## 🗳️ Project Overview

A secure digital election platform built with **Spring Boot microservices architecture** for organizational voting. The system guarantees strict voter identity verification, one-vote-per-user integrity, anonymous ballot processing, and transparent result calculation.

**Company:** CivicVote Technologies

---

## 🏗️ Architecture

```
                    +----------------------+
                    |      Frontend        |
                    | HTML/CSS/JavaScript  |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |     API Gateway      |
                    |        :8080         |
                    +----------+-----------+
                               |
               +---------------+---------------+
               |               |               |
               v               v               v
        +-----------+   +-------------+  +-------------+
        |   Auth    |   |  Election   |  |   Voting    |
        |  Service  |   |   Service   |  |   Service   |
        |   :8081   |   |    :8082    |  | :8083/:8093 |
        +-----------+   +-------------+  +------+------+
                                                |
                                                | OpenFeign
                                                v
                                        +---------------+
                                        |    Result     |
                                        |    Service    |
                                        |     :8084     |
                                        +---------------+

                       Eureka Server :8761
                  (All services register here)
```

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| Backend | Java 17, Spring Boot 3.3.4 |
| Database | PostgreSQL 16 |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Inter-Service Communication | Spring Cloud OpenFeign |
| Load Balancing | Spring Cloud LoadBalancer |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Testing | JUnit 5, Mockito, MockMvc |
| Deployment | Docker, Docker Compose |

---

## 📦 Microservices

| Service | Port | Database | Responsibility |
|---------|------|----------|---------------|
| Eureka Server | 8761 | — | Service Discovery |
| API Gateway | 8080 | — | Routing, JWT Filter, Load Balancing |
| Auth Service | 8081 | civicvote_auth | Registration, Login, JWT |
| Election Service | 8082 | civicvote_election | Election & Candidate CRUD |
| Voting Service | 8083/8093 | civicvote_voting | Vote Casting, Validation |
| Result Service | 8084 | civicvote_result | Vote Counting, Results |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 16+

### 1. Create Databases
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

# Terminal 4 — Voting Service (Instance 1)
cd voting-service && mvn spring-boot:run

# Terminal 5 — Voting Service (Instance 2 — Load Balancing)
cd voting-service && SERVER_PORT=8093 mvn spring-boot:run

# Terminal 6 — Result Service
cd result-service && mvn spring-boot:run

# Terminal 7 — API Gateway
cd api-gateway && mvn spring-boot:run
```

### 3. Verify
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Frontend: Open `frontend/index.html` in your browser

---

## 🔐 Security Features

- **JWT Authentication** — Stateless token-based auth with configurable secret
- **BCrypt Password Hashing** — Passwords never stored in plain text
- **Role-Based Authorization** — ADMIN and VOTER roles
- **Gateway JWT Filter** — Global authentication at the API Gateway
- **Anonymous Ballots** — Voter identity never reaches the Result Service
- **One-Vote-Per-User** — Application-level + PostgreSQL UNIQUE constraint

---

## 📋 API Endpoints

### Auth Service
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | /auth/register | Public | Register user |
| POST | /auth/login | Public | Login and get JWT |

### Election Service
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | /elections | ADMIN | Create election |
| GET | /elections | All | List elections |
| GET | /elections/{id} | All | Get election |
| PUT | /elections/{id} | ADMIN | Update election |
| DELETE | /elections/{id} | ADMIN | Delete election |
| POST | /elections/{id}/candidates | ADMIN | Add candidate |
| GET | /elections/{id}/candidates | All | List candidates |
| GET | /elections/{id}/status | All | Get status |

### Voting Service
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | /votes | VOTER | Cast vote |
| GET | /votes/status/{electionId} | VOTER | Check if voted |

### Result Service
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | /results/vote | Internal | Record vote (from Voting Service) |
| GET | /results/{electionId} | All | Get results |
| GET | /results/{electionId}/winner | All | Get winner |

---

## 🧪 Demo Flow

1. **Eureka** — Open http://localhost:8761, verify all services registered
2. **Admin Login** — Register as ADMIN, login
3. **Create Election** — "Student Council Election 2026", add 3 candidates
4. **Voter Login** — Register as VOTER, login
5. **Cast Vote** — Select candidate, vote → "Vote successfully recorded"
6. **Duplicate Vote** — Try again → 409 Conflict
7. **View Results** — See vote counts, percentages, winner
8. **Security Demo** — Call API without JWT → 401; VOTER on admin endpoint → 403

---

## 📁 Project Structure

```
civicvote/
├── eureka-server/          # Service Discovery
├── api-gateway/            # API Gateway + JWT Filter
├── auth-service/           # Authentication + JWT
├── election-service/       # Election Management
├── voting-service/         # Vote Casting
├── result-service/         # Result Calculation
├── frontend/               # HTML/CSS/JS UI
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── admin-dashboard.html
│   ├── elections.html
│   ├── vote.html
│   ├── results.html
│   ├── css/style.css
│   └── js/
├── docker-compose.yml
├── init-db.sh
├── docs/
└── README.md
```

---

## 👨‍💻 Authors

CivicVote Technologies Team

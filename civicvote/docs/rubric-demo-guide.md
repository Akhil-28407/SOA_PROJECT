# CivicVote Technologies — College Review Demonstration Guide

This guide provides the exact demonstration steps, commands, and expected outputs required for **Rubric 1**, **Rubric 2**, and **Rubric 3** evaluation.

---

## Service Ports & Architecture Summary

| Component | Port | Technology | Key Responsibility |
|---|---|---|---|
| **Eureka Server** | `8761` | Spring Cloud Netflix Eureka | Service Discovery & Registry |
| **Auth Service** | `8081` | Spring Boot, Spring Security, JPA | User Registration, Login, JWT Generation (BCrypt) |
| **Election Service** | `8082` | Spring Boot, Spring Data JPA, JWT Filter | Election creation, candidate management, Role validation (`ROLE_ADMIN`) |
| **Voting Service** | `8083` | Spring Boot, OpenFeign, JPA, JWT Filter | Ballot casting, duplicate vote prevention (`UNIQUE(user_id, election_id)`) |
| **Result Service** | `8084` | Spring Boot, Spring Data JPA, JWT Filter | Anonymous vote tallying, result computation |

---

# RUBRIC 1: Problem Analysis and Requirements

### Review Points to Highlight:
1. **Requirements Document**: Located at [`civicvote/docs/requirements.md`](file:///Users/akhilbulusu/Documents/KLU/2026-2027/SOA_MICROSERVICES/Project/civicvote/docs/requirements.md).
2. **Problems Solved**:
   * Voter verification & impersonation
   * Duplicate voting
   * Manual result miscalculation
   * Ballot box tampering & lack of transparency
   * Centralized administrative bottlenecks
3. **Core Actors**: `ADMIN` and `VOTER`.
4. **Traceability**: See Section 6 of `requirements.md` for the direct Requirement-to-Feature mapping table.

---

# RUBRIC 2: Microservice Identification & Service Discovery

### Eureka Service Discovery Dashboard
* **Dashboard URL**: `http://localhost:8761`
* Open `http://localhost:8761` in any browser during the review to show all microservices registered under **Instances currently registered with Eureka**:
  * `AUTH-SERVICE` (port 8081)
  * `ELECTION-SERVICE` (port 8082)
  * `VOTING-SERVICE` (port 8083)
  * `RESULT-SERVICE` (port 8084)

### Inter-Service Communication via Eureka:
* Microservices discover each other dynamically via Eureka without hardcoding IP addresses or ports:
  * In `voting-service`:
    * Calls `ELECTION-SERVICE` via `@FeignClient(name = "ELECTION-SERVICE")` to verify election status and candidate eligibility.
    * Calls `RESULT-SERVICE` via `@FeignClient(name = "RESULT-SERVICE")` to dispatch anonymous vote counts.
  * In `result-service`:
    * Calls `ELECTION-SERVICE` via `@FeignClient(name = "ELECTION-SERVICE")` to resolve candidate names when computing results.

---

# RUBRIC 3: JWT Authentication & Role-Based Access Control

### Authentication Flow

```text
Register User (BCrypt password hash stored in PostgreSQL civicvote_auth)
   ↓
Login with credentials (POST http://localhost:8081/auth/login)
   ↓
Auth Service validates credentials (BCrypt.matches)
   ↓
Auth Service generates signed HMAC-SHA256 JWT
   ↓
JWT sent in response body
   ↓
Client sends Bearer token: Authorization: Bearer <JWT>
   ↓
Each microservice directly validates JWT signature & expiration using JwtUtil
   ↓
Spring Security SecurityContextHolder sets Role:
   - ADMIN: Allowed to create elections, add candidates, view results
   - VOTER: Allowed to view elections, cast vote
```

### JWT Claims Structure
Each JWT contains the following payload claims:
```json
{
  "sub": "user@example.com",
  "userId": 1,
  "username": "alice",
  "role": "VOTER",
  "iat": 1726970000,
  "exp": 1727056400
}
```

---

# STEP-BY-STEP REVIEW DEMONSTRATION SCRIPT

Commands are executed directly against the respective microservice ports.

### Step 1: Register an Administrator (Port 8081)

```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_user",
    "email": "admin@civicvote.edu",
    "password": "AdminPassword123!",
    "role": "ADMIN"
  }'
```

**Expected Response (HTTP 201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "admin_user",
  "email": "admin@civicvote.edu",
  "role": "ADMIN"
}
```
> Export token: `export ADMIN_TOKEN="<token_from_above>"`

---

### Step 2: Register a Voter (Port 8081)

```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@civicvote.edu",
    "password": "VoterPassword123!",
    "role": "VOTER"
  }'
```

**Expected Response (HTTP 201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 2,
  "username": "john_doe",
  "email": "john@civicvote.edu",
  "role": "VOTER"
}
```
> Export token: `export VOTER_TOKEN="<token_from_above>"`

---

### Step 3: Demonstrate Admin Login (Port 8081)

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin_user",
    "password": "AdminPassword123!"
  }'
```

**Expected Response (HTTP 200 OK):**
Returns the validated user profile with signed JWT.

---

### Step 4: Admin Creates an Election (Port 8082 — Protected Admin API)

```bash
curl -X POST http://localhost:8082/elections \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "title": "University Student Council 2026",
    "description": "Annual student body presidential election",
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-10-31T23:59:59"
  }'
```

**Expected Response (HTTP 201 Created):**
```json
{
  "electionId": 1,
  "title": "University Student Council 2026",
  "description": "Annual student body presidential election",
  "startDate": "2026-09-01T00:00:00",
  "endDate": "2026-10-31T23:59:59",
  "status": "ACTIVE",
  "candidates": []
}
```

---

### Step 5: Admin Adds Candidates to the Election (Port 8082)

```bash
# Add Candidate 1
curl -X POST http://localhost:8082/elections/1/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "candidateName": "Alice Johnson",
    "description": "Platform: Digital campus transparency"
  }'

# Add Candidate 2
curl -X POST http://localhost:8082/elections/1/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "candidateName": "Bob Smith",
    "description": "Platform: Sustainable student infrastructure"
  }'
```

---

### Step 6: Voter Views Elections & Candidates (Port 8082)

```bash
curl -X GET http://localhost:8082/elections \
  -H "Authorization: Bearer $VOTER_TOKEN"
```

**Expected Response (HTTP 200 OK):**
Shows the active election and the two candidates (`candidateId: 1` and `candidateId: 2`).

---

### Step 7: Voter Casts Ballot (Port 8083)

```bash
curl -X POST http://localhost:8083/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VOTER_TOKEN" \
  -d '{
    "electionId": 1,
    "candidateId": 1
  }'
```

**Expected Response (HTTP 201 Created):**
```json
{
  "voteId": 1,
  "electionId": 1,
  "candidateId": 1,
  "castAt": "2026-09-22T10:00:00",
  "message": "Vote successfully recorded"
}
```

---

### Step 8: Demonstrate Duplicate Vote Prevention (Port 8083)

Attempting to vote again in the same election with the same voter token:

```bash
curl -X POST http://localhost:8083/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VOTER_TOKEN" \
  -d '{
    "electionId": 1,
    "candidateId": 2
  }'
```

**Expected Response (HTTP 409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "User has already voted in this election"
}
```
> The unique constraint `uk_user_election` on `(user_id, election_id)` strictly enforces one vote per user per election at the database level.

---

### Step 9: View Election Results (Port 8084)

```bash
curl -X GET http://localhost:8084/results/1 \
  -H "Authorization: Bearer $VOTER_TOKEN"
```

**Expected Response (HTTP 200 OK):**
```json
[
  {
    "candidateId": 1,
    "candidateName": "Alice Johnson",
    "voteCount": 1,
    "percentage": 100.0
  },
  {
    "candidateId": 2,
    "candidateName": "Bob Smith",
    "voteCount": 0,
    "percentage": 0.0
  }
]
```

To view the winning candidate:
```bash
curl -X GET http://localhost:8084/results/1/winner \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### Step 10: Demonstrate Security & Role Enforcement

#### 10.1 Voter attempting Admin Action on Election Service (Role Protection)
```bash
curl -X POST http://localhost:8082/elections \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VOTER_TOKEN" \
  -d '{
    "title": "Hacked Election",
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-10-31T23:59:59"
  }'
```
**Expected Response: HTTP 403 Forbidden (`"message": "Admin access required"`).**

#### 10.2 Request without JWT (Unauthorized)
```bash
curl -X GET http://localhost:8082/elections
```
**Expected Response: HTTP 401 Unauthorized (`"message": "Missing or invalid JWT token"`).**

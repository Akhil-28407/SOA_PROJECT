# Problem Analysis and Requirement Specification

## Tamper-Resistant Digital Election & Vote Auditing System

---

## 1. Problem Statement

Traditional and manual organizational elections (e.g., student councils, university governance, club committees) rely on paper ballots or ad-hoc manual procedures that suffer from several recurring problems:

* **Voter Verification**: Difficult and error-prone verification of eligible voter identities at the polling station.
* **Duplicate Voting**: Inadequate safeguards allowing malicious or accidental double-voting by individuals.
* **Result Calculation**: Laborious manual tallying of physical ballots prone to human miscounting and extended delays.
* **Security**: Physical ballot boxes are susceptible to stuffing, tampering, loss, or unauthorized access.
* **Transparency**: Voters cannot verify if their vote was properly received and counted without risking identity exposure.
* **Centralized Management**: Administrative bottlenecks and lack of audit trails create disputes over electoral legitimacy.

CivicVote resolves these problems by implementing a decoupled, microservices-based digital election platform with cryptographic authentication and automated auditing.

---

## 2. Functional Requirements

* **Admin Login**: Election administrators can securely log in using credentials to obtain an authorized JWT.
* **Voter Login**: Registered voters can securely log in using credentials to obtain an authorized JWT.
* **Create Election**: Administrators can define and publish new elections with specific titles, start dates, and end dates.
* **Add Candidates**: Administrators can nominate and add candidates to specific elections.
* **View Elections**: Voters and administrators can view active and upcoming elections along with candidate slates.
* **Cast Vote**: Authenticated voters can cast a vote for an approved candidate in an active election.
* **Prevent Duplicate Vote**: The system guarantees one-person-one-vote; duplicate vote attempts for the same election are strictly rejected.
* **Calculate Results**: The system tallies all submitted votes per candidate and dynamically computes percentages.
* **Admin Monitors Results**: Administrators and observers can view real-time election tallies and winner determinations.

---

## 3. Non-Functional Requirements

* **Security**: Role-based access control (RBAC) enforced via stateless HMAC-SHA256 signed JWTs; passwords hashed using BCrypt.
* **Data Integrity**: Database-level unique constraints (`user_id + election_id`) and ACID transactions guarantee ballot uniqueness and tamper resistance.
* **Availability**: High availability achieved through Netflix Eureka service discovery and independent stateless microservices.
* **Scalability**: Decoupled architecture allows independent horizontal scaling of high-throughput services (e.g., Voting Service).
* **Maintainability**: Clean modular separation of concerns across single-responsibility microservices with clear REST API contracts.

---

## 4. Actors

* **ADMIN**: Election administrator responsible for election setup, candidate onboarding, and monitoring results.
* **VOTER**: Eligible organizational member authorized to view elections and cast exactly one ballot per election.

---

## 5. Basic Use Cases

### Admin Use Cases

```text
Admin → Login
Admin → Create Election
Admin → Add Candidates
Admin → View Results
```

* **Admin → Login**: Administrator enters credentials; upon successful BCrypt verification, receives a JWT with `role: ADMIN`.
* **Admin → Create Election**: Administrator submits election details (`title`, `startDate`, `endDate`) to initialize an election cycle.
* **Admin → Add Candidates**: Administrator links candidate names and descriptions to an active or upcoming election.
* **Admin → View Results**: Administrator queries the Result Service to view vote counts, percentages, and winning candidates.

### Voter Use Cases

```text
Voter → Login
Voter → View Election
Voter → Cast Vote
Voter → Cannot Vote Twice
```

* **Voter → Login**: Voter authenticates with username/password; receives a signed JWT with `role: VOTER`.
* **Voter → View Election**: Voter browses current elections and reviews candidate profiles.
* **Voter → Cast Vote**: Voter selects a candidate and submits a ballot during the active voting window.
* **Voter → Cannot Vote Twice**: If the voter attempts to submit a second ballot for the same election, the system detects the existing record and rejects the request with HTTP 409 Conflict.

---

## 6. Requirement-to-Feature Mapping Table

| Requirement ID | Requirement Name | Actor | Microservice Responsible | REST API Endpoint | Database Entity / Constraint |
|---|---|---|---|---|---|
| **FR-01** | Admin Login | ADMIN | `auth-service` (:8081) | `POST /auth/login` | `users` (role = ADMIN) |
| **FR-02** | Voter Login | VOTER | `auth-service` (:8081) | `POST /auth/login` | `users` (role = VOTER) |
| **FR-03** | Create Election | ADMIN | `election-service` (:8082) | `POST /elections` | `elections` |
| **FR-04** | Add Candidates | ADMIN | `election-service` (:8082) | `POST /elections/{id}/candidates` | `candidates` |
| **FR-05** | View Elections | VOTER / ADMIN | `election-service` (:8082) | `GET /elections`, `GET /elections/{id}` | `elections`, `candidates` |
| **FR-06** | Cast Vote | VOTER | `voting-service` (:8083) | `POST /votes` | `votes` |
| **FR-07** | Prevent Duplicate Vote | VOTER | `voting-service` (:8083) | `POST /votes` | `UNIQUE(user_id, election_id)` |
| **FR-08** | Calculate Results | SYSTEM | `result-service` (:8084) | `GET /results/{electionId}` | `results` |
| **FR-09** | Admin Monitors Results | ADMIN / OBSERVER | `result-service` (:8084) | `GET /results/{electionId}/winner` | `results` |

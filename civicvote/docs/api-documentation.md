# API Documentation — CivicVote Technologies

## Base URL
All requests go through the API Gateway:
```
http://localhost:8080
```

---

## Auth Service

### POST /auth/register
Register a new user.

**Request:**
```json
{
  "username": "student01",
  "email": "student01@example.com",
  "password": "Password@123",
  "role": "VOTER"
}
```

**Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "student01",
  "role": "VOTER",
  "userId": 1
}
```

### POST /auth/login
Authenticate and receive JWT.

**Request:**
```json
{
  "email": "student01@example.com",
  "password": "Password@123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "student01",
  "role": "VOTER",
  "userId": 1
}
```

---

## Election Service

### POST /elections *(ADMIN only)*
```json
{
  "title": "Student Council Election 2026",
  "description": "CivicVote Student Council Election",
  "startDate": "2026-09-21T09:00:00",
  "endDate": "2026-09-21T17:00:00"
}
```

### GET /elections
Returns all elections with status and candidates.

### GET /elections/{id}
Returns single election detail.

### PUT /elections/{id} *(ADMIN only)*
Update election details.

### DELETE /elections/{id} *(ADMIN only)*
Delete election and cascade to candidates.

### POST /elections/{id}/candidates *(ADMIN only)*
```json
{
  "candidateName": "Candidate A",
  "description": "Experienced student leader"
}
```

### GET /elections/{id}/candidates
Returns candidates for an election.

### GET /elections/{id}/status
```json
{
  "electionId": 1,
  "status": "ACTIVE"
}
```

---

## Voting Service

### POST /votes *(Authenticated)*
```json
{
  "electionId": 1,
  "candidateId": 2
}
```

**Success (201):**
```json
{
  "voteId": 1,
  "electionId": 1,
  "candidateId": 2,
  "castAt": "2026-09-21T10:30:00",
  "message": "Vote successfully recorded"
}
```

**Duplicate (409):**
```json
{
  "timestamp": "2026-09-21T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "User has already voted in this election"
}
```

### GET /votes/status/{electionId} *(Authenticated)*
```json
{
  "electionId": 1,
  "hasVoted": true
}
```

---

## Result Service

### GET /results/{electionId}
```json
[
  {
    "candidateId": 1,
    "candidateName": "Candidate A",
    "voteCount": 125,
    "percentage": 55.56
  },
  {
    "candidateId": 2,
    "candidateName": "Candidate B",
    "voteCount": 100,
    "percentage": 44.44
  }
]
```

### GET /results/{electionId}/winner
```json
{
  "candidateId": 1,
  "candidateName": "Candidate A",
  "voteCount": 125,
  "percentage": 55.56
}
```

### POST /results/vote *(Internal — called by Voting Service)*
```json
{
  "electionId": 1,
  "candidateId": 2
}
```

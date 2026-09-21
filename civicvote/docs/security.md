# Security Architecture — CivicVote Technologies

## JWT Authentication Flow (Rubric 3)

```
1. User submits credentials (email + password)
         ↓
2. Auth Service validates credentials
         ↓
3. BCrypt verifies password hash
         ↓
4. Auth Service generates JWT containing:
   - userId
   - username
   - email (as subject)
   - role (ADMIN/VOTER)
   - issued time
   - expiration time (24 hours)
         ↓
5. JWT returned to Frontend
         ↓
6. Frontend stores JWT in localStorage
         ↓
7. All subsequent requests include:
   Authorization: Bearer <JWT>
         ↓
8. API Gateway validates JWT:
   - Verifies signature
   - Checks expiration
   - Extracts claims
   - Adds X-User-Id, X-User-Role headers
         ↓
9. Downstream service receives user context
   via headers (not JWT directly)
```

## Role-Based Authorization

| Endpoint | ADMIN | VOTER |
|----------|-------|-------|
| POST /elections | ✅ | ❌ (403) |
| PUT /elections/{id} | ✅ | ❌ (403) |
| DELETE /elections/{id} | ✅ | ❌ (403) |
| POST /elections/{id}/candidates | ✅ | ❌ (403) |
| GET /elections | ✅ | ✅ |
| POST /votes | ✅ | ✅ |
| GET /results/{id} | ✅ | ✅ |

## Password Security

- **BCrypt** with default strength (10 rounds)
- Passwords NEVER stored in plain text
- Passwords NEVER logged
- Passwords NEVER returned in API responses

## Anonymous Ballot Processing

```
Voter Identity          Anonymous Data
      |                       |
      v                       v
Voting Service ──────> Result Service
(knows voter)         (no voter info)
```

The Voting Service sends ONLY `{electionId, candidateId}` to the Result Service via OpenFeign. No username, email, JWT, or personal voter information is transmitted.

## Security Responses

| Scenario | Status | Response |
|----------|--------|----------|
| No JWT token | 401 | `{"error":"Unauthorized","message":"Missing or invalid Authorization header"}` |
| Expired JWT | 401 | `{"error":"Unauthorized","message":"Invalid or expired JWT token"}` |
| VOTER accessing ADMIN endpoint | 403 | `{"error":"Forbidden","message":"Admin access required"}` |
| Duplicate vote | 409 | `{"error":"Conflict","message":"User has already voted in this election"}` |

# Database Architecture — CivicVote Technologies

## Database Per Service

Each microservice owns its own PostgreSQL database, demonstrating proper microservice data ownership.

## Schema Definitions

### civicvote_auth

```sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'VOTER',
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL
);
```

### civicvote_election

```sql
CREATE TABLE elections (
    election_id     BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    start_date      TIMESTAMP NOT NULL,
    end_date        TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);

CREATE TABLE candidates (
    candidate_id    BIGSERIAL PRIMARY KEY,
    election_id     BIGINT NOT NULL REFERENCES elections(election_id),
    candidate_name  VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL
);
```

### civicvote_voting

```sql
CREATE TABLE votes (
    vote_id           BIGSERIAL PRIMARY KEY,
    voter_reference   VARCHAR(255) NOT NULL,
    election_id       BIGINT NOT NULL,
    candidate_id      BIGINT NOT NULL,
    cast_at           TIMESTAMP NOT NULL,
    CONSTRAINT uk_voter_election UNIQUE (voter_reference, election_id)
);
```

**Key constraint:** `UNIQUE(voter_reference, election_id)` — ensures one vote per user per election at the database level.

### civicvote_result

```sql
CREATE TABLE results (
    result_id       BIGSERIAL PRIMARY KEY,
    election_id     BIGINT NOT NULL,
    candidate_id    BIGINT NOT NULL,
    vote_count      BIGINT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_election_candidate UNIQUE (election_id, candidate_id)
);
```

## Entity Relationships

```
users (civicvote_auth)
  |
  | voter_reference
  v
votes (civicvote_voting)  ──→ results (civicvote_result)
  |                              |
  | election_id                  | election_id
  | candidate_id                 | candidate_id
  v                              v
elections (civicvote_election)
  |
  | 1:Many
  v
candidates (civicvote_election)
```

## Data Integrity Guarantees

| Mechanism | Purpose |
|-----------|---------|
| `PRIMARY KEY` | Unique row identification |
| `FOREIGN KEY` (candidates → elections) | Referential integrity |
| `UNIQUE(voter_reference, election_id)` | One-vote-per-user |
| `UNIQUE(election_id, candidate_id)` in results | One result row per candidate per election |
| `NOT NULL` constraints | Required fields enforcement |
| `@Transactional` in Java | Atomic operations |

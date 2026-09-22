# Database Architecture — CivicVote Technologies

## Database Per Service

Each microservice owns its own isolated PostgreSQL database, demonstrating proper microservice data ownership and decoupled storage.

---

## Schema Definitions

### 1. civicvote_auth

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

### 2. civicvote_election

```sql
CREATE TABLE elections (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    start_date      TIMESTAMP NOT NULL,
    end_date        TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);

CREATE TABLE candidates (
    id              BIGSERIAL PRIMARY KEY,
    election_id     BIGINT NOT NULL REFERENCES elections(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL
);
```

### 3. civicvote_voting

```sql
CREATE TABLE votes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    election_id     BIGINT NOT NULL,
    candidate_id    BIGINT NOT NULL,
    cast_at         TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_election UNIQUE (user_id, election_id)
);
```

**Key Constraint**: `UNIQUE(user_id, election_id)` — strictly enforces one vote per voter per election at the PostgreSQL database level.

### 4. civicvote_result

```sql
CREATE TABLE results (
    id              BIGSERIAL PRIMARY KEY,
    election_id     BIGINT NOT NULL,
    candidate_id    BIGINT NOT NULL,
    vote_count      BIGINT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_election_candidate UNIQUE (election_id, candidate_id)
);
```

---

## Summary of Schemas

| Service | Database Name | Table Name | Key Columns | Constraints |
|---|---|---|---|---|
| **Auth Service** | `civicvote_auth` | `users` | `id`, `username`, `email`, `password`, `role` | PK(`id`), UNIQUE(`username`), UNIQUE(`email`) |
| **Election Service** | `civicvote_election` | `elections`<br>`candidates` | `id`, `title`, `start_date`, `end_date`<br>`id`, `election_id`, `name` | PK(`id`)<br>PK(`id`), FK(`election_id`) |
| **Voting Service** | `civicvote_voting` | `votes` | `id`, `user_id`, `election_id`, `candidate_id` | PK(`id`), **UNIQUE(`user_id`, `election_id`)** |
| **Result Service** | `civicvote_result` | `results` | `id`, `election_id`, `candidate_id`, `vote_count` | PK(`id`), UNIQUE(`election_id`, `candidate_id`) |

---

## Data Integrity Guarantees

* **Duplicate Vote Prevention**: Guaranteed by `UNIQUE(user_id, election_id)` on the `votes` table.
* **Ballot Privacy**: Voting Service tallies votes in Result Service anonymously via OpenFeign sending only `{electionId, candidateId}`. No `user_id` is passed into `civicvote_result`.
* **ACID Transactions**: Handled via Spring Data JPA and `@Transactional` to prevent partial updates.

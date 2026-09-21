# Viva Questions & Answers — CivicVote Technologies

## 1. Why microservices instead of a monolithic architecture?
Microservices allow independent deployment, scaling, and fault isolation. If the Result Service fails, voters can still register and elections can still be managed. Each service can be developed and deployed independently by different teams.

## 2. Why Spring Boot?
Spring Boot provides auto-configuration, embedded servers, and a rich ecosystem (Spring Security, Spring Data JPA, Spring Cloud) that accelerates development. It's the industry standard for building production-ready Java microservices.

## 3. Why PostgreSQL instead of MySQL?
PostgreSQL offers superior data integrity features, including better constraint enforcement, transactional DDL, and advanced data types. The UNIQUE constraint with our composite key pattern works reliably for preventing duplicate votes.

## 4. Why separate databases for each service?
Database-per-service ensures loose coupling. Each service owns its data schema and can evolve independently. No service directly accesses another service's tables, preventing tight coupling at the data layer.

## 5. Why Eureka for service discovery?
Eureka provides dynamic service registration and discovery. Services register themselves on startup, and the API Gateway can discover service instances without hard-coded IP addresses. This enables dynamic scaling and zero-downtime deployments.

## 6. Why an API Gateway?
The API Gateway provides a single entry point for all client requests. It handles cross-cutting concerns like JWT validation, CORS, routing, and load balancing. The frontend only needs to know one URL (localhost:8080).

## 7. Why JWT instead of sessions?
JWT is stateless — no server-side session storage is needed. This makes horizontal scaling easy because any instance can validate the token. The token contains all necessary user information (userId, role, email).

## 8. Why BCrypt for password hashing?
BCrypt is a one-way adaptive hashing function with built-in salt. It's intentionally slow to prevent brute-force attacks. The cost factor can be increased as hardware improves.

## 9. How does JWT work in our system?
User logs in → Auth Service validates credentials with BCrypt → generates JWT with userId, email, username, role, expiry → Frontend stores token → sends `Authorization: Bearer <token>` → Gateway validates signature and extracts claims → passes user info as headers to downstream services.

## 10. What is service discovery?
Service discovery allows services to find each other dynamically. When a service starts, it registers with Eureka (IP + port). When the Gateway needs to route a request, it queries Eureka for available instances of the target service.

## 11. What is load balancing?
Load balancing distributes requests across multiple instances of a service. We have 2 Voting Service instances (8083, 8093). Spring Cloud LoadBalancer uses round-robin to distribute requests, improving throughput and availability.

## 12. What is OpenFeign?
OpenFeign is a declarative REST client. Instead of writing HTTP client code manually, we define a Java interface with annotations, and Feign generates the implementation. The Voting Service uses Feign to call the Result Service.

## 13. How is one-vote-per-user enforced?
Two layers: (1) **Application-level** — before saving, we check `existsByVoterReferenceAndElectionId()`. (2) **Database-level** — PostgreSQL `UNIQUE(voter_reference, election_id)` constraint catches concurrent duplicates that slip past the application check.

## 14. Why use a PostgreSQL UNIQUE constraint?
The UNIQUE constraint provides database-level integrity that cannot be bypassed by application bugs or concurrent requests. Even if two identical vote requests arrive simultaneously, the database will reject the duplicate.

## 15. How are anonymous ballots achieved?
The Voting Service knows who voted (via JWT). But when it calls the Result Service via OpenFeign, it sends **only** `{electionId, candidateId}` — no voter identity. The Result Service never receives or stores who voted for whom.

## 16. How does Voting communicate with Result Service?
Via OpenFeign over HTTP. The Voting Service has a `@FeignClient(name = "RESULT-SERVICE")` interface. Feign uses Eureka to discover the Result Service's address and makes a `POST /results/vote` call with anonymous vote data.

## 17. What happens if Result Service is unavailable?
The vote is saved in the Voting Service database, but the `@Transactional` annotation will roll back the entire transaction if the Feign call fails. This prevents silently recording a vote without updating results. In production, we'd use a message queue (Kafka) for eventual consistency.

## 18. How are transactions handled?
`@Transactional` ensures atomicity. In the Voting Service, if the Result Service call fails after saving the vote, the entire transaction rolls back. In the Result Service, vote count increments are atomic.

## 19. How are 401 and 403 different?
**401 Unauthorized** — the user is not authenticated (no JWT or invalid JWT). **403 Forbidden** — the user is authenticated but lacks permission (e.g., a VOTER trying to create an election).

## 20. Why use DTOs instead of exposing JPA entities?
DTOs decouple the API contract from the database schema. We can change the entity without breaking the API. DTOs also prevent accidental exposure of sensitive fields (like password hashes) in API responses.

## 21. Why use centralized exception handling?
`@RestControllerAdvice` provides consistent error response formatting across the entire service. Every error returns a structured JSON with timestamp, status, error type, and message. This improves API usability and debugging.

## 22. How is data integrity maintained?
Multiple layers: (1) Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`) at the API layer. (2) Application-level checks in service methods. (3) Database constraints (`UNIQUE`, `NOT NULL`, `FOREIGN KEY`). (4) `@Transactional` for atomic operations.

## 23. How is scalability achieved?
Microservices can be independently scaled. The Voting Service runs 2 instances for load balancing. New instances register with Eureka automatically. The Gateway's `lb://` scheme distributes load without configuration changes.

## 24. How is the system deployed?
Docker Compose orchestrates all services. Each service has a multi-stage Dockerfile (build with Maven, run with JRE). PostgreSQL is containerized with an init script that creates 4 databases. Services start in dependency order with health checks.

## 25. What are future improvements?
- **Apache Kafka** for event-driven vote processing (instead of synchronous Feign)
- **Blockchain** for immutable audit trail
- **WebSocket** for real-time result updates
- **Biometric authentication** for stronger voter verification
- **Mobile app** for broader accessibility
- **Kubernetes** for container orchestration and auto-scaling

---

# MOOC Certificate Checklist

| Course Name | Platform | Certificate Status | Completion Date | Certificate Link |
|------------|----------|-------------------|-----------------|-----------------|
| | | ☐ Not Started / ☐ In Progress / ☐ Completed | | |
| | | ☐ Not Started / ☐ In Progress / ☐ Completed | | |
| | | ☐ Not Started / ☐ In Progress / ☐ Completed | | |

> **Note:** Fill in with your actual MOOC certificates. Do NOT fabricate certificates — the actual score depends on your real certificates.

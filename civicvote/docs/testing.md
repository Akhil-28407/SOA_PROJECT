# Testing Strategy — CivicVote Technologies

## Unit Test Cases

### Auth Service Tests
| Test | Input | Expected |
|------|-------|----------|
| Successful registration | Valid user data | 201, JWT returned |
| Duplicate email | Existing email | 409 Conflict |
| Duplicate username | Existing username | 409 Conflict |
| Successful login | Valid credentials | 200, JWT returned |
| Invalid password | Wrong password | 401 Unauthorized |
| Invalid email | Non-existent email | 401 Unauthorized |
| Blank email | Empty email field | 400 Bad Request |

### Election Service Tests
| Test | Input | Expected |
|------|-------|----------|
| Create election | Valid election data | 201, election returned |
| Get all elections | — | 200, list |
| Get election by ID | Valid ID | 200, election |
| Get nonexistent election | Invalid ID | 404 Not Found |
| Update election | Valid data | 200, updated |
| Delete election | Valid ID | 204 No Content |
| Add candidate | Valid candidate | 201, candidate returned |
| End before start | Invalid dates | 400 Bad Request |

### Voting Service Tests
| Test | Input | Expected |
|------|-------|----------|
| Successful vote | Active election, valid candidate | 201, success |
| Duplicate vote | Same user, same election | 409 Conflict |
| Closed election | Ended election | 400 Bad Request |
| Invalid candidate | Wrong candidate ID | 400 Bad Request |
| Candidate from another election | Mismatched IDs | 400 Bad Request |

### Result Service Tests
| Test | Input | Expected |
|------|-------|----------|
| Record vote | Vote event | vote_count incremented |
| Get results | Election with votes | Sorted by count, with percentages |
| Get winner | Election with votes | Highest vote candidate |
| No votes | Empty election | 404 or empty list |

## Integration Test Flow

```
Register ADMIN → Login → Get JWT
    ↓
Create Election
    ↓
Add Candidates (3)
    ↓
Register VOTER → Login → Get JWT
    ↓
Cast Vote → 201 Success
    ↓
Try Duplicate Vote → 409 Conflict
    ↓
Get Results → Verify counts
    ↓
No JWT request → 401 Unauthorized
    ↓
VOTER on admin endpoint → 403 Forbidden
```

## Test Commands
```bash
# Run all tests for a service
cd auth-service && mvn test
cd election-service && mvn test
cd voting-service && mvn test
cd result-service && mvn test
```

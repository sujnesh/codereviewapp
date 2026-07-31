# CodeReviewApp

Small Spring Boot backend starter for automated code review workflow experiments.

This repository is intentionally minimal today: a Java 17/Spring Boot 3 service skeleton with Maven wrapper support and an application context test. It is a base for exploring repository/PR ingestion, review generation, and review workflow APIs.

## Current State

- Spring Boot 3.0.4
- Java 17
- Maven wrapper included
- Web and Spring Data REST dependencies
- HAL explorer dependency
- Application context smoke test

## Run Locally

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

## Next Useful Work

- Add endpoints for repository or pull-request submission
- Store review jobs and review results
- Add a queue boundary for long-running review work
- Add integration tests around the first real review flow

## Recruiter Note

This is a starter repo, not the strongest portfolio project. For deeper backend and AI-system work, start with [TokenOps](https://github.com/sujnesh/tokenops), [RepWise](https://github.com/sujnesh/repwise), or [Attunedd](https://github.com/sujnesh/attunedd).

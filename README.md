# Tamper-Resistant Digital Election & Vote Auditing System

## 📌 Project Overview

The **Tamper-Resistant Digital Election & Vote Auditing System** is a Service-Oriented Architecture (SOA) project designed to provide a secure and modular platform for conducting digital elections.

The system uses a **microservice architecture** where authentication, election management, voting, and result management are separated into independent services.

## 🎯 Objectives

- Provide secure user authentication using JWT.
- Manage elections through dedicated services.
- Allow authenticated users to cast votes.
- Maintain election and voting data securely.
- Generate and manage election results.
- Use service discovery for communication between microservices.
- Provide a modular and maintainable SOA-based architecture.

## 🏗️ System Architecture

```text
                         ┌───────────────────┐
                         │      Client       │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │    API Gateway    │
                         └─────────┬─────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
              ▼                    ▼                    ▼
       ┌─────────────┐      ┌──────────────┐     ┌──────────────┐
       │ Auth Service│      │Election      │     │Voting Service│
       │    JWT      │      │Service       │     │              │
       └─────────────┘      └──────────────┘     └──────┬───────┘
              │                    │                    │
              └────────────────────┼────────────────────┘
                                   ▼
                         ┌───────────────────┐
                         │  Result Service   │
                         └───────────────────┘

                         ┌───────────────────┐
                         │   Eureka Server   │
                         │ Service Discovery │
                         └───────────────────┘

                         ┌───────────────────┐
                         │    PostgreSQL     │
                         │     Database      │
                         └───────────────────┘

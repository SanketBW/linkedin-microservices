# linkedin-microservices
LinkedIn distributed platform built with Java, Spring Boot, Spring Cloud, PostgreSQL, Redis and Apache Kafka.

**Features :**
User Service — Registration, authentication, JWT security, and profile management.
Post Service — Create, update, delete, and retrieve posts with pagination.
Connection Service — Send, accept, and manage connection requests.
Engagement Service — Like and unlike posts with duplicate protection.
Notification Service — Asynchronous notifications using Apache Kafka.
API Gateway — Centralized routing, authentication, and rate limiting.
Service Discovery — Dynamic service registration using Netflix Eureka.
Caching — Redis-based caching and performance optimization.
File Uploads — Profile images and post attachments using object storage.

**Technology Stack :**
Java 21,
Spring Boot,
Spring Security and JWT,
Spring Cloud Gateway,
Netflix Eureka,
Spring Data JPA and Hibernate
PostgreSQL,
Redis,
Apache Kafka,
Maven,
Docker and Docker Compose,
JUnit 5, Mockito, and Testcontainers.

**Architecture :**
The application consists of independently deployable microservices that communicate through REST APIs and asynchronous Kafka events. Each business service owns its data, while PostgreSQL provides persistent storage and Redis supports caching.

**Learning Objectives :**
Microservices architecture and service discovery,
REST API design and secure authentication,
Database schema design and transaction management,
Event-driven communication using Kafka,
Distributed systems reliability and idempotency,
Redis caching and performance optimization,
Docker-based development and integration testing.

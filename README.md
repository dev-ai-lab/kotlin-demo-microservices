# Developing microservices with Kotlin

This repository contains the source code used for the blog series "Developing Microservices in Kotlin" (Part I–V). It implements a small example microservices system with four main modules and accompanying deployment artifacts.

Microservices in this repo

- `shop-user-service` — user management and authentication
- `shop-item-service` — item/catalog service
- `shop-order-service` — order processing
- `shop-payment-service` — payment handling
- `shop-deployment` — docker-compose and deployment helpers

This README explains how to build, run, test, and interact with each service on a developer workstation (macOS). It also includes recommended workflows and troubleshooting tips.

---

## Table of contents

- Prerequisites
- Build & run (per-service)
  - Run locally (Gradle + Ktor)
  - Run with Docker (image + docker-compose)
- API quick reference & example curl commands
  - `shop-user-service` example endpoints
- Tests
  - Unit tests
  - Integration tests (custom `integrationTest` source set)
- Postman collections
- Configuration and environment
- Troubleshooting & tips

---

## Prerequisites

Minimum recommended developer environment (what this repo expects):

- macOS with a modern JDK installed (recommended: Java 21). Example to set JAVA_HOME on macOS/zsh:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

- Gradle wrapper is provided; use the wrapper (`./gradlew`) — no system Gradle required.
- Docker & Docker Compose (if you plan to run the docker-compose deployment)
- Recommended: use an IDE with Kotlin support (IntelliJ IDEA) and enable the Gradle wrapper in the IDE.

---

## Build & run

General notes:
- Each microservice is a standard Kotlin/JVM project built with Gradle and uses Ktor for the HTTP server.
- The Gradle plugin and JVM toolchain are configured in each service to use Java 21.

Top-level build: rebuild all modules

```bash
# from repository root
./gradlew clean build
```

This will compile all modules and run the unit tests. The `check` lifecycle also runs the integration tests if configured to do so in a module.


### Run a single microservice locally (development mode)

Each microservice (for example `shop-user-service`) includes an application main entry and can be run with the Gradle application plugin.

Example: run the user service

```bash
# run from repo root
./gradlew :shop-user-service:run
```

The service starts in-process using the Ktor Netty engine. By default most services listen on port `8080` unless overridden in an `application.conf` (HOCON) inside the service's `resources`.

If you prefer to run the generated distribution script (after assembling):

```bash
# build and run the distribution script
./gradlew :shop-user-service:installDist
# then
./shop-user-service/build/install/shop-user-service/bin/shop-user-service
```


### Build Docker image and run with Docker Compose

A docker-based deployment is available under `shop-deployment`. Each service has a `Dockerfile` that can produce an image.

Build a single image and run it (example):

```bash
# build the jar first (from repo root)
./gradlew :shop-user-service:shadowJar
# build docker image (example tag)
docker build -t shop-user-service:local ./shop-user-service
# run image (example)
docker run -p 8080:8080 --env-file ./shop-user-service/.env shop-user-service:local
```

Run the provided docker-compose setup (from `shop-deployment`):

```bash
cd shop-deployment
# bring up the whole stack
docker-compose up --build
```

See `shop-deployment/README.md` for service-specific compose settings and environment variables.

---

## API quick reference & example interactions

The services expose REST endpoints. The exact paths are implemented in each service's routing; below are the most commonly used endpoints for the `shop-user-service` module (examples assume the service listens on `http://localhost:8080`).

Important: the `shop-user-service` uses JSON request/response bodies and JWT for authentication for protected endpoints.

### shop-user-service (common endpoints)

- GET / — root welcome message (plain text)
- GET /health — returns a JSON health object, e.g. `{ "health": "HEALTHY" }`
- POST /api/v1/signup — register a new user
- POST /api/v1/login — login and receive access/refresh tokens
- GET /api/v1/profile — retrieve the user's profile (requires Authorization: Bearer <accessToken>)
- POST /api/v1/profile — update a user's profile (requires Authorization)
- POST /api/v1/token/refresh — refresh access token using refresh token

Example curl flows

1) Signup

```bash
curl -X POST http://localhost:8080/api/v1/signup \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"Password123!"}'
```

2) Login (obtain tokens)

```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"Password123!"}'
# expected json contains accessToken/token and refreshToken/refresh_token
```

3) Call protected endpoint (profile)

```bash
curl -v http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer <accessToken>"
```

4) Refresh token (example JSON body)

```bash
curl -X POST http://localhost:8080/api/v1/token/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refresh_token_here>"}'
```

Notes:
- The exact key names for access/refresh tokens vary by implementation (`token`, `accessToken`, `access_token` for access tokens and `refreshToken` / `refresh_token` for refresh tokens). The integration tests in this repo are written to tolerate common variants.
- If any of the above endpoints are mounted under a different path or prefix in your build, update the URLs accordingly.

---

## Tests

This project uses separate source sets for unit tests (`src/test`) and integration tests (`src/integrationTest`) in the `shop-user-service` module.

### Run unit tests

```bash
# run unit tests for the user service
./gradlew :shop-user-service:test
# run all unit tests in the repo
./gradlew test
```

### Run integration tests (per-module)

Integration tests use a dedicated Gradle task `integrationTest` and run the tests in the `src/integrationTest` source set.

Run all integration tests for the `shop-user-service` module:

```bash
./gradlew :shop-user-service:integrationTest
```

Run a single integration test class or method (useful for iteration):

```bash
# run a single test class
./gradlew :shop-user-service:integrationTest --tests 'com.shop.userservice.web.api.v1.RouteApiV1IntTest'

# run a single test method
./gradlew :shop-user-service:integrationTest --tests 'com.shop.userservice.web.api.v1.RouteApiV1IntTest.testRefreshToken'
```

Note: the project already includes `testApplication`-based Ktor tests which are fast because they run the Ktor app in-process; heavier integration tests that use Testcontainers or external services will be slower.

---

## Postman collections

There are Postman collections in the service directories, e.g. `shop-user-service/user-service.postman_collection.json`, that you can import into Postman or Insomnia to exercise the endpoints and visualize request/response shapes.

---

## Configuration and environment

Each service uses Typesafe HOCON (`application.conf`) and Gradle properties. Typical places to look:
- `src/main/resources/application.conf` or `application.yml` (service-specific runtime configuration)
- `gradle.properties` files in service directories for build-time properties

Environment variables and config keys (examples)
- `SHOP_USER_SERVICE_PORT` or the corresponding setting in `application.conf` can change the HTTP port.
- JWT signing secrets, DB URLs, and other sensitive values should be provided via environment variables or a secure vault in production — do not commit secrets to git.

---


## Command
```
Setting up gradle wrapper makes the project executable in any IDE even without gradle
/usr/libexec/java_home -V

export JAVA_HOME=$(/usr/libexec/java_home -v 21.0.7)
export PATH=$JAVA_HOME/bin:$PATH

./gradlew wrapper --gradle-version 9.1.0
```

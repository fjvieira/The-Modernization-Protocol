# The Modernization Protocol Course Series

This repository is used as part of an [O'Reilly](https://www.oreilly.com/) live
course series, **The Modernization Protocol**, created by [Fernando J Vieira](https://www.linkedin.com/in/fernandojvieira/).

The legacy application is intended for use in the courses. The code, including its
imperfections, is provided for educational purposes only.

## Shipping Service

A microservice for calculating shipping costs, including loyalty benefits and promotions.

### Stack

- Java 11
- Lombok
- Spring Boot 2.7.18
- Maven Wrapper
- PostgreSQL
- JUnit 5 and Mockito

### Prerequisites

Install or provide:

- Java 11
- PostgreSQL
    - The application expects a PostgreSQL database named `shipping_db`.
    - The database schema is in [`src/main/resources/schema.sql`](src/main/resources/schema.sql).
- A carrier-rate HTTP endpoint (configurable by `external.carrier.url`)

Connection and carrier settings can be changed in [`src/main/resources/application.properties`](src/main/resources/application.properties).

### Run Locally

From the repository root, use the Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

Calculate shipping with:

```text
POST /api/v2/checkout/calculate-shipping
```

The request accepts cart, destination, parcel, customer tier, and promotion details.

To build the application:

```powershell
.\mvnw.cmd clean package
```

The packaged JAR is created under `target/`.

### Run Tests

Run the complete test suite:

```powershell
.\mvnw.cmd test
```

Test reports are written to `target/surefire-reports/`.


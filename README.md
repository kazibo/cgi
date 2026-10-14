# Bank Account API

REST API for managing bank accounts with multiple currencies (EUR, USD, SEK, GBP), deposits, withdrawals, and currency exchange.

## Requirements

- Java 21
- Maven 3.9+ (optional if using the Maven Wrapper)
- Docker and Docker Compose (only for containerized execution)

## Option 1: Run with Docker Compose

From the project root:

```bash
docker compose up --build
```

The API will be available at:

`http://localhost:8080`

To stop the application:

```bash
docker compose down
```

## Option 2: Run locally

Make sure Java 21 is installed:

```bash
java -version
```

From the project root, start the application using the Maven Wrapper, if included.

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, if Maven is installed:

```bash
mvn spring-boot:run
```

To build the application and run the packaged JAR:

```bash
mvn clean package
java -jar target/<application-jar-name>.jar
```

Replace `<application-jar-name>.jar` with the actual JAR filename generated in the `target` directory.

The API will be available at:

`http://localhost:8080`

Stop the application with `Ctrl+C`.

## API documentation

If Springdoc OpenAPI is configured, open Swagger UI at:

`http://localhost:8080/swagger-ui/index.html`

## Database

The application uses H2. Database persistence depends on the configured H2 URL. With an in-memory database, data is lost when the application stops. With file-based H2, data is stored on disk.

## Configuration

External logging URL and other application settings can be configured in `src/main/resources/application.yaml`.

## Testing

Database is seeded with sample data. 4 total accounts with ids 1-4. Go to http://localhost:8080/swagger-ui/index.html to try out how endpoints work.

Run tests with `.\mvnw.cmd test` on Windows and `./mvnw test` on Linux / macOS

## Future improvements

It is clear that the project is merely a sample and a lot more could be done to bring it closer to real world use.

For example:

- Exchange rates could be taken from Swedbank's exchange rates page dynamically
- Exchange rates could be direct between non EUR currencies, now everything is converted through EUR
- Account creation. The idea is when account is created, row with balances for each currency is created, but it depends on bank policy, maybe currency for account should be created when first operation with said currency is attempted, or even should be created separately from bank account settings 
- Withdraw/exchange limits
- Rounding rules for operations and precision should be corrected according to bank rules
- Logging should be done in other places, not just before withdrawal request (i guess), but I will just do what was told in the task
- Not sure what should be done if the external service requested before withdrawal does not respond. If it is just logging, then probably money should still be withdrawn, but for example if it's security identity check, then only success response should allow withdrawal
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

- Exchange rates could be taken from Swedbank's exchange rates page
- Withdraw/exchange limits
- Rounding rules for operations
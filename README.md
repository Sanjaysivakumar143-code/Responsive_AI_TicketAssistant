# Ticket Assistant

Support ticket API with AI-based analysis. You create a ticket, it gets saved right
away, and an LLM analyzes it in the background (category, summary, suggested reply,
recommended team). Comes with a mock LLM provider so you can run everything locally
without an API key.

## Requirements

- Java 17
- That's it — use the included `mvnw`/`mvnw.cmd`, no need to install Maven.

## Running it

**./mvnw spring-boot:run**

App comes up on **http://localhost:8080**. Database is H2, in-memory, wiped every
restart — fine for dev, not for anything you want to keep.

By default llm.provider is set to **mock in application.yml**, so it just runs
some keyword rules instead of calling a real model. No key needed.

If you want to hit the real OpenAI API instead, set your key as an env var first:

$env:OPENAI_API_KEY = "sk-..."      # windows powershel

then run with:
./mvnw spring-boot:run -Dspring-boot.run.arguments=--llm.provider=openai

## Tests

./mvnw test

## Trying the API

Create a ticket:

curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust-123",
    "subject": "Cannot log in",
    "description": "Getting an error when trying to log in",
    "priority": "HIGH",
    "product": "Portal"
  }'

You'll get back the ticket with status `PENDING`. Analysis happens async, so check
back on it:

curl http://localhost:8080/api/tickets/1

Status goes **PENDING -> PROCESSING -> COMPLETED** (with the analysis attached) or
FAILED (with a reason, e.g. the LLM timed out).

## Config quick reference

Everything's in src/main/resources/application.yml:

- `llm.provider` - `mock` or `openai`
- `llm.timeout-ms` - timeout for the LLM call, default 5000
- `llm.openai.api-key` - pulled from `OPENAI_API_KEY` env var
- `llm.openai.model` - defaults to `gpt-4o-mini`

H2 console if you want to poke at the DB: **http://localhost:8080/h2-console**,
**JDBC URL jdbc:h2:mem:ticketsdb, user sa, no password.**
Swagger Documentation: **http://localhost:8080/swagger-ui/index.html**


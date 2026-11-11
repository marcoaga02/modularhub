# modularhub

Monorepo with a Spring Boot backend (Java 21) and an Angular frontend, meant to run alongside Postgres and Keycloak.

## Project structure

The backend lives at the root of the repository (`pom.xml`, `src/main/java`, `src/main/resources`). The Angular frontend lives in `src/main/modularhub-ui` and is its own project with its own `package.json`.

## Prerequisites

Building everything only requires Java 21 and Maven (or the `./mvnw` wrapper included in the repo). Node and npm don't need to be installed manually: the Maven plugin downloads them on its own into a temporary folder under `target/` during the build.

Running the application also requires Docker and Docker Compose, used to start Postgres and Keycloak.

## Configuration

The development credentials for Postgres and Keycloak (`modularhub`/`modularhub` for the database, `admin`/`admin` for the Keycloak admin) are already hardcoded in `compose.yaml` and have a matching default in `application.yaml`. For local development you don't need to create or export any environment variable for these.

The only required variable is the Keycloak client secret used by the backend, which has no default on purpose. Put it in a `.env` file at the root of the repo (gitignored):

```
MODULARHUB_BACKEND_CLIENT_SECRET=<your-client-secret>
```

The same secret is used by Keycloak when it imports the realm (see [Keycloak realm](#keycloak-realm)), so you can pick any value. Optionally, to let Keycloak send emails (e.g. password reset), add the SMTP credentials too:

```
MODULARHUB_SMTP_FROM=<sender-address>
MODULARHUB_SMTP_USER=<smtp-user>
MODULARHUB_SMTP_PASSWORD=<smtp-password>
```

`docker compose` reads `.env` automatically, but Maven and your IDE don't: the backend needs it connected explicitly, in one of these ways.

**IntelliJ IDEA** (no plugin needed): open the run configuration for `ModularhubApplication` (or `spring-boot:run`), expand the "Environment variables" field (the small sheet icon next to it) and paste the whole content of `.env` in there — the editor recognizes `KEY=value` lines on its own and splits them into separate variables, no need to type them one by one.

**From the terminal**, regardless of the IDE:

```bash
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

Without the variable set, the application fails to start because Spring can't resolve the placeholder.

## Running locally

Docker compose is not started automatically by the backend: you need to bring it up by hand before launching the application.

```
docker compose up -d
```

This starts Postgres (port 5432) and Keycloak (port 8090, admin console at `/admin`), with the development credentials already in place.

### Keycloak realm

On startup Keycloak imports the `modularhub` realm from `keycloak/import/modularhub-realm.json`, with:

- the `modularhub-frontend` public client and the `modularhub-backend` confidential client (service account with `realm-admin`), whose secret is `MODULARHUB_BACKEND_CLIENT_SECRET`
- the `USER_MANAGEMENT` realm role and the `ADMIN` group that grants it
- a development user `admin` / `admin` in the `ADMIN` group

The import only happens if the realm doesn't exist yet: an existing realm is left untouched. To apply changes made to the file, delete the `modularhub` realm from the admin console (or the whole `keycloak` database) and restart Keycloak with `docker compose restart keycloak`.

You can then launch the backend (with `MODULARHUB_BACKEND_CLIENT_SECRET` set, see Configuration above):

```
./mvnw spring-boot:run
```

The backend responds on `http://localhost:8080/api`.

For the frontend in development mode, with hot reload:

```
cd src/main/modularhub-ui
npm install
npm start
```

The frontend responds on `http://localhost:4200`.

## Full build

```
./mvnw clean package
```

This builds both the backend and the frontend and produces a single executable jar in `target/`, with the Angular app already compiled and served as a static resource by the backend. To run it, just have Postgres and Keycloak already up with `docker compose up -d` and then:

```
java -jar target/modularhub-0.0.1-SNAPSHOT.jar
```

If you want to skip the frontend build (for example to speed up a backend-only test cycle), pass `-Dskip.ui=true`:

```
./mvnw clean package -Dskip.ui=true
```

## Tests

```
./mvnw verify
```

Runs unit tests and integration tests (in `src/it`). The latter are based on Testcontainers, so Docker needs to be running, but you don't need to start the compose stack by hand: the containers are managed directly by the tests.

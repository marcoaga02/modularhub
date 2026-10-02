# modularhub

Monorepo con backend Spring Boot (Java 21) e frontend Angular, pensato per girare insieme a Postgres e Keycloak.

## Struttura del progetto

Il backend vive alla radice del repository (`pom.xml`, `src/main/java`, `src/main/resources`). Il frontend Angular si trova in `src/main/modularhub-ui` ed è un progetto a sé stante con il proprio `package.json`.

## Prerequisiti

Per buildare tutto serve solo Java 21 e Maven (o il wrapper `./mvnw` incluso nel repo). Node e npm non vanno installati a mano: il plugin Maven li scarica da solo in una cartella temporanea sotto `target/` durante la build.

Per far girare l'applicazione servono anche Docker e Docker Compose, usati per avviare Postgres e Keycloak.

## Configurazione

Copia il file `.env.example` (se presente) in `.env` oppure crealo tu stesso nella root del progetto, valorizzando queste variabili:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: credenziali del database applicativo
- `KEYCLOAK_DB`, `KEYCLOAK_DB_USERNAME`, `KEYCLOAK_DB_PASSWORD`: credenziali del database usato da Keycloak
- `KC_BOOTSTRAP_ADMIN_USERNAME`, `KC_BOOTSTRAP_ADMIN_PASSWORD`: credenziali dell'admin Keycloak al primo avvio
- `MODULARHUB_BACKEND_CLIENT_SECRET`: client secret del client Keycloak usato dal backend

## Avvio in locale

Il docker compose non viene avviato automaticamente dal backend: va tirato su a mano prima di lanciare l'applicazione.

```
docker compose up -d
```

Questo avvia Postgres (porta 5432) e Keycloak (porta 8090, admin console su `/admin`).

A questo punto puoi lanciare il backend:

```
./mvnw spring-boot:run
```

Il backend risponde su `http://localhost:8080/api`.

Per il frontend in modalità sviluppo, con hot reload:

```
cd src/main/modularhub-ui
npm install
npm start
```

Il frontend risponde su `http://localhost:4200`.

## Build completa

```
./mvnw clean package
```

Questo comando builda sia il backend sia il frontend e produce un unico jar eseguibile in `target/`, con l'app Angular già compilata e servita come risorsa statica dal backend. Per lanciarlo basta avere Postgres e Keycloak già su con `docker compose up -d` e poi:

```
java -jar target/modularhub-0.0.1-SNAPSHOT.jar
```

Se vuoi saltare la build del frontend (ad esempio per velocizzare un ciclo di test sul solo backend) puoi passare `-Dskip.ui=true`:

```
./mvnw clean package -Dskip.ui=true
```

## Test

```
./mvnw verify
```

Esegue i test unitari e quelli di integrazione (in `src/it`), questi ultimi basati su Testcontainers quindi serve Docker attivo, ma non serve avviare il compose a mano: i container vengono gestiti direttamente dai test.

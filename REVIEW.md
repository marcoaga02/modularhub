[//]: # ( FIXME fare fix dei punti)
# Code Review — Bug e migliorie

Ordinato per gravità decrescente.

## 1. `@RolesAllowed` non applicato (sicurezza, critico)

`UserController.java:21` usa `jakarta.annotation.security.RolesAllowed`, ma da nessuna parte nel progetto è presente `@EnableMethodSecurity(jsr250Enabled = true)`. Spring Security ignora quindi l'annotazione: l'unica regola attiva è `SecurityConfig.java:36-38` (`anyRequest().authenticated()`), che consente a **qualsiasi utente autenticato**, indipendentemente dal ruolo, di chiamare tutti gli endpoint di `UserController` (create/update/delete utenti inclusi).

- Fix: aggiungere `@EnableMethodSecurity(jsr250Enabled = true)` su una `@Configuration`, oppure spostare i controlli di ruolo in `SecurityConfig.filterChain` con `requestMatchers(...).hasRole(...)`.

## 2. Pattern di autorizzazione incoerente (sicurezza, alto)

`UserController` è annotato con `@RolesAllowed`, mentre `AccountController`, `GroupController`, `LanguageController`, `PluginRegistryController` non hanno alcun controllo di ruolo esplicito e si affidano solo a `SecurityConfig`. Il risultato pratico è che nessuno dei due meccanismi applica realmente restrizioni per ruolo. Da standardizzare su un solo approccio (conseguenza diretta del punto 1, ma da sistemare esplicitamente su ogni controller).

## 3. `SecurityConfig` priva di test diretti (alto)

Nessun test diretto per `core/config/*`, in particolare `SecurityConfig`, nonostante codifichi regole di autorizzazione critiche. Data la gravità dei punti 1 e 2, la mancanza di test è ciò che ha permesso al buco di restare invisibile.

## 4. `GlobalExceptionHandler` senza test dedicato (medio)

Verificato solo indirettamente (status HTTP) tramite i test dei controller; nessun test verifica il corpo `ErrorDTO` (`errorCode`, `timestamp`, lista `fields` di validazione) né il fallback `handleGenericException`. Una regressione sul formato dell'errore passerebbe inosservata.

## 5. `PluginRegistryController` senza test (medio)

Unico controller senza test dedicato — gli altri (`AccountController`, `GroupController`, `LanguageController`, `UserController`) lo hanno.

## 6. Doppia validazione lingua ridondante (medio)

`UserService.java:224-227` e `AccountPreferencesService.java:66-69` eseguono lo stesso controllo (`languageRepository.findByUuid(...).orElseThrow(...)`), causando un doppio hit al DB nella stessa transazione.

## 7. `UserService.createUser` / `updateUser` troppo lunghi (basso-medio)

Righe ~94-139 e 142-191: mescolano validazione unicità, mapping, costruzione DTO identity provider e preferenze, assemblaggio risposta. Contengono inoltre 3 blocchi quasi identici per i controlli di unicità (taxId/username/email), estraibili in un helper data-driven.

## 8. `KeycloakIdentityProviderService` — duplicazione (basso-medio)

Costruzione di `UserRepresentation` duplicata tra `createUser` e `updateUser` (righe 54-92, differiscono solo per `emailVerified`); due overload `execute(...)` (righe 184-198) quasi identici, unificabili.

## 9. `UserSpecification.java` — stringhe magiche (basso)

Usa stringhe magiche (`"deletedOn"`, `"firstname"`, ecc.) per i path JPA invece del metamodel o costanti tipizzate. Un rename del campo in `User` non verrebbe intercettato dal compilatore.

## 10. TODO dimenticato (basso)

`AccountService.java:28` (`// TODO valutare se fare caching`) — da rimuovere o tracciare come ticket.

## Nota

Report generato senza modifiche al codice; nessuna riga sopra elencata è stata toccata.

# Ändringslogg

Alla märkbara ändringar i projektet står här, nyaste först. Formatet följer [Keep a Changelog](https://keepachangelog.com/sv/1.1.0/). Varje rad hänvisar till den commit där ändringen gjordes.

Arbetsrutinen för nya funktioner står i `AGENTS.md`. Vad som byggdes och varför står i `docs/decisions/`.

## Ej publicerat

### Tillagt

- Öppettider per restaurang för vardag och helg, obligatoriska i POST/PUT och synliga i GET. Nya bokningar måste rymmas inom öppettiderna under hela sittningen på 120 minuter, annars ges 400. Befintliga bokningar bevaras när tiderna ändras. Tester och HTTP-exempel ingår.
- `CHANGELOG.md` och `CLAUDE.md` med arbetsrutinen.
- `AGENTS.md` med arbetsregler och arbetsrutin för både Claude Code och Codex. `CLAUDE.md` importerar den.
- `docs/decisions/` med beslut för `capacity-check`, `guest-own-bookings` och `restaurant-update`.
- `docs/architecture.md` är nu en del av repot.

### Ändrat

- README är uppdaterad till en projektguide med implementerade funktioner, lokal snabbstart, konfiguration, roller, tester, dokumentationslänkar och nuvarande begränsningar.
- `.gitignore`: i `docs/` är bara `decisions/` och `architecture.md` med i git. Planeringen är privat.

### Borttaget

- `requirements.md`. Kraven finns nu i respektive uppgifts diskussion, som är privat.

## 2026-10-22

### Tillagt

- Kapacitetskontroll vid ny bokning. En bokning upptar platser i 120 minuter, och fullt ger 409. Tid bakåt i tiden, för stort sällskap och sällskap större än `seatCount` ger 400. Ett lås på restaurangens rad gör kontrollen säker vid samtidiga anrop, och ett anrop som väntar för länge får 503. (`7f5ebed`)
- En sänkning av `seatCount` som skulle ge överbeläggning ger 409. (`7f5ebed`)
- `ClockConfig` med tidszonen `Europe/Stockholm`. (`7f5ebed`)
- Tester för kapaciteten: `CapacityCheckerTest`, `BookingCapacityTest` och `RestaurantSeatCountTest`. (`7f5ebed`)
- HTTP-anrop för kapacitetskontrollen i `request.http/kapacitetskontroll.http`. (`e95fc4a`)

### Ändrat

- `requests.http` ligger nu i mappen `request.http/`. (`e95fc4a`)

### Fixat

- En gäst ser bara sina egna bokningar. Någon annans bokning ger 403 som `ProblemDetail`, även med `If-None-Match`. Personalen ser fortfarande alla bokningar. (`8018dd9`)

### Borttaget

- `skriftliga_fragor.md` och `docs/reflektion.md`. (`8018dd9`)

## 2026-10-20

### Borttaget

- CI-jobben som publicerade imagen till GHCR och körde `deploy.sh` på VPS:en. CI kör nu bara tester. (`723408e`)

## 2026-10-18

### Ändrat

- Javadoc i `OpenApiConfig` och `RestaurantAuthoritiesConverter`. (`d35f8c6`)

## 2026-10-17

### Tillagt

- `PUT /restaurants/{id}` för ADMIN med scopet `restaurants:write`. CORS tillåter `PUT`. (`bdd4427`)

## 2026-10-15

### Tillagt

- Driftfiler i `deploy/`: Docker Compose, Caddy, PostgreSQL med två databaser och `deploy.sh`. (`fbd6ee5`)
- CI publicerar imagen till GHCR och driftsätter på VPS:en. (`fbd6ee5`, `72fdf0a`)
- Nya användare får rollen `GUEST` automatiskt vid inloggning, via en Post Login Action i Auth0. (`512bfc0`)

### Ändrat

- `deploy/README.md` och `deploy/deploy.sh`. (`6173e64`, `72fdf0a`)

## 2026-10-13

### Tillagt

- API:t är en resource server som validerar JWT från Auth0. Roller och scopes styr varje endpoint. Ingen token ger 401 och fel behörighet ger 403. (`3b58e62`)
- CORS för frontenden och inloggning med PKCE i Swagger UI. (`3b58e62`)
- Profilen `prod` med PostgreSQL, en `Dockerfile` och startdata i `data.sql`. (`3b58e62`)
- Säkerhetstester i `SecurityTest`. (`3b58e62`)

### Borttaget

- Kontrakttestet med Spring Cloud Contract: modulen `notification-service`, `NotificationContractTest` och `HttpNotificationService`. (`3b58e62`)

## 2026-09-28

### Ändrat

- Kontrakttestet körs med Stub Runner och `mvn install`. Maven Wrapper är tillagd. (`ca89e88`)

### Fixat

- `mvnw` fungerar i CI. (`d496925`)

## 2026-09-27

### Tillagt

- `NotificationService` med `LoggingNotificationService`. Gästen aviseras när en bokning bekräftas. (`1d02593`)
- API-tester med RestAssured för skapande, hämtning, 404 och 409. (`1d02593`)
- Kontrakttest för aviseringen med Spring Cloud Contract och WireMock. (`74c3699`)

## 2026-09-22

### Tillagt

- Endpoints för `confirm`, `seat` och `cancel`. (`9a174ad`)
- HATEOAS-länkar för bokningens tillåtna statusbyten. (`9a174ad`)
- `ETag` och `If-None-Match` på `GET /bookings/{id}`. (`9a174ad`)
- Gemensam felhantering med `ProblemDetail` för 400, 404, 409 och 500. (`9a174ad`)

### Ändrat

- Enklare uppbyggnad av HATEOAS-länkarna i `BookingModelAssembler`. (`69a020b`)

## 2026-09-20

### Tillagt

- Grundläggande REST-API för restauranger och bokningar: skapa, lista och hämta. (`acc25c1`)
- Domänmodellerna `Restaurant` och `Booking` med bokningens tillståndsmaskin. (`acc25c1`)
- CI som bygger och testar. (`acc25c1`)

### Ändrat

- Backendprojektet ligger nu i `backend/`. (`acc25c1`)

## 2026-09-16

### Tillagt

- Projektets startpunkt: Spring Boot-applikation med `GET /health`, uppgiften och `requests.http`. (`ddab060`, `9634510`)

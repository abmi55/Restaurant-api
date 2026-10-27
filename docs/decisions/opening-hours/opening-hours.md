# Öppettider per restaurang

Implementerad 2026-10-27. Ändringen är inte committad. Manuell HTTP-kontroll och drift återstår.

## Sammanfattning

Varje restaurang har obligatoriska öppettider för vardag och helg. Nya bokningar måste rymmas inom dagens öppettider under hela sittningen på 120 minuter.

## Varför

Bokningar kunde tidigare skapas vilken tid som helst på dygnet. Restaurangernas öppettider behövs även som underlag för en senare uppgift om tillgänglighet.

## Beslut

| Fråga | Valt | Bortvalt |
|---|---|---|
| Omfattning | Egna tider per restaurang, ett intervall för vardagar och ett för helger | Gemensamma tider för alla restauranger och egna tider per enskild veckodag |
| Lagring | Fyra `LocalTime`-fält direkt i `Restaurant`, alla `NOT NULL` | En separat intervallklass med attributöverskrivningar: mer kod för två intervall |
| Dagens intervall | `Restaurant.opensAt(DayOfWeek)` och `closesAt(DayOfWeek)`; `isWeekend(DayOfWeek)` samlar helgregeln | En kopia av helgregeln i bokningsservicen |
| Validering | `@NotNull`, två `@AssertTrue`-metoder och strikt `HH:mm` på request-fälten | Nytt constraint eller nytt undantag: befintlig validering och felhantering räcker |
| Bokningskontroll | Privat kontroll i `BookingService.create` före kapacitetskontrollen, med `LocalDateTime` och `Booking.DURATION` | Bara `LocalTime`: skulle kunna godkänna en sittning över midnatt |
| Ändrade tider | Befintliga bokningar bevaras, även utanför nya tider | Automatisk avbokning eller att neka ändringen |
| Befintlig testdata i drift | Användaren raderar tabellerna före deploy och återskapar restauranger via API:t | Migreringsverktyg och manuell uppdatering för att behålla testdatan |

## Beteende

- `weekdayOpensAt`, `weekdayClosesAt`, `weekendOpensAt` och `weekendClosesAt` krävs i POST och PUT och visas i båda GET-endpointsen som `HH:mm`.
- Måndag–fredag använder vardagstider och lördag–söndag helgtider, enligt bokningens datum i `Europe/Stockholm`.
- Intervallen måste rymma minst 120 minuter samma dag. Saknade tider, ogiltiga tider och för korta intervall ger 400. `24:00` avvisas och omvandlas inte till `00:00`.
- En ny bokning får börja exakt vid öppning och senast 120 minuter före stängning. Tid utanför intervallet ger 400 som `ProblemDetail` med dagtyp, öppettider och sista starttid.
- Fel som inte beror på restaurangen ger först 400, sedan kommer 404 för okänd restaurang, 400 för öppettider och sist 409 för platsbrist.
- Ändrade öppettider tar inte bort befintliga bokningar. `confirm` och `seat` kontrollerar inte öppettiderna.
- Utanför: tider per enskild veckodag, helgdagar, stängda dagar, intervall över midnatt, särskild sommartidshantering, tillgänglighetsendpoint, frontendändringar och migreringsverktyg.

## Verifiering

- `./mvnw test` från roten: 77 tester, 0 fel, 0 överhoppade.
- 27 nya testfall verifierar obligatoriska fält (alla fyra i POST, ett i PUT), intervallens längd, ogiltig tid och format, GET-svar, bevarad bokning efter PUT, tidsgränser på vardag, lördag och söndag, sittning över midnatt, felordning samt confirm/seat utan ny kontroll.
- `/v3/api-docs` visar bara de fyra tiderna som nya fält i `RestaurantCreateRequest`. Valideringsmetoderna är dolda med `@JsonIgnore`.
- De 50 befintliga testerna behåller sina påståenden; bara restaurangernas testdata och request-JSON har fått öppettider.
- Ett riktigt lokalt `GET /restaurants/1` gav de fyra tiderna som `11:00`, `23:00`, `12:00` och `22:00`.
- Diffen granskades mot godkänt beteende. Controllers, säkerhet, undantagshantering, kapacitetskontroll och konfiguration är oförändrade. `git diff --check` passerade. Arkitekturöversikten och planen granskades tillsammans.
- Overifierat, steg 4: användaren behöver köra HTTP-filerna i IntelliJ mot lokal miljö.
- Overifierat, steg 6: användaren behöver radera testtabellerna `booking` och `restaurant` i drift, deploya, återskapa restauranger med öppettider och bekräfta GET-svaret. Inga driftkommandon har körts av agenten. All data i tabellerna försvinner vid raderingen enligt det godkända beslutet.

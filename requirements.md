# Inlämningsuppgift: Java Web Services

**Kurs:** Java Web Services, 45 YH-poäng
**Form:** Individuell
**Betyg:** Icke godkänt (IG) / Godkänt (G) / Väl godkänt (VG)
**Deadline:** måndag 2026-10-12 kl. 08:00
**Muntlig redovisning:** tisdag 2026-10-13 och torsdag 2026-10-15

## Så här fungerar upplägget

Du får alla fyra delar redan nu. De bygger på varandra inom ett och samma projekt. Del 1 är API:et, del 2 testar det, del 3 säkrar och driftsätter det och del 4 är texten du skriver om arbetet.

Det finns bara en fast deadline. Fram till dess har vi tre kontrollpunkter under handledningen. Vid varje kontrollpunkt visar du att det du har byggt fungerar. Kontrollpunkten påverkar inte betyget och du kan inte bli underkänd på den. Den finns för att du inte först i vecka 42 ska upptäcka att del 1 aldrig blev klar.

| Kontrollpunkt | Datum | Du visar |
| --- | --- | --- |
| Del 1 | torsdag 2026-09-24 | API:et svarar |
| Del 2 | torsdag 2026-10-01 | Testerna är gröna |
| Del 3 | torsdag 2026-10-08 | Tjänsten är åtkomlig utifrån |

Delarna 1, 2 och 3 tar ungefär en till två arbetsdagar vardera. Del 4 tar två timmar, inte två dagar. Ta hänsyn till det när du planerar. Annars riskerar du att skjuta upp del 4 till natten före deadline och gå miste om VG på den delen.

## Domänen: restaurang och bordsbokning

Du bygger ett API för bordsbokningar. Det räcker med två resurser.

**Restaurant:** namn, stad, kök, antal platser.

**Booking:** restaurang, gästens namn, gästens e-post, tidpunkt, sällskapets storlek och ett tillstånd.

En bokning går igenom fyra tillstånd:

```text
REQUESTED --> CONFIRMED --> SEATED
     |             |
     +-------------+--> CANCELLED
```

Tillståndsmaskinen är inte bara en dekoration. Den gör HATEOAS meningsfullt i del 1, ger dig en anledning att returnera 409 i del 2 och skiljer gästens behörighet från personalens i del 3. Ändra den inte utan att fråga.

Du får lägga till fler fält och resurser om du vill, men det är inte något du bedöms på.

## Del 1: REST-API

- Två resurser under `/restaurants` och `/bookings`. Minst funktionalitet för att lista, hämta en enskild och skapa samt minst en tillståndsövergång som en egen endpoint.
- Rätt verb och statuskoder. En lyckad POST ger 201 med en `Location`-header.
- Ett okänt id ger 404 som `ProblemDetail`. En otillåten tillståndsövergång ger 409.
- Entiteten och DTO:n ska vara separata klasser. Klienten får aldrig se din JPA-entitet.
- Paginering på `/restaurants`. Du väljer offset eller cursor och motiverar valet i del 4.
- `ETag` och `If-None-Match` på `GET /bookings/{id}`, så att ett oförändrat svar ger 304.
- HATEOAS-länkar på en bokning. De övergångar som är möjliga just nu ska visas som länkar. En avbokad bokning har inga.
- OpenAPI via springdoc. Swagger UI ska svara.

## Del 2: Tester och mockning

- RestAssured mot en slumpmässig port. Minst tre fall: skapa en bokning (201), hämta den (200) och hämta en som inte finns (404).
- Ditt API anropar en extern aviseringstjänst när en bokning bekräftas. Den tjänsten finns inte i verkligheten. Definiera ett interface för den och mocka den i testerna. Du väljer mellan Mockito och WireMock och motiverar valet i del 4.
- Minst ett test som visar att en otillåten tillståndsövergång ger 409.

Håll dig till den omfattningen för G. Det mer djupgående testet beskrivs under VG längre ner.

## Del 3: Säkerhet och drift

- Ditt API är en resource server som validerar JWT.
- Scopes och roller per endpoint. Gästen får läsa och skapa bokningar. Personalen får bekräfta bokningar och placera gäster. Administratören får ändra restauranger.
- 401 och 403 ska användas korrekt. Ingen token ger 401. Fel behörighet ger 403.
- CORS ska vara konfigurerat för din frontends origin. Du ska inte använda wildcard tillsammans med credentials, och du ska veta varför.
- CSRF: fatta ett beslut och skriv en kommentar i koden om varför det är rätt beslut för just din tjänst.
- Tester av minst två säkrade endpoints med `SecurityMockMvcRequestPostProcessors.jwt()`. En tillåten och en nekad.
- Tjänsten ska vara driftsatt och åtkomlig via internet vid redovisningen.

**Om authorization server.** Du ska inte bygga en egen. Återanvänd den in-process-server som vi använde under lektionerna i kapitel 12 till 16, kör Keycloak i Docker eller använd ett kostnadsfritt utvecklarkonto hos en molnleverantör. Poängen är att ditt API validerar tokens korrekt, inte att du har skrivit en identitetsleverantör.

**Om driftsättning.** Valfri host: Render, Railway, Fly.io, Hetzner eller något annat. Du deployade redan i DevOps-kursen, så tekniken är inte ny. Välj det billigaste alternativet som fungerar och spara motiveringen till del 4.

## Frivilligt: en frontend

Du får bygga en frontend, och du får vibe-koda den helt eller delvis. Den bedöms inte och påverkar varken G eller VG.

Det finns två skäl att ändå göra det. CORS blir konkret först när en riktig webbsida på en annan origin anropar ditt API. Dessutom får du något annat än Swagger UI att visa under redovisningen.

## Del 4: Teknikreflektion

Fem frågor, totalt ungefär två sidor. De finns i en separat fil:

**[skriftliga_fragor.md](skriftliga_fragor.md)**

Svaren lämnar du in som `reflektion.md` i ditt repo. Frågorna täcker de kursmål som handlar om att känna till något snarare än att bygga det: SOAP, WSDL, Spring Cloud, driftsättningsalternativ och end-to-end-perspektivet.

Två av frågorna bygger på kapitel som behandlas under vecka 40 och 41. Du kan alltså inte bli klar med del 4 förrän i slutet av kursen, och det är avsiktligt.

## Muntlig redovisning

Cirka 10-15 minuter per person.

- Demonstrera tjänsten live. Alla deltagare i mötet ska kunna nå din URL från sin egen dator.
- **Om tjänsten inte är igång när du redovisar blir uppgiften underkänd.** Testa den dagen innan.
- Visa ett anrop som lyckas och ett som nekas på grund av otillräcklig behörighet.
- Svara på följdfrågor om dina svar i del 4.

## Inlämning

Lämna in tre saker senast måndag 2026-10-12 kl. 08:00:

1. En länk till ditt GitHub-repo.
2. URL:en till den driftsatta tjänsten.
3. `reflektion.md` i repot med svaren på de fem frågorna.

## Kriterier för G

- Delarna 1, 2 och 3 är genomförda enligt punkterna ovan.
- `mvn test` körs utan fel.
- Tjänsten är driftsatt och nåbar under redovisningen.
- De fem frågorna i del 4 är besvarade.
- Du kan svara på följdfrågor om din egen kod.

## Kriterier för VG

Allt som krävs för G samt tre ytterligare saker. De är hämtade direkt från kursplanens VG-text.

**1. Motiverade teknikval.** Frågorna 3 och 4 i del 4 besvaras med verkliga avvägningar, inte med uppräkningar. Du ska ha valt bort något och kunna förklara varför. Du försvarar valen muntligt.

**2. Ett test som bevisar något icke-trivialt.** Välj en av tre vägar:

- Ett consumer-driven contract test med Spring Cloud Contract mellan ditt API och den mockade aviseringstjänsten.
- Ett test som visar mock drift: du ändrar den riktiga tjänstens svar, testet mot stubben är fortfarande grönt och du visar hur du upptäcker glidningen.
- Ett test som kör hela auktorisationsmatrisen från del 3, alltså varje kombination av roll och endpoint med både tillåtna och nekade fall.

**3. Självständighet.** Uppgiften är individuell. Du får ställa frågor, läsa och använda AI, men du ska kunna förklara varje rad du lämnar in. Om du inte kan det vid redovisningen blir det inte VG, oavsett hur koden ser ut.

VG bedöms alltså endast utifrån del 2 och del 4 samt redovisningen. Delarna 1 och 3 är antingen genomförda eller inte.

## Komplettering

Du har ett kompletteringstillfälle. Deadline för kompletteringen är fredag 2026-11-06 kl. 23:59.

## Kom igång

Forka repot och läs [README.md](README.md). Det innehåller ett tomt Spring Boot-projekt med en enda endpoint, så att du slipper börja med en tom mapp.

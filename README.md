# Inlämningsuppgift: Java Web Services

Forka det här repot. Allt du behöver finns här.

## Läs först

1. **[requirements.md](requirements.md)** är uppgiften. Den innehåller fyra delar, kraven för G och VG samt datum.
2. **[skriftliga_fragor.md](skriftliga_fragor.md)** innehåller de fem frågorna i del 4.

Deadline är onsdag den 14 oktober 2026 kl. 08.00. Muntlig redovisning sker onsdagen den 14 oktober 2026.

## Vad som redan finns

Ett tomt Spring Boot-projekt. Det innehåller Spring Web och ingenting annat, så att du slipper börja med en tom mapp och själv kan välja alla beroenden som du lägger till.

```text
pom.xml                          Spring Boot 4.1.0, Java 25
requests.http                    färdiga anrop mot API:et
src/main/java/.../restaurant/
    RestaurantApiApplication.java
    HealthController.java        GET /health, svarar {"status":"UP"}
src/main/resources/
    application.yaml             appnamn och port
src/test/java/.../restaurant/
    RestaurantApiApplicationTests.java
```

`HealthController` är inte en del av uppgiften. Behåll den för en snabb kontroll efter en deploy, eller radera den när du har egna endpoints.

## Kom igång

```bash
mvn spring-boot:run
```

I ett annat fönster:

```bash
curl localhost:8080/health
```

Du ska få tillbaka `{"status":"UP","service":"restaurant-api"}`. Om du får det fungerar din miljö, och du kan börja med del 1.

Kör testerna från repots rot med:

```bash
./mvnw test
```

På Windows (cmd eller PowerShell) används `mvnw.cmd test`. Maven Wrapper låser Maven 3.9.11, samma version som CI.

## requests.http

I `request.http/requests.http` finns anropen mot API:et. Öppna filen i IntelliJ och klicka på den gröna pilen till vänster om ett anrop, eller installera tillägget REST Client i VS Code.

Endast hälsokontrollen fungerar i det utdelade projektet. Resten är bortkommenterad, med ett anrop per krav i uppgiften, och du tar fram dem allteftersom du bygger. Anropen visar också vad som ska hända, till exempel att en okänd bokning ger 404 som `problem+json` och att en gäst som försöker bekräfta får 403 i stället för 401.

Byt ut `@host` överst i filen mot URL:en till din driftsatta tjänst inför redovisningen. Då har du en färdig demo i stället för att behöva skriva curl-kommandon inför publik.

## Beroenden du själv lägger till

Ingenting utöver Spring Web finns i `pom.xml`. Du lägger själv till det du behöver, del för del.

För del 1 behöver du persistens, validering, HATEOAS och springdoc. För del 2 behöver du RestAssured och en mockningsmodul. För del 3 behöver du Spring Security som resource server. Kapitlen visar exakt vilka artefakter det gäller.

## Fyra fällor som kostar timmar

Det här är sådant som inte syns i ett felmeddelande och som du inte lär dig något av att fastna på.

**1. `ProblemDetail` är avstängt som standard.** Del 1 kräver att ett okänt id ger 404 som `problem+json`. Spring Boot svarar med en tom body tills du lägger till följande i `application.yaml`:

```yaml
spring:
  mvc:
    problemdetails:
      enabled: true
```

**2. Boot 4 delade upp testmodulerna.** `spring-boot-starter-test` innehåller inte längre allt. `@WebMvcTest` kräver `spring-boot-webmvc-test`, `@DataJpaTest` kräver `spring-boot-data-jpa-test`, och `TestRestTemplate` kräver både `spring-boot-resttestclient` och `spring-boot-restclient`. Alla ska ha `test`-scope. Om de saknas får du ett annotationsfel som ser ut som ett stavfel.

**3. Jackson 3 bytte paket.** Importerna heter `tools.jackson.*`, inte `com.fasterxml.jackson.*`. Om du kopierar en kodsnutt från nätet är den nästan alltid skriven för Jackson 2.

**4. `/health` låses när du lägger till säkerhet i del 3.** Din hostingplattform pingar oftast en hälsoendpoint för att avgöra om tjänsten är igång. Om den svarar med 401 stängs din deploy ner. Släpp uttryckligen igenom den i din `SecurityFilterChain`.

## Frivilligt: en frontend

Du får bygga en frontend och vibe-koda den. Den bedöms inte, men CORS i del 3 blir mycket mer konkret när en riktig sida på en annan origin anropar ditt API. Dessutom får du något annat än Swagger UI att visa under redovisningen.

**Lycka till**

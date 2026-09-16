# Del 4: Teknikreflektion

Fem frågor om ditt eget projekt. Svara i en fil som heter `reflektion.md` och lägg den i repots rot.

## Så här svarar du

Räkna med 150–250 ord per fråga, alltså ungefär två sidor totalt. Om projektet redan är klart tar uppgiften cirka två timmar.

Två regler avgör om ett svar är tillräckligt eller inte:

**Utgå från din egen tjänst, inte från tekniken i allmänhet.** Frågan ”Vad är SOAP?” går att besvara med hjälp av kursboken. Det går däremot inte att hitta ett färdigt svar på frågan ”När hade du valt SOAP i din bokningstjänst?”. Det är den senare typen av frågor du får här.

**Beskriv vad du valde bort.** Ett val utan alternativ är inget val. Nämn vilket alternativ du valde bort och varför.

Du får använda AI när du skriver. Under den muntliga redovisningen kommer du att få följdfrågor om dina svar, så skriv inget som du inte kan förklara.

## Fråga 1: CSRF och CORS

Du fattade två beslut i del 3. Du konfigurerade CORS för en origin och valde att antingen stänga av eller behålla CSRF-skyddet.

Förklara båda besluten. Vad skyddar CORS mot, vad skyddar CSRF mot och varför är det inte samma sak? Beskriv konkret vad som hade kunnat hända i just din bokningstjänst om du hade fattat fel beslut om CSRF.

**Räcker för G:** du håller isär de två sakerna och motiverar ditt CSRF-beslut korrekt utifrån din arkitektur.

**Bygger på:** kapitel 10 och 11. Du kan besvara den här frågan redan nu.

## Fråga 2: SOAP och WSDL

Ditt API är byggt med REST. Beskriv vad SOAP är och vad ett WSDL-dokument innehåller.

Föreställ dig sedan att bokningstjänsten ska integreras med ett hotellkedjesystem från 2009 som bara kommunicerar via SOAP. Vad hade du behövt göra annorlunda och vad hade WSDL-filen gett dig som du inte får från din OpenAPI-specifikation?

**Räcker för G:** du beskriver envelope, body och fault samt förklarar vad WSDL beskriver och vad det används till.

**Bygger på:** kapitel 17 och 18, vecka 39.

## Fråga 3: Driftsättning

I del 3 driftsatte du din tjänst någonstans.

Vilka tre alternativ vägde du mot varandra innan du fattade ditt beslut? Vad kostade de, vad krävde de av dig och vad avgjorde ditt val? Beskriv också hur ditt val hade påverkats om tjänsten plötsligt fått tusen samtidiga användare.

**Räcker för G:** tre alternativ är namngivna och jämförda utifrån åtminstone pris och driftsansvar, och ditt val är motiverat.

**Lyfter till VG:** du har faktiskt valt bort ett alternativ av ett skäl som är relevant för din tjänst, inte bara av vana. Du kan förklara vad valet kan kosta dig senare och vad som skulle få dig att byta.

**Bygger på:** kapitel 25 och 26, vecka 41.

## Fråga 4: End to end

Rita kedjan från gästen till databasen: webbläsaren eller klienten, ditt API, aviseringstjänsten som du mockade i del 2 och databasen. Det räcker med en enkel skiss i text eller bild.

Svara sedan på följande frågor: Var brister kedjan först? Vad märker gästen när det händer? Vad hade du behövt mäta för att upptäcka problemet innan gästen ringer?

**Räcker för G:** kedjan är komplett, du pekar ut en svag punkt och beskriver vad användaren ser.

**Lyfter till VG:** du resonerar om vad som händer när aviseringstjänsten är långsam men fortfarande fungerar, och hur det påverkar svarstiden för din egen tjänst. Du föreslår något konkret att mäta, inte bara ”loggning”.

**Bygger på:** kapitel 27 och 28, vecka 41. Kedjan kan du rita redan efter del 2.

## Fråga 5: Spring Cloud

Din bokningstjänst består av en enda applikation. Anta att den delas upp i tre applikationer: en för restauranger, en för bokningar och en för aviseringar.

Vilka nya problem uppstår så snart du gör uppdelningen? Vilka av dem löser Spring Cloud åt dig, och hur? Nämn minst två komponenter vid namn och förklara vad de gör.

**Räcker för G:** du beskriver två konkreta problem som uppstår vid uppdelningen och kopplar dem till rätt Spring Cloud-komponenter.

**Bygger på:** kapitel 23 och 24, vecka 40.

## Vilka kursmål frågorna täcker

Frågorna finns eftersom kursplanen kräver kunskaper om sådant som du inte behöver bygga.

| Fråga | Kursmål |
|---|---|
| 1 | Kunskaper om vad CSRF och CORS är |
| 2 | Kunskaper om vad SOAP är och vad WSDL är |
| 3 | Kunskaper om alternativ för driftsättning av Enterprise-applikationer |
| 4 | Kompetens i att värdera sammansatta tjänster ur ett end to end-perspektiv |
| 5 | Kunskaper om vad Spring Cloud är |

Tillbaka till [requirements.md](requirements.md).

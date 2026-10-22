# Nya funktioner

Den här filen beskriver nya funktioner: vad de ska göra och varför. Planeringen görs inte här. Varje funktion får en egen plan i `docs/plans/new-plans/`.

## Gäster ser bara sina egna bokningar

**Varför:** En inloggad gäst kan i dag läsa alla bokningar via `GET /bookings` och `GET /bookings/{id}`. Svaret innehåller andra gästers namn och e-post.

**Beteende:**

- `GET /bookings` som gäst returnerar bara gästens egna bokningar.
- `GET /bookings/{id}` på någon annans bokning ger 403 som `ProblemDetail`.
- Ett okänt id ger fortfarande 404, även för en gäst.
- En gäst kan inte lista ut status på någon annans bokning med `If-None-Match`. Svaret blir 403, inte 304.
- Personalen ser fortfarande alla bokningar.
- Bokningar som skapades innan funktionen fanns har ingen ägare. Endast personalen ser dem.

**Utanför:**

- Behörigheten per roll och endpoint ändras inte.
- Gäster kan fortfarande inte avboka. Avbokning för gäster är ett separat beslut.

## Kapacitetskontroll vid bokning

**Varför:** `BookingService.create` kontrollerar aldrig `Restaurant.seatCount`. En restaurang med 40 platser kan få obegränsat många bokningar samma kväll. Bokningar bakåt i tiden godkänns också. Besluten och motiveringen finns i `docs/features/Kapacitetskontroll.md`.

**Beteende vid ny bokning:**

- Varje bokning upptar platser i 120 minuter från sin starttid. En bokning 18.00 gäller under intervallet `[18.00, 20.00)`. Längden är samma för alla restauranger.
- Bokningar med status `REQUESTED`, `CONFIRMED` och `SEATED` upptar platser under hela sitt intervall. `CANCELLED` upptar inga platser.
- En ny bokning godkänns när den högsta samtidiga beläggningen under bokningens intervall plus `partySize` är högst `seatCount`. Exakt full kapacitet godkänns.
- När en bokning slutar samtidigt som en annan börjar är platserna lediga för den nya.
- Om platserna inte räcker blir svaret 409 som `ProblemDetail`. Meddelandet innehåller inga uppgifter om andra gäster eller bokningar.
- `dateTime` tolkas i tidszonen `Europe/Stockholm` och måste ligga efter aktuell tid. En tid som är nu eller tidigare ger 400.
- `partySize` får vara högst 8. Taket är konfigurerbart. Ett större sällskap ger 400 med ett meddelande om att kontakta restaurangen.
- `partySize` större än restaurangens `seatCount` ger 400.
- Ett okänt restaurang-id ger 404. Fel som inte beror på restaurangen, till exempel tid i det förflutna eller ett för stort sällskap, ger 400 före 404.
- Två samtidiga anrop om de sista platserna kan inte båda godkännas.
- Ett anrop som inte får tillgång till restaurangen inom 5 sekunder, eftersom ett annat anrop håller den, avbryts utan att något sparas och får 503 som `ProblemDetail`. Tiden är konfigurerbar.

**Beteende vid ändring av restaurang:**

- En sänkning av `seatCount` som skulle ge överbeläggning i ett pågående eller framtida intervall ger 409 som `ProblemDetail`.
- En oförändrad eller höjd `seatCount` godkänns alltid, även om restaurangen redan är överbokad.

**Statusbyten:**

- `confirm` och `seat` kontrollerar inte kapaciteten. Platserna reserverades när bokningen skapades.

**Utanför:**

- Bordsplacering. Restaurangen räknas som en pool av platser, inte som enskilda bord.
- Öppettider. Alla tider räknas som bokningsbara.
- Faktisk sluttid eller förlängning när gäster sitter kvar längre än 120 minuter.
- Gräns för antal aktiva bokningar per gäst.
- Särskild hantering av tider vid byte till och från sommartid.

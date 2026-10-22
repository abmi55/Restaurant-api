# Nya funktioner

Den här filen beskriver nya funktioner: vad de ska göra och varför. Planeringen görs inte här. Varje funktion får en egen plan i `agents/plans/new-plans/`.

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

# PUT för restauranger

Levererad 2026-10-17 (`bdd4427`).

## Sammanfattning

Administratören kan ändra en befintlig restaurang med `PUT /restaurants/{id}`.

## Varför

Kravet säger att administratören får ändra restauranger. Tidigare fanns bara `POST /restaurants`, som skapar.

## Beslut

| Fråga | Valt | Bortvalt |
|---|---|---|
| Endpoint | Bara `PUT /restaurants/{id}` | `DELETE`: krävs inte, och bokningar pekar på restaurangen |
| Behörighet | `ADMIN` med scopet `restaurants:write`, samma som `POST` | Nytt scope: kräver ändringar i Auth0 och frontenden utan behov |
| Semantik | Hela resursen ersätts, med samma DTO och validering som `POST`. Svar 200 | `PATCH`: kräver delvis uppdatering, och inget krav finns |
| Okänt id | 404. `PUT` skapar aldrig en restaurang | `PUT` som skapar: databasen genererar id:t |
| Ändring i entiteten | En domänmetod `Restaurant.update(...)` | Setters: ändrar ett fält i taget och ger `setId` |
| CORS | `PUT` är tillåten | — |

## Beteende

- `PUT /restaurants/{id}` med admintoken ger 200 med den ändrade restaurangen.
- Fel roll ger 403, och ingen token ger 401.
- Utanför: radering av restauranger.

## Verifiering

Befintliga tester passerar. Inga nya tester skrevs.

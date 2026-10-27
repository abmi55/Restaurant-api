# Gäster ser bara sina egna bokningar

Levererad 2026-10-22 (`8018dd9`).

## Sammanfattning

En inloggad gäst ser och hämtar bara sina egna bokningar. Personalen ser alla bokningar.

## Varför

En gäst kunde läsa alla bokningar via `GET /bookings` och `GET /bookings/{id}`, med andra gästers namn och e-post.

## Beslut

| Fråga | Valt | Bortvalt |
|---|---|---|
| Hur ägaren identifieras | `sub` från token, sparat som `ownerId` på bokningen | E-post: finns inte i access token och kan förfalskas med en overifierad adress |
| Svar på någon annans bokning | 403 | 404: döljer att bokningen finns, men id:n är lätta att gissa |
| Var kontrollen ligger | I `BookingService`, som får anroparens id och roll från controllern | `@PostFilter`: filtrerar i minnet. `@PostAuthorize`: kräver att ägaren exponeras i svaret |
| Exponering | `ownerId` finns inte i `BookingResponse` | Att visa fältet: klienten behöver det inte |

## Beteende

- `GET /bookings` som gäst ger bara gästens egna bokningar.
- Någon annans bokning ger 403 som `ProblemDetail`. Ett okänt id ger 404.
- Ägarkontrollen sker före `If-None-Match`, så en gäst kan inte lista ut status med en gissad ETag.
- Bokningar utan ägare syns bara för personalen.
- Utanför: behörigheten per roll ändras inte, och gäster kan inte avboka.

## Verifiering

Testerna i `BookingOwnershipTest` och `SecurityTest` passerar. Den slutliga kontrollen i planen är inte verifierad.

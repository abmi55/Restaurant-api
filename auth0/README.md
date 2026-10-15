# Auth0-uppsättning

API:t är en OAuth2 resource server. Auth0 är authorization server och enda issuer, både lokalt och i den driftsatta tjänsten. Här beskrivs hur tenanten är uppsatt, så att den kan återskapas och förklaras. Filen innehåller inga hemligheter.

## Tenant

| | |
|---|---|
| Domän | `restaurant-api-abbas.eu.auth0.com` (region EU) |
| Issuer (`iss`) | `https://restaurant-api-abbas.eu.auth0.com/` (med avslutande `/`) |

## API (Applications → APIs → Restaurant API)

| Inställning | Värde | Varför |
|---|---|---|
| Identifier | `https://restaurant-api` | Blir `aud` i access token. Kontrolleras av `jwt.audiences`. |
| JWT Profile | Auth0 | Ger `scope` som mellanslagsseparerad sträng, som Spring mappar till `SCOPE_*`. |
| Signing Algorithm | RS256 | Spring hämtar den publika nyckeln från tenantens JWKS. |
| User-delegated access | Per-app authorization | Bara uttryckligen godkända appar får begära token för användare. |
| Client access | No apps allowed | Ingen maskin-till-maskin-klient. Token kommer bara från användarinloggning. |
| Enable RBAC | På | Token får bara de begärda scopes som användarens roller har. |
| Add Permissions in the Access Token | På | Lägger till claimet `permissions`. |
| Allow Skipping User Consent | På | Ingen samtyckesdialog vid inloggning. |
| Allow Offline Access | Av | Inga refresh tokens. |

### Permissions

| Permission | Beskrivning |
|---|---|
| `bookings:write` | Skapa egna bokningar |
| `bookings:manage` | Bekräfta, placera och avboka bokningar |
| `restaurants:write` | Hantera restauranger |

## Roller (User Management → Roles)

| Roll | Permission |
|---|---|
| `GUEST` | `bookings:write` |
| `STAFF` | `bookings:manage` |
| `ADMIN` | `restaurants:write` |

Rollnamnen skrivs med versaler. `SecurityConfig` kräver `ROLE_GUEST`, `ROLE_STAFF` och `ROLE_ADMIN`.

## Användare (User Management → Users)

Connection `Username-Password-Authentication`. Lösenorden finns i en lösenordshanterare, inte i repot.

| Användare | Roll |
|---|---|
| `guest1@restaurant.test` | `GUEST` |
| `staff1@restaurant.test` | `STAFF` |
| `admin1@restaurant.test` | `ADMIN` |

`.test` är en reserverad testdomän, så ingen e-post når en riktig mottagare.

## Post Login Action (Actions → Library)

`Add roles to access token`, trigger Login / Post Login, Node 22. Koden finns i [post-login-roles.js](post-login-roles.js). Den lägger användarens roller i claimet `https://restaurant-api/roles`, som `app.security.roles-claim` pekar på. Actionen är deployad och ligger i flödet under Actions → Triggers → post-login.

En ny användare har ingen roll. Med RBAC får den då inga scopes. Därför ger actionen rollen `GUEST` till varje användare som saknar roll. Den tilldelar rollen i Auth0 via Management API och lägger också `GUEST` och `bookings:write` i den token som skapas vid samma inloggning. Användare som redan finns men saknar roll får `GUEST` vid nästa inloggning. Personal och administratörer får sina roller manuellt som tidigare.

Actionen kräver tre secrets:

| Secret | Värde |
|---|---|
| `M2M_CLIENT_ID` | Client ID för `Restaurant Action Role Assigner` |
| `M2M_CLIENT_SECRET` | Client Secret för samma applikation |
| `GUEST_ROLE_ID` | ID för rollen `GUEST` (User Management → Roles → GUEST, `rol_...`) |

`Restaurant Action Role Assigner` är en Machine to Machine-applikation. Den är auktoriserad för Auth0 Management API med bara scopet `create:role_members`.

## Applikation för Swagger UI (Applications → Applications)

`Restaurant API Swagger UI`, typ Single Page Application. Det är en publik klient med PKCE och utan secret (Token Endpoint Authentication Method `None`).

| Inställning | Värde |
|---|---|
| Allowed Callback URLs | `http://localhost:8080/swagger-ui/oauth2-redirect.html`, `https://app.136-148-214-21.sslip.io`, `https://api.136-148-214-21.sslip.io/swagger-ui/oauth2-redirect.html`, `https://images.136-148-214-21.sslip.io/swagger-ui/oauth2-redirect.html` |
| Allowed Logout URLs | `https://app.136-148-214-21.sslip.io` |
| Allowed Web Origins | `http://localhost:8080`, `https://app.136-148-214-21.sslip.io`, `https://api.136-148-214-21.sslip.io`, `https://images.136-148-214-21.sslip.io` |
| Allowed Origins (CORS) | `http://localhost:8080`, `https://app.136-148-214-21.sslip.io`, `https://api.136-148-214-21.sslip.io`, `https://images.136-148-214-21.sslip.io` |
| Application Login URI | Tom. Frontenden har ingen sida som startar inloggningen direkt. |
| Grant Types | Endast Authorization Code |
| Åtkomst till Restaurant API | User-delegated access med alla tre permissions; ingen client access |

Client ID (`MjuU7HNoxc7r4EFP3SS1ztoutVOu9q2H`) är inte hemligt för en publik klient. Det är standardvärde för `SWAGGER_CLIENT_ID` i `application.yaml`.

## Miljövariabler för API:t

| Variabel | Värde |
|---|---|
| `JWT_ISSUER_URI` | `https://restaurant-api-abbas.eu.auth0.com/` (standardvärde) |
| `JWT_AUDIENCE` | `https://restaurant-api` (standardvärde) |
| `SWAGGER_CLIENT_ID` | Client ID för `Restaurant API Swagger UI` (standardvärde) |

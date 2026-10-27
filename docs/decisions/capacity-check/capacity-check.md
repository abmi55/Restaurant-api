# Kapacitetskontroll vid bokning

Levererad 2026-10-22 (`7f5ebed`).

## Sammanfattning

En ny bokning godkänns bara om restaurangen har platser under hela bokningens tid. En sänkning av `seatCount` som skulle ge överbeläggning avvisas.

## Varför

`BookingService.create` jämförde aldrig med `Restaurant.seatCount`. En restaurang med 40 platser kunde få obegränsat många bokningar samma kväll, och bokningar bakåt i tiden godkändes.

## Beslut

| Fråga | Valt | Bortvalt |
|---|---|---|
| Hur tiden räknas | Varje bokning upptar platser i 120 minuter från starttiden, samma för alla restauranger | Fasta sittningar: begränsar valfri bokningstid |
| Hur beläggning räknas | Högsta samtidiga beläggning under intervallet | Summa av alla överlappande bokningar: avvisar bokningar som ryms |
| Statusar som upptar platser | `REQUESTED`, `CONFIRMED`, `SEATED` | Att `SEATED` frigör platser: gästerna sitter kvar |
| Samtidiga anrop | `PESSIMISTIC_WRITE` på restaurangens rad, timeout 5 s, därefter 503 | Ingen låsning: två anrop kan ta de sista platserna. 409 vid timeout: blandar ihop "fullt" med "försök igen" |
| Var beräkningen ligger | Komponenten `CapacityChecker`, som används av både bokningar och restauranger | En kopia per service: reglerna kan glida isär |
| Aktuell tid | En `Clock` i `Europe/Stockholm` | `LocalDateTime.now()`: följer containerns tidszon |
| Stora sällskap | Tak på 8 gäster, konfigurerbart | Kvot per gäst: inget behov ännu |

## Beteende

- Fullt ger 409 som `ProblemDetail`, utan uppgifter om andra gäster.
- Tid nu eller tidigare, fler än 8 gäster eller fler gäster än `seatCount` ger 400. Dessa fel kommer före 404.
- En bokning som slutar när en annan börjar krockar inte.
- `confirm` och `seat` kontrollerar inte kapaciteten.
- En sänkt `seatCount` som ger överbeläggning ger 409. En höjd eller oförändrad `seatCount` godkänns alltid.
- Utanför: bordsplacering, öppettider, förlängd sittning och sommartidsbyten.

## Verifiering

Alla 50 automatiska tester passerar, och anropen i `request.http/kapacitetskontroll.http` är kontrollerade manuellt. Låset i PostgreSQL och en slutkontroll i drift är inte verifierade.

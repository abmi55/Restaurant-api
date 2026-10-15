# Drift på VPS

Hela systemet körs med Docker Compose på en HostUp VPS (Ubuntu 24.04). Caddy tar emot all trafik på port 80 och 443 och hämtar TLS-certifikat från Let's Encrypt automatiskt.

| Tjänst | Image | Adress |
|---|---|---|
| Frontend | `ghcr.io/abmi55/restaurant-frontend` | `https://$APP_HOST` |
| Restaurang-API | `ghcr.io/abmi55/restaurant-api` | `https://$API_HOST` |
| Bild-API | `ghcr.io/abmi55/restaurant-images` | `https://$IMAGES_HOST` |
| PostgreSQL 17 | `postgres:17-alpine` | bara internt, databaserna `restaurant` och `images` |

Värdnamnen bygger på sslip.io, till exempel `api.136-148-214-21.sslip.io`.

## Uppsättning av VPS:en

Görs en gång, som root:

1. Skapa användaren `deploy`, lägg den i gruppen `sudo` och kopiera `/root/.ssh/authorized_keys` till `/home/deploy/.ssh/`.
2. Kontrollera att `ssh deploy@<ip>` och `sudo whoami` fungerar. Lägg sedan `PermitRootLogin no`, `PasswordAuthentication no` och `KbdInteractiveAuthentication no` i `/etc/ssh/sshd_config.d/00-hardening.conf` och kör `systemctl restart ssh`.
3. Brandvägg: `ufw allow 22/tcp`, `ufw allow 80/tcp`, `ufw allow 443/tcp`, `ufw allow 443/udp` och `ufw enable`. HostUps panelbrandvägg tillåter samma portar.
4. Swap på 2 GB i `/swapfile`, med en rad i `/etc/fstab`.
5. `apt install unattended-upgrades` och `dpkg-reconfigure -plow unattended-upgrades`.
6. Docker Engine och `docker-compose-plugin` från Dockers apt-repo. Lägg `deploy` i gruppen `docker`.

Docker publicerar portar förbi ufw. Därför har bara `caddy` några `ports` i `docker-compose.yml`.

## Första start

Som `deploy`:

```bash
sudo mkdir -p /opt/restaurant
sudo chown deploy:deploy /opt/restaurant
git clone https://github.com/abmi55/07_java_web_services_assignment_individual.git /opt/restaurant
cd /opt/restaurant/deploy
cp .env.example .env
chmod 600 .env
nano .env
bash deploy.sh
```

Fyll i värdnamnen i `.env` och skapa varje lösenord med `openssl rand -hex 32`.

`postgres-init/01-databases.sh` körs bara när volymen `pgdata` är tom. Den skapar rollerna och databaserna `restaurant` och `images`. Om ett lösenord ändras senare måste det också ändras i databasen:

```bash
docker compose exec postgres psql -U postgres -c "ALTER ROLE restaurant PASSWORD '<nytt lösenord>'"
```

Produktionsdatabasen startar tom. Logga in som admin i frontenden och skapa restaurangerna.

## Deploy

```bash
cd /opt/restaurant/deploy
bash deploy.sh
```

Skriptet hämtar driftfilerna med `git pull` och de senaste imagen med `docker compose pull`. Det startar om ändrade containrar och väntar sedan i högst 120 s på att alla tre adresser svarar. Om någon inte svarar avslutas det med felkod.

## Rollback

Sätt taggen till en tidigare commit-SHA i `.env`, till exempel `API_TAG=<sha>`, och kör `bash deploy.sh`. Sätt tillbaka `latest` när felet är rättat.

## Loggar och status

```bash
docker compose ps
docker compose logs -f <tjänst>
```

Tjänsterna heter `postgres`, `restaurant-api`, `restaurant-images`, `frontend` och `caddy`.

Ta aldrig bort volymen `caddy_data`. Den innehåller certifikaten, och Let's Encrypt begränsar hur många nya certifikat som får utfärdas. Volymerna `pgdata` och `images` innehåller all data.

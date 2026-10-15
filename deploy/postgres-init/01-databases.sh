#!/bin/sh
# Runs only when the pgdata volume is empty. To change a password later, use ALTER ROLE in the database.
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v restaurant_pw="$RESTAURANT_DB_PASSWORD" \
    -v images_pw="$IMAGES_DB_PASSWORD" <<'EOSQL'
CREATE ROLE restaurant LOGIN PASSWORD :'restaurant_pw';
CREATE DATABASE restaurant OWNER restaurant;
REVOKE CONNECT ON DATABASE restaurant FROM PUBLIC;

CREATE ROLE images LOGIN PASSWORD :'images_pw';
CREATE DATABASE images OWNER images;
REVOKE CONNECT ON DATABASE images FROM PUBLIC;
EOSQL

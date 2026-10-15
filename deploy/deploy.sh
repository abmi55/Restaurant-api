#!/usr/bin/env bash
# Pulls the latest deploy files and images, restarts changed containers and checks that all three hosts answer.
set -euo pipefail

cd "$(dirname "$0")"

set -a
source .env
set +a

git pull --ff-only
docker compose pull
docker compose up -d --remove-orphans
docker image prune -f

wait_for() {
    local url=$1
    local deadline=$((SECONDS + 120))
    until curl -fsS -o /dev/null "$url"; do
        if ((SECONDS >= deadline)); then
            echo "FAIL: $url did not answer within 120 s" >&2
            return 1
        fi
        sleep 5
    done
    echo "OK: $url"
}

wait_for "https://$API_HOST/health"
wait_for "https://$IMAGES_HOST/health"
wait_for "https://$APP_HOST"

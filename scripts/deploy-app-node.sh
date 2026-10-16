#!/usr/bin/env bash
set -euo pipefail

: "${AWS_REGION:?AWS_REGION is required}"
: "${ECR_REGISTRY:?ECR_REGISTRY is required}"
: "${BACKEND_IMAGE:?BACKEND_IMAGE is required}"
: "${FRONTEND_IMAGE:?FRONTEND_IMAGE is required}"
: "${APP_IMAGE_TAG:?APP_IMAGE_TAG is required}"

APP_DIR=/opt/stackoverflow/app
ENV_FILE="$APP_DIR/.env.app"
COMPOSE_FILE="$APP_DIR/docker-compose.app.yml"

cd "$APP_DIR"

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

sed -i.bak \
  -e '/^BACKEND_IMAGE=/d' \
  -e '/^FRONTEND_IMAGE=/d' \
  -e '/^APP_IMAGE_TAG=/d' \
  "$ENV_FILE"

cat >> "$ENV_FILE" <<EOF
BACKEND_IMAGE=$BACKEND_IMAGE
FRONTEND_IMAGE=$FRONTEND_IMAGE
APP_IMAGE_TAG=$APP_IMAGE_TAG
EOF

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" \
  up -d --no-build --remove-orphans

for attempt in $(seq 1 60); do
  info=$(curl -fsS http://localhost/api/actuator/info 2>/dev/null || true)
  if [[ "$info" == *"$APP_IMAGE_TAG"* ]]; then
    echo "Deployed application version $APP_IMAGE_TAG"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
    exit 0
  fi
  sleep 5
done

echo "Application did not become healthy with version $APP_IMAGE_TAG" >&2
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps >&2
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=100 >&2
exit 1

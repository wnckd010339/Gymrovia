#!/usr/bin/env bash

set -Eeuo pipefail

readonly CONTAINER_NAME="gymrovia"
readonly ROLLBACK_CONTAINER_NAME="gymrovia-rollback"
readonly ENV_FILE="${ENV_FILE:-/opt/gymrovia/.env.docker}"
readonly HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/actuator/health}"

if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <dockerhub-username>/gymrovia sha-<40-character-git-commit>" >&2
    exit 1
fi

readonly IMAGE_REPOSITORY="$1"
readonly IMAGE_TAG="$2"
readonly IMAGE="${IMAGE_REPOSITORY}:${IMAGE_TAG}"

if [[ ! "$IMAGE_REPOSITORY" =~ ^[a-z0-9._-]+/gymrovia$ ]]; then
    echo "Image repository must have the form <dockerhub-username>/gymrovia." >&2
    exit 1
fi

if [[ ! "$IMAGE_TAG" =~ ^sha-[0-9a-f]{40}$ ]]; then
    echo "Image tag must have the form sha-<40-character-git-commit>." >&2
    exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
    echo "Environment file not found: $ENV_FILE" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker is not installed." >&2
    exit 1
fi

rollback() {
    echo "Health check failed. Restoring the previous container." >&2
    docker logs --tail 100 "$CONTAINER_NAME" >&2 || true
    docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

    if docker container inspect "$ROLLBACK_CONTAINER_NAME" >/dev/null 2>&1; then
        docker rename "$ROLLBACK_CONTAINER_NAME" "$CONTAINER_NAME"
        docker start "$CONTAINER_NAME" >/dev/null
        echo "Previous container restored." >&2
    else
        echo "No previous container is available for rollback." >&2
    fi

    exit 1
}

echo "Pulling ${IMAGE}"
docker pull "$IMAGE"

docker rm -f "$ROLLBACK_CONTAINER_NAME" >/dev/null 2>&1 || true

if docker container inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
    echo "Preserving the current container for rollback."
    docker stop --time 35 "$CONTAINER_NAME" >/dev/null
    docker rename "$CONTAINER_NAME" "$ROLLBACK_CONTAINER_NAME"
fi

echo "Starting ${IMAGE}"
if ! docker run -d \
    --name "$CONTAINER_NAME" \
    --restart unless-stopped \
    --env-file "$ENV_FILE" \
    --env SPRING_PROFILES_ACTIVE=prod \
    --publish 127.0.0.1:8080:8080 \
    "$IMAGE" >/dev/null; then
    rollback
fi

for attempt in $(seq 1 60); do
    if curl --fail --silent "$HEALTH_URL" \
        | grep --quiet '"status"[[:space:]]*:[[:space:]]*"UP"'; then
        echo "Deployment succeeded: ${IMAGE}"
        exit 0
    fi

    if [[ "$(docker inspect --format '{{.State.Status}}' "$CONTAINER_NAME" 2>/dev/null || true)" == "exited" ]]; then
        rollback
    fi

    echo "Waiting for health check (${attempt}/60)"
    sleep 2
done

rollback

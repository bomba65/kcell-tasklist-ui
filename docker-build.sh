#/bin/bash

set -e

DOCKER_COMPOSE_YML=$1

if [ -z "$DOCKER_COMPOSE_YML"]; then
	DOCKER_COMPOSE_YML="docker-compose.yml"
fi

BASEDIR=`pwd`

cd "$BASEDIR/asset-management"
./gradlew build

cd "$BASEDIR/kcell-demo-process-app"
./mvnw clean install
./mvnw package

cd "$BASEDIR"
docker-compose -f "$DOCKER_COMPOSE_YML" build

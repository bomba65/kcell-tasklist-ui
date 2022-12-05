#/bin/bash

set -e

DOCKER_COMPOSE_YML=$1
DOCKER_COMPOSE_P=$2

if [ -z "$DOCKER_COMPOSE_YML" ]; then
	DOCKER_COMPOSE_YML="docker-compose.yml"
fi

BASEDIR=`pwd`

cd "$BASEDIR/asset-management"
./gradlew --no-daemon clean build -x test 

cd "$BASEDIR/kcell-process-app"
./gradlew --no-daemon clean build -x test 

cd "$BASEDIR/kcell-tasklist-ui/js"
npm install

cd "$BASEDIR"
#/usr/bin/git log --graph -3 > nginx/html/version.html
echo -e "<pre>$(git log --graph -10)</pre>" > nginx/html/version.html

cd "$BASEDIR"
if [ -z "$DOCKER_COMPOSE_P" ]; then
	docker-compose -f "$DOCKER_COMPOSE_YML" build
else
	docker-compose -f "$DOCKER_COMPOSE_YML" -p "$DOCKER_COMPOSE_P" build
fi


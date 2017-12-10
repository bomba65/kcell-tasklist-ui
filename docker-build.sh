#/bin/bash

set -e

DOCKER_COMPOSE_YML=$1

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
docker-compose -f "$DOCKER_COMPOSE_YML" build

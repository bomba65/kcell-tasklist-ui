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

cd "$BASEDIR/minio-client"
npm install

cd "$BASEDIR"
mkdir -p build/nginx/conf/
echo "return 200" $(git log -1 --pretty=tformat:'"%H"') ";" > build/nginx/conf/version.conf

cd "$BASEDIR"
docker-compose -f "$DOCKER_COMPOSE_YML" build

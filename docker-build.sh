#/bin/bash

set -e

DOCKER_COMPOSE_YML=$1

if [ -z "$DOCKER_COMPOSE_YML"]; then
	DOCKER_COMPOSE_YML="docker-compose.yml"
fi

BASEDIR=`pwd`
MVNREPO="$HOME/.m2/repository"

cd "$BASEDIR/asset-management"
./gradlew clean build

cd "$BASEDIR/kcell-demo-process-app"
./mvnw clean package -Dmaven.test.skip=true

cd "$BASEDIR/ldap-authentication"
./mvnw clean package

cp "$BASEDIR/ldap-authentication/target/ldap-authentication-7.6.0.jar" \
   "$BASEDIR/kcell-camunda/modules/kz/kcell/camunda/authentication/main/"

# cp "$MVNREPO/org/camunda/bpm/extension/camunda-bpm-mail-core/1.1.0/camunda-bpm-mail-core-1.1.0.jar" \
#    "$BASEDIR/kcell-camunda/modules/org/camunda/bpm/extension/camunda-bpm-mail-core/main/camunda-bpm-mail-core-1.1.0.jar"

cd "$BASEDIR"
docker-compose -f "$DOCKER_COMPOSE_YML" build

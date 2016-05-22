#!/bin/bash



d=$(cd `dirname $0` && pwd)

# invoke CF
$d/cf.sh

cd $d/..

./mvnw -DskipTests=true clean package
an=cd-production-promotion
cf push --no-start
cf set-env $an  ARTIFACTORY_API_TOKEN_SECRET $ARTIFACTORY_API_TOKEN_SECRET
cf restart  $an
cf logs --recent $an

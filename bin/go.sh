#!/bin/bash


d=$(cd `dirname $0` && pwd)

# invoke CF
$d/cf.sh

cd $d/..

./mvnw -DskipTests=true clean package
an=cd-production-promotion
cf push --no-start
cf set-env $an PIVOTAL_TRACKER_TOKEN_SECRET $PIVOTAL_TRACKER_TOKEN_SECRET
cf set-env $an ARTIFACTORY_API_TOKEN_SECRET $ARTIFACTORY_API_TOKEN_SECRET
cf set-env $an BINTRAY_USERNAME $BINTRAY_USERNAME
cf set-env $an BINTRAY_PASSWORD $BINTRAY_PASSWORD
cf restart $an
cf logs --recent $an

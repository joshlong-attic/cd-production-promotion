#!/bin/bash

./mvnw -DskipTests=true clean package  
an=cd-production-promotion
#cf push --no-start 
cf set-env $an  ARTIFACTORY_API_TOKEN_SECRET $ARTIFACTORY_API_TOKEN_SECRET
cf start  
cf logs $an

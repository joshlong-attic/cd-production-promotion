#!/bin/bash
./mvnw -DskipTests=true clean package  &&  cf push && cf logs cd-production-promotion 

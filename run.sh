#!/bin/sh

mvn clean package spring-boot:repackage -DskipTests

java -jar target/crawler-1.0.0.jar $@


#!/bin/sh

mvn clean package spring-boot:repackage -DskipTests

java -jar target/crawler-0.9.2.jar $@


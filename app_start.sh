#!/bin/bash
java -jar -Djava.security.egd=file:/dev/urandom -Dhttp.proxyHost=192.168.80.2 -Dhttp.proxyPort=10909 -Dhttps.proxyHost=192.168.80.2 -Dhttps.proxyPort=10909 -Dhttp.nonProxyHosts="127.0.0.1|192.168.*|172.*|*.local" -Dspring.datasource.url=${DB_URL} -Dspring.datasource.username=${DB_USERNAME} -Dspring.datasource.password=${DB_PASSWORD} /app/app.jar

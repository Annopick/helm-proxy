#/bin/bash
java -jar -Djava.security.egd=file:/dev/urandom -Dspring.datasource.url=${DB_URL} -Dspring.datasource.username=${DB_USERNAME} -Dspring.datasource.password=${DB_PASSWORD} /app/app.jar

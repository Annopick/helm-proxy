FROM openjdk:17-ea-17-jdk-slim
LABEL authors="annopick"

WORKDIR /app

COPY target/helm-proxy-1.0.0-RELEASE.jar /app/app.jar

ENV DB_URL=""
ENV DB_USERNAME=""
ENV DB_PASSWORD=""

EXPOSE 8081

COPY app_start.sh /app/app_start.sh
RUN chmod +x /app/app_start.sh

ENTRYPOINT ["/app/app_start.sh"]

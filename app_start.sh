#!/bin/bash

# 解析代理配置
PROXY_OPTS=""

if [ -n "$HTTP_PROXY" ]; then
    HTTP_PROXY_HOST=$(echo $HTTP_PROXY | sed -e 's|^[^/]*//||' -e 's|/.*$||' -e 's|:.*$||')
    HTTP_PROXY_PORT=$(echo $HTTP_PROXY | sed -e 's|^[^/]*//||' -e 's|/.*$||' -e 's|^[^:]*:||')
    PROXY_OPTS="$PROXY_OPTS -Dhttp.proxyHost=$HTTP_PROXY_HOST -Dhttp.proxyPort=$HTTP_PROXY_PORT"
fi

if [ -n "$HTTPS_PROXY" ]; then
    HTTPS_PROXY_HOST=$(echo $HTTPS_PROXY | sed -e 's|^[^/]*//||' -e 's|/.*$||' -e 's|:.*$||')
    HTTPS_PROXY_PORT=$(echo $HTTPS_PROXY | sed -e 's|^[^/]*//||' -e 's|/.*$||' -e 's|^[^:]*:||')
    PROXY_OPTS="$PROXY_OPTS -Dhttps.proxyHost=$HTTPS_PROXY_HOST -Dhttps.proxyPort=$HTTPS_PROXY_PORT"
fi

if [ -n "$NO_PROXY" ]; then
    # 将逗号分隔的NO_PROXY转换为竖线分隔，并将CIDR格式转换为通配符
    NO_PROXY_FORMATTED=$(echo $NO_PROXY | sed -e 's|,|\\||g' -e 's|/[0-9]*||g' -e 's|\.\*|.*|g')
    PROXY_OPTS="$PROXY_OPTS -Dhttp.nonProxyHosts=\"$NO_PROXY_FORMATTED\""
fi

java -jar -Djava.security.egd=file:/dev/urandom $PROXY_OPTS -Dspring.datasource.url=${DB_URL} -Dspring.datasource.username=${DB_USERNAME} -Dspring.datasource.password=${DB_PASSWORD} /app/app.jar

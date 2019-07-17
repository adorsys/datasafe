#!/bin/sh

EXTRA_ARGS=""

if [[ -z "$API_URL" ]]; then
    API_URL="http://localhost:8080"
fi

# Bind API url and credentials
sed -i 's!${API_URL}!'"$API_URL"'!g' "$APP_HOME/frontend/env.js"

# do not expose sensitive data by default
LOGIN=""
PASSWORD=""
if [[ "$EXPOSE_API_CREDS" == "true" ]]; then
    LOGIN="$DEFAULT_USER"
    PASSWORD="$DEFAULT_PASSWORD"
fi

sed -i 's!${API_USERNAME}!'"$LOGIN"'!g' "$APP_HOME/frontend/env.js"
sed -i 's!${API_PASSWORD}!'"$PASSWORD"'!g' "$APP_HOME/frontend/env.js"

# Expose frontend
EXTRA_ARGS="-DSTATIC_RESOURCES=""file://$APP_HOME/frontend/"

java "$EXTRA_ARGS" -jar "$JAR_FILE"

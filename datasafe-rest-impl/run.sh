#!/bin/sh

EXTRA_ARGS=""

# frontend will be available only if $ENABLE_FRONTEND is set
if [[ "$ENABLE_FRONTEND" == "true" ]]; then

    if [[ -z "$API_URL" ]]; then
        API_URL="http://localhost:8080"
    fi

    # Bind API url and credentials
    sed -i 's!${API_URL}!'"$API_URL"'!g' "$APP_HOME/frontend/env.js"
    sed -i 's!${API_USERNAME}!'"$DEFAULT_USER"'!g' "$APP_HOME/frontend/env.js"
    sed -i 's!${API_PASSWORD}!'"$DEFAULT_PASSWORD"'!g' "$APP_HOME/frontend/env.js"

    EXTRA_ARGS="-DSTATIC_RESOURCES=""file://$APP_HOME/frontend/"
fi

java "$EXTRA_ARGS" -jar "$JAR_FILE"

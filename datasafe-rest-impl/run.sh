#!/bin/sh

# Bind API url and credentials
sed -i 's!${API_URL}!'"$API_URL"'!g' "$APP_HOME/frontend/env.js"
sed -i 's!${API_USERNAME}!'"$API_USERNAME"'!g' "$APP_HOME/frontend/env.js"
sed -i 's!${API_PASSWORD}!'"$API_PASSWORD"'!g' "$APP_HOME/frontend/env.js"

java -DSTATIC_RESOURCES="file://$APP_HOME/frontend/" -jar "$JAR_FILE"
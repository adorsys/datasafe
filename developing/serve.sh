#!/bin/bash
# Launches Jekyll server that will serve code from repository, note that you should be in `developing` dir.

DIR=$( cd ".." && pwd )
docker run -p 4000:4000 \
 --mount "type=bind,source=$DIR,target=/github-pages/web/source" \
 -e "PAGES_REPO_NWO=adorsys/datasafe" \
 jekyll_gh_pages:latest
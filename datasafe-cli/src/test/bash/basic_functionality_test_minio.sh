#!/bin/bash

if [[ -z "$1" ]] ; then
  echo "Test expects datasafe-cli executable path as 1st argument"
  exit 1
fi

# Docker not available on TravisCI-provided MacOS, allow to pass
if ! [ -x "$(command -v docker)" ]; then
  echo 'Error: docker is not installed. Will exit with code 0 not to fail pipeline'
  exit 0
fi

CLI="$1"
PROFILE_ROOT="$(pwd)/cli-profiles/"
MINIO_ACCESS_KEY="minio-access-key"
MINIO_SECRET_KEY="minio-secret-key"
MINIO_BUCKET="testBucket"
VOLUME="$(pwd)/minio"
SECRET_TEXT="Secret text"
PRIVATE_ON_MINIO="folder-minio/secret_on_minio.txt"


# create minio volume bucket
mkdir -p "$VOLUME/$MINIO_BUCKET"

# Spin up minio to imitate S3, minio will create MINIO_BUCKET based on volume
docker run -d \
  --name MINIO \
  -p 9000:9000 \
  -e MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY" \
  -e MINIO_SECRET_KEY="$MINIO_SECRET_KEY" \
  --mount type=bind,source="$VOLUME,target=/data" \
  minio/minio \
  server /data || exit 1

# Wait some time for minio to spin up
sleep 5

do_exit() {
  # Stop minio
  echo "Stopping minio"
  docker rm -f MINIO
  exit "$1"
}

echo 'Creating profile'
yes '' | $CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile create || do_exit 1

echo 'Register MINIO storage'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile storage add -i=MINIO -p="http://127.0.0.1:9000/eu-central-1/$MINIO_BUCKET/" || do_exit 1
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile storage credentials add -m="http://.+" -u="$MINIO_ACCESS_KEY" -p="$MINIO_SECRET_KEY" || do_exit 1

echo "$SECRET_TEXT" > my_secret.txt

# Checking how private files work on MINIO:
echo 'Store secret in MINIO'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cp -s=MINIO my_secret.txt "$PRIVATE_ON_MINIO" || do_exit 1

echo 'Verify MINIO'
MINIO_TEXT=$($CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cat -s=MINIO "$PRIVATE_ON_MINIO")
[[ "$MINIO_TEXT" != "$SECRET_TEXT" ]] && echo "Minio has wrong file content" && do_exit 1

do_exit 0
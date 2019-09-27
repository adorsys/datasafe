#!/bin/bash

if [[ -z "$1" ]] ; then
  echo "Test expects datasafe-cli executable path as 1st argument"
  exit 1
fi

CLI="$1"
PROFILE_ROOT="$(pwd)/cli-profiles/"
MINIO_ACCESS_KEY="minio-access-key"
MINIO_SECRET_KEY="minio-secret-key"
MINIO_BUCKET="testBucket"
VOLUME="$(pwd)/minio"
SECRET_TEXT="Secret text"
PRIVATE_ON_FS="folder-fs/secret_on_fs.txt"
INBOX_ON_FS="hello.txt"
PRIVATE_ON_MINIO="folder-minio/secret_on_minio.txt"

# create minio volume bucket
mkdir -p "$VOLUME/$MINIO_BUCKET"

# Spin up minio to imitate S3, minio will create MINIO_BUCKET based on volume
docker run -d \
  -p 9000:9000 \
  -e MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY" \
  -e MINIO_SECRET_KEY="$MINIO_SECRET_KEY" \
  --mount type=bind,source="$VOLUME,target=/data" \
  minio/minio \
  server /data \
  || exit 1

echo 'Creating profile'
yes '' | $CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile create || exit 1

echo 'Register MINIO storage'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile storage add -i=MINIO -p="http://127.0.0.1:9000/eu-central-1/$MINIO_BUCKET/" || exit 1
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile storage credentials add -m="http://.+" -u="$MINIO_ACCESS_KEY" -p="$MINIO_SECRET_KEY" || exit 1

echo "$SECRET_TEXT" > my_secret.txt

echo 'Store secret in FS'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cp my_secret.txt "$PRIVATE_ON_FS" || exit 1

echo 'Verify FS file content'
FS_TEXT=$($CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cat "$PRIVATE_ON_FS")
[[ "$FS_TEXT" != "$SECRET_TEXT" ]] && echo "Filesystem has wrong file content" && exit 1
echo 'Verify FS file list'
FS_LIST=$($CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private ls)
[[ "$FS_LIST" != "$PRIVATE_ON_FS" ]] && echo "Filesystem has wrong file list" && exit 1

echo 'Creating profile to share with'
yes '' | $CLI -u=friend -p=password-friend -sp=system_password -rd="$PROFILE_ROOT" profile create || exit 1
echo 'Sharing file with friend'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" inbox share -s=my_secret.txt -r=friend -f="$INBOX_ON_FS" || exit 1
echo 'Verify FS inbox file content'
FS_INBOX_TEXT=$($CLI -u=friend -p=password-friend -sp=system_password -rd="$PROFILE_ROOT" inbox cat "$INBOX_ON_FS")
[[ "$FS_INBOX_TEXT" != "$SECRET_TEXT" ]] && echo "Filesystem has wrong shared file content" && exit 1
echo 'Verify FS inbox file list'
FS_INBOX_LIST=$($CLI -u=friend -p=password-friend -sp=system_password -rd="$PROFILE_ROOT" inbox ls)
[[ "$FS_INBOX_LIST" != "$INBOX_ON_FS" ]] && echo "Filesystem has wrong shared file list" && exit 1

# Checking how private files work on MINIO:
echo 'Store secret in MINIO'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cp -s=MINIO my_secret.txt "$PRIVATE_ON_MINIO" || exit 1

echo 'Verify MINIO'
MINIO_TEXT=$($CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cat -s=MINIO "$PRIVATE_ON_MINIO")
[[ "$MINIO_TEXT" != "$SECRET_TEXT" ]] && echo "Minio has wrong file content" && exit 1
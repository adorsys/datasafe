#!/bin/bash

if [[ -z "$1" ]] ; then
  echo "Test expects datasafe-cli executable path as 1st argument"
  exit 1
fi

CLI="$1"
PROFILE_ROOT="$(pwd)/cli-profiles/"
SECRET_TEXT="Secret text"
PRIVATE_ON_FS="folder-fs/secret_on_fs.txt"
INBOX_ON_FS="hello.txt"

do_exit() {
  exit "$1"
}

echo 'Creating profile'
yes '' | $CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" profile create || do_exit 1

echo "$SECRET_TEXT" > my_secret.txt

echo 'Store secret in FS'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cp my_secret.txt "$PRIVATE_ON_FS" || do_exit 1

echo 'Verify FS file content'
FS_TEXT=$($CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private cat "$PRIVATE_ON_FS")
[[ "$FS_TEXT" != "$SECRET_TEXT" ]] && echo "Filesystem has wrong file content" && do_exit 1
echo 'Verify FS file list'
FS_LIST=$($CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" private ls)
[[ "$FS_LIST" != "$PRIVATE_ON_FS" ]] && echo "Filesystem has wrong file list" && do_exit 1

echo 'Creating profile to share with'
yes '' | $CLI -u=friend -p=password-friend -sp=system_password -rd="$PROFILE_ROOT" profile create || do_exit 1
echo 'Sharing file with friend'
$CLI -u=user -p=password -sp=system_password -rd="$PROFILE_ROOT" inbox share -s=my_secret.txt -r=friend -f="$INBOX_ON_FS" || do_exit 1
echo 'Verify FS inbox file content'
FS_INBOX_TEXT=$($CLI -u=friend -p=password-friend -sp=system_password -rd="$PROFILE_ROOT" inbox cat "$INBOX_ON_FS")
[[ "$FS_INBOX_TEXT" != "$SECRET_TEXT" ]] && echo "Filesystem has wrong shared file content" && do_exit 1
echo 'Verify FS inbox file list'
FS_INBOX_LIST=$($CLI -u=friend -p=password-friend -sp=system_password -rd="$PROFILE_ROOT" inbox ls)
[[ "$FS_INBOX_LIST" != "$INBOX_ON_FS" ]] && echo "Filesystem has wrong shared file list" && exit 1

do_exit 0
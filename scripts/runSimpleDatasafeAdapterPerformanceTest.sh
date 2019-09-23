#!/bin/sh
ERR=
REQ_ENVS="AWS_ACCESS_KEY AWS_SECRET_KEY AWS_BUCKET AWS_REGION"
for ENV in ${REQ_ENVS}; do
  if [ -z "${!ENV}" ] ; then
    echo "ERROR: missing ${ENV} environment variable"
    ERR=1
  fi
done

help()
{
  echo
  echo "********************************************************************************************"
  echo "Script usage:"
  echo "Before running test script ensures that environment variables are set."
  echo "List of used variables:"
  echo "AWS_ACCESS_KEY"
  echo "AWS_SECRET_KEY"
  echo "AWS_BUCKET"
  echo "AWS_REGION"
  echo "AWS_URL - optional. Used when storage location is not default aws s3 (for example for ceph)"
  echo "********************************************************************************************"
}

if [ "$ERR" = "1" ]; then
  help
  exit 1
fi

cd "$(dirname "$0")/../datasafe-long-run-tests/datasafe-business-tests-random-actions/" || exit
mvn \
-DAWS_ACCESS_KEY="$AWS_ACCESS_KEY" \
-DAWS_SECRET_KEY="$AWS_SECRET_KEY" \
-DAWS_BUCKET="$AWS_BUCKET" \
-DAWS_URL="$AWS_URL" \
-DAWS_REGION="$AWS_REGION" \
-DtestArgs="-Xmx256m -DSTORAGE_PROVIDERS="AMAZON" -DFIXTURE_SIZE="SMALL" -DTHREADS=2 -DFILE_SIZES=100" \
-Dtest=RandomActionsOnSimpleDatasafeAdapterTest test
#!/bin/sh
if [ $# -eq 0 ]; then
    echo "No arguments provided"
    echo "Example usage: sh runSimpleDatasafeAdapterPerformanceTest.sh ACCESS=your_aws_access_key SECRET=your_aws_secret_key BUCKET=bucket_name"
    exit 1
fi

for ARGUMENT in "$@"
do
    KEY=$(echo $ARGUMENT | cut -f1 -d=)
    VALUE=$(echo $ARGUMENT | cut -f2 -d=)

    case "$KEY" in
            ACCESS)    AWS_ACCESS_KEY=${VALUE} ;;
            SECRET)    AWS_SECRET_KEY=${VALUE} ;;
            BUCKET)    AWS_BUCKET=${VALUE} ;;
            *)
    esac
done

cd "$(dirname "$0")/../datasafe-long-run-tests/datasafe-business-tests-random-actions/" || exit
mvn \
-DAWS_ACCESS_KEY="$AWS_ACCESS_KEY" \
-DAWS_SECRET_KEY="$AWS_SECRET_KEY" \
-DAWS_BUCKET="$AWS_BUCKET" \
-DDEFAULT_USER="username" \
-DDEFAULT_PASSWORD="password" \
-DAWS_REGION="eu-central-1" \
-DtestArgs="-Xmx256m -DSTORAGE_PROVIDERS="AMAZON" -DFIXTURE_SIZE="SMALL" -DTHREADS=2 -DFILE_SIZES=100" \
-Dtest=RandomActionsOnSimpleDatasafeAdapterTest test
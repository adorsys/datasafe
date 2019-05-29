#!/bin/bash
set -x

URL=http://localhost:8080
URL=http://datasafe-rest-server-psp-docusafe-performancetest.cloud.adorsys.de

trap error ERR

function error () {
	print "  A N    E R R O R    O C C U R R E D"
	exit 1
}
function print () {
	{
	echo "$(date) ==================================================================================="
	echo "$(date) $*"
	echo "$(date)  "
	} | tee -a curl.log
}


function checkCurl() {
	status=$1
	shift
	rm -f curl.out
	rm -f curl.error
	curl "$@" > curl.out 2>curl.error
	ret=$?
	httpStatus=ERROR
	if (( ret==0 )) 
	then
		print "curl went ok $ret"
		cat curl.out >> curl.log
		httpStatus=$(cat curl.out | head -n 1 | cut -d$' ' -f2)
	fi
	if (( ret==22 )) 
	then
		print "curl went error $ret"
		cat curl.error >> curl.log
		httpStatus=$(cat curl.error)
                httpStatus=$(echo ${httpStatus##*The requested URL returned error: })
	fi
	rm -f curl.error
	if [[ httpStatus -eq "ERROR" ]]
	then
		print "exit now due to previous error with exit code $ret"
		exit $ret
	fi

	if [[ status -eq "any" ]]
	then
		print "$httpStatus is ignored"
	else
		if (( httpStatus!=status )) 
		then
			print "expected status $status but was $httpStatus of cmd $@"
			exit 1
		else
			print "as expected status was $httpStatus of cmd $@"
		fi
	fi
}


checkCurl 200 -f -X PUT "$URL/user" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"password\": \"peter\", \"userName\": \"peter2\"}"

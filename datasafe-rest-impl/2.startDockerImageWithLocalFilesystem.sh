#
# trap with sigint because container can not be stopped from command line otherwise
#

trap stop SIGINT

function stop() {
	echo stop
	docker stop -t 0 datasafe-rest-test
}

mkdir target/ROOT_BUCKET

docker run 										                                        \
	--rm 											                                    \
	--name datasafe-rest-test								                            \
	-e JWT_SECRET=your_personal_secret                                               	\
    -e DEFAULT_USER=your_user_to_come_to_the_rest_api 							        \
    -e DEFAULT_PASSWORD=password_for_rest_api 								            \
	-p 8080:8080 										                                \
	-e ENABLE_FRONTEND=true 								                            \
	-e USE_FILESYSTEM=/usr/app/ROOT_BUCKET                                              \
	-v $(pwd)/target/ROOT_BUCKET:/usr/app/ROOT_BUCKET                                   \
	datasafe-rest-test:latest > startDockerImage.log 2>&1 &

echo "http://localhost:8080/static/index.html" | pbcopy
echo =====================================================
echo "PLEASE VISIT http://localhost:8080/static/index.html"
echo
echo "this url is already in your clipboard :-)"
echo "pres CTRL-C to stop container"
echo
echo "All files created with the client will be stored locally in target/ROOT_BUCKET"

sleep 7

tail -n 1000 -f startDockerImage.log

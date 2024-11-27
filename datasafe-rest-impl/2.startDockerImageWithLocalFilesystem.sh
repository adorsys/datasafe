#
# trap with sigint because container can not be stopped from command line otherwise
#

echo =====================================================
echo "PLEASE VISIT http://localhost:8080/static/index.html"
echo
which pbcopy
if (( $? == 0 ))
then
    echo "http://localhost:8080/static/index.html" | pbcopy
    echo "this url is already in your clipboard :-)"
fi
echo "pres CTRL-C to stop container"
echo
echo =====================================================


mkdir target/ROOT_BUCKET

docker run 										                                        \
	--rm 											                                    \
	-it 											                                    \
	--name datasafe-rest-test								                            \
	-e JWT_SECRET=jnknjknvkjdfnjkvnkdfnvjkndfivfnjkvnskcnncjksnjkvndjfknjkvndfknvjk 	\
    -e DEFAULT_USER=your_user_to_come_to_the_rest_api 							        \
    -e DEFAULT_PASSWORD=password_for_rest_api 								            \
	-p 8080:8080 										                                \
	-e EXPOSE_API_CREDS=true 								                            \
	-e USE_FILESYSTEM=file:///usr/app/ROOT_BUCKET                                       \
	-v $(pwd)/target/ROOT_BUCKET:/usr/app/ROOT_BUCKET                                   \
	datasafe-rest-test:latest


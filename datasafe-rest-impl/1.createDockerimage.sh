trap error ERR
function error() {
	echo "failed due to error"
	echo pwd $(pwd)
	pwd
	exit 1
}


mvn clean install -DskipTests
cd ../frontend/datasafe-ui 
npm install 
ng build --deploy-url /static/ --base-href /static/ 
mv dist ../../datasafe-rest-impl/target/dist 
cd ../../datasafe-rest-impl
docker build . -t datasafe-rest-test:latest --build-arg JAR_FILE=datasafe-rest-impl-*.jar

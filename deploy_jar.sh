mvn clean
mvn package
mvn assembly:single
cp target/jar2lib-1.0-SNAPSHOT-deps.jar $1

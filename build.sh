source ./env.sh

"$MVN_HOME/mvn" --version
"$MVN_HOME/mvn" clean install -DskipTests
source ../env.sh

"$MVN_HOME/mvn" --version -o
"$MVN_HOME/mvn" jetty:run -X -Pjetty-windows -o
#JAVA_HOME="/cygdrive/C/Program Files/Java/jdk1.5.0_22"
echo $JAVA_HOME
export JAVA_HOME="/cygdrive/C/Program Files/Java/jdk1.5.0_22"
MVN_HOME="/cygdrive/C/Program Files (x86)/apache-maven-2.2.1/bin"
echo $JAVA_HOME
"$MVN_HOME/mvn" --version
"$MVN_HOME/mvn" jetty:run
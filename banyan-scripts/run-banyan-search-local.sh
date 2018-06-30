# I don't know why I need both of these on the classpath, but I think I do
# note - you have to run this from the parent folder
# BUT that's not working, unless I copy commands - need to learn how in linux
export CLASSPATH="`ls ./banyan-search/target/banyan-search-*-SNAPSHOT.jar`;./banyan-js/src/main"
echo "CLASSPATH=$CLASSPATH"
java -cp "$CLASSPATH" org.springframework.boot.loader.JarLauncher
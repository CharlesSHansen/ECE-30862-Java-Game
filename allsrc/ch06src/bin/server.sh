export CLASSPATH=.:./tmpclasses:lib/log4j-1.2.6.jar:$CLASSPATH

java -server -cp $CLASSPATH com.hypefiend.javagamebook.server.GameServer

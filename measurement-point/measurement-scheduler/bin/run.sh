#!/bin/bash
java -Xmx256m -Djava.net.preferIPv4Stack=true -jar target/measurement-scheduler-0.1.one-jar.jar $*
##
#Use below to run JMX agent thread and memory monitoring
#java -Xmx256m -Dmaddash.jmx.port=8080 -javaagent:target/measurement-scheduler-0.1-jmx.jar -Djava.net.preferIPv4Stack=true -jar target/measurement-scheduler-0.1.one-jar.jar  $*


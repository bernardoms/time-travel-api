FROM openjdk:11

WORKDIR app

RUN curl -O "http://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip" && \
unzip newrelic-java.zip -d .

ADD target/*.jar /app/time-travel-api.jar

CMD java -javaagent:/app/newrelic/newrelic.jar  $JAVA_OPTS -jar /app/time-travel-api.jar

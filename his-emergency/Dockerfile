FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -XX:MaxRAMPercentage=75"
COPY target/*.jar /app.jar
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]

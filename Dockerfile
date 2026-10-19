FROM eclipse-temurin:21-jre-jammy

# CI에서 bootJar로 만든 실행 가능한 jar를 app.jar로 복사해 둔 파일
COPY app.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]

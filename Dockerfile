FROM adoptopenjdk/openjdk11:alpine-jre

ENV PROFILE ""
ARG ARTIFACT_PATH=./target/CacheReload*.jar
EXPOSE 8080
EXPOSE 9091
COPY ${ARTIFACT_PATH} /opt/shan/CacheReload.jar
WORKDIR /opt/shan/
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "/opt/shan/CacheReload.jar"]
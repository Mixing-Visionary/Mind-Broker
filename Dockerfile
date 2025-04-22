FROM eclipse-temurin:19-jdk-jammy
WORKDIR /mind-broker

COPY target/*.jar mind-broker.jar

ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "mind-broker.jar"]
FROM eclipse-temurin:19-jre-jammy
WORKDIR /mind-broker
COPY ./mind-broker.jar ./app.jar

ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
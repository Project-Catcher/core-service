FROM amd64/openjdk:17-buster

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java","-jar","/app/app.jar"]
# stage 1: build
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /seraphina

USER root

COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle settings.gradle gradle.properties ./

RUN chmod 755 ./gradlew

RUN ./gradlew dependencies --no-daemon || return 0

COPY ./src ./src

RUN ./gradlew dockerBuild -x test --no-daemon

# stage 2: run
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /seraphina

USER root

COPY --from=build /seraphina/build/libs/app.jar /seraphina/release/libs/app.jar

CMD ["java", "-jar", "/seraphina/release/libs/app.jar"]
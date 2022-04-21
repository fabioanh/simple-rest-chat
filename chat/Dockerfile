FROM openjdk:17-jdk-alpine AS TEMP_BUILD_IMAGE
MAINTAINER fabio.navarrete

WORKDIR /app/

COPY build.gradle.kts settings.gradle.kts gradlew /app/
RUN true
COPY gradle /app/gradle
RUN true
COPY ./build.gradle.kts ./build.gradle.kts
RUN true
# Copy the project sources
COPY ./src ./src

RUN ./gradlew build -x test || return 0
RUN true
COPY . .
RUN true
RUN ./gradlew build -x test


FROM openjdk:17-jdk-alpine

WORKDIR /app/

EXPOSE 8080
COPY --from=TEMP_BUILD_IMAGE /app/build/libs/chat-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java","-jar","chat-0.0.1-SNAPSHOT.jar"]
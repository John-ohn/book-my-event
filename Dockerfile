# BUILD STAGE
FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# RUNTIME STAGE
FROM amazoncorretto:17
ARG APP_VERSION=0.1
ARG PROFILE=prod
WORKDIR /app
COPY --from=build /build/target/BookMyEvent-*.jar /app/
EXPOSE 8080
ENV JAR_VERSION=${APP_VERSION}
ENV ACTIVE_PROFILE=${PROFILE}
CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} BookMyEvent-${JAR_VERSION}.jar
#ENTRYPOINT ["java", "-jar", "bookti.jar"]

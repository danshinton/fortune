#
# The next two commands are needed to get an Java 17 running on alpine on
# Apple Silicon. If you are running on an Intel platform, you can simply
# use the following in place of thesse two commands:
#
#  FROM openjdk:17-alpine
#
FROM arm64v8/alpine:3.16

RUN apk add --no-cache openjdk17-jre

#
# This signing key is for testing only. Please overide this using a secret
# when deployed to production.
#
ENV FORTUNE_JWT_SIGNING_KEY=Tdk/EboSiyf/LmIdJKDn9KgNMLgJweHPe3qKABh6gCIuCLhQ4J11xYuV6OvB2/aiaJQ/mo9zZddZSxvoSlZftg== \
    FORTUNE_PUBLIC_HOST=localhost

#
# Make sure the project has been recently built or you may include an old jar
#
COPY fortune-api/target/fortune-api.jar /root/fortune-api.jar

WORKDIR /root

CMD ["java", "-server", "-jar", "/root/fortune-api.jar"]

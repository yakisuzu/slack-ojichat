FROM adoptopenjdk:11-jre-hotspot

RUN apt-get update && apt-get install golang-go -y

RUN go get -u github.com/greymd/ojichat

CMD ["java", "-jar", "slack-ojichat.jar"]

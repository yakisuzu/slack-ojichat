FROM adoptopenjdk:11-jre-hotspot

RUN apt-get update
RUN apt-get install golang-go git -y

RUN go get -u github.com/greymd/ojichat
ENV PATH $PATH:~/go/bin

CMD ["java", "-jar", "slack-ojichat.jar"]

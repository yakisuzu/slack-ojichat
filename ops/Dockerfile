FROM adoptopenjdk:11-jre-hotspot

RUN apt-get update \
  && apt-get install -y --no-install-recommends \
  golang-go \
  git

RUN go get -u github.com/greymd/ojichat
ENV PATH $PATH:/root/go/bin

COPY slack-ojichat.jar .
CMD ["java", "-jar", "slack-ojichat.jar"]

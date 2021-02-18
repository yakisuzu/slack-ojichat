# マルチステージビルドしたい
FROM adoptopenjdk:11-jre-hotspot
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ Asia/Tokyo
ENV PATH $PATH:/root/go/bin
ARG HEROKU_APP_NAME
ENV HEROKU_APP_NAME ${HEROKU_APP_NAME}
ARG SLACK_TOKEN
ENV SLACK_TOKEN ${SLACK_TOKEN}

RUN apt-get update \
  && apt-get install -y --no-install-recommends \
  tzdata \
  golang-go \
  git

RUN go get -u github.com/greymd/ojichat

COPY slack-ojichat.jar .
CMD ["java", "-jar", "slack-ojichat.jar"]

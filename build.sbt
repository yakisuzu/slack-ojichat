name := "slack-ojichat"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "com.typesafe.akka" %% "akka-protobuf" % "2.5.23",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "org.typelevel" %% "cats-core"   % "1.6.1",
  "org.typelevel" %% "cats-effect" % "1.3.1",
)

assemblyOutputPath in assembly := file(s"ops/${name.value}.jar")

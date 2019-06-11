name := "slack-ojichat"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.typelevel"              %% "cats-core"     % "1.6.1",
  "org.typelevel"              %% "cats-effect"   % "1.3.1"
)
libraryDependencies ++= Seq(
  "ch.qos.logback"   % "logback-classic" % "1.2.3",
  "com.typesafe"     % "config"          % "1.3.4",
  "com.ullink.slack" % "simpleslackapi"  % "1.2.0",
  "org.scalatest"    % "scalatest_2.12"  % "3.0.5" % "test"
)

assemblyOutputPath in assembly := file(s"ops/${name.value}.jar")

// https://docs.scala-lang.org/overviews/compiler-options/index.html
scalacOptions ++= Seq(
  // Standard Settings
  "-language:_",  // Enable or disable language features
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf8",       // Specify character encoding used by source files.
  "-feature",   // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  // Private Settings
  "-opt:l:inline",    // Enable cross-method optimizations (note: inlining requires -opt-inline-from): l:method,inline.
  "-opt-inline-from", // Patterns for classfile names from which to allow inlining,
  // Warning Settings
  "-Xfatal-warnings",      // Fail the compilation if there are any warnings.
  "-Ywarn-dead-code",      // Warn when dead code is identified.
  "-Ywarn-value-discard",  // Warn when non-Unit expression results are unused.
  "-Ywarn-numeric-widen",  // Warn when numerics are widened.
  "-Ywarn-unused:_",       // Enable or disable specific unused warnings
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-self-implicit",  // Warn when an implicit resolves to an enclosing self-definition.
  "-Xlint:_"               // Enable or disable specific warnings
)

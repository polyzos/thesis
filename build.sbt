name := "twitter-crawler"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.19" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % Test,

  "com.danielasfregola" %% "twitter4s" % "5.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.json4s" %% "json4s-jackson" % "3.6.3"
)

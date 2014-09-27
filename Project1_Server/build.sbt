name := "Project1_Server"

version := "1.0"

scalaVersion := "2.11.2"

mainClass := Some("BitCoin")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-remote" % "2.3.6"
)

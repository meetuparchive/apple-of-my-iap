description := "IAP API Interfaces"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.2.5" excludeAll(
    ExclusionRule(name="scala-reflect"),
    ExclusionRule(name="mockito-all")),
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" %"1.2",
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.10.1",
  "com.ning" % "async-http-client" % "1.7.16",
  "org.slf4j" % "slf4j-log4j12" % "1.6.2",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test" )

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.meetup"

version := "0.1"

bintrayOrganization := Some("meetup")

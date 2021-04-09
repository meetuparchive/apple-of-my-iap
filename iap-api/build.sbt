description := "IAP API Interfaces"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.2.11",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" %"1.2",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.2",
  "com.ning" % "async-http-client" % "1.7.16",
  "org.slf4j" % "slf4j-log4j12" % "1.6.2"
)

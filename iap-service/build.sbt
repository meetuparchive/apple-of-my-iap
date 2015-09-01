Revolver.settings

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-io" % "1.3.2",
  "net.databinder" %% "unfiltered-filter" % "0.7.1",
  "net.databinder" %% "unfiltered-directives" % "0.7.1",
  "net.databinder" %% "unfiltered-jetty" % "0.7.1"
)

description := "Mock Apple IAP Server"

fork in Test := true

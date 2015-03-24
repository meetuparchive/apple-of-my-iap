seq(Common.subprojectSettings:_*)

libraryDependencies := Seq("net.databinder" %% "unfiltered-jetty" % "0.7.1")

description := "Mock Apple IAP Server"

fork in Test := true

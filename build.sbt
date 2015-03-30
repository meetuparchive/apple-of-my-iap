seq(Common.projectSettings:_*)

description := "IAP API Interfaces"

libraryDependencies ++=
  Seq(
    "org.json4s" %% "json4s-native" % "3.2.5" excludeAll(
      ExclusionRule(name="scala-reflect"),
      ExclusionRule(name="mockito-all")),
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" %"1.2")

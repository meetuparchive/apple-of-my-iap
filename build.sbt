lazy val commonSettings = Seq(
  organization := "com.meetup",
  version := "0.6",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  scalaVersion := "2.11.7",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.11.5" % "test"
  )
)

bintrayOrganization := Some("meetup")
bintrayRepository := "maven"

lazy val `apple-of-my-iap` = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    test := {} /* No tests in the root project and this prevents an annoying
                  message. */
  )
  .aggregate(`iap-api`, `iap-service`)

lazy val `iap-api` = project.settings(commonSettings: _*)
lazy val `iap-service` = project.settings(commonSettings: _*).dependsOn(`iap-api`)

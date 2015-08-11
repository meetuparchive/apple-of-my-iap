lazy val `iap-api` = project
lazy val `iap-service` = project.dependsOn(`iap-api`) 

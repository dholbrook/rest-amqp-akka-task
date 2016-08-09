resolvers += "Flyway" at "http://flywaydb.org/repo"

libraryDependencies ++= {
  val mysqlConnectorJavaVersion = "5.1.35"
  Seq(
    "mysql" % "mysql-connector-java" % mysqlConnectorJavaVersion
  )
}

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.2.1")

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.2.6")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "0.2.11")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")
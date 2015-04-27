resolvers += "Flyway" at "http://flywaydb.org/repo"

libraryDependencies ++= {
  val mysqlConnectorJavaVersion = "5.1.35"
  Seq(
    "mysql" % "mysql-connector-java" % mysqlConnectorJavaVersion
  )
}

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.2.1")

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.2.6")
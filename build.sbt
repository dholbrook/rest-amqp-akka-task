name         := "rest-amqp-akka-task"
organization := "com.example"
scalaVersion := "2.11.8"
version      := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

resolvers += "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/"

libraryDependencies ++= {
  val akkaVersion               = "2.4.4"
  val akkaStreamVersion         = "2.4.4"
  val scalaTestVersion          = "2.2.6"
  val akkaAmqpClientVersion     = "1.5"
  val logbackClassicVersion     = "1.1.7"
  val scalaikejdbcVersion       = "2.3.5"
  val commonsDbcpVersion        = "1.4"
  val mysqlConnectorJavaVersion = "5.1.38"
  val h2Version                 = "1.4.191"
  val flywayVersion             = "4.0"
  Seq(
    "com.typesafe.akka"     %% "akka-actor"                              % akkaVersion,
    "com.typesafe.akka"     %% "akka-stream"                             % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-core"                          % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-spray-json-experimental"       % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-testkit"                       % akkaStreamVersion,
    "com.github.sstone"     %% "amqp-client"                             % akkaAmqpClientVersion,
    "ch.qos.logback"        %  "logback-classic"                         % logbackClassicVersion,
    "org.scalikejdbc"       %% "scalikejdbc"                             % scalaikejdbcVersion,
    "org.scalikejdbc"       %% "scalikejdbc-config"                      % scalaikejdbcVersion,
    "commons-dbcp"          %  "commons-dbcp"                            % commonsDbcpVersion,
    "mysql"                 %  "mysql-connector-java"                    % mysqlConnectorJavaVersion,
    "org.scalatest"         %% "scalatest"                               % scalaTestVersion            % "test",
    "org.scalikejdbc"       %% "scalikejdbc-test"                        % scalaikejdbcVersion         % "test",
    "com.h2database"        %  "h2"                                      % h2Version                   % "test",
    "org.flywaydb"          %  "flyway-core"                             % flywayVersion               % "test"
  )
}

//flyway migrations
seq(flywaySettings: _*)
flywayUrl := "jdbc:mysql://192.168.59.103/task"
flywayUser := "task"
flywayPassword := "taskpwd"

//reverse engineering support
scalikejdbcSettings
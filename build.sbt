name         := "rest-amqp-akka-task"
organization := "com.example"
scalaVersion := "2.11.6"
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
  val akkaVersion               = "2.3.10"
  val akkaStreamVersion         = "1.0-RC1"
  val scalaTestVersion          = "2.2.1"
  val akkaAmqpClientVersion     = "1.4"
  val logbackClassicVersion     = "1.1.2"
  val scalaikejdbcVersion       = "2.2.6"
  val mysqlConnectorJavaVersion = "5.1.35"
  val h2Version                 = "1.4.187"
  val flywayVersion             = "3.2.1"
  Seq(
    "com.typesafe.akka"     %% "akka-actor"                              % akkaVersion,
    "com.typesafe.akka"     %% "akka-stream-experimental"                % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-core-experimental"             % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-scala-experimental"            % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-spray-json-experimental"       % akkaStreamVersion,
    "com.typesafe.akka"     %% "akka-http-testkit-scala-experimental"    % akkaStreamVersion,
    "com.github.sstone"     %% "amqp-client"                             % akkaAmqpClientVersion,
    "ch.qos.logback"        %  "logback-classic"                         % logbackClassicVersion,
    "org.scalikejdbc"       %% "scalikejdbc"                             % scalaikejdbcVersion,
    "org.scalikejdbc"       %% "scalikejdbc-config"                      % scalaikejdbcVersion,
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
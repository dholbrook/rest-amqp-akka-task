package com.example.task

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.Http
import com.example.task.actors.{AkkaModule, TaskActorModule}
import com.example.task.amqp.AmqpModule
import com.example.task.rest.RouteModule
import scalikejdbc.config.{DBs, DBsWithEnv}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object TodoApplication
    extends App
    with ConfigModule
    with AkkaModule
    with TaskActorModule
    with RouteModule
    with AmqpModule {

  val banner = """|
      |  ______           __      __    _      __
      | /_  __/___ ______/ /__   / /   (_)____/ /_
      |  / / / __ `/ ___/ //_/  / /   / / ___/ __/
      | / / / /_/ (__  ) ,<    / /___/ (__  ) /_
      |/_/  \__,_/____/_/|_|  /_____/_/____/\__/
      |
    """.stripMargin

  println(banner)
  println("Application Starting")

  DBsWithEnv("production").setupAll()

  val httpConfig = HttpConfig(config)
  Http().bindAndHandle(handler = routes,
                       interface = httpConfig.interface,
                       port = httpConfig.port)

  sys.addShutdownHook {
    actorSystem.terminate()
    DBs.closeAll()
    println("Application Stopped")
  }

}

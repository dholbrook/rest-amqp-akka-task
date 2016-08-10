package com.example.task

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.task.amqp.AmqpModule
import com.example.task.domain.TaskActor
import com.example.task.rest.RouteModule
import com.typesafe.config.{Config, ConfigFactory}
import scalikejdbc.config.DBsWithEnv

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object TaskListApplication {

  val banner = """|
      |  ______           __      __    _      __
      | /_  __/___ ______/ /__   / /   (_)____/ /_
      |  / / / __ `/ ___/ //_/  / /   / / ___/ __/
      | / / / /_/ (__  ) ,<    / /___/ (__  ) /_
      |/_/  \__,_/____/_/|_|  /_____/_/____/\__/
      |
    """.stripMargin

  def main(args: Array[String]): Unit = {

    println(banner)
    println("Task List Application Starting")

    //Configuration
    val config: Config = ConfigFactory.load()
    val httpConfig     = HttpConfig(config)

    //Akka Actor System
    implicit val actorSystem                                = ActorSystem("task-list-actor-system", config)
    implicit val materializer                               = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    //Database initialization
    DBsWithEnv("production").setupAll()

    //Domain Actor
    val taskActorRef = actorSystem.actorOf(TaskActor.props(), "task")

    //Http Interface
    val routeModule = new RouteModule(taskActorRef)
    val httpBindingFuture = Http().bindAndHandle(
        handler = routeModule.routes,
        interface = httpConfig.interface,
        port = httpConfig.port
    )
    httpBindingFuture.onComplete {
      case Success(binding) ⇒
        actorSystem.log.info(s"Server bound to ${httpConfig.interface}:${httpConfig.port}")
      case Failure(e) ⇒
        actorSystem.log.error(s"Binding failed with ${e.getMessage}", e)
        actorSystem.terminate()
    }

    //Amqp Interface
    val amqpModule = new AmqpModule(taskActorRef)
  }
}
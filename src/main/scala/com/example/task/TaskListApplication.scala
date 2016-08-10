package com.example.task

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.example.task.amqp.{AmqpConfig, AmqpModule}
import com.example.task.domain.TaskActor
import com.example.task.http.{HttpConfig, HttpModule}
import com.typesafe.config.{Config, ConfigFactory}
import scalikejdbc.config.DBsWithEnv

import scala.concurrent.ExecutionContextExecutor

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
    val amqpConfig = AmqpConfig(config)

    //Akka Actor System
    implicit val actorSystem                                = ActorSystem("task-list-actor-system", config)
    implicit val materializer: ActorMaterializer            = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    //Database initialization
    DBsWithEnv("production").setupAll()

    //Domain Actor
    val taskActorRef = actorSystem.actorOf(TaskActor.props(), "task")

    //Http Interface
    val routeModule = new HttpModule(taskActorRef, httpConfig)

    //Amqp Interface
    val amqpModule = new AmqpModule(taskActorRef, amqpConfig)
  }
}

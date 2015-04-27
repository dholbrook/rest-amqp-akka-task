package com.example.task.actors

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import com.example.task.ConfigComponent

import scala.concurrent.ExecutionContextExecutor

trait AkkaComponent {
  implicit def actorSystem: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit def flowMaterializer: ActorFlowMaterializer
}

trait AkkaModule extends AkkaComponent {
  self: ConfigComponent =>

  override implicit lazy val actorSystem: ActorSystem = ActorSystem("todo-actor-system", config)
  override implicit lazy val executor: ExecutionContextExecutor = actorSystem.dispatcher
  override implicit lazy val flowMaterializer: ActorFlowMaterializer = ActorFlowMaterializer()
}


package com.example.task.actors

import akka.actor.{ActorRef, Props}

trait TaskActorComponent {
  def taskActorRef: ActorRef
}

trait TaskActorModule extends TaskActorComponent {
  self: AkkaComponent =>

  override lazy val taskActorRef = actorSystem.actorOf(Props(classOf[TaskActor]), "task")
}
package com.example.task.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.example.task.actors.TaskActor._
import com.example.task.actors.{AkkaComponent, TaskActorComponent, TaskProtocol}
import com.example.task.models._

import scala.concurrent.duration._

trait RouteModule extends TaskProtocol {
  self: AkkaComponent with TaskActorComponent =>

  private implicit val timeout = Timeout(3.seconds)

  val routes = {
    logRequestResult("rest-amqp-akka-task") {
      pathPrefix("task") {
        (get & pathEnd) {
          complete {
            (taskActorRef ? FindAllTasks).mapTo[List[Task]]
          }
        } ~
          (get & path(LongNumber)) { id =>
            complete {
              (taskActorRef ? FindOneTask(id)).mapTo[Some[Task]]
            }
          }
      } ~
        (post & entity(as[CreateTask])) { createTask =>
          complete {
            (taskActorRef ? createTask).mapTo[Task]
          }
        } ~
        (put & path(LongNumber) & entity(as[SaveTask])) { (id,saveTask) =>
          complete {
            (taskActorRef ? saveTask).mapTo[Task]
          }
        }
    }
  }

}

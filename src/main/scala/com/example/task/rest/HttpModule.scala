package com.example.task.rest

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.task.HttpConfig
import com.example.task.domain.TaskActor._
import com.example.task.domain.{TaskJsonProtocol, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class HttpModule(taskActorRef: ActorRef, httpConfig: HttpConfig)(implicit actorSystem: ActorSystem,
                                                                 executionContext: ExecutionContext,
                                                                 materializer: ActorMaterializer)
    extends TaskJsonProtocol {

  private implicit val timeout = Timeout(3.seconds)

  // format: off
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
  // format: on

  val httpBindingFuture = Http().bindAndHandle(
      handler = routes,
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

}

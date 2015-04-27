package com.example.task.actors

import akka.actor._
import com.example.task.actors.TaskActor._
import com.example.task.models.Task
import spray.json.DefaultJsonProtocol

trait TaskProtocol extends DefaultJsonProtocol {
  implicit val taskFormat = jsonFormat3(Task.apply)
  implicit val newTaskFormat = jsonFormat1(CreateTask.apply)
  implicit val updateTaskFormat = jsonFormat3(SaveTask.apply)
  implicit val findOneTaskFormat = jsonFormat1(FindOneTask.apply)
}

object TaskActor {

  sealed trait TaskCommand

  case class CreateTask(description: String) extends TaskCommand

  case class SaveTask(id: Long, description: String, complete: Boolean) extends TaskCommand

  sealed trait TaskQuery

  case class FindOneTask(id: Long) extends TaskQuery

  case object FindAllTasks extends TaskQuery

}

class TaskActor extends Actor with ActorLogging {

  def receive = {
    case FindAllTasks =>
      log.debug("FindAllTasks called")
      sender() ! Task.findAll()
    case FindOneTask(id) =>
      log.debug(s"FindOneTask($id)")
      sender() ! Task.find(id)
    case CreateTask(description) =>
      log.debug(s"NewTask($description)")
      sender() ! Task.create(description, false)
    case SaveTask(id, description, complete) =>
      log.debug(s"UpdateTask($id, $description, $complete)")
      sender() ! Task.save(Task(id, description, complete))
    case u => unhandled(u)
  }

}

package com.example.task.amqp

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.RpcServer._
import com.github.sstone.amqp._
import com.example.task.ConfigComponent
import com.example.task.actors.TaskActor.{CreateTask, FindAllTasks, FindOneTask, SaveTask}
import com.example.task.actors.{AkkaComponent, TaskActorComponent, TaskProtocol}
import com.example.task.models._
import com.rabbitmq.client.ConnectionFactory
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.implicitConversions

trait AmqpModule extends TaskProtocol {
  self: AkkaComponent with TaskActorComponent with ConfigComponent =>

  val connectionFactory = createConnectionFactory
  val connectionOwner = actorSystem.actorOf(ConnectionOwner.props(connectionFactory, 1.second), "amqp-connection-owner")
  private implicit val timeout = Timeout(3.seconds)

  private implicit def stringToBytes(s: String): Array[Byte] = s.getBytes("UTF-8")

  def createRpcServer(queueParams: QueueParameters, routingKey: String, processor: IProcessor, name: String): ActorRef =
    ConnectionOwner.createChildActor(connectionOwner,
      RpcServer.props(queueParams,
        StandardExchanges.amqDirect,
        routingKey,
        processor,
        ChannelParameters(qos = 1)), Some(name))

  def createConnectionFactory: ConnectionFactory = {
    val connFactory = new ConnectionFactory()
    //TODO: use config
    connFactory.setUri("amqp://guest:guest@192.168.59.103/%2F")
    connFactory
  }

  val createTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      Future(JsonParser(delivery.body).convertTo[CreateTask])
        .flatMap(createTask => taskActorRef ? createTask).mapTo[Task]
        .map(task => ProcessResult(Some(task.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) = ProcessResult(Some("server error: " + e.getMessage))
  }

  val saveTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      Future(JsonParser(delivery.body).convertTo[SaveTask])
        .flatMap(saveTask => taskActorRef ? saveTask).mapTo[Task]
        .map(task => ProcessResult(Some(task.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) = ProcessResult(Some("server error: " + e.getMessage))
  }


  val findAllTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      (taskActorRef ? FindAllTasks).mapTo[List[Task]]
        .map(task => ProcessResult(Some(task.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) = ProcessResult(Some("server error: " + e.getMessage))
  }


  val findOneTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      Future(JsonParser(delivery.body).convertTo[FindOneTask])
        .flatMap(findOneTask => taskActorRef ? findOneTask).mapTo[Some[Task]]
        .map((task: Some[Task]) => ProcessResult(task.map(t => t.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) = ProcessResult(Some("server error: " + e.getMessage))
  }

  val createTaskParams: QueueParameters = QueueParameters("task.create.Q", passive = false, durable = false, exclusive = false, autodelete = true)
  val saveTaskParams: QueueParameters = QueueParameters("task.save.Q", passive = false, durable = false, exclusive = false, autodelete = true)
  val findAllTaskParams: QueueParameters = QueueParameters("task.find.all.Q", passive = false, durable = false, exclusive = false, autodelete = true)
  val findOneTaskParams: QueueParameters = QueueParameters("task.find.one.Q", passive = false, durable = false, exclusive = false, autodelete = true)

  val createTaskRpcServer: ActorRef = createRpcServer(createTaskParams,"task.create", createTaskProcessor, "task-create")
  val saveTaskRpcServer: ActorRef = createRpcServer(saveTaskParams,"task.save", saveTaskProcessor, "task-save")
  val findAllTaskRpcServer: ActorRef = createRpcServer(findAllTaskParams, "task.find.all", findAllTaskProcessor, "task-find-all")
  val findOneTaskRpcServer: ActorRef = createRpcServer(findOneTaskParams, "task.find.one", findOneTaskProcessor, "task-find-one")

}

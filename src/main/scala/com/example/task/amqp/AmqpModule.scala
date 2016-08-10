package com.example.task.amqp

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.example.task.domain.TaskActor._
import com.example.task.domain._
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.RpcServer._
import com.github.sstone.amqp._
import com.rabbitmq.client.ConnectionFactory
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class AmqpModule(taskActorRef: ActorRef, amqpConfig: AmqpConfig)(implicit val actorSystem: ActorSystem,
                                         implicit val executionContext: ExecutionContext)
    extends TaskJsonProtocol {

  val connectionFactory = createConnectionFactory
  val connectionOwnerRef =
    actorSystem.actorOf(ConnectionOwner.props(connectionFactory, 1.second), "amqp-connection-owner")
  private implicit val timeout = Timeout(3.seconds)

  private implicit def stringToBytes(s: String): Array[Byte] =
    s.getBytes("UTF-8")

  def createRpcServer(queueParams: QueueParameters,
                      routingKey: String,
                      processor: IProcessor,
                      name: String): ActorRef =
    ConnectionOwner.createChildActor(
        connectionOwnerRef,
        RpcServer.props(queueParams, StandardExchanges.amqDirect, routingKey, processor, ChannelParameters(qos = 1)),
        Some(name))

  def createConnectionFactory: ConnectionFactory = {
    val connFactory = new ConnectionFactory()
    connFactory.setHost(amqpConfig.host)
    connFactory.setPort(amqpConfig.port)
    connFactory.setUsername(amqpConfig.username)
    connFactory.setPassword(amqpConfig.password)
    connFactory
  }

  val createTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      Future(JsonParser(delivery.body).convertTo[CreateTask])
        .flatMap(createTask => taskActorRef ? createTask)
        .mapTo[Task]
        .map(task => ProcessResult(Some(task.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) =
      ProcessResult(Some("server error: " + e.getMessage))
  }

  val saveTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      Future(JsonParser(delivery.body).convertTo[SaveTask])
        .flatMap(saveTask => taskActorRef ? saveTask)
        .mapTo[Task]
        .map(task => ProcessResult(Some(task.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) =
      ProcessResult(Some("server error: " + e.getMessage))
  }

  val findAllTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      (taskActorRef ? FindAllTasks).mapTo[List[Task]].map(task => ProcessResult(Some(task.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) =
      ProcessResult(Some("server error: " + e.getMessage))
  }

  val findOneTaskProcessor = new IProcessor {
    override def process(delivery: Delivery): Future[ProcessResult] = {
      Future(JsonParser(delivery.body).convertTo[FindOneTask])
        .flatMap(findOneTask => taskActorRef ? findOneTask)
        .mapTo[Some[Task]]
        .map((task: Some[Task]) => ProcessResult(task.map(t => t.toJson.toString())))
    }

    override def onFailure(delivery: Delivery, e: Throwable) =
      ProcessResult(Some("server error: " + e.getMessage))
  }

  val createTaskParams = QueueParameters(
      name = "task.create.Q",
      passive = false,
      durable = false,
      exclusive = false,
      autodelete = true
  )

  val saveTaskParams = QueueParameters(
      name = "task.save.Q",
      passive = false,
      durable = false,
      exclusive = false,
      autodelete = true
  )

  val findAllTaskParams = QueueParameters(
      name = "task.find.all.Q",
      passive = false,
      durable = false,
      exclusive = false,
      autodelete = true
  )

  val findOneTaskParams = QueueParameters(
      name = "task.find.one.Q",
      passive = false,
      durable = false,
      exclusive = false,
      autodelete = true
  )

  val createTaskRpcServer = createRpcServer(
      queueParams = createTaskParams,
      routingKey = "task.create",
      processor = createTaskProcessor,
      name = "task-create"
  )

  val saveTaskRpcServer = createRpcServer(
      queueParams = saveTaskParams,
      routingKey = "task.save",
      processor = saveTaskProcessor,
      name = "task-save"
  )

  val findAllTaskRpcServer = createRpcServer(
      queueParams = findAllTaskParams,
      routingKey = "task.find.all",
      processor = findAllTaskProcessor,
      name = "task-find-all"
  )

  val findOneTaskRpcServer = createRpcServer(
      queueParams = findOneTaskParams,
      routingKey = "task.find.one",
      processor = findOneTaskProcessor,
      name = "task-find-one"
  )

}

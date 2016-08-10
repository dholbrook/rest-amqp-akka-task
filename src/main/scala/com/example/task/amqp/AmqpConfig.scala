package com.example.task.amqp

import com.typesafe.config.{Config, ConfigFactory}

case class AmqpConfig(host: String, port: Int, username: String, password: String)

object AmqpConfig {
  def apply(config: Config): AmqpConfig = {
    AmqpConfig(
      host = config.getString("amqp.host"),
      port = config.getInt("amqp.port"),
      username = config.getString("amqp.username"),
      password = config.getString("amqp.password")
    )
  }

  def apply(): AmqpConfig = {
    apply(ConfigFactory.load())
  }
}
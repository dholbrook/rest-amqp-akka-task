package com.example.task

import com.typesafe.config.{Config, ConfigFactory}

case class HttpConfig(interface: String, port: Int)

object HttpConfig {
  def apply(config: Config): HttpConfig = {
    HttpConfig(
      interface = config.getString("http.interface"),
      port = config.getInt("http.port")
    )
  }

  def apply(): HttpConfig = {
    apply(ConfigFactory.load())
  }
}
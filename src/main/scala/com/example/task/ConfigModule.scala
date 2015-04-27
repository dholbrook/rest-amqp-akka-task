package com.example.task

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigComponent {
  def config: Config
}

trait ConfigModule extends ConfigComponent {
  override lazy val config: Config = ConfigFactory.load()
}

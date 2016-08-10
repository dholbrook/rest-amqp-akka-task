package com.example.task.domain

import org.flywaydb.core.Flyway
import org.scalatest._
import scalikejdbc._
import scalikejdbc.config.DBsWithEnv
import scalikejdbc.scalatest.AutoRollback


class TaskSpec extends fixture.FlatSpec with Matchers with AutoRollback {

  DBsWithEnv("testing").setupAll()

  val t = Task.syntax("t")

  var id: Long = _

  override def fixture(implicit session: DBSession): Unit = {
    val flyway = new Flyway()
    flyway.setDataSource(ConnectionPool.dataSource())
    flyway.clean()
    flyway.migrate()
    id = sql"""insert into task(description, complete) values ('test task', false)"""
      .updateAndReturnGeneratedKey().apply()
    ()
  }

  behavior of "Task"

  it should "find by primary keys" in { implicit session =>
    val maybeFound = Task.find(id)
    maybeFound.isDefined should be(true)
  }
  it should "find by where clauses" in { implicit session =>
    val maybeFound = Task.findBy(sqls.eq(t.id, id))
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    val allResults = Task.findAll()
    allResults.size should be > (0)
  }
  it should "count all records" in { implicit session =>
    val count = Task.countAll()
    count should be > (0L)
  }
  it should "find all by where clauses" in { implicit session =>
    val results = Task.findAllBy(sqls.eq(t.id, id))
    results.size should be > (0)
  }
  it should "count by where clauses" in { implicit session =>
    val count = Task.countBy(sqls.eq(t.id, id))
    count should be > (0L)
  }
  it should "create new record" in { implicit session =>
    val created = Task.create(description = "MyString", complete = false)
    created should not be (null)
  }
  it should "save a record" in { implicit session =>
    val entity = Task.findAll().head
    val modified = entity.copy(description = "modified test task")
    val updated = Task.save(modified)
    updated should not equal (entity)
  }
  it should "destroy a record" in { implicit session =>
    val entity = Task.findAll().head
    Task.destroy(entity)
    val shouldBeNone = Task.find(id)
    shouldBeNone.isDefined should be(false)
  }

}

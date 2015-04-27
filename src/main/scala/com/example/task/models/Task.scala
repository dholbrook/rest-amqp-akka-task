package com.example.task.models

import scalikejdbc._

case class Task(id: Long,
                description: String,
                complete: Boolean) {

  def save()(implicit session: DBSession = Task.autoSession): Task = Task.save(this)(session)

  def destroy()(implicit session: DBSession = Task.autoSession): Unit = Task.destroy(this)(session)

}

object Task extends SQLSyntaxSupport[Task] {

  override val tableName = "task"

  override val columns = Seq("id", "description", "complete")

  def apply(t: SyntaxProvider[Task])(rs: WrappedResultSet): Task = apply(t.resultName)(rs)

  def apply(t: ResultName[Task])(rs: WrappedResultSet): Task = new Task(
    id = rs.get(t.id),
    description = rs.get(t.description),
    complete = rs.get(t.complete)
  )

  val t = Task.syntax("t")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Task] = {
    withSQL {
      select.from(Task as t).where.eq(t.id, id)
    }.map(Task(t.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Task] = {
    withSQL(select.from(Task as t)).map(Task(t.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Task as t)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Task] = {
    withSQL {
      select.from(Task as t).where.append(where)
    }.map(Task(t.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Task] = {
    withSQL {
      select.from(Task as t).where.append(where)
    }.map(Task(t.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Task as t).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(description: String,
             complete: Boolean)(implicit session: DBSession = autoSession): Task = {
    val generatedKey = withSQL {
      insert.into(Task).columns(
        column.description,
        column.complete
      ).values(
          description,
          complete
        )
    }.updateAndReturnGeneratedKey().apply()

    Task(
      id = generatedKey,
      description = description,
      complete = complete)
  }

  def save(entity: Task)(implicit session: DBSession = autoSession): Task = {
    withSQL {
      update(Task).set(
        column.id -> entity.id,
        column.description -> entity.description,
        column.complete -> entity.complete
      ).where.eq(column.id, entity.id)
    }.update().apply()
    entity
  }

  def destroy(entity: Task)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(Task).where.eq(column.id, entity.id)
    }.update().apply()
    ()
  }

}

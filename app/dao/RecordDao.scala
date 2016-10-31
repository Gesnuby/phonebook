package dao
import javax.inject.{Inject, Singleton}

import dao.helpers.FilteringHelper
import models.Record
import paging.Paging.{Page, Pageable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author iakhatov 
  */
@Singleton
class RecordDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with FilteringHelper {

  import driver.api._

  private class RecordTable(tag: Tag) extends Table[Record](tag, "records") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def phone = column[String]("phone")
    def * = (id.?, username, phone) <>(Record.tupled, Record.unapply)
  }

  private lazy val Records = TableQuery[RecordTable]

  def find(pageable: Pageable, name: Option[String]): Future[Page[Record]] = {
    val searchQuery = Records.filterBy(name)((rt, str) => regexpMatch(rt.username, str))
    val countQuery = searchQuery.length
    val dataQuery = searchQuery.sortBy(_.username)
                    .drop(pageable.offset)
                    .take(pageable.limit)
    val action = (for {
      count <- countQuery.result
      data <- dataQuery.result
    } yield Page(data, pageable, count)).transactionally
    db.run(action)
  }

  def find(username: String, phone: String): Future[Seq[Record]] = {
    db.run(Records.filter(r => r.username.toLowerCase === username.toLowerCase && r.phone === phone).result)
  }

  def create(record: Record): Future[Record] = db.run(Records.returning(Records) += record)

  def delete(id: Long): Future[Int] = db.run(Records.filter(_.id === id).delete)
}

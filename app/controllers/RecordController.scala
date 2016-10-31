package controllers
import javax.inject.Inject

import dao.RecordDao
import models.Record
import paging.Paging.PageAction
import paging.Paging.PageWrites._
import play.api.libs.json.{JsError, Json, Writes}
import play.api.mvc.{Action, Controller, Result}
import util.Response
import validation.RecordValidationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.{Failure, Success}

/**
  * @author iakhatov 
  */
class RecordController @Inject()(recordDao: RecordDao, recordValidationService: RecordValidationService) extends Controller {

  implicit val recordFormat = Json.format[Record]

  private def success[T](response: T)(implicit writes: Writes[T]): Result = Ok(Json.toJson(response))
  private def error(message: String, details: Seq[String]): Result = BadRequest(Json.toJson(Response(message, details)))

  /**
    * Постраничный поиск записей с опциональной фильтрацией по вхождению подстроки имени.
    *
    * @param name подстрока поиска в имени
    * @return страница с найденными записями
    */
  def find(name: Option[String]) = PageAction.async { request =>
    recordDao.find(request.pageable, name).map(records => success(records))
  }

  /**
    * Создание новой записи.
    * При создании выполняется набор проверок, описанных в сервисе валидации.
    *
    * @return созданная запись
    */
  def create = Action.async(parse.json) { request =>
    import scalaz.Scalaz._
    request.body.validate[Record].fold(error => Future.successful(BadRequest(JsError.toJson(error))), record => {
      recordValidationService.validateNewRecord(record).flatMap {
        case Failure(nel) => Future.successful(error("Record failed validation", nel.toList))
        case Success(rec) => recordDao.create(rec).map(r => success(r))
      }
    })
  }

  /**
    * Удаление записи
    *
    * @param id идентификатор записи
    * @return сообщение об удачности удаления
    */
  def delete(id: Long) = Action.async {
    recordDao.delete(id).map {
      case 1 => success(Response("Record successfully deleted", List()))
      case 0 => error("No such record", List())
    }
  }
}

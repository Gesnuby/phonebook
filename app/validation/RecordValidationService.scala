package validation
import javax.inject.Inject

import dao.RecordDao
import models.Record

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz._

/**
  * Сервис валидации записи
  *
  * @author iakhatov 
  */
class RecordValidationService @Inject()(recordDao: RecordDao) {

  /**
    * Валидации новой записи
    *
    * @param record запись
    * @return исходная запись, либо список непройденных проверок
    */
  def validateNewRecord(record: Record): Future[ValidationNel[String, Record]] = {
    val uniquenessValidation = mustBeUnique(record)
    val validation = for {
      uniquenessCheck <- uniquenessValidation
      emptynessCheck <- mustHaveEmptyId(record)
      fillnessCheck <- mustHaveUsernameAndPhone(record)
    } yield (emptynessCheck.toValidationNel |@| uniquenessCheck.toValidationNel |@| fillnessCheck.toValidationNel) {
      case _ => record
    }
    validation.recover {
      case ex =>
        val errorMessage = s"Exception occured during record validation: ${ex.getMessage}"
        errorMessage.failureNel[Record]
    }
  }

  /**
    * Поле <b>id</b> не должно быть заполнено
    */
  private def mustHaveEmptyId(record: Record): Future[Validation[String, Record]] = Future.successful {
    if (record.id.isDefined) "Record shouldn't have 'id' field!".failure
    else record.success
  }

  /**
    * Поля <b>username</b> и <b>phone</b> должны быть заполнены
    */
  private def mustHaveUsernameAndPhone(record: Record): Future[Validation[String, Record]] = Future.successful {
    if (record.username.trim.isEmpty || record.phone.trim.isEmpty) "Record should have name and phone!".failure
    else record.success
  }

  /**
    * Запись должна быть уникальна в рамках бд по паре имя-номер
    */
  private def mustBeUnique(record: Record): Future[Validation[String, Record]] = {
    recordDao.find(record.username, record.phone).map { records =>
      if (records.nonEmpty) "Record with such name and phone already exists!".failure
      else record.success
    }
  }
}

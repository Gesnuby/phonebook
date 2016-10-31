package validation
import dao.RecordDao
import models.Record
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.Future
import scalaz.{Failure, NonEmptyList, Success}

/**
  * @author iakhatov 
  */
class RecordValidationServiceTest extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  "RecordValidationService#validateNewRecord" must {
    "return source record if it passes all validations" in {
      val record = Record(None, "Bob", "89126673412")
      val recordDao = mock[RecordDao]
      when(recordDao.find(record.username, record.phone)) thenReturn Future.successful(List())

      val recordValidationService = new RecordValidationService(recordDao)

      whenReady(recordValidationService.validateNewRecord(record)) { result =>
        result mustBe Success(record)
      }
    }
    "return 'Record shouldn't have 'id' field!' error message when record has 'id field'" in {
      val record = Record(Some(1), "Bob", "89126673412")
      val recordDao = mock[RecordDao]
      when(recordDao.find(record.username, record.phone)) thenReturn Future.successful(List())

      val recordValidationService = new RecordValidationService(recordDao)
      whenReady(recordValidationService.validateNewRecord(record)) { result =>
        result mustBe Failure(NonEmptyList[String]("Record shouldn't have 'id' field!"))
      }
    }
    "return 'Record should have name and phone!' error message when record doesn't have 'username' or 'phone' fields" in {
      val record = Record(None, "", "")
      val recordDao = mock[RecordDao]
      when(recordDao.find(record.username, record.phone)) thenReturn Future.successful(List())

      val recordValidationService = new RecordValidationService(recordDao)
      whenReady(recordValidationService.validateNewRecord(record)) { result =>
        result mustBe Failure(NonEmptyList[String]("Record should have name and phone!"))
      }
    }
    "return 'Record with such name and phone already exists!' error message when db already has such record" in {
      val record = Record(None, "Bob", "89127783543")
      val recordDao = mock[RecordDao]
      when(recordDao.find(record.username, record.phone)) thenReturn Future.successful(List(record))

      val recordValidationService = new RecordValidationService(recordDao)
      whenReady(recordValidationService.validateNewRecord(record)) { result =>
        result mustBe Failure(NonEmptyList[String]("Record with such name and phone already exists!"))
      }
    }
  }
}

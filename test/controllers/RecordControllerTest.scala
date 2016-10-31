package controllers
import dao.RecordDao
import models.Record
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play._
import paging.Paging.PageWrites._
import paging.Paging.{Page, Pageable}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import util.Response
import validation.RecordValidationService

import scala.concurrent.Future
import scalaz.Success

/**
  * @author iakhatov 
  */
class RecordControllerTest extends PlaySpec with MockitoSugar {

  implicit val recordFormat = Json.format[Record]

  val recordValidationService = mock[RecordValidationService]
  val defaultPaging = Pageable(0, 10)

  val bobRecord = Record(Some(1), "Bob", "89127784635")
  val johnRecord = Record(Some(2), "John", "89920084635")
  val bobbyRecord = Record(Some(3), "Bobby", "89124582035")
  val testRecords = List(bobbyRecord, johnRecord, bobbyRecord)
  val emptyResult = Page(List.empty[Record], defaultPaging, 0)

  "RecordController#find" must {
    "return empty result when no records is stored in db" in {
      val recordDao = mock[RecordDao]
      when(recordDao.find(defaultPaging, None)) thenReturn Future.successful(emptyResult)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.find(None).apply(FakeRequest())
      contentAsJson(result) mustBe Json.toJson(emptyResult)
    }
    "return 3 results when there are 3 records in the db and default paging params are applied" in {
      val recordsResult = Page(testRecords, defaultPaging, testRecords.size)
      val recordDao = mock[RecordDao]
      when(recordDao.find(defaultPaging, None)) thenReturn Future.successful(recordsResult)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.find(None).apply(FakeRequest())
      contentAsJson(result) mustBe Json.toJson(recordsResult)
    }
    "return 1 result with username 'John' when there are 3 records in the db and paging params (limit=1&offset=1) are applied" in {
      val customPaging = Pageable(1, 1)
      val recordsResult = Page(List(johnRecord), customPaging, testRecords.size)
      val recordDao = mock[RecordDao]
      when(recordDao.find(customPaging, None)) thenReturn Future.successful(recordsResult)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.find(None).apply(FakeRequest("GET", "?limit=1&offset=1", FakeHeaders(), AnyContentAsEmpty))
      contentAsJson(result) mustBe Json.toJson(recordsResult)
    }
    "return 2 results with usernames 'Bob' and 'Bobby' when search substring 'ob' with default paging params is applied" in {
      val recordsResult = Page(List(bobRecord, bobbyRecord), defaultPaging, 2)
      val recordDao = mock[RecordDao]
      when(recordDao.find(defaultPaging, Some("ob"))) thenReturn Future.successful(recordsResult)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.find(Some("ob")).apply(FakeRequest("GET", "?name=ob", FakeHeaders(), AnyContentAsEmpty))
      contentAsJson(result) mustBe Json.toJson(recordsResult)
    }
    "return empty result when search substring 'mar' is applied" in {
      val recordDao = mock[RecordDao]
      when(recordDao.find(defaultPaging, Some("mar"))) thenReturn Future.successful(emptyResult)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.find(Some("mar")).apply(FakeRequest("GET", "?name=mar", FakeHeaders(), AnyContentAsEmpty))
      contentAsJson(result) mustBe Json.toJson(emptyResult)
    }
  }
  "RecordController#create" must {
    "create new record with name 'Bob' and phone '89126673542'" in {
      val newRecord = Record(None, "Bob", "89126673542")
      val recordDao = mock[RecordDao]
      when(recordDao.create(newRecord)) thenReturn Future.successful(newRecord)
      when(recordValidationService.validateNewRecord(newRecord)) thenReturn Future.successful(Success(newRecord))

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.create().apply(FakeRequest("POST", "/", FakeHeaders(), Json.toJson(newRecord)))
      contentAsJson(result) mustBe Json.toJson(newRecord)
    }
  }
  "RecordController#delete" must {
    "delete existing record with id 233" in {
      val recordDao = mock[RecordDao]
      when(recordDao.delete(233)) thenReturn Future.successful(1)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.delete(233).apply(FakeRequest("DELETE", "/", FakeHeaders(), AnyContentAsEmpty))
      contentAsJson(result) mustBe Json.toJson(Response("Record successfully deleted", Seq()))
    }
    "return error when trying to delete nonexistent record" in {
      val recordDao = mock[RecordDao]
      when(recordDao.delete(56)) thenReturn Future.successful(0)

      val recordController = new RecordController(recordDao, recordValidationService)
      val result = recordController.delete(56).apply(FakeRequest("DELETE", "/", FakeHeaders(), AnyContentAsEmpty))
      contentAsJson(result) mustBe Json.toJson(Response("No such record", List()))
    }
  }
}

package paging
import play.api.libs.json.{JsArray, JsValue, Json, Writes}
import play.api.mvc.{ActionBuilder, ActionTransformer, Request, WrappedRequest}

import scala.concurrent.Future
import scala.util.Try

/**
  * @author iakhatov
  */
object Paging {

  /**
    * Pageable object containing offset and limit params for page
    *
    * @param offset how many elements to skip
    * @param limit  number of elements on page
    */
  case class Pageable(offset: Int, limit: Int)

  class PageRequest[A](val pageable: Pageable, val request: Request[A]) extends WrappedRequest[A](request)

  object PageAction extends ActionBuilder[PageRequest] with ActionTransformer[Request, PageRequest] {
    private val LIMIT_QUERY_PARAM = "limit"
    private val OFFSET_QUERY_PARAM = "offset"
    private val DEFAULT_LIMIT = 10
    private val DEFAULT_OFFSET = 0

    override protected def transform[A](request: Request[A]): Future[PageRequest[A]] = Future.successful {
      def getIntQueryParam(name: String): Option[Int] = {
        Try(request.queryString(name).headOption.map(_.toInt)).toOption.flatten
      }
      val limit = getIntQueryParam(LIMIT_QUERY_PARAM).filter(_ >= 0).getOrElse(DEFAULT_LIMIT)
      val offset = getIntQueryParam(OFFSET_QUERY_PARAM).filter(_ >= 0).getOrElse(DEFAULT_OFFSET)
      new PageRequest[A](Pageable(offset, limit), request)
    }
  }

  case class Page[T](content: Seq[T], pageable: Pageable, totalElements: Int)

  object PageWrites {
    implicit def pageWrites[R](implicit fmt: Writes[R]): Writes[Page[R]] = new Writes[Page[R]] {
      override def writes(o: Page[R]): JsValue = Json.obj(
        "content" -> JsArray(o.content.map(c => fmt.writes(c))),
        "limit" -> o.pageable.limit,
        "offset" -> o.pageable.offset,
        "totalElements" -> o.totalElements
      )
    }
  }

}

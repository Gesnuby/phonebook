package util
import play.api.libs.json.Json

/**
  * @author iakhatov 
  */
case class Response(message: String, details: Seq[String])

object Response {
  implicit val responseWrites = Json.writes[Response]
}

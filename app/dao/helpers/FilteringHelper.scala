package dao.helpers
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

/**
  * @author iakhatov 
  */
trait FilteringHelper {
  configProvider: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  implicit class OptionFilter[A, B](query: Query[A, B, Seq]) {
    /**
      * Фильтрация по опциональному параметру в случае, когда сравниваемое поле является обязательным.
      * Если параметр не указан, то возвращается исходный запрос.
      *
      * @param op опциональный параметр
      * @param f  условие фильтрации
      * @tparam T тип параметра
      * @return запрос
      */
    def filterBy[T](op: Option[T])(f: (A, T) => Rep[Boolean]): Query[A, B, Seq] = {
      op.fold(query)(o => query.filter(f(_, o)))
    }
  }

  private val caseInsensitiveMatch = SimpleBinaryOperator.apply[Boolean]("~*")

  /**
    * Регистронезависимый поиск по обязательному полю через регулярное выражение
    *
    * @param left  поле
    * @param right регулярное выражение
    * @return true если содержимое поля удовлетворяет регулярному выражению
    */
  def regexpMatch(left: Rep[String], right: String) = caseInsensitiveMatch(left, right)
}

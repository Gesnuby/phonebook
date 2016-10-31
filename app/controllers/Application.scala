package controllers
import play.api.mvc.{Action, Controller}

/**
  * @author iakhatov 
  */
class Application extends Controller {
  def index() = Action {
    Ok(views.html.index())
  }
}

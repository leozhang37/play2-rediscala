package play.modules.rediscala

import play.api._
import java.net.URI
import scala.Some
import redis.RedisClient
import akka.actor.ActorSystem

class RedisPlugin(app: Application) extends Plugin {

  lazy val confs:Map[String, (String, Int, Option[(String, String)])] =
    app.configuration.getConfig("redis").fold(Map("default" -> RedisPlugin.parseConf(app.configuration))) { conf =>
      conf.subKeys.map(key => (key, RedisPlugin.parseConf(app.configuration, key))).toMap
    }

  override def onStart() {
    Logger.info("RedisPlugin starting...")
    confs
    Logger.info("RedisPlugin started")
  }

  def client(db:String)(implicit system:ActorSystem):RedisClient = confs.get(db) match {
    case Some(conf) => new RedisClient(conf._1, conf._2, conf._3.map(_._2))
    case _ => throw new PlayException("RedisPlugin Error", s"No configuration found for db $db")
  }
  
}

object RedisPlugin {

  def client(db: String = "default")(implicit app: Application, system: ActorSystem) = current.client(db)

  def current(implicit app: Application): RedisPlugin = app.plugin[RedisPlugin] match {
    case Some(plugin) => plugin
    case _            => throw new PlayException("RedisPlugin Error", "The RedisPlugin has not been initialized! Please edit your conf/play.plugins file and add the following line: '400:play.modules.rediscala.RedisPlugin' (400 is an arbitrary priority and may be changed to match your needs).")
  }

  val default:(String, Int, Option[(String, String)]) = ("localhost", 6379, None)

  def parseConf(configuration: Configuration, name: String = "default"):(String, Int, Option[(String, String)]) = {
    configuration.getConfig("redis."+name).fold(default){ conf =>
      parseConf(conf.getString("uri"), conf.getString("host"), conf.getInt("port"), conf.getString("user"), conf.getString("password"))
    }
  }

  def parseConf(uri: Option[String], host: Option[String], port: Option[Int], user:Option[String], password:Option[String]):(String, Int, Option[(String, String)]) = {
    val auth = for{
      u <- user
      p <- password
    } yield (u, p)
    uri.fold[(String, Int, Option[(String, String)])]((host.getOrElse(default._1), port.getOrElse(default._2), auth.orElse(default._3)))(parseURI)
  }

  private def parseURI(uri: String):(String, Int, Option[(String, String)]) = {
    val jUri = new URI(uri);

    val port = jUri.getPort match {
      case -1 => default._2
      case p:Int => p
    }

    val userInfo = Option(jUri.getUserInfo).map {
      _.split(":").toList match {
        case username :: password => (username, password.mkString)
        case _ => null
      }
    }
    (jUri.getHost, port, userInfo)
  }
}
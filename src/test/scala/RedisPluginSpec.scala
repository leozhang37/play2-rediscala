package play.modules.rediscala

import org.specs2.mutable.Specification
import java.net.URISyntaxException
import play.api.test.FakeApplication
import play.api.libs.concurrent.Akka
import play.api.PlayException

class RedisPluginSpec extends Specification {

  "RedisPlugin" should {
    "Parse complete URI" in {
      val parsed = RedisPlugin.parseConf(Some("redis://user:password@an.host.com:9092/"), None, None, None, None)
      parsed must be equalTo ("an.host.com", 9092, Some("user", "password"))
    }
    "Parse URI without user info" in {
      val parsed = RedisPlugin.parseConf(Some("redis://an.host.com:9092/"), None, None, None, None)
      parsed must be equalTo ("an.host.com", 9092, None)
    }
    "Parse URI without port" in {
      val parsed = RedisPlugin.parseConf(Some("redis://an.host.com/"), None, None, None, None)
      parsed must be equalTo ("an.host.com", 6379, None)
    }
    "Provide configuration without URI" in {
      val parsed = RedisPlugin.parseConf(None, Some("an.host.com"), Some(9092), Some("user"), Some("password"))
      parsed must be equalTo ("an.host.com", 9092, Some("user", "password"))
    }
    "Throw an exception with malformated URI" in {
      RedisPlugin.parseConf(Some("redis://"), None, None, None, None) must throwA[URISyntaxException]
    }
    "Parse URI from application configuration" in {
      val parsed = RedisPlugin.parseConf(FakeApplication(additionalConfiguration = Map("redis.default.uri" -> "redis://user:password@an.host.com:9092/")).configuration)
      parsed must be equalTo ("an.host.com", 9092, Some("user", "password"))
    }
    "Parse URI from application configuration for non default db" in {
      val parsed = RedisPlugin.parseConf(FakeApplication(additionalConfiguration = Map("redis.mydb.uri" -> "redis://user:password@an.host.com:9092/")).configuration, "mydb")
      parsed must be equalTo ("an.host.com", 9092, Some("user", "password"))
    }
    "Parse from application configuration" in {
      val parsed = RedisPlugin.parseConf(FakeApplication(
        additionalConfiguration =
          Map(
            "redis.default.host" -> "an.host.com",
            "redis.default.port" -> 9092,
            "redis.default.user" -> "user",
            "redis.default.password" -> "password"
          )
        ).configuration)

      parsed must be equalTo ("an.host.com", 9092, Some("user", "password"))
    }
    "Provide default configuration" in {
      val parsed = RedisPlugin.parseConf(FakeApplication().configuration)
      parsed must be equalTo ("localhost", 6379, None)
    }

    "Should provide RedisClient for default configuration" in {
      val app = FakeApplication(additionalPlugins = Seq("play.modules.rediscala.RedisPlugin"))
      val client = RedisPlugin.client()(app, Akka.system(app))
      client.stop()
      (client.host, client.port) must be equalTo ("localhost", 6379)
    }
    "Throw an exception if plugin is not registered" in {
      val app = FakeApplication(withoutPlugins = Seq("play.modules.rediscala.RedisPlugin"))
      RedisPlugin.client()(app, Akka.system(app)) must throwA[PlayException]
    }
    "Should provide RedisClient for multiple databases" in {
      val app = FakeApplication(
        additionalPlugins = Seq("play.modules.rediscala.RedisPlugin"),
        additionalConfiguration = Map(
          "redis.default.uri" -> "redis://localhost",
          "redis.mydb.uri" -> "redis://user:password@an.host.com:9092/"
        )
      )
      val default = RedisPlugin.client()(app, Akka.system(app))
      default.stop()
      (default.host, default.port) must be equalTo ("localhost", 6379)
      val mydb = RedisPlugin.client("mydb")(app, Akka.system(app))
      mydb.stop()
      (mydb.host, mydb.port) must be equalTo ("an.host.com", 9092)
    }
    "Throw a an exception if database is not configured" in {
      val app = FakeApplication(additionalPlugins = Seq("play.modules.rediscala.RedisPlugin"))
      RedisPlugin.client("mydb")(app, Akka.system(app)) must throwA[PlayException]
    }
  }


}

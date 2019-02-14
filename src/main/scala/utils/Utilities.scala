package utils

import java.io.File
import java.nio.file.Paths

import actors.RetweetHandlerActor
import akka.NotUsed
import akka.actor.{ActorContext, ActorSystem}
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.typesafe.config.ConfigFactory
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, NoTypeHints}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

trait Utilities {

  private val logger = LoggerFactory.getLogger(getClass)
  private val outputDir = new File(String.valueOf(System.getProperty("user.dir") + "/tweets"))

  def saveToDisk(tweets: List[Tweet], filename: String)(implicit system: ActorSystem): Unit = {
    import java.nio.file.StandardOpenOption._

    if (!outputDir.exists()) outputDir.mkdir()

    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val source: Source[Tweet, NotUsed] = Source(tweets)

    val serialize: Flow[Tweet, ByteString, NotUsed] =
      Flow[Tweet].map { t =>
        implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)
        ByteString(write(t) + "\n")
      }

    val sink: Sink[ByteString, Future[IOResult]] =
      FileIO.toPath(Paths.get(outputDir + "/" + filename), Set(CREATE, WRITE, APPEND))

    val runnableGraph: RunnableGraph[Future[IOResult]] =
      source
        .via(serialize)
        .toMat(sink)(Keep.right)

    runnableGraph.run().foreach { result =>
      logger.info(s"${result.status}, ${result.count} bytes written.")
    }
  }

  def credentialsParser(prefix: String): (ConsumerToken, AccessToken) = {
    val config = ConfigFactory.load()

    val consumerKey = config.getString(s"$prefix.consumer.key")
    val consumerSecret = config.getString(s"$prefix.consumer.secret")
    val accessKey = config.getString(s"$prefix.access.key")
    val accessSecret = config.getString(s"$prefix.access.secret")

    val accessToken = AccessToken(key = accessKey, secret = accessSecret)
    val consumerToken = ConsumerToken(key = consumerKey, secret = consumerSecret)

    (consumerToken, accessToken)
  }
}

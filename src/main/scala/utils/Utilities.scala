package utils

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import com.danielasfregola.twitter4s.entities.Tweet
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, NoTypeHints}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

object Utilities {

  private val logger = LoggerFactory.getLogger(getClass)
  private val outputFile = "src/main/resources/tweets.json"

  def saveToDisk(tweets: List[Tweet])(implicit system: ActorSystem) = {
    import java.nio.file.StandardOpenOption._

    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val source: Source[Tweet, NotUsed] = Source(tweets)

    val serialize: Flow[Tweet, ByteString, NotUsed] =
      Flow[Tweet].map { t =>
        implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)
        ByteString(write(t) + "\n")
      }

    val sink: Sink[ByteString, Future[IOResult]] =
      FileIO.toPath(Paths.get(outputFile), Set(CREATE, WRITE, APPEND))

    val runnableGraph: RunnableGraph[Future[IOResult]] =
      source
        .via(serialize)
        .toMat(sink)(Keep.right)

    runnableGraph.run().foreach { result =>
      logger.info(s"${result.status}, ${result.count} bytes written.")
    }
  }
}

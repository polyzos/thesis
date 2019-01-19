package actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import utils.Utilities

import scala.util.Random


object StreamListenerActor {
  def props(userID: Int) = Props(new StreamListenerActor(userID))

  case object Terminate
  case class MonitorUser(id: Int)
}


class StreamListenerActor(userID: Int) extends Actor with ActorLogging {

  private val streamingClient = TwitterStreamingClient()
  private var streamCache = scala.collection.mutable.ListBuffer.empty[Tweet]

  import StreamListenerActor._

  override def preStart(): Unit = {
    log.info(s"Starting '${context.self.path.name}' actor to monitor tweet stream for user id ''.")
    self ! MonitorUser(userID)
  }

  override def postStop(): Unit = {
    log.info(s"Actor '${context.self.path.name}' fetched tweet stream for user with id '$userID' and now exits.")
  }

  override def receive: Receive = {
    case MonitorUser(id) => monitorStream(id)
    case Terminate       => self ! PoisonPill
  }

  private def monitorStream(id: Int) = {
    streamingClient.filterStatuses(
      follow = Seq(14075928),
      stall_warnings = true,
      languages = Seq(Language.English))(cacheTweet)
  }

  private def cacheTweet: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet =>
      log.info(s"Received tweet: ${tweet.text.toString}")

      if (streamCache.size > 20) {
        log.info(s"Total tweets in cache '${streamCache.size}' - saving data to disk.")
        Utilities.saveToDisk(streamCache.toList)(context.system)
        streamCache.clear()
      }
      streamCache += tweet
      checkForNewPost(tweet)
    case _ =>
      log.info("Unknown object received from twitter stream.")
  }

  private def checkForNewPost(tweet: Tweet) = {
    if (tweet.retweeted_status.isDefined && !tweet.text.startsWith("RT")) {
      context.system.actorOf(RetweetHandlerActor.props(
        tweet.retweeted_status.get.id),
        s"retweet_fetcher_${Random.nextInt()}")
    }
  }
}

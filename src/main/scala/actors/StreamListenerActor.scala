package actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import models.ParsedTweet
import utils.Utilities

import scala.concurrent.Future


object StreamListenerActor {
  def props(userID: Seq[Long]) = Props(new StreamListenerActor(userID))

  case object Terminate
  case class MonitorUser(id: Seq[Long])
}


class StreamListenerActor(ids: Seq[Long]) extends Actor
  with ActorLogging
  with Utilities {

  private val streamingClient = TwitterStreamingClient()
  private var streamCache = scala.collection.mutable.ListBuffer.empty[ParsedTweet]

  import StreamListenerActor._

  override def preStart(): Unit = {
    log.info(s"Starting '${context.self.path.name}' actor to monitor tweet stream for user ids '${ids.mkString(",")}'.")
    self ! MonitorUser(ids)
  }

  override def postStop(): Unit = {
    log.info(s"Actor '${context.self.path.name}' fetched tweet stream for users with ids '${ids.mkString(",")}' and now exits.")
  }

  override def receive: Receive = {
    case MonitorUser(userIds) => monitorStream(userIds)
    case Terminate       => self ! PoisonPill
  }

  private def monitorStream(ids: Seq[Long]): Future[TwitterStream] = {
    streamingClient.filterStatuses(
      follow = ids,
      stall_warnings = true,
      languages = Seq(Language.English))(cacheTweet)
  }

  private def cacheTweet: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet =>
      log.info(s"Received tweet: ${tweet.text.toString}")
      log.info(tweet.retweeted_status.toString)
      log.info(tweet.retweeted_status.isDefined.toString)
      log.info(tweet.text.startsWith("RT").toString)
      val parsedTweet = parseTweetHandler(tweet)
      if (streamCache.size > 20) {
        log.info(s"Total tweets in cache '${streamCache.size}' - saving data to disk.")
        saveToDisk(streamCache.toList, "fake_tweets.json")(context.system)
        streamCache.clear()
      }
      streamCache += parsedTweet
      checkForNewPost(parsedTweet)(context)
    case _ =>
      log.info("Unknown object received from twitter stream.")
  }
}

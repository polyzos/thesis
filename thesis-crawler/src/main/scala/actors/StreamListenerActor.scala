package actors

import actors.RetweetHandlerActor.{CheckIfNewPost, FetchRetweets}
import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import utils.Utilities

import scala.concurrent.Future
import scala.util.Random


object StreamListenerActor {
  def props(userID: Seq[Long]) = Props(new StreamListenerActor(userID))

  case object Terminate
  case class MonitorUser(id: Seq[Long])
}


class StreamListenerActor(ids: Seq[Long]) extends Actor
  with ActorLogging
  with Utilities {

  private val streamingClient = TwitterStreamingClient()
  private val retweetHandlerActor = context.actorOf(
    RetweetHandlerActor.props,
    s"retweet_fetcher_${Random.nextInt()}")

  private var streamCache = scala.collection.mutable.ListBuffer.empty[Tweet]
  private var postsCache = scala.collection.mutable.ListBuffer.empty[Long]

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
      //      log.info(s"\nReceived tweet: ${tweet.text.toString}")

      if (tweet.retweeted_status.isDefined && !postsCache.contains(tweet.retweeted_status.get.id)) {
        val id = tweet.retweeted_status.get.id
        postsCache += id
        retweetHandlerActor ! FetchRetweets(id)
        if (postsCache.size > 1000) postsCache.clear()
      }

      if (streamCache.size > 200) {
        saveToDisk(streamCache.toList, "fake_tweets.json")(context.system)
        streamCache.clear()
      }
      streamCache += tweet
    //      retweetHandlerActor ! CheckIfNewPost(tweet)
    case _ =>
      log.info("Unknown object received from twitter stream.")
  }
}

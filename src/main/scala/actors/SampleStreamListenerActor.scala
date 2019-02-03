package actors

import akka.actor.{Actor, ActorLogging, Props}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import models.ParsedTweet
import utils.Utilities

object SampleStreamListenerActor {
  def props(followersCountThreshold: Int = 0, streamingClient: TwitterStreamingClient) = Props(
    new SampleStreamListenerActor(followersCountThreshold, streamingClient)
  )

  case object MonitorSampleStream
}

class SampleStreamListenerActor(followersCountThreshold: Int, streamingClient: TwitterStreamingClient) extends Actor
  with ActorLogging
  with Utilities {

  private var sampleStreamCache = scala.collection.mutable.ListBuffer.empty[ParsedTweet]

  import SampleStreamListenerActor._

  override def preStart(): Unit = {
    log.info(s"Starting '${context.self.path.name}' actor to monitor tweet sample stream.")
    self ! MonitorSampleStream
  }

  override def receive: Receive = {
    case MonitorSampleStream   =>
      streamingClient.sampleStatuses(
        stall_warnings = true,
        languages = Seq(Language.English))(sampleTweetHandler)
  }

  private def sampleTweetHandler: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet =>
      val parsedTweet = parseTweetHandler(tweet)
      if (tweet.user.get.followers_count > followersCountThreshold) {
        log.info(s"Received tweet: ${tweet.text.toString}")
        if (sampleStreamCache.size > 200) {
          log.info(s"Total tweets in cache '${sampleStreamCache.size}' - saving data to disk.")
          saveToDisk(sampleStreamCache.toList, "sample_tweets_stream.json")(context.system)
          sampleStreamCache.clear()
        }
        sampleStreamCache += parsedTweet
        checkForNewPost(parsedTweet)(context)
      }
    case _ =>
      log.info("Unknown object received from twitter stream.")
  }
}

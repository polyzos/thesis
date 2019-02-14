package actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.Tweet
import utils.Utilities

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}


object RetweetHandlerActor {
  def props() = Props(new RetweetHandlerActor())

  case object Terminate
  case class CheckIfNewPost(tweet: Tweet)
  case class FetchRetweets(id: Long)
}

class RetweetHandlerActor() extends Actor
  with ActorLogging
  with Utilities {

  private val client = TwitterRestClient()
  private var streamCache = scala.collection.mutable.ListBuffer.empty[Tweet]

  import RetweetHandlerActor._

  override def receive: Receive = {
    case FetchRetweets(id)  =>
      retrieveRetweets(id)
    case CheckIfNewPost(tweet) =>
      if (tweet.retweeted_status.isDefined) {
        self ! FetchRetweets(tweet.retweeted_status.get.id)
      }
  }

  private def retrieveRetweets(id: Long): Unit = {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    client.retweets(id = id) onComplete {
      case Success(result) =>
        log.info(s"Fetched '${result.data.size}' retweets for tweet $id.")
        val retweets = result.data.toList
        saveToDisk(retweets, "retweets_batch.json")(context.system)
      case Failure(exception) => log.error("Failed to retrieve retweets for '$id': ", exception.printStackTrace())
    }
  }
}

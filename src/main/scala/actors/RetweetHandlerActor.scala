package actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.danielasfregola.twitter4s.TwitterRestClient
import utils.Utilities

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}


object RetweetHandlerActor {
  def props(tweetID: Long) = Props(new RetweetHandlerActor(tweetID))

  case object Terminate
  case class FetchRetweets(id: Long)
}

class RetweetHandlerActor(tweetID: Long) extends Actor
  with ActorLogging
  with Utilities {

  private val client = TwitterRestClient()

  import RetweetHandlerActor._

  override def preStart(): Unit = {
    log.info(s"Starting '${context.self.path.name}' actor handler to retrieve retweets for tweet with id $tweetID")
    self ! FetchRetweets(tweetID)
  }

  override def postStop(): Unit = {
    log.info(s"Actor '${context.self.path.name}' fetched retweets for tweet with id '$tweetID' and now exits.")
  }

  override def receive: Receive = {
    case FetchRetweets(id)  =>
      retrieveRetweets(id)
    case Terminate          =>
      self ! PoisonPill
  }

  private def retrieveRetweets(id: Long): Unit = {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    client.retweets(id = id) onComplete {
      case Success(result) =>
        log.info(s"Fetched '${result.data.size}' retweets for tweet $id.")
        val retweets = result.data.map(parseTweetHandler).toList
        saveToDisk(retweets, "retweets_batch.json")(context.system)
        self ! Terminate
      case Failure(exception) => log.error("Failed to retrieve retweets for '$id': ", exception.printStackTrace())
    }
  }
}

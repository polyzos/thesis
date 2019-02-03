import SampleStreamWatcher.credentialsParser
import actors.{SampleStreamListenerActor, StreamListenerActor}
import akka.actor.ActorSystem
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}

import scala.concurrent.Await

object UserFeedWatcher {

  private val twitterAccounts = Seq("BreitbartNews",
    "TheOnion",
    "politicususa",
    "TheBlaze_Prod",
    "beforeitsnews",
    "OccupyDemocrats",
    "redflag_RBLX",
    "DCClothesline",
    "Bipartisanism",
    "worldnetdaily"
  )

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("tweet-crawler")

    val client = TwitterRestClient()
    val ids = twitterAccounts.map { acc => fetchTwitterIds(acc, client) }
    system.actorOf(StreamListenerActor.props(ids),s"fake-news-stream-listener")
//
//    val tokens = credentialsParser("twitter-2")
//    val streamingClient = new TwitterStreamingClient(tokens._1, tokens._2)
//
//    system.actorOf(SampleStreamListenerActor.props(1000, streamingClient = streamingClient),"sample-stream-listener")
  }

  private def fetchTwitterIds(account: String, client: TwitterRestClient)(implicit system: ActorSystem): Long = {
    import scala.concurrent.duration._
    system.log.info(s"Fetching twitter id for user '$account'.")
    val user = client.user(account)
    val futureResult = Await.result(user, 5000 seconds)
    val id = futureResult.data.id
    system.log.info(s"Twitter id for user '$account' - '$id'")
    id
  }
}

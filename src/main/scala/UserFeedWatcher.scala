import actors.{SampleStreamListenerActor, StreamListenerActor}
import akka.actor.ActorSystem
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import utils.Utilities

import scala.concurrent.Await

object MonitorUserFeed extends Utilities {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("tweet-crawler")

    import scala.concurrent.duration._

    val client = TwitterRestClient()
    val user = client.user("TheOnion")
    val futureResult = Await.result(user, 5000 seconds)

    system.actorOf(StreamListenerActor.props(Seq(futureResult.data.id)),s"the-onion-stream-listener")

  }
}

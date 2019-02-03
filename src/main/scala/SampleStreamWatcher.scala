import actors.SampleStreamListenerActor
import akka.actor.ActorSystem
import com.danielasfregola.twitter4s.TwitterStreamingClient
import utils.Utilities

object SampleStreamWatcher extends Utilities {

  def startSampleStreamWatcher(): Unit= {
    implicit val system: ActorSystem = ActorSystem("tweet-crawler")

    val tokens = credentialsParser("twitter-2")
    val streamingClient = new TwitterStreamingClient(tokens._1, tokens._2)

    system.actorOf(SampleStreamListenerActor.props(1000, streamingClient = streamingClient),"sample-stream-listener")
  }
}

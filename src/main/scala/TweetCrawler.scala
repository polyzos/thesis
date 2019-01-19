import actors.StreamListenerActor
import akka.actor.ActorSystem

object TweetCrawler {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("tweet-crawler")

    system.actorOf(StreamListenerActor.props(14075928),
      s"stream-listener")
  }
}

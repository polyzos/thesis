import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values
import repository.Neo4jConnection
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*


fun main() {
    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey("")
        .setOAuthConsumerSecret("")
        .setOAuthAccessToken("")
        .setOAuthAccessTokenSecret("")
    val tf = TwitterFactory(cb.build())
    val twitter1 = tf.instance

    val cb2 = ConfigurationBuilder()
    cb2.setDebugEnabled(true)
        .setOAuthConsumerKey("")
        .setOAuthConsumerSecret("")
        .setOAuthAccessToken("")
        .setOAuthAccessTokenSecret("")
    val tf2 = TwitterFactory(cb2.build())
    val twitter2 = tf2.instance

    val cb3 = ConfigurationBuilder()
    cb3.setDebugEnabled(true)
        .setOAuthConsumerKey("")
        .setOAuthConsumerSecret("")
        .setOAuthAccessToken("")
        .setOAuthAccessTokenSecret("")
    val tf3 = TwitterFactory(cb3.build())
    val twitter3 = tf3.instance

    val connection = Neo4jConnection("bolt://:7687", "neo4j", "")
    try {
        val users = connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH (user: User) RETURN user.screen_name"""
                ).list().map { r -> r.values()[0].toString().replace("\"", "") }
            }

        println(users.size)

        val chunk1 = users.chunked(154)[0]
        val chunk2 = users.chunked(154)[1]
        val chunk3 = users.chunked(154)[2]
        val chunk4 = users.chunked(154)[3]
        println(chunk1.size)
        println(chunk2.size)
        println(chunk3.size)
        println(chunk4.size)
        val chunk1Thread = object: Thread(){
            override fun run(){
                retrieveUserRelationship(chunk1, twitter1, "data/follows_chunk1.txt")
            }
        }
        val chunk2Thread = object: Thread(){
            override fun run(){
                retrieveUserRelationship(chunk2, twitter2, "data/follows_chunk2.txt")
            }
        }
        val chunk3Thread = object: Thread(){
            override fun run(){
                retrieveUserRelationship(chunk3, twitter3, "data/follows_chunk3.txt")
            }
        }

        chunk1Thread.start()
        chunk2Thread.start()
        chunk3Thread.start()

        chunk1Thread.join()
        chunk2Thread.join()
        chunk3Thread.join()
        retrieveUserRelationship(chunk4, twitter3, "data/follows_chunk4.txt")

    } catch (e: Throwable) {
        println("Failed to retrieve all user nodes: $e")
    }
}

fun createFollowsRelationship(follower: String, followee: String, driver: Driver) {
    try {
        driver.session()
            .writeTransaction {
                it.run("""
                    MATCH (user1: User), (user2: User)
                    WHERE user1.screen_name='$follower' and user2.screen_name='$followee'
                    MERGE (user1)-[f: FOLLOWS]->(user2)
                    RETURN user1, user2, f""").summary()
            }

    } catch (e: Throwable) {
        println("Failed txn in createFollowsRelationship: $e")
    }
}

fun retrieveUserRelationship(users: List<String>, twitter: Twitter, filename: String) {
    users.forEach { user1 ->
        users.forEach { user2 ->
            if (user1 != user2) {
                try {
                    println("Searching for users: $user1 - $user2")
                    val requestResult = twitter.friendsFollowers().showFriendship(user1, user2)
                    val follows = requestResult.isSourceFollowedByTarget
                    try {
                        Files.write(Paths.get(filename), "$user1,$user2,$follows".
                            toByteArray(), StandardOpenOption.APPEND)
                    } catch (e: IOException) {

                    }
                    val remaining = requestResult.rateLimitStatus.remaining
                    if (remaining == 0) {
                        println("Reaching Rate Limit - Remaining: $remaining")
                        Thread.sleep(15 * 60 * 1001)
                    }
                } catch (e: TwitterException) {
                    println("Failed to retrieve relationship for users: $user1 and $user2")
                }
            }
        }
    }
}
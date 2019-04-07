import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values
import repository.Neo4jConnection
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*


fun main() {
    val twitter1 = getTwitterClient("", "", "", "")

    val connection = Neo4jConnection("", "neo4j", "")
    try {
        val users = connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH (user: User) RETURN user.screen_name"""
                ).list().map { r -> r.values()[0].toString().replace("\"", "") }
            }

//        println(users.size)
//        val users2 = mutableListOf<String>()
//        (1 .. 10).toList().forEach {
//            File("data/follows_chunk$it.txt").readLines().forEach { line ->
//                val split = line.split(",")
//                users2.add(split[0])
//            }
//        }

        val chunk1Thread = object: Thread(){
            override fun run(){
                retrieveUserRelationship(users, users, twitter1, "data/follows_chunk1.txt")
            }
        }

        chunk1Thread.start()
        chunk1Thread.join()

    } catch (e: Throwable) {
        println("Failed to retrieve all user nodes: $e")
    }
}

fun getTwitterClient(consumerKey: String,
                     consumerSecret: String,
                     accessKey:String,
                     accessTokenSecret: String): Twitter {
    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(consumerKey)
        .setOAuthConsumerSecret(consumerSecret)
        .setOAuthAccessToken(accessKey)
        .setOAuthAccessTokenSecret(accessTokenSecret)
    val tf = TwitterFactory(cb.build())
    return tf.instance
}

fun retrieveUserRelationship(users: List<String>, totalUsers: List<String>, twitter: Twitter, filename: String) {
    users.forEach { user1 ->
        totalUsers.take(4*180).forEach { user2 ->
            if (user1 != user2) {
                try {
                    println("Searching for users: $user1 - $user2")
                    val requestResult = twitter.friendsFollowers().showFriendship(user1, user2)
                    val follows = requestResult.isSourceFollowedByTarget
                    try {
                        Files.write(Paths.get(filename), "$user1,$user2,$follows\n".
                            toByteArray(), StandardOpenOption.APPEND)
                    } catch (e: IOException) {

                    }
                    try {
                        val remaining: Int? = requestResult.rateLimitStatus.remaining
                        if (remaining == 0) {
                            println("Reaching Rate Limit - Remaining: $remaining")
                            Thread.sleep(15 * 60 * 1001)
                        }
                    } catch (i: IllegalStateException) {
                        println("This boring exception again")
                    }
                } catch (e: TwitterException) {
                    println("Failed to retrieve relationship for users: $user1 and $user2")
                }
            }
        }
    }
}
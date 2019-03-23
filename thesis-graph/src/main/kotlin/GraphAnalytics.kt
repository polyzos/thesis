import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values
import repository.Neo4jConnection
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder


fun main() {
    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey("")
        .setOAuthConsumerSecret("")
        .setOAuthAccessToken("")
        .setOAuthAccessTokenSecret("")
    val tf = TwitterFactory(cb.build())
    val twitter = tf.instance


    val connection = Neo4jConnection("bolt://:7687",
        "neo4j",
        "")
    try {
        val users = connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH (user: User) RETURN user.screen_name"""
                ).list()
            }

        users.forEach { user1 ->
            println("Scanning for user: $user1")
            users.forEach { user2 ->
                if (user1.values()[0].toString() != user2.values()[0].toString()) {
                    try {
                        val isFollower= twitter.showFriendship(user1.values()[0].toString()
                            .replace("\"", ""),
                            user2.values()[0].toString().replace("\"", ""))
                        if(isFollower.isSourceFollowingTarget) {
                            println(user1.values()[0].toString()
                                .replace("\"", "") + " FOLLOWS " + user2.values()[0].toString()
                                .replace("\"", ""))
                            createFollowsRelationship(user1.values()[0].toString()
                                .replace("\"", ""),
                                user2.values()[0].toString()
                                    .replace("\"", ""), connection.getDriver())
                        }
                        if (isFollower.rateLimitStatus.remaining - 1 == 0) {
                            println("Rate Limit Exceeded.")
                            Thread.sleep(15 * 60 * 1000)
                        }
                    } catch (t: TwitterException) {
                        println("Failed to retrieve info for user: $t")
                    }
                }
            }
        }

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
                    RETURN user1, user2, f""").peek()
            }

    } catch (e: Throwable) {
        println("Failed txn in createFollowsRelationship: $e")
    }
}
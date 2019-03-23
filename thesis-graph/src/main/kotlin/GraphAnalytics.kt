import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values
import repository.Neo4jConnection
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.util.*


fun main() {
    val connection = Neo4jConnection("bolt://localhost:7687", "neo4j", "12345")
    try {
        val users = connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH (user: User) RETURN user.screen_name"""
                ).list().map { it.values()[0].toString().replace("\"", "") }
            }

        users.forEach {
            if (File("data/user_followers/$it.json").exists()) {
                println("Retrieving followers for user: $it")
                val followers = File("data/user_followers/$it.json")
                    .useLines { f -> f.toList() }
                println("User '$it' has ${followers.size} followers.")
                followers.forEach { follower ->
                    createFollowsRelationship(follower, it, connection.getDriver())
                }
            } else {
                println("Failed to retrieve followers for user $it")
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
                    RETURN user1, user2, f""").summary()
            }

    } catch (e: Throwable) {
        println("Failed txn in createFollowsRelationship: $e")
    }
}
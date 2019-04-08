import org.neo4j.driver.v1.Driver
import repository.Neo4jConnection
import java.io.File

fun main() {
    val connection = Neo4jConnection("")

    (1 .. 10).toList().forEach {
        File("data/follows_chunk$it.txt").readLines().forEach { line ->
            val split = line.split(",")
            if (split[2] == true.toString()) {
                createFollowsRelationship(split[1], split[0], connection.getDriver())
            }
        }
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

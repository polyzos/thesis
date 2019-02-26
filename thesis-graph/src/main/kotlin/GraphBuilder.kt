import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import models.Post
import models.RetweetInfo
import java.io.File

fun main() {

    val mapper = jacksonObjectMapper()

    val post = loadAndCreateTweet(mapper)
    val retweets = loadAndCreateReTweets(mapper)
    val uri = "bolt://localhost:7687"

    val connection = Neo4jConnection(uri)
    val schemaConstraint = SchemaConstraints(uri)
    val onionId: Long = 14075928

//    connection.deleteAll()
//    schemaConstraint.dropAll()
//
//    schemaConstraint.createUserConstraints()
//    schemaConstraint.createTweetConstraints()

//    connection.createUserNode(post.user_id, post.user_screen_name)
//    connection.createTweetNode(post.id, "TWEET")
//    connection.createTweetedRelationship(post.user_screen_name, post.id)
//
//    retweets.forEach {
//        connection.createUserNode(it.id, it.user_screen_name)
//        connection.createTweetNode(it.id, "RETWEET")
//        connection.createRetweetedRelationship(it.user_screen_name, it.retweet_status.retweet_status_id)
//    }
//
//    val users = loadUsers()
//    var counter: Long = 0;
//    users.forEach {
//        connection.createUserNode(++counter, it)
//        connection.createFollowsRelationship(counter, onionId)
//    }
//
//    connection.createUserNode(23145, "J. Cole")
//    connection.createUserNode(214354, "Logic")
//    connection.createUserNode(12453, "Eminem")
//    connection.createFollowsRelationship(23145, 12453)
//    connection.createFollowsRelationship(214354, 12453)
//    connection.createFollowsRelationship(12453, 214354)
//
//    connection.createTweetNode(12345, "Wet Dreamz")
//    connection.createTweetedRelationship(23145, 12345)
//
//    connection.createTweetNode(65432, "Everyday")
//    connection.createTweetedRelationship(214354, 65432)
//    connection.createRetweetedRelationship(12453, 65432)

    println(connection.getTotalNodesCount())
    println(connection.getTweetNodesCount())
    println(connection.getUserNodesCount())

    println(connection.getTotalEdgesCount())
    println(connection.getFollowsEdgesCount())
    println(connection.getTweetedEdgesCount())
    println(connection.getRetweetedEdgesCount())

}

fun loadAndCreateTweet(mapper: ObjectMapper): Post {
    return mapper.readValue(
        File("src/main/resources/post.json")
            .inputStream()
            .readBytes()
            .toString(Charsets.UTF_8)
    )
}

fun loadAndCreateReTweets(mapper: ObjectMapper): List<RetweetInfo> {
    val retweets = ArrayList<RetweetInfo>()
    File("src/main/resources/post_retweets.json")
        .forEachLine { retweets.add(mapper.readValue(it)) }
    return retweets
}

fun loadUsers(): List<String> {
    val users = ArrayList<String>()
    File("src/main/resources/TheOnion.txt")
        .forEachLine { users.add(it) }
    return users
}
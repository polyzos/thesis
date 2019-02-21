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

    val connection = Neo4jConnection("bolt://localhost:7687",
        "neo4j",
        "123456")

    connection.createUserNode(post.user_id, post.user_screen_name)
    connection.createPostNode(post.id, "TWEET")
    connection.createTweetedRelationShip(post.user_screen_name, post.id)

    retweets.forEach {
        connection.createUserNode(it.id, it.user_screen_name)
        connection.createPostNode(it.id, "RETWEET")
        connection.createRetweetedFromRelationShip(it.id, it.retweet_status.retweet_status_id)
        connection.createReTweetedRelationShip(it.user_screen_name, it.id)
    }
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
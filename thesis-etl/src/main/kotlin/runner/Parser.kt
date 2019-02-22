
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

//package runner
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import models.Tweet
//import java.io.File
//
//fun loadAndCreateReTweets(mapper: ObjectMapper): List<Tweet> {
//    val retweets = ArrayList<Tweet>()
//    File("src/main/resources/tweets/fake_tweets.json")
//        .forEachLine {
//            retweets.add(mapper.readValue(it)) }
//    return retweets
//}
//
//fun main() {
//    val mapper = jacksonObjectMapper()
//
//    loadAndCreateReTweets(mapper).forEach { println(it) }
//}

//val cb = ConfigurationBuilder()
//    .setOAuthConsumerKey("yUFhTPAFZ79L2MWFFBpJMeQWq")
//    .setOAuthConsumerSecret("HJnCbJUuAzM9ieCmUeN9mUStie6JVOqfYLioGsWdQo2FZNNxLJ")
//    .setOAuthAccessToken("931176203528097794-zBZBSmuLTsD7lM6SX6qReBZ7pfGPQh8")
//    .setOAuthAccessTokenSecret("i1aUkCcUpNLZwAxfaclJLKQLHCETNSTPbhdO4DG9fSOZI").build()
//
//val twitter = TwitterFactory(cb) .instance
//
//fun getRetweetsById(id: Long) {
//    println("Fetching For Id: $id")
//    val res = twitter.getRetweets(id)
//    println(res.size)
//    res.forEach { println(it) }
//    Thread.sleep(1000)
//}
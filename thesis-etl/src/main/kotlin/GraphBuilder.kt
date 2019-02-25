import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import utils.GraphUtils

fun main() {

    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Graph-Builder")
        .master("local[*]")
        .orCreate

    val tweets = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("src/main/resources/output/tweets.json")

    val retweets = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("src/main/resources/output/retweets.json")

    tweets.createOrReplaceTempView("tweets")
    retweets.createOrReplaceTempView("retweets")

    val tweetsList = tweets.collectAsList().map {GraphUtils.rowToParsedTweet(it) }
    println(tweetsList)
    GraphUtils.showPostsCountByUser(spark)

    val top25 = GraphUtils.findTop25Retweets(spark)

    val removePostsWithNoRetweets = GraphUtils.removeRetweetsLessThanThreshold(0, spark)

    val post = spark.sql(
        """
        SELECT * FROM tweets WHERE id = 1099368369780940806
    """)

    val postRetweets = GraphUtils.findPostRetweets(1099368369780940806, spark)

    val uri = "bolt://localhost:7687"

    val connection = Neo4jConnection(uri)

//    connection.createUserNode(post.user_id, post.user_screen_name)
//    connection.createPostNode(post.id, "TWEET")
//    connection.createTweetedRelationship(post.user_screen_name, post.id)
//
//    retweets.forEach {
//        connection.createUserNode(it.id, it.user_screen_name)
//        connection.createPostNode(it.id, "RETWEET")
//        connection.createRetweetedFromRelationship(it.id, it.retweet_status.retweet_status_id)
//        connection.createRetweetedRelationship(it.user_screen_name, it.id)
//    }

    tweetsList.forEach {
        // foreach post find its retweets
        val fetchedRetweets = GraphUtils.findPostRetweets(it.id, spark)
        println("Tweet ${it.id} has ${fetchedRetweets.count()} retweets.")

        // TODO: store tweet in the database

        // TODO: store each of its retweets in the database
    }

    spark.close()
}
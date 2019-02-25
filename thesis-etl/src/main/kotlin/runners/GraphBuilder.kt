package runners

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

    GraphUtils.showPostsCountByUser(spark)

    val top25 = GraphUtils.findTop25Retweets(spark)

    val removePostsWithNoRetweets = GraphUtils.removeRetweetsLessThanThreshold(0, spark)

    val post = spark.sql(
        """
        SELECT * FROM tweets WHERE id = 1099368369780940806
    """)

    val postRetweets = GraphUtils.findPostRetweets(1099368369780940806, spark)

    val uri = "bolt://localhost:7687"

    val connection = Neo4jConnection(uri, "neo4j", "12345")

    connection.clearDB()


    tweetsList.forEach {
        // foreach post find its retweets
        val fetchedRetweets = GraphUtils.findPostRetweets(it.id, spark)

        println("Tweet ${it.id} has ${fetchedRetweets.count()} retweets.")

        // Store tweet in the database
        connection.createUserNode(it.user_id, it.user_screen_name)
        connection.createPostNode(it.id, "TWEET")
        connection.createTweetedRelationship(it.user_screen_name, it.id)

        // Store each of its retweets in the database
        fetchedRetweets.collectAsList()
            .map { fr -> GraphUtils.rowToParsedRetweet(fr) }
            .forEach { fr ->
                connection.createUserNode(fr.user_id, fr.user_screen_name)
                connection.createPostNode(fr.id, "RETWEET")
                connection.createRetweetedFromRelationship(fr.id, fr.retweeted_status_id)
                connection.createRetweetedRelationship(fr.user_screen_name, fr.id)
        }
    }

    println("Saved records to the database.")

    connection.close()
    spark.close()
}
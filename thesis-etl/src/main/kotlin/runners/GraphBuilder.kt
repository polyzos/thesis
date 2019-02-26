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

    val replies = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("src/main/resources/output/replies.json")

    tweets.createOrReplaceTempView("tweets")
    retweets.createOrReplaceTempView("retweets")
    replies.createOrReplaceTempView("replies")

    val connection = Neo4jConnection("bolt://localhost:7687", "neo4j", "12345")
    connection.clearDB()


    tweets.collectAsList()
        .map { GraphUtils.rowToParsedTweet(it) }
        .forEach {
            // foreach post find its retweets
            val fetchedRetweets = GraphUtils.findPostRetweets(it.id, spark)
            val fetchedReplies = GraphUtils.findPostReplies(it.id, spark)
            println("Tweet ${it.id} has ${fetchedRetweets.count()} retweets and ${fetchedReplies.count()} replies.")

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

            fetchedReplies.collectAsList()
                .map { fr -> GraphUtils.rowToParsedReply(fr) }
                .forEach { fr ->
                    connection.createUserNode(fr.user_id, fr.user_screen_name)
                    connection.createPostNode(fr.id, "IN_REPLY_TO")
                    connection.createRepliedToRelationship(fr.id, fr.in_reply_to_status_id)
                    connection.createRetweetedRelationship(fr.user_screen_name, fr.id)
                }

//            if (File("src/main/resources/user_followers/${it.user_screen_name}.json").exists()) {
//                val followers =
//                    File("src/main/resources/user_followers/${it.user_screen_name}.json")
//                        .useLines { f -> f.toList() }
//                followers.forEach { f ->
//                    connection.createUserNode(null, f)
//                    connection.createFollowsRelationship(f, it.user_screen_name)
//                }
//
//            } else {
//                println("Failed to retrieve followers for user ${it.user_screen_name}")
//            }
        }

    println("Saved records to the database.")

    connection.close()
    spark.close()
}
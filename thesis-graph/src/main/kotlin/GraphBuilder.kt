import models.ParsedReTweet
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import repository.GraphRepositoryImpl
import repository.Neo4jConnection
import repository.SchemaConstraints
import utils.GraphUtils
import utils.Utilities

fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Graph-Builder")
        .master("local[*]")
        .orCreate

    val tweets = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("data/output/tweets.json")

    val retweets = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("data/output/retweets.json")

    val replies = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("data/output/replies.json")

    tweets.createOrReplaceTempView("tweets")
    retweets.createOrReplaceTempView("retweets")
    replies.createOrReplaceTempView("replies")

    val connection = Neo4jConnection("bolt://localhost:7687")
    val graphRepository = GraphRepositoryImpl(connection.getDriver())
    val schemaConstraints = SchemaConstraints(connection.getDriver())

    graphRepository.deleteAll()
    schemaConstraints.dropAll()
    schemaConstraints.createConstraints()


    tweets.collectAsList()
        .map { Utilities.rowToParsedTweet(it) }
        .forEach {
            // foreach post find its retweets
            val fetchedRetweets = GraphUtils.findPostRetweets(it.id, spark)
            val fetchedReplies = GraphUtils.findPostReplies(it.id, spark)
            println("Tweet ${it.id} has ${fetchedRetweets.count()} retweets and ${fetchedReplies.count()} replies.")

            // Store tweet in the database
            graphRepository.createUserNode(it.user_id, it.user_screen_name)
            graphRepository.createTweetNode(it.id, it.created_at, it.text, "TWEET")
            graphRepository.createTweetedRelationship(it.user_screen_name, it.id)

            // Store each of its retweets in the database
            fetchedRetweets.collectAsList()
                .map { fr -> Utilities.rowToParsedRetweet(fr) }
                .forEachIndexed { index , fr ->
                    graphRepository.createUserNode(fr.user_id, fr.user_screen_name)
                    graphRepository.createTweetNode(fr.id, fr.created_at, fr.text, "RETWEET")
                    if (index == 0) {
                        graphRepository.createRetweetedFromRelationship(fr.id, fr.retweeted_status_id, fr.created_at)
                    } else {
                        val previous = fetchedRetweets.collectAsList()
                            .map { fr -> Utilities.rowToParsedRetweet(fr) }
                            .get(index - 1)
                        graphRepository.createRetweetedFromRelationship(fr.id, previous.id, fr.created_at)
                    }
                    graphRepository.createRetweetedRelationship(fr.user_screen_name, fr.id)

                }

            fetchedReplies.collectAsList()
                .map { fr -> Utilities.rowToParsedReply(fr) }
                .forEach { fr ->
                    graphRepository.createUserNode(fr.user_id, fr.user_screen_name)
                    graphRepository.createTweetNode(fr.id, fr.created_at, fr.text, "IN_REPLY_TO")
                    graphRepository.createRepliedToRelationship(fr.id, fr.in_reply_to_status_id)
                    graphRepository.createRetweetedRelationship(fr.user_screen_name, fr.id)
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
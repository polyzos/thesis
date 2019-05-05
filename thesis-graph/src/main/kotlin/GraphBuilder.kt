import models.ParsedTweet
import models.ParsedUser
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import repository.*
import utils.GraphUtils
import utils.Utilities

private val twitterAccounts = listOf("BreitbartNews",
    "TheOnion",
    "politicususa",
    "TheBlaze_Prod",
    "beforeitsnews",
    "OccupyDemocrats",
    "redflag_RBLX",
    "DCClothesline",
    "Bipartisanism",
    "worldnetdaily",
    "21WIRE",
    "ActivistPost",
    "AmericanNewsLLC",
    "AmplifyingG",
    "ChristWire",
    "ChronicleLive",
    "ClickHole",
    "conscious_news",
    "disclosetv",
    "CRG_CRM",
    "LibAmericaOrg",
    "NewsBiscuit",
    "WorldTruthTV"
)

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
    retweets.withColumnRenamed("id", "retweet_id")
        .createOrReplaceTempView("retweets")
    replies.createOrReplaceTempView("replies")

    /**
     * Find number of retweets of each tweets
     * */
    val tweetsWithRetweetCounts = GraphUtils.retrieveTweetsWithRetweetCounts(spark)
    tweetsWithRetweetCounts.cache()

    /**
     * Find some statistics for count
     * */
    tweetsWithRetweetCounts.describe().show()

    // Keep tweets with more than 10 retweets
    val tweetsAboveThreshold = GraphUtils.retrieveTweetsAboveThreshold(
        20, 1000,
        tweetsWithRetweetCounts,
        spark)

    println(tweetsAboveThreshold.count())   // 41 stories
    insights(tweetsAboveThreshold, spark)


    val tweetsSample = retrieveSubsetOfStories(tweetsAboveThreshold)
    createGraph(tweetsSample, spark)

    spark.close()
}


internal fun retrieveSubsetOfStories(tweetsAboveThreshold: Dataset<Row>,
                                     threshold: Int = 10): MutableList<ParsedTweet> {

    val names = mutableListOf<String>()
    val tweetsSample = mutableListOf<ParsedTweet>()

    tweetsAboveThreshold.collectAsList()
        .map { Utilities.rowToParsedTweet(it) }
        .forEach {
            if (!names.contains(it.user_screen_name) && names.size < threshold) {
                names.add(it.user_screen_name)
                tweetsSample.add(it)
            }
        }
    return tweetsSample
}

internal fun insights(tweetsAboveThreshold: Dataset<Row>, spark: SparkSession) {
    println("------- Insights -------")
    tweetsAboveThreshold.createOrReplaceTempView("tat")
    val names = spark.sql(
        """
            SELECT DISTINCT(user_screen_name)
            FROM tat
        """.trimIndent()
    )
    println("Total users: ${names.count()}")
    names.collectAsList().map { it.getString(0) }.forEach {
        println(it + " -- " + twitterAccounts.contains(it))
    }
    println(tweetsAboveThreshold.count())

    spark.sql(
        """
            SELECT COUNT(user_screen_name) as total, user_screen_name
            FROM tat
            GROUP BY user_screen_name
        """.trimIndent()
    ).show(20)
}

internal fun createGraph(tweetStories: List<ParsedTweet>,
                         spark: SparkSession) {

    val connection = Neo4jConnection(
        "bolt://localhost:7687",
        "neo4j",
        "thesis@2019!kontog"
    )

    val graphRepository = GraphRepositoryImpl(connection.getDriver())
    val schemaConstraints = SchemaConstraints(connection.getDriver())
    val nodeRepository = NodeRepositoryImpl(connection.getDriver())
    val relationshipRepository = RelationshipRepositoryImpl(connection.getDriver())

    graphRepository.deleteAll()
    schemaConstraints.dropAll()
    schemaConstraints.createConstraints()

    tweetStories
        .filter { it.user_screen_name != "21WIRE" && it.user_screen_name != "BreitbartNews" }
        .forEach {
            // foreach post find its retweets
            val fetchedRetweets = GraphUtils.findPostRetweets(it.id, spark)
            val fetchedReplies = GraphUtils.findPostReplies(it.id, spark)
            val user = ParsedUser(it.user_id, it.user_screen_name, it.user_followers_count, it.user_friends_count)
            println("Tweet ${it.id} has ${fetchedRetweets.count()} retweets and ${fetchedReplies.count()} replies.")

            nodeRepository.createUserNode(user)
            nodeRepository.createTweetNode(it)
            relationshipRepository.createTweetedRelationship(user, it)
            val fetchedReactions: MutableList<Row> = mutableListOf()
            fetchedRetweets.collectAsList()
                .forEach { fr -> fetchedReactions.add(fr) }
            fetchedReplies.collectAsList()
                .forEach { fr -> fetchedReactions.add(fr) }

            fetchedReactions.sortBy { fr -> Utilities.parseDate(fr.getString(0).split(".")[0]) }
            fetchedReactions.forEachIndexed { index, fr ->
                if (fr.size() == 10) {
                    val reply = Utilities.rowToParsedReply(fr)
                    val replyUser = ParsedUser(
                        reply.user_id,
                        reply.user_screen_name,
                        reply.user_followers_count,
                        reply.user_friends_count
                    )
                    nodeRepository.createUserNode(replyUser)
                    nodeRepository.createReplyNode(reply)

                    relationshipRepository.createRepliedRelationship(replyUser, reply)
                    if (index == 0) {
                        relationshipRepository.createRepliedToRelationship(it.id, reply)
                    } else {
                        if (fetchedReactions[index - 1].size() == 10) {
                            val previous = Utilities.rowToParsedReply(fetchedReactions[index - 1])
                            relationshipRepository.createRepliedToRelationship(previous.id, reply)
                        } else {
                            val previous = Utilities.rowToParsedRetweet(fetchedReactions[index - 1])
                            relationshipRepository.createRepliedToRelationship(previous.id, reply)
                        }
                    }
                } else {
                    val retweet = Utilities.rowToParsedRetweet(fr)
                    val retweetUser = ParsedUser(
                        retweet.id,
                        retweet.user_screen_name,
                        retweet.user_followers_count,
                        retweet.user_friends_count
                    )

                    nodeRepository.createUserNode(retweetUser)
                    nodeRepository.createRetweetNode(retweet)

                    relationshipRepository.createRetweetedRelationship(retweetUser, retweet)

                    if (index == 0) {
                        relationshipRepository.createRetweetedFromRelationship(it.id, retweet)
                    } else {
                        if (fetchedReactions[index - 1].size() == 10) {
                            val previous = Utilities.rowToParsedReply(fetchedReactions[index - 1])
                            relationshipRepository.createRetweetedFromRelationship(previous.id, retweet)
                        } else {
                            val previous = Utilities.rowToParsedRetweet(fetchedReactions[index - 1])
                            relationshipRepository.createRetweetedFromRelationship(previous.id, retweet)
                        }
                    }
                }

            }
        }

    println("Saved records to the database.")
    connection.close()
}
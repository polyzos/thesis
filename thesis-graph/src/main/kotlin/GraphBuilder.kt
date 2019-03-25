import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
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
    retweets.withColumnRenamed("id", "retweet_id").createOrReplaceTempView("retweets")
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
        10,
        tweetsWithRetweetCounts,
        spark)

    val connection = Neo4jConnection("bolt://localhost:7687", "neo4j", "12345")
    val graphRepository = GraphRepositoryImpl(connection.getDriver())
    val schemaConstraints = SchemaConstraints(connection.getDriver())

    graphRepository.deleteAll()
    schemaConstraints.dropAll()
    schemaConstraints.createConstraints()

    tweetsAboveThreshold.collectAsList()
        .map { Utilities.rowToParsedTweet(it) }
        .forEach {
            // foreach post find its retweets
            val fetchedRetweets = GraphUtils.findPostRetweets(it.id, spark)
            val fetchedReplies = GraphUtils.findPostReplies(it.id, spark)
            println("Tweet ${it.id} has ${fetchedRetweets.count()} retweets and ${fetchedReplies.count()} replies.")

            // Store tweet in the database
            graphRepository.createUserNode(it.user_id, it.user_screen_name)
            graphRepository.createTweetNode(it.id, it.created_at, it.text.replace("\"",""),"TWEET")
            graphRepository.createTweetedRelationship(it.user_screen_name, it.id)

            // Store each of its retweets in the database
            fetchedRetweets.collectAsList()
                .map { fr -> Utilities.rowToParsedRetweet(fr) }
                .forEachIndexed {index, fr ->
                    graphRepository.createUserNode(fr.user_id, fr.user_screen_name)
                    graphRepository.createTweetNode(fr.id, fr.created_at,
                        fr.text.replace("\"",""),
                        "RETWEET")
                    if (index == 0) {
                        graphRepository.createRetweetedFromRelationship(fr.id, fr.retweeted_status_id, fr.created_at)
                    } else {
                        val previous = fetchedRetweets.collectAsList()
                            .map { fr -> Utilities.rowToParsedRetweet(fr) }[index - 1]
                        graphRepository.createRetweetedFromRelationship(fr.id, previous.id, fr.created_at)
                    }
                    graphRepository.createRetweetedRelationship(fr.user_screen_name, fr.id)
                }

            fetchedReplies.collectAsList()
                .map { fr -> Utilities.rowToParsedReply(fr) }
                .forEach { fr ->
                    graphRepository.createUserNode(fr.user_id, fr.user_screen_name)
                    graphRepository.createTweetNode(fr.id, fr.created_at,
                        fr.text.replace("\"",""),
                        "IN_REPLY_TO")
                    graphRepository.createRepliedToRelationship(fr.id, fr.in_reply_to_status_id)
                    graphRepository.createRetweetedRelationship(fr.user_screen_name, fr.id)
                }
        }

    println("Saved records to the database.")

    connection.close()
    spark.close()
}

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


internal fun insights(tweetsAboveThreshold: Dataset<Row>, spark: SparkSession) {
    tweetsAboveThreshold.createOrReplaceTempView("tat")
    val names = spark.sql(
        """
            SELECT DISTINCT(user_screen_name)
            FROM tat
        """.trimIndent()
    )
    println(names.count())
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

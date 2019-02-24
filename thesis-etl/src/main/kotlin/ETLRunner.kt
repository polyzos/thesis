import jp.nephy.penicillin.PenicillinClient
import jp.nephy.penicillin.core.session.config.account
import jp.nephy.penicillin.core.session.config.application
import jp.nephy.penicillin.core.session.config.token
import models.ParsedTweet
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import utils.Utilities
import java.io.File


suspend fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Tweets-Etl")
        .master("local[*]")
        .config("spark.driver.memory", "4g")
        .orCreate

//    Utilities.runSparkJob("../../tweets/", spark)
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


    val tweetsList = tweets.collectAsList().map {
        ParsedTweet(
            Utilities.parseDate(it.getString(0).split(".")[0]),
            it.getLong(1),
            it.getString(2),
            it.getLong(3),
            it.getLong(4),
            it.getLong(5),
            it.getString(6))
    }

    spark.sql("""
        SELECT COUNT(user_screen_name) as total_count, user_screen_name
        FROM tweets
        GROUP BY user_screen_name
        ORDER BY total_count DESC
        """).show()

//    val tweetUsernames = Utilities.retrieveUsernames(spark)
//        .collectAsList().map { it.getString(0) }

    val top25 = Utilities.findTop25Retweets(spark)

    val removePostsWithNoRetweets = Utilities.removeRetweetsLessThanThreshold(0, spark)

    val post = spark.sql(
        """
        SELECT * FROM tweets WHERE id = 1099368369780940806
    """)

    val postRetweets = Utilities.findPostRetweets(1099368369780940806, spark)


    val client = PenicillinClient {
        account {
            application(
                "EtK6PfV3Tzkl0JdB6MtiQx2A5",
                "QvbYmhdRzjAjoY7GYMz2UuPouqTJkUV7HzAM3oh6ibPpNF9urV")
            token(
                "931176203528097794-nvHdZKfn4y4aV4jKnH37vFzaFzlUnER",
                "cOolP4cYpsSz0rr8MKZeJ1Yq6hwwb684gDebSb6YIHcpU")
        }
    }
    tweetsList.forEach {
//        val p = Utilities.findPostRetweets(it.id, spark)
//
//        println(it.user_screen_name)
//        println(it.user_followers_count)
//
//        println("Tweet ${it.id} has ${p.count()} retweets")
        println("User ${it.user_screen_name} should have ${it.user_followers_count} followers")
        Utilities.retrieveFollowersIds(it.user_screen_name, client)
    }

    client.close()

    spark.close()
}

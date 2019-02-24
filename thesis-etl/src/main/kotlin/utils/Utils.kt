package utils

import jp.nephy.penicillin.PenicillinClient
import jp.nephy.penicillin.core.exceptions.PenicillinException
import jp.nephy.penicillin.core.session.ApiClient
import jp.nephy.penicillin.core.session.config.account
import jp.nephy.penicillin.core.session.config.application
import jp.nephy.penicillin.core.session.config.token
import jp.nephy.penicillin.endpoints.followers
import jp.nephy.penicillin.endpoints.followers.listIdsByScreenName
import jp.nephy.penicillin.endpoints.followers.listIdsByUserId
import jp.nephy.penicillin.extensions.cursor.untilLast
import jp.nephy.penicillin.extensions.rateLimit
import org.apache.spark.sql.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Utilities {

    private val fakeTweets = "fake_tweets.json"
    private val retweetsBatch = "retweets_batch.json"
    private val sampleTweetsStream = "sample_tweets_stream.json"
    private val twitterDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    internal fun runSparkJob(pathPrefix: String, spark: SparkSession) {
        val fakeTweetsData = spark.read().format("json")
            .option("header", "true")
            .option("inferSchema", "true")
            .load(pathPrefix + fakeTweets)
        .coalesce(2)

        val fakeTweetsDataWithFields = fieldExtractor(fakeTweetsData)
        val fakeTweetsDataFlattened = flattenTweetUser(fakeTweetsDataWithFields)
        fakeTweetsDataFlattened.cache()

        val sampleTweetsStreamData = spark.read().format("json")
            .option("header", "true")
            .option("inferSchema", "true")
            .load(pathPrefix + sampleTweetsStream)
            .coalesce(5)

        val sampleTweetsStreamDataWithFields = fieldExtractor(sampleTweetsStreamData)
        val sampleTweetsStreamDataFlattened = flattenTweetUser(sampleTweetsStreamDataWithFields)
        sampleTweetsStreamDataFlattened.cache()

        val retweetsBatchData = spark.read().format("json")
            .option("header", "true")
            .option("inferSchema", "true")
            .load(pathPrefix + retweetsBatch)
            .coalesce(2)

        val tweets1 = extractTweetPosts(fakeTweetsDataFlattened)
            .drop(    "in_reply_to_screen_name", "in_reply_to_status_id", "in_reply_to_user_id","retweeted_status")
        val tweets2 = extractTweetPosts(sampleTweetsStreamDataFlattened)
            .drop(    "in_reply_to_screen_name", "in_reply_to_status_id", "in_reply_to_user_id","retweeted_status")

        val tweets = tweets1?.union(tweets2)?.dropDuplicates("id")

        val retweets1 = flattenRetweetStatus(extractRetweetPosts(fakeTweetsDataFlattened))
            .drop(    "in_reply_to_screen_name", "in_reply_to_status_id", "in_reply_to_user_id", "retweeted_status")


        val retweets2 = flattenRetweetStatus(extractRetweetPosts(sampleTweetsStreamDataFlattened))
            .drop(    "in_reply_to_screen_name", "in_reply_to_status_id", "in_reply_to_user_id", "retweeted_status")
        val retweets = retweets1?.union(retweets2)?.dropDuplicates("id")

        val replies1 =  extractReplyPosts(fakeTweetsDataFlattened).drop("retweeted_status", "user_verified")
        val replies2 = extractReplyPosts(sampleTweetsStreamDataFlattened).drop("retweeted_status", "user_verified")

        val replies = replies1?.union(replies2)?.dropDuplicates("id")

        tweets?.createOrReplaceTempView("tweet_posts")
        retweets?.createOrReplaceTempView("retweet_posts")
        replies?.createOrReplaceTempView("reply_posts")

        val tweetsWithRetweets = spark.sql(
            """
            SELECT *
            FROM tweet_posts
            WHERE id IN (SELECT retweeted_status_id FROM retweet_posts)
            """
        )

        val retweetPostsWithTweet = spark.sql(
            """
            SELECT *
            FROM retweet_posts
            WHERE retweeted_status_id IN (SELECT id FROM tweet_posts)
            """)

        val repliesWithTweets = spark.sql(
            """
            SELECT *
            FROM reply_posts
            WHERE in_reply_to_status_id IN (SELECT id FROM tweet_posts)
            """)

        val moreRetweets = extractReTweetPostsFromBatch(retweetsBatchData)

        moreRetweets?.createOrReplaceTempView("moreRetweets")

        val moreRetweetsPostsWithTweet = spark.sql(
            """
            SELECT *
            FROM moreRetweets
            WHERE retweeted_status_id IN (SELECT id FROM tweet_posts)
            """)
            .drop("retweeted_status")


        val retweetPostsWithTweetMerged = retweetPostsWithTweet.union(moreRetweetsPostsWithTweet).dropDuplicates("id")
        println("""Found:
            |   ${tweetsWithRetweets.count()} tweets
            |    ${retweetPostsWithTweetMerged.count()} retweets
            |    ${repliesWithTweets.count()} replies""".trimMargin()
        )

        saveToDisk(tweetsWithRetweets, "tweets.json")
        saveToDisk(retweetPostsWithTweetMerged, "retweets.json")
        saveToDisk(repliesWithTweets, "replies.json")
    }

    internal fun removeRetweetsLessThanThreshold(threshold: Int, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT  COUNT(retweeted_status_id) as total, retweeted_status_id
                FROM retweets
                GROUP BY retweeted_status_id
                HAVING total > $threshold
                """)
    }

    internal fun findTop25Retweets(spark: SparkSession): Dataset<Row> {
        return  spark.sql("""
            SELECT  COUNT(retweeted_status_id) as total, retweeted_status_id
            FROM retweets
            GROUP BY retweeted_status_id
            ORDER BY total DESC
            LIMIT 25
            """)
    }

    internal fun findPostRetweets(retweetId: Long, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT *
                FROM retweets
                WHERE retweeted_status_id=$retweetId
                """)
    }

    internal fun retrieveUsernames(spark: SparkSession): Dataset<Row> {
        return spark.sql("""
            SELECT DISTINCT(user_screen_name)
            FROM tweets
            """)
    }

    internal fun parseDate(date: String): Date {
        return twitterDateFormatter.parse(date.replace("T"," "))
    }

    internal fun retrieveFollowersIds(screenName: String, client: ApiClient) {
        println("Retrieving followers for user: $screenName")
        try {
            val ids =
                client.followers.listIdsByScreenName(screenName)
                    .untilLast().fold(emptySequence<Long>()) { acc, nextRes ->
                        if (nextRes.rateLimit.remaining == 0) {
                            println("Sleeping for 15 minutes because rate limit exceeded")
                            Thread.sleep(60 * 15 * 1000 + 500)
                        }
                        println(nextRes.result.ids.size)
                        acc + nextRes.result.ids
                    }.toList()

            println("Retrieved: ${ids.size} follower's ids for user $screenName\n")
            saveToFile(ids.map { it.toString() },screenName + ".txt")
        } catch (e: PenicillinException) {
            println("Failed because: $e")
        }

    }

    private fun saveToFile(data: List<String>, filename: String) {
        File("src/main/resources/user_followers/$filename").bufferedWriter().use { out ->
            data.forEach {  out.write("$it\n") }
        }

    }
    private fun extractReTweetPostsFromBatch(data: Dataset<Row>): Dataset<Row>? {
        val dataWithFields = data.select(
            "created_at",
            "id",
            "retweeted_status",
            "text",
            "user"
        )
        return flattenRetweetStatus(flattenTweetUser(dataWithFields))
    }

    private fun flattenRetweetStatus(data: Dataset<Row>): Dataset<Row> {
        return  data.withColumn("retweeted_status_id", functions.col("retweeted_status.id"))
            .withColumn("retweeted_status_text", functions.col("retweeted_status.text"))
            .withColumn("retweeted_status_user_followers_count", functions.col("retweeted_status.user.followers_count"))
            .withColumn("retweeted_status_user_friends_count", functions.col("retweeted_status.user.friends_count"))
            .withColumn("retweeted_status_user_id", functions.col("retweeted_status.user.id"))
            .withColumn("retweeted_status_user_screen_name", functions.col("retweeted_status.user.screen_name"))
    }


    private fun fieldExtractor(data: Dataset<Row>): Dataset<Row> {
        return data.select(
            "created_at",
            "id",
            "in_reply_to_screen_name",
            "in_reply_to_status_id",
            "in_reply_to_user_id",
            "retweeted_status",
            "text",
            "user"
        )
    }

    private fun flattenTweetUser(data: Dataset<Row>): Dataset<Row> {
        return data
            .withColumn("user_followers_count", functions.col("user.followers_count"))
            .withColumn("user_friends_count", functions.col("user.friends_count"))
            .withColumn("user_id", functions.col("user.id"))
            .withColumn("user_screen_name", functions.col("user.screen_name"))
            .drop("user")

    }

    private fun extractTweetPosts(data: Dataset<Row>): Dataset<Row> {
        return data.where(
            functions
                .col("retweeted_status").isNull
                .and(
                    functions
                        .col("in_reply_to_status_id").isNull
                )
        )
            .dropDuplicates("id")
    }

    private fun extractRetweetPosts(data: Dataset<Row>): Dataset<Row> {
        return data.where(
            functions
                .col("retweeted_status").isNotNull
                .and(
                    functions
                        .col("in_reply_to_status_id").isNull
                )
        )
            .dropDuplicates("id")
    }

    private fun extractReplyPosts(data: Dataset<Row>): Dataset<Row> {
        return data.where(
            functions
                .col("retweeted_status").isNull
                .and(
                    functions
                        .col("in_reply_to_status_id").isNotNull
                )
        )
            .dropDuplicates("id")
    }

    private fun saveToDisk(data: Dataset<Row>, outputPath: String, mode: SaveMode = SaveMode.Append) {
        data.coalesce(1)
            .write()
            .format("json")
            .mode(mode)
            .save("src/main/resources/output/$outputPath")

    }
}
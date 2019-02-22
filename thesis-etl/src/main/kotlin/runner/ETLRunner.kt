package runner

import getRetweetsById
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.*

fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Tweets-Etl")
        .master("local[*]")
        .orCreate

    val data = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("src/main/resources/tweets/sample_tweets_stream.json")
//        .coalesce(5)
    data.cache()

    // Keep only the fields of interest
    val tweets = data.select(
        "created_at",
        "id",
        "in_reply_to_screen_name",
        "in_reply_to_status_id",
        "in_reply_to_user_id",
        "retweeted_status",
        "text",
        "user"
    )

    val tweetsWithUserFlattened = tweets
        .withColumn("user_followers_count",col("user.followers_count"))
        .withColumn("user_friends_count",col("user.friends_count"))
        .withColumn("user_id",col("user.id"))
        .withColumn("user_screen_name",col("user.screen_name"))
        .withColumn("user_verified",col("user.verified"))
        .drop("user")
    tweetsWithUserFlattened.show()

    tweetsWithUserFlattened.createOrReplaceTempView("tweets")
    val tweetPosts = spark.sql(
        """
            SELECT *
            FROM tweets
            WHERE retweeted_status IS NULL AND in_reply_to_status_id IS NULL
        """.trimIndent())
        .dropDuplicates("id")
    tweetPosts.createOrReplaceTempView("tweet_posts")


    val retweetPosts = spark.sql(
        """
            SELECT *
            FROM tweets
            WHERE retweeted_status IS NOT NULL AND in_reply_to_status_id IS NULL
        """.trimIndent()).dropDuplicates("id")
    retweetPosts.createOrReplaceTempView("retweet_posts")


    val retweetPostsWithTweet = spark.sql(
        """
        SELECT *
        FROM retweet_posts
        WHERE retweet_posts.retweeted_status.id IN (SELECT id FROM tweet_posts)
    """)

    println(retweetPosts.count())
    println(retweetPostsWithTweet.count())
//    spark.sql("""
//        SELECT retweeted_status.id FROM retweet_posts
//        """.trimIndent())
//        .groupBy("id")
//        .agg(countDistinct("id").alias("count"))
//        .sort(asc("count"))
//        .show()

    val tweetsWithRetweets = spark.sql(
        """
        SELECT *
        FROM tweet_posts
        WHERE id IN (SELECT retweet_posts.retweeted_status.id FROM retweet_posts)
    """)

    println(tweetPosts.count())
    println(tweetsWithRetweets.count())

    val replies = spark.sql(
        """
            SELECT *
            FROM tweets
            WHERE retweeted_status IS NULL AND in_reply_to_status_id IS NOT NULL
        """.trimIndent())
        .dropDuplicates("id")

    spark.close()
}
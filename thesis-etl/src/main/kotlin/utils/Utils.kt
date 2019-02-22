package utils

import org.apache.spark.sql.*

internal fun runSparkJob(path: String, spark: SparkSession) {
    val data = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load(path)
//        .coalesce(5)
    data.cache()

    // Keep only the fields of interest
    val tweets: Dataset<Row> = fieldExtractor(data)

    val tweetsWithUserFlattened = flattenTweetUser(tweets)

    tweetsWithUserFlattened.createOrReplaceTempView("tweets")
    val tweetPosts = extractTweetPosts(tweetsWithUserFlattened)

    tweetPosts.createOrReplaceTempView("tweet_posts")


    val retweetPosts = extractRetweetPosts(tweetsWithUserFlattened)
    retweetPosts.createOrReplaceTempView("retweet_posts")

    val retweetPostsWithTweet = spark.sql(
        """
            SELECT *
            FROM retweet_posts
            WHERE retweet_posts.retweeted_status.id IN (SELECT id FROM tweet_posts)
            """
    )

    val tweetsWithRetweets = spark.sql(
        """
            SELECT *
            FROM tweet_posts
            WHERE id IN (SELECT retweet_posts.retweeted_status.id FROM retweet_posts)
            """
    )

    saveToDisk(tweetsWithRetweets, "tweets.json")
    saveToDisk(retweetPostsWithTweet, "retweets.json")

    val replies = extractReplyPosts(tweetsWithUserFlattened)
    saveToDisk(replies, "replies.json")

    println("Found: ${tweetsWithRetweets.count()} tweets, ${retweetPostsWithTweet.count()} retweets and ${replies.count()} replies")
}

internal fun fieldExtractor(data: Dataset<Row>): Dataset<Row> {
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

internal fun flattenTweetUser(data: Dataset<Row>): Dataset<Row> {
    return data
        .withColumn("user_followers_count", functions.col("user.followers_count"))
        .withColumn("user_friends_count", functions.col("user.friends_count"))
        .withColumn("user_id", functions.col("user.id"))
        .withColumn("user_screen_name", functions.col("user.screen_name"))
        .withColumn("user_verified", functions.col("user.verified"))
        .drop("user")

}

internal fun extractTweetPosts(data: Dataset<Row>): Dataset<Row> {
    return data.where(functions
        .col("retweeted_status").isNull
        .and(functions
            .col("in_reply_to_status_id").isNull))
        .dropDuplicates("id")
}

internal fun extractRetweetPosts(data: Dataset<Row>): Dataset<Row> {
    return data.where(
        functions
            .col("retweeted_status").isNotNull
            .and(
                functions
                    .col("in_reply_to_status_id").isNull))
        .dropDuplicates("id")
}

internal fun extractReplyPosts(data: Dataset<Row>): Dataset<Row> {
    return data.where(
        functions
            .col("retweeted_status").isNull
            .and(
                functions
                    .col("in_reply_to_status_id").isNotNull))
        .dropDuplicates("id")
}

internal fun saveToDisk(data: Dataset<Row>, outputPath: String, mode: SaveMode = SaveMode.Append) {
    data.coalesce(1)
        .write()
        .format("json")
        .mode(mode)
        .save("src/main/resources/output/$outputPath")

}
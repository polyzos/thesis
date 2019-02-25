package utils

import models.ParsedReTweet
import models.ParsedTweet
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession

object GraphUtils {

    internal fun findTop25Retweets(spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
            SELECT  COUNT(retweeted_status_id) as total, retweeted_status_id
            FROM retweets
            GROUP BY retweeted_status_id
            ORDER BY total DESC
            LIMIT 25
            """
        )
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

    internal fun findPostRetweets(retweetId: Long, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT *
                FROM retweets
                WHERE retweeted_status_id=$retweetId
                """)
    }

    internal fun showPostsCountByUser(spark: SparkSession) {
        spark.sql("""
            SELECT COUNT(user_screen_name) as total_count, user_screen_name
            FROM tweets
            GROUP BY user_screen_name
            ORDER BY total_count DESC
            """).show()
    }

    internal fun rowToParsedTweet(row: Row): ParsedTweet {
        return ParsedTweet(
            Utilities.parseDate(row.getString(0).split(".")[0]),
            row.getLong(1),
            row.getString(2),
            row.getLong(3),
            row.getLong(4),
            row.getLong(5),
            row.getString(6))
    }

    internal fun rowToParsedRetweet(row: Row): ParsedReTweet {
        return ParsedReTweet(
            Utilities.parseDate(row.getString(0).split(".")[0]),
            row.getLong(1),
            row.getLong(2),
            row.getString(3),
            row.getLong(4),
            row.getLong(5),
            row.getLong(6),
            row.getString(7),
            row.getString(8),
            row.getLong(9),
            row.getLong(10),
            row.getLong(11),
            row.getString(12)
        )
    }
}
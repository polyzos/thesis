package utils

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

    internal fun findPostReplies(replyId: Long, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT *
                FROM replies
                WHERE in_reply_to_status_id=$replyId
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
}
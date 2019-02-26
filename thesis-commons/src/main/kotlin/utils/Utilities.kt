package utils

import models.ParsedReTweet
import models.ParsedReply
import models.ParsedTweet
import org.apache.spark.sql.Row
import java.text.SimpleDateFormat
import java.util.*

object Utilities {

    private val twitterDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun parseDate(date: String): Date {
        return twitterDateFormatter.parse(date.replace("T"," "))
    }

    fun rowToParsedTweet(row: Row): ParsedTweet {
        return ParsedTweet(
            parseDate(row.getString(0).split(".")[0]),
            row.getLong(1),
            row.getString(2),
            row.getLong(3),
            row.getLong(4),
            row.getLong(5),
            row.getString(6)
        )
    }

    fun rowToParsedRetweet(row: Row): ParsedReTweet {
        return ParsedReTweet(
            parseDate(row.getString(0).split(".")[0]),
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

    fun rowToParsedReply(row: Row): ParsedReply {
        return ParsedReply(
            parseDate(row.getString(0).split(".")[0]),
            row.getLong(1),
            row.getString(2),
            row.getLong(3),
            row.getLong(4),
            row.getString(5),
            row.getLong(6),
            row.getLong(7),
            row.getLong(8),
            row.getString(9)
        )
    }
}
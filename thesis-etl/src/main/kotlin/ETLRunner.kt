import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import utils.Utilities

fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Tweets-Etl")
        .master("local[*]")
        .orCreate

    Utilities.runSparkJob("src/main/resources/tweets/fake_tweets.json", spark)
    Utilities.runSparkJob("src/main/resources/tweets/sample_tweets_stream.json", spark)
//    runSparkJob("src/main/resources/tweets/retweets_batch.json", spark, false)

    spark.close()
}


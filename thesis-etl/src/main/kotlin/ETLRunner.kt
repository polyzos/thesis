import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import utils.Utilities

fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Tweets-Etl")
        .master("local[*]")
        .config("spark.driver.memory", "4g")
        .orCreate

    Utilities.runSparkJob("../../tweets/", spark)

    spark.close()
}

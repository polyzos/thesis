import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import utils.ETLUtils


fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("tweets-etl")
        .master("local[*]")
        .orCreate

    ETLUtils.runSparkJob("../../tweets/", spark)

    spark.close()
}

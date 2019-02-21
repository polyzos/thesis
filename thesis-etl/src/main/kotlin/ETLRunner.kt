import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession

fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Tweets-Etl")
        .master("local[*]")
        .orCreate

//    val data = spark.read().format("json")
//        .option("header", "true")
//        .option("inferSchema", "true")
//        .load("src/main/resources/")
//        .coalesce(5)
//    data.cache()
//    data.createOrReplaceTempView("")

    spark.close()
}
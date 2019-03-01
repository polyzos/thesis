import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import jp.nephy.penicillin.PenicillinClient
import jp.nephy.penicillin.core.session.config.account
import jp.nephy.penicillin.core.session.config.application
import jp.nephy.penicillin.core.session.config.token
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import utils.FollowersFetcherUtils
import utils.Utilities

fun main() {
    Logger.getLogger("org.apache").level = Level.WARN

    val spark = SparkSession.builder()
        .appName("Followers-Fetcher")
        .master("local[*]")
        .orCreate

    val tweets = spark.read().format("json")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("data/output/tweets.json")

    val config = ConfigurationProperties.fromResource("config.properties")

    val consumerKey         = config[Key("consumer.key", stringType)]
    val consumerSecret      = config[Key("consumer.secret", stringType)]
    val accessToken         = config[Key("access.token", stringType)]
    val accessTokenSecret   = config[Key("access.token.secret", stringType)]

    val client = PenicillinClient {
        account {
            application(consumerKey,consumerSecret)
            token(accessToken,accessTokenSecret)
        }
    }

    val tweetsList = tweets.collectAsList().map { Utilities.rowToParsedTweet(it) }

    tweetsList.forEach {
        println("User ${it.user_screen_name} should have ${it.user_followers_count} followers")
        FollowersFetcherUtils.retrieveFollowersIds(it.user_screen_name, client)
    }

    client.close()

    spark.close()
}
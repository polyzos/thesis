package utils

import jp.nephy.penicillin.core.exceptions.PenicillinException
import jp.nephy.penicillin.core.session.ApiClient
import jp.nephy.penicillin.endpoints.followers
import jp.nephy.penicillin.endpoints.followers.listIdsByScreenName
import jp.nephy.penicillin.extensions.cursor.untilLast
import jp.nephy.penicillin.extensions.rateLimit
import java.io.File

object FollowersFetcherUtils {
    internal fun retrieveFollowersIds(screenName: String, client: ApiClient) {
        println("Retrieving followers for user: $screenName")
        try {
            val ids =
                client.followers.listIdsByScreenName(screenName)
                    .untilLast().fold(emptySequence<Long>()) { acc, nextRes ->
                        if (nextRes.rateLimit.remaining == 0) {
                            println("Sleeping for 15 minutes because rate limit exceeded")
                            Thread.sleep(60 * 15 * 1000 + 500)
                        }
                        println(nextRes.result.ids.size)
                        acc + nextRes.result.ids
                    }.toList()
            Thread.sleep(500)
            println("Retrieved: ${ids.size} follower's ids for user $screenName\n")
            saveToFile(ids.map { it.toString() }, "$screenName.txt")
        } catch (e: PenicillinException) {
            println("Failed because: $e")
        }

    }

    private fun saveToFile(data: List<String>, filename: String) {
        File("src/main/resources/user_followers/$filename").bufferedWriter().use { out ->
            data.forEach { out.write("$it\n") }
        }

    }
}
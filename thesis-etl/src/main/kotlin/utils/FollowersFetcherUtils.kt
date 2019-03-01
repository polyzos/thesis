package utils

import jp.nephy.penicillin.core.exceptions.PenicillinException
import jp.nephy.penicillin.core.session.ApiClient
import jp.nephy.penicillin.endpoints.followers
import jp.nephy.penicillin.endpoints.followers.listIdsByScreenName
import jp.nephy.penicillin.extensions.cursor.untilLast
import java.io.File

object FollowersFetcherUtils {
    internal fun retrieveFollowersIds(screenName: String, client: ApiClient) {
        println("Retrieving followers for user: $screenName")
        try {
            val ids =
                client.followers.listIdsByScreenName(screenName)
                    .untilLast().fold(emptySequence<Long>()) { acc, nextRes ->
                        println("${nextRes.result.ids.size} - ${nextRes.result.ids}")
                        acc + nextRes.result.ids
                    }.toList()
            println("Retrieved: ${ids.size} follower's ids for user $screenName\n")
            saveToFile(ids.map { it.toString() }, "$screenName.txt")
        } catch (e: PenicillinException) {
            println("Failed because: $e")
        }

    }

    private fun saveToFile(data: List<String>, filename: String) {
        File("data/test/$filename").bufferedWriter().use { out ->
            data.forEach { out.write("$it\n") }
        }

    }
}
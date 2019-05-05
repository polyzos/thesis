import org.neo4j.driver.v1.Values
import repository.Neo4jConnection
import twitter4j.Relationship
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


fun main() {
    val twitterConnections = listOf(
        getTwitterClient(
            "CRQ2F7OVCnlw4s8Q6VWt4MYfG",
            "u9WQECaSRutSNdYD84qP4SMUs2wUl4U1tnb9iqY0fFJdg1JTF3",
            "405836734-jFnYURTXVLsdxMnK4ME51M0srDMl3s3RRYWazKXG",
            "DL2RITe7G5Xg0NYtHPJo38zC09NJ1alwtL6wcBIN2hx4x"),
        getTwitterClient(
        "RvltTntT8vLV8ePFwBgjxo06J",
        "s6JkNrDXXr6wy1qgTG7Ye5bbpj1UJnKAeFifRi91WLWM9WAXma",
        "405836734-fBNd3B0gSeBjwzgrdzZoE5l6HPcRvw8NUUfydrql",
        "1uBZIm2aSnEKrkCKxj56JlJp9qhAfAVwlIOrDVFozv9iF"),
        getTwitterClient(
        "KkyWonRmpx5VwQ1Sfxklk6sQa",
        "cmhEcEik8NV88CYNtG2Zo5znYfmOXeqIvjejgJMmH1qywF1wZE",
        "405836734-wcwBggrItUUFagKmxeQ7ajGFX8G4L1PNKa7R0MY4",
        "IgmDPxgan6GtknD8Er1pMrTTbw0I61SU8OImuTK5jFVgb"),
        getTwitterClient(
        "9D70vgeJJbywTZsOsMZm1YcWm",
        "J23FpGHi7g3dCCw2fXzuzQsTs2wRpgYKkjY4WsmfFMqPSWXWpl",
        "405836734-eWg9vCROosI3xzBAzI1vP5jSQFtPIqzt2jZltx3a",
        "OEVPfNuROotS8ra3sCWmxPUn8NnTF8ZWxMl10qHOpZaLn"),
        getTwitterClient(
            "Xmhwq8UFXFXW6ZkMQTZPG5EXV",
            "4XHkJ22CLLcUprrrSOTSZIcc4jkHlbI6Y6Imr9WNIZwJAYCEOo",
        "405836734-1RmIVwot3q8AwFo0UKVVTsFtt7BsvAOEAybRYDuc",
        "47OLiQhQcziDPSWffTHYJ1J0sMiWglIPe3x0oeTQS4s4b"),
        getTwitterClient(
        "oDpG0XZyBRQXQ00Z6KcxSqevF",
        "RRTmb8sMJJfypi1zt62qWFGN4LvIjDoqY9GBUXX5FAIyyUg7lN",
        "405836734-hOYHO86SvveQEfU0xw3OE09rOuUA9cAMhf4c1CXt",
        "Pjn1Gq8erxxpSqBbe2GdVzLTiionTbtEgV4WpNjif5EiV"),
        getTwitterClient(
        "V1KBJLcDFWIz6APYWbh7SX4ue",
        "yL9Up8cGdN8rAgvqVfwhPtOgtMB0u1qhjxRmibZlx0orOWD64I",
        "405836734-mtXS4NpNwev3K2b9iJdZQNYHnjQ7gW9j247M3LOZ",
        "McWNI1Nu3doXiYVjfhDaQJxYnp1CKOe8mOu5WcKHkYW51"),
        getTwitterClient(
        "DbVfIqKjf6SrMv5BuwQt15WcK",
        "P31sb2irK0VkjhOunvG8HIG02X4pWP9sDc4umiFmEgQIB4b8xf",
        "405836734-yRNu3OwvRra67PFB5mVAdifoXhac96OXYDfOUKdG",
        "zgQ5bJyyG4835IUEkZnfsCG8dJIhpTuQf6Aq5lKJMn1K9")
    )

    val connection = Neo4jConnection(
        "bolt://thesis.polyzos.dev:7687",
        "neo4j",
        "thesis@2019!kontog"
    )

    val originalPostIds =  connection.getDriver().session()
        .writeTransaction {
            it.run(
                """
                    MATCH (tw:Tweet {type: "TWEET"}) RETURN tw.id
                """.trimIndent()
            ).list()
        }.map { it[0].asLong() }


    val threads = originalPostIds.chunked(2)
        .mapIndexed { index, stories ->
            Thread {
                processStories(stories,twitterConnections[index],connection)
            }
        }

    threads.forEach { it.start() }
    threads.forEach { it.join() }
}

fun processStories(storyIds: List<Long>, twitter: Twitter, connection: Neo4jConnection) {
    storyIds.forEach {
        processStoryChain(it, twitter, connection)
    }
}

fun processStoryChain(id: Long, twitter: Twitter, connection: Neo4jConnection) {
    try {
        val result =  connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
                 WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
                 RETURN user.screen_name as user, end.created_at as timestamp, storyStarter.screen_name as starter, start.created_at as origin""", Values.parameters("id", id)
                ).list()
            }

        val storyUsers = result.map {
            it[0].asString()
        }.toMutableList()
        storyUsers.add(result[0][2].asString())

        val file = File("data/relationships/eight/$id.txt")
        if (!file.exists()) {
                file.createNewFile()
            }
        val userPairs = mutableListOf<String>()
        storyUsers.forEach { user1 ->
            storyUsers.forEach { user2 ->
                if (user1 != user2) {
                    userPairs.add("$user1,$user2")
                }
            }
        }

        userPairs.forEach {
            val user1 = it.split(",")[0]
            val user2 = it.split(",")[1]
            retrieveUserRelationship(user1, user2, twitter, id)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun getTwitterClient(consumerKey: String,
                     consumerSecret: String,
                     accessKey:String,
                     accessTokenSecret: String): Twitter {
    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(consumerKey)
        .setOAuthConsumerSecret(consumerSecret)
        .setOAuthAccessToken(accessKey)
        .setOAuthAccessTokenSecret(accessTokenSecret)
    val tf = TwitterFactory(cb.build())
    return tf.instance
}

fun retrieveUserRelationship(source: String,
                             target: String,
                             twitter: Twitter,
                             id: Long) {

    println("Searching for users: $source - $target")
    var requestResult: Relationship? = null
    try {
         requestResult = twitter.friendsFollowers().showFriendship(source, target)
    } catch (e: TwitterException) {
        println("---- Something went wrong when retrieving relationship ----")
    }
    val isSourceFollowedByTarget= requestResult?.isSourceFollowedByTarget
    val  isTargetFollowedBySource= requestResult?.isTargetFollowedBySource
    try {
        Files.write(Paths.get("data/relationships/eight/$id.txt"), "$source,$target,$isSourceFollowedByTarget\n".
            toByteArray(), StandardOpenOption.APPEND)
        Files.write(Paths.get("data/relationships/eight/$id.txt"), "$target,$source,$isTargetFollowedBySource\n".
            toByteArray(), StandardOpenOption.APPEND)
    } catch (e: IOException) {

    }
    try {
        val remaining: Int? = requestResult?.rateLimitStatus?.remaining
        if (remaining == 0) {
            println("Reaching Rate Limit - Remaining: $remaining")
            Thread.sleep(15 * 60 * 1001)
        }
    } catch (i: IllegalStateException) {
        println("This boring exception again")
    }
}

//fun retrieveUserRelationship(users: List<String>, totalUsers: List<String>, twitter: Twitter, filename: String) {
//    users.forEach { user1 ->
//        totalUsers.take(4*180).forEach { user2 ->
//            if (user1 != user2) {
//                try {
//                    println("Searching for users: $user1 - $user2")
//                    val requestResult = twitter.friendsFollowers().showFriendship(user1, user2)
//                    val follows = requestResult.isSourceFollowedByTarget
//                    try {
//                        Files.write(Paths.get(filename), "$user1,$user2,$follows\n".
//                            toByteArray(), StandardOpenOption.APPEND)
//                    } catch (e: IOException) {
//
//                    }
//                    try {
//                        val remaining: Int? = requestResult.rateLimitStatus.remaining
//                        if (remaining == 0) {
//                            println("Reaching Rate Limit - Remaining: $remaining")
//                            Thread.sleep(15 * 60 * 1001)
//                        }
//                    } catch (i: IllegalStateException) {
//                        println("This boring exception again")
//                    }
//                } catch (e: TwitterException) {
//                    println("Failed to retrieve relationship for users: $user1 and $user2")
//                }
//            }
//        }
//    }
//}

//fun getTweetChain(id: Long) {
//    val connection = Neo4jConnection("bolt://localhost:7687")
//    try {
//        val results = connection.getDriver().session()
//            .writeTransaction {
//                it.run(
//                    """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
//                 WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
//                 RETURN user.screen_name as user, end.created_at as timestamp, storyStarter.screen_name as starter, start.created_at as origin""",
//                    Values.parameters("id", id)
//                ).list()
//            }
//    } catch (e: Throwable) {
//        e.printStackTrace()
//    }
//
//}

//fun throughFiles() {
////    val filenames = ArrayList<File>()
////    File("data/relationships/").walk().forEach { filenames.add(it) }
////
////    filenames.forEach{ it.forEachLine { println(it) }}
//
//    File("data/relationships/").walk()
//        .forEach {
//            if (it.toString() != "data/relationships") {
//                val id = it.toString()
//                    .split(".")[0]
//                    .split("relationships/")[1].toLong()
//
//                val connection = Neo4jConnection("bolt://localhost:7687")
//                try {
//                    val results = connection.getDriver().session()
//                        .writeTransaction {txn ->
//                            txn.run(
//                                """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
//                                WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
//                                RETURN user.screen_name as user, end.created_at as timestamp, storyStarter.screen_name as starter, start.created_at as origin""",
//                                Values.parameters("id", id)
//                            ).list()
//                        }
//
//                    val users = HashMap<String, ArrayList<String>>()
//                    it.forEachLine { r ->
//                        val split = r.split(",")
//                        val source = split[0]
//                        val target = split[1]
//                        val followed = split[2]
//                        if (followed == "true") {
//                            if (users.containsKey(source)) {
//                                users[source]?.add(target)
//                            } else {
//                                val l = ArrayList<String>()
//                                l.add(target)
//                                users[source] = l
//                            }
//                        }
//                    }
//                    val starter = results[0]
//                        .asMap()["starter"].toString()
//                    val origin = results[0]
//                        .asMap()["origin"].toString()
//                    val line = StringBuilder()
//                        .append(starter)
//                        .append(",")
//                        .append(origin)
//                        .append(",[")
//
//                    users[starter]?.forEachIndexed { index, e ->
//                        if (index + 1 != users[starter]?.size) {
//                            line.append(e).append(",")
//                        } else {
//                            line.append(e)
//                        }
//                    }
//                    line.append("]")
//                    val file = File("data/stories/$id.txt")
//                    file.createNewFile()
//                    try {
//                        file.appendText(line.toString() + "\n")
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    results.forEach {r ->
//                        val currentUser = r.asMap()["user"].toString()
//                        val timestamp = r.asMap()["timestamp"].toString()
//                        val l = StringBuilder()
//                            .append(currentUser)
//                            .append(",")
//                            .append(timestamp)
//                            .append(",[")
//                        users[currentUser]?.forEachIndexed { index, e ->
//                            if (index + 1 != users[currentUser]?.size) {
//                                line.append(e).append(",")
//                            } else {
//                                line.append(e)
//                            }
//                        }
//                        l.append("]")
//                        val newFile = File("data/stories/$id.txt")
//                        try {
//                            newFile.appendText(l.toString() + "\n")
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }
//                    }
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                }
//
//            }
//        }
//}
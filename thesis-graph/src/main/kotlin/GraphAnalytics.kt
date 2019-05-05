import org.neo4j.driver.v1.Record
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
    val twitter1 = getTwitterClient(
        "CRQ2F7OVCnlw4s8Q6VWt4MYfG",
        "u9WQECaSRutSNdYD84qP4SMUs2wUl4U1tnb9iqY0fFJdg1JTF3",
        "405836734-jFnYURTXVLsdxMnK4ME51M0srDMl3s3RRYWazKXG",
        "DL2RITe7G5Xg0NYtHPJo38zC09NJ1alwtL6wcBIN2hx4x")

    val twitter2 = getTwitterClient(
        "RvltTntT8vLV8ePFwBgjxo06J",
        "s6JkNrDXXr6wy1qgTG7Ye5bbpj1UJnKAeFifRi91WLWM9WAXma",
        "405836734-fBNd3B0gSeBjwzgrdzZoE5l6HPcRvw8NUUfydrql",
        "1uBZIm2aSnEKrkCKxj56JlJp9qhAfAVwlIOrDVFozv9iF")

    val twitter3 = getTwitterClient(
        "KkyWonRmpx5VwQ1Sfxklk6sQa",
        "cmhEcEik8NV88CYNtG2Zo5znYfmOXeqIvjejgJMmH1qywF1wZE",
        "405836734-wcwBggrItUUFagKmxeQ7ajGFX8G4L1PNKa7R0MY4",
        "IgmDPxgan6GtknD8Er1pMrTTbw0I61SU8OImuTK5jFVgb")

    val twitter4 = getTwitterClient(
        "9D70vgeJJbywTZsOsMZm1YcWm",
        "J23FpGHi7g3dCCw2fXzuzQsTs2wRpgYKkjY4WsmfFMqPSWXWpl",
        "405836734-eWg9vCROosI3xzBAzI1vP5jSQFtPIqzt2jZltx3a",
        "OEVPfNuROotS8ra3sCWmxPUn8NnTF8ZWxMl10qHOpZaLn")

    val twitter5 = getTwitterClient(
        "Xmhwq8UFXFXW6ZkMQTZPG5EXV",
        "4XHkJ22CLLcUprrrSOTSZIcc4jkHlbI6Y6Imr9WNIZwJAYCEOo",
        "405836734-1RmIVwot3q8AwFo0UKVVTsFtt7BsvAOEAybRYDuc",
        "47OLiQhQcziDPSWffTHYJ1J0sMiWglIPe3x0oeTQS4s4b")

    val twitter6 = getTwitterClient(
        "oDpG0XZyBRQXQ00Z6KcxSqevF",
        "RRTmb8sMJJfypi1zt62qWFGN4LvIjDoqY9GBUXX5FAIyyUg7lN",
        "405836734-hOYHO86SvveQEfU0xw3OE09rOuUA9cAMhf4c1CXt",
        "Pjn1Gq8erxxpSqBbe2GdVzLTiionTbtEgV4WpNjif5EiV")

    val twitter7 = getTwitterClient(
        "V1KBJLcDFWIz6APYWbh7SX4ue",
        "yL9Up8cGdN8rAgvqVfwhPtOgtMB0u1qhjxRmibZlx0orOWD64I",
        "405836734-mtXS4NpNwev3K2b9iJdZQNYHnjQ7gW9j247M3LOZ",
        "McWNI1Nu3doXiYVjfhDaQJxYnp1CKOe8mOu5WcKHkYW51")

    val twitter8 = getTwitterClient(
        "DbVfIqKjf6SrMv5BuwQt15WcK",
        "P31sb2irK0VkjhOunvG8HIG02X4pWP9sDc4umiFmEgQIB4b8xf",
        "405836734-yRNu3OwvRra67PFB5mVAdifoXhac96OXYDfOUKdG",
        "zgQ5bJyyG4835IUEkZnfsCG8dJIhpTuQf6Aq5lKJMn1K9")

    val connection = Neo4jConnection(
        "bolt://thesis.polyzos.dev:7687",
        "neo4j",
        "thesis@2019!kontog"
    )
    val listOfIds = listOf(1100056351584239619,
    1099511061953294336,
    1099775353038417920,
    1099700578047967232,
    1099774228994420737,
    1099790746134241283,
    1100737937434644480,
    1099366365016682496)

    val chunk1Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[0], twitter1, connection)
        }
    }

    val chunk2Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[1], twitter2, connection)
        }
    }

    val chunk3Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[2], twitter3, connection)
        }
    }

    val chunk4Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[3], twitter4, connection)
        }
    }

    val chunk5Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[4], twitter5, connection)
        }
    }

    val chunk6Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[5], twitter6, connection)
        }
    }

    val chunk7Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[6], twitter7, connection)
        }
    }

    val chunk8Thread = object: Thread() {
        override fun run() {
            getChainUsers(listOfIds[7], twitter8, connection)
        }
    }

    chunk1Thread.start()
    chunk2Thread.start()
    chunk3Thread.start()
    chunk4Thread.start()
    chunk5Thread.start()
    chunk6Thread.start()
    chunk7Thread.start()
    chunk8Thread.start()
    chunk1Thread.join()
    chunk2Thread.join()
    chunk3Thread.join()
    chunk4Thread.join()
    chunk5Thread.join()
    chunk6Thread.join()
    chunk7Thread.join()
    chunk8Thread.join()
//    try {
//        val users = connection.getDriver().session()
//            .writeTransaction {
//                it.run(
//                    """MATCH (user: User) RETURN user.screen_name"""
//                ).list().map { r -> r.values()[0].toString().replace("\"", "") }
//            }
//
////        println(users.size)
////        val users2 = mutableListOf<String>()
////        (1 .. 10).toList().forEach {
////            File("data/follows_chunk$it.txt").readLines().forEach { line ->
////                val split = line.split(",")
////                users2.add(split[0])
////            }
////        }
//
//        val chunk1Thread = object: Thread(){
//            override fun run(){
//                retrieveUserRelationship(users, users, twitter1, "data/follows_chunk1.txt")
//            }
//        }
//
//        chunk1Thread.start()
//        chunk1Thread.join()
//
//    } catch (e: Throwable) {
//        println("Failed to retrieve all user nodes: $e")
//    }

//    getChainUsers(1109901712586915846, twitter1)
//    listOfIds.forEach { getChainUsers(it, twitter1) }
//    throughFiles()
}

fun getChainUsers(id: Long, twitter: Twitter, connection: Neo4jConnection) {
    val users = HashMap<String, HashMap<String, ArrayList<String>>>()
    try {
        val result =  connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
                 WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
                 RETURN user.screen_name as user, end.created_at as timestamp, storyStarter.screen_name as starter, start.created_at as origin""", Values.parameters("id", id)
                ).list()
            }
        result.forEachIndexed { index, it ->
            val key = it.asMap().get("user").toString()
            val value = it.asMap().get("timestamp").toString()
            if (users.containsKey(key)) {
                users.get(key)?.get("timestamps")?.add(value)
            } else {
                val values = HashMap<String, ArrayList<String>>()
                val timestamps = ArrayList<String>()
                timestamps.add(value)
                values.put("timestamps", timestamps)
                users.put(key, values)
            }
            for (i in (index + 1)..(result.size - 1)) {
                val target = result[i].asMap().get("user").toString()
                if (!key.equals(target)) {
                    val res = retrieveUserRelationship(target, key, twitter, id)
                    if (res != null) {
                        if (res) {
                            if (users.get(key)?.containsKey("follows")!!) {
                                users.get(key)?.get("follows")?.add(target)
                            } else {
                                val follows = ArrayList<String>()
                                follows.add(target)
                                users.get(key)?.put("follows", follows)
                            }
                        }
                    }
                }
            }
            println(it.asMap().get("user"))
        }
        result.forEach {
            val key = it.asMap().get("user").toString()
            val starter = it.asMap().get("starter").toString()
            retrieveUserRelationship(starter, key, twitter, id)
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

fun retrieveUserRelationship(source: String, target: String, twitter: Twitter, id: Long): Boolean? {
//    val result = twitter.friendsFollowers().showFriendship(source, target)
//
//    return result.isSourceFollowedByTarget
    var file = File("data/relationships/eight/$id.txt")
    file.createNewFile()
    println("Searching for users: $source - $target")
    var requestResult: Relationship? = null
    try {
         requestResult = twitter.friendsFollowers().showFriendship(source, target)
    } catch (e: TwitterException) {
        println("Passing exception")
    }
    val follows = requestResult?.isSourceFollowedByTarget
    try {
        Files.write(Paths.get("data/relationships/eight/$id.txt"), "$source,$target,$follows\n".
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


    return follows
}

fun retrieveUserRelationship(users: List<String>, totalUsers: List<String>, twitter: Twitter, filename: String) {
    users.forEach { user1 ->
        totalUsers.take(4*180).forEach { user2 ->
            if (user1 != user2) {
                try {
                    println("Searching for users: $user1 - $user2")
                    val requestResult = twitter.friendsFollowers().showFriendship(user1, user2)
                    val follows = requestResult.isSourceFollowedByTarget
                    try {
                        Files.write(Paths.get(filename), "$user1,$user2,$follows\n".
                            toByteArray(), StandardOpenOption.APPEND)
                    } catch (e: IOException) {

                    }
                    try {
                        val remaining: Int? = requestResult.rateLimitStatus.remaining
                        if (remaining == 0) {
                            println("Reaching Rate Limit - Remaining: $remaining")
                            Thread.sleep(15 * 60 * 1001)
                        }
                    } catch (i: IllegalStateException) {
                        println("This boring exception again")
                    }
                } catch (e: TwitterException) {
                    println("Failed to retrieve relationship for users: $user1 and $user2")
                }
            }
        }
    }
}

fun getTweetChain(id: Long) {
    val connection = Neo4jConnection("bolt://localhost:7687")
    try {
        val results = connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
                 WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
                 RETURN user.screen_name as user, end.created_at as timestamp, storyStarter.screen_name as starter, start.created_at as origin""",
                    Values.parameters("id", id)
                ).list()
            }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

}

fun throughFiles() {
//    val filenames = ArrayList<File>()
//    File("data/relationships/").walk().forEach { filenames.add(it) }
//
//    filenames.forEach{ it.forEachLine { println(it) }}

    File("data/relationships/").walk()
        .forEach {

            if (!it.toString().equals("data/relationships")) {
                val id = it.toString().split(".")[0].split("relationships/")[1].toLong()

                val connection = Neo4jConnection("bolt://localhost:7687")
                try {
                    val results = connection.getDriver().session()
                        .writeTransaction {
                            it.run(
                                """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
                                WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
                                RETURN user.screen_name as user, end.created_at as timestamp, storyStarter.screen_name as starter, start.created_at as origin""",
                                Values.parameters("id", id)
                            ).list()
                        }

                    val users = HashMap<String, ArrayList<String>>()
                    it.forEachLine {
                        val split = it.split(",")
                        val source = split[0]
                        val target = split[1]
                        val followed = split[2]
                        if (followed.equals("true")) {
                            if (users.containsKey(source)) {
                                users.get(source)?.add(target)
                            } else {
                                val l = ArrayList<String>()
                                l.add(target)
                                users.put(source, l)
                            }
                        }
                    }
                    val starter = results.get(0).asMap().get("starter").toString()
                    val origin = results.get(0).asMap().get("origin").toString()
                    val line = StringBuilder().append(starter).append(",").append(origin).append(",[")
                    users.get(starter)?.forEachIndexed { index, it ->
                        if (index + 1 != users.get(starter)?.size) {
                            line.append(it).append(",")
                        } else {
                            line.append(it)
                        }
                    }
                    line.append("]")
                    val file = File("data/stories/$id.txt")
                    file.createNewFile()
                    try {
                        file.appendText(line.toString() + "\n")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    results.forEach {
                        val currentUser = it.asMap().get("user").toString()
                        val timestamp = it.asMap().get("timestamp").toString()
                        val line = StringBuilder().append(currentUser).append(",").append(timestamp).append(",[")
                        users.get(currentUser)?.forEachIndexed { index, it ->
                            if (index + 1 != users.get(currentUser)?.size) {
                                line.append(it).append(",")
                            } else {
                                line.append(it)
                            }
                        }
                        line.append("]")
                        val file = File("data/stories/$id.txt")
                        try {
                            file.appendText(line.toString() + "\n")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }


        }
}
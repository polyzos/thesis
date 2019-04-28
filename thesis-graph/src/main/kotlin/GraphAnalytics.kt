import org.neo4j.driver.v1.Values
import repository.Neo4jConnection
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


fun main() {
    val twitter1 = getTwitterClient("", "", "", "")

    val connection = Neo4jConnection("bolt://localhost:7687", "", "")
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

    getChainUsers(1109901712586915846, twitter1)
}

fun getChainUsers(id: Long, twitter: Twitter) {
    val connection = Neo4jConnection("bolt://localhost:7687", "", "")
    val users = HashMap<String, HashMap<String, ArrayList<String>>>()
    try {
        val result =  connection.getDriver().session()
            .writeTransaction {
                it.run(
                    """MATCH u=(user:User)-[r:POSTED_REPLY|POSTED_RETWEET]->(end:Tweet)-[:REPLIED_TO|RETWEETED_FROM*]->(start:Tweet {id: $id, type: 'TWEET'})<-[:TWEETED]-(storyStarter:User)
                 WHERE end.type = 'RETWEET' OR end.type = 'REPLY'
                 RETURN user.screen_name as user, end.created_at as timestamp""", Values.parameters("id", id)
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
                    if (retrieveUserRelationship(target, key, twitter, id)) {
                        if(users.get(key)?.containsKey("follows")!!) {
                            users.get(key)?.get("follows")?.add(target)
                        } else {
                            val follows = ArrayList<String>()
                            follows.add(target)
                            users.get(key)?.put("follows", follows)
                        }
                    }
                }
            }

            println(it.asMap().get("user"))
        }
        result.forEach {
            val key = it.asMap().get("user").toString()
            retrieveUserRelationship("TheOnion", key, twitter, id)
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

fun retrieveUserRelationship(source: String, target: String, twitter: Twitter, id: Long): Boolean {
//    val result = twitter.friendsFollowers().showFriendship(source, target)
//
//    return result.isSourceFollowedByTarget
    println("Searching for users: $source - $target")
    val requestResult = twitter.friendsFollowers().showFriendship(source, target)
    val follows = requestResult.isSourceFollowedByTarget
    try {
        Files.write(Paths.get("data/relationships/$id.txt"), "$source,$target,$follows\n".
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
package repository

import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values

class GraphRepositoryImpl(val driver: Driver): GraphRepository {

    /**
     * TODO: Need to break it down further
     *  - maybe in tweet, retweet, reply, followers repos
     * */
    override fun createUserNode(id: Long, screenName: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MERGE (user:User {screen_name: '$screenName', id : $id})
                            RETURN user.id""",
                        Values.parameters("id", id, "screen_name", screenName)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createUserNode: $e")
        }
    }

    override fun createFollowsRelationship(follower: Long, followee: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (follower: User {id: $follower})
                            MATCH (followee: User {id: $followee})
                            MERGE (follower)-[:FOLLOWS]->(followee)
                            RETURN follower.id, followee.id""",
                        Values.parameters("id", follower, "id", followee)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createFollowsRelationship: $e")
        }
    }

    override fun createTweetNode(id: Long, type: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MERGE (tweet:Tweet {id: $id, type:'$type'})
                            RETURN tweet.id""",
                        Values.parameters("id", id, "type", type)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createTweetNode: $e")
        }
    }

    override fun createTweetedRelationship(screenName: String, id: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run( """
                        MATCH (u:User {screen_name: '$screenName'})
                        MATCH (p:Tweet {id: $id})
                        MERGE (u)-[:TWEETED]->(p)
                        RETURN u.id, p.id
                        """,
                        Values.parameters("screen_name", screenName, "id", id)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createTweetedRelationship: $e")
        }
    }

    override fun createRetweetedRelationship(screenName: String, id: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (u:User {screen_name: '$screenName'})
                            MATCH (p:Tweet {id: $id})
                            MERGE (u)-[:MADE_RETWEETED]->(p)
                            RETURN u.id, p.id
                            """,
                        Values.parameters("screen_name", screenName, "id", id)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createRetweetedRelationship: $e")
        }
    }

    override fun createRepliedToRelationship(tweetId: Long, replyId: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (p1:Tweet {id: $tweetId})
                            MATCH (p2:Tweet {id: $replyId})
                            MERGE (p1)-[:REPLIED_TO]->(p2)
                            RETURN p1.id, p2.id
                            """,
                        Values.parameters("tweetId", tweetId, "replyId", replyId)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createRepliedToRelationship: $e")
        }
    }

    override fun createRetweetedFromRelationship(tweetId: Long, retweetId: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (p1:Tweet {id: $tweetId})
                            MATCH (p2:Tweet {id: $retweetId})
                            MERGE (p1)-[:RETWEETED_FROM]->(p2)
                            RETURN p1.id, p2.id
                            """,
                        Values.parameters("tweetId", tweetId, "retweetId", retweetId)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in createRetweetedFromRelationship: $e")
        }
    }

    override fun getTweetInfo(id: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                       MATCH (tweet:Tweet {id: $id})
                       RETURN tweet.id
                       """,
                        Values.parameters("id", id)
                    )
                        .single().get(0)
                }
        } catch (e: Throwable) {
            println("Failed txn in getTweetInfo: $e")
        }
    }

    override fun deleteAll() {
        try {
            println("Dropping all data from the database.")
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (n)
                            DETACH DELETE n
                        """
                    )
                }
        } catch (e: Throwable) {
            println("Failed txn in deleteAll: $e")
        }
    }

    override fun getTotalNodesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                           MATCH (n)
                           RETURN count(*)
                           """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getTotalNodesCount: $e")
            return -1
        }
    }

    override fun getUserNodesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                           MATCH (:User)
                           RETURN count(*)
                           """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getUserNodesCount: $e")
            return -1
        }
    }

    override fun getTweetNodesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                           MATCH (:Tweet)
                           RETURN count(*)
                           """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getTweetNodesCount: $e")
            return -1
        }
    }

    override fun getTotalEdgesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                           MATCH ()-->()
                           RETURN count(*)
                           """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getAllEdgesCount: $e")
            return -1
        }

    }

    override fun getFollowsEdgesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                        MATCH ()-[:FOLLOWS]->()
                        RETURN count(*)
                        """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getFollowsEdgesCount: $e")
            return -1
        }
    }

    override fun getTweetedEdgesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH ()-[:TWEETED]->()
                            RETURN count(*)
                        """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getTweetedEdgesCount: $e")
            return -1
        }
    }

    override fun getRetweetedEdgesCount(): Int {
        try {
            return driver.session()
                .writeTransaction {
                    it.run(
                        """
                           MATCH ()-[:RETWEETED]->()
                           RETURN count(*)
                        """)
                        .single().get(0).asInt()
                }
        } catch (e: Throwable) {
            println("Failed txn in getRetweetedEdgesCount: $e")
            return -1
        }
    }
}
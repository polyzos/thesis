package repository

import org.neo4j.driver.v1.Driver

class SchemaConstraints(private val driver: Driver) {

    internal fun dropAll() {
        println("Dropping all constraints that exist in the database.")
        dropUserConstraints()
        dropTweetConstraints()
    }

    internal fun createConstraints() {
        createUserConstraints()
        createTweetConstraints()
    }

    private fun createUserConstraints() {
        try {
            println("Adding a constraint for :User")
            driver.session()
                .writeTransaction {
                    it.run("CREATE CONSTRAINT ON (user:User) ASSERT user.id IS UNIQUE")
                }
        } catch (e: Throwable) {
            println("Failed adding constraint on user: $e")
        }
    }

    private fun createTweetConstraints() {
        try {
            println("Adding a constraint for :Tweet")
            driver.session()
                .writeTransaction {
                    it.run("CREATE CONSTRAINT ON (tweet:Tweet) ASSERT tweet.id IS UNIQUE")
                }
        } catch (e: Throwable) {
            println("Failed adding constraint on tweet: $e")
        }
    }

    private fun dropUserConstraints() {
        try {
            driver.session()
                .writeTransaction {
                    it.run("DROP CONSTRAINT ON (user:User) ASSERT user.id IS UNIQUE")
                }
        } catch (e: Throwable) {
            println("Failed dropping constraints on user: $e")
        }
    }

    private fun dropTweetConstraints() {
        try {
            driver.session()
                .writeTransaction {
                    it.run("DROP CONSTRAINT ON (tweet:Tweet) ASSERT tweet.id IS UNIQUE")
                }
        } catch (e: Throwable) {
            println("Failed dropping constraints on tweet: $e")
        }
    }
}
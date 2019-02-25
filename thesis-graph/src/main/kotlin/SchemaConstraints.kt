import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase

class SchemaConstraints(uri: String,
             user: String? = null,
             password: String? = null): AutoCloseable {
    private val driver: Driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))

    fun createUserConstraints() {
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

    fun createTweetConstraints() {
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

    fun dropUserConstraints() {
        try {
            driver.session()
                .writeTransaction {
                    it.run("DROP CONSTRAINT ON (user:User) ASSERT user.id IS UNIQUE")
                }
        } catch (e: Throwable) {
            println("Failed dropping constraints on user: $e")
        }
    }

    fun dropTweetConstraints() {
        try {
            driver.session()
                .writeTransaction {
                    it.run("DROP CONSTRAINT ON (tweet:Tweet) ASSERT tweet.id IS UNIQUE")
                }
        } catch (e: Throwable) {
            println("Failed dropping constraints on tweet: $e")
        }
    }

    fun dropAll() {
        println("Dropping all constraints that exist in the database.")
        dropUserConstraints()
        dropTweetConstraints()
    }

    override fun close() {
        driver.close()
    }
}
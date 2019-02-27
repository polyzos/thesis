package repository

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase

class Neo4jConnection(uri: String,
                      user: String? = null,
                      password: String? = null): AutoCloseable {
    private val driver: Driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))

    fun getDriver(): Driver = driver

    override fun close() {
        driver.close()
    }
}
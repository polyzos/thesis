plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin libs
    "implementation"(kotlin("stdlib-jdk8"))

    // Spark
    compile(group="org.apache.spark", name="spark-core_2.12", version= "2.4.0")
    compile(group="org.apache.spark", name="spark-sql_2.12", version= "2.4.0")

}
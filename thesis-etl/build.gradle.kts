plugins {
    kotlin("jvm")
}


repositories {
    jcenter()
    maven(url = "https://kotlin.bintray.com/ktor")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://kotlin.bintray.com/kotlin-eap")
    maven(url = "https://dl.bintray.com/nephyproject/penicillin")
}


dependencies {
    // Kotlin libs
    "implementation"(kotlin("stdlib-jdk8"))

    implementation(project(":thesis-commons"))

    // Spark
    compile(group="org.apache.spark", name="spark-core_2.12", version= "2.4.0")
    compile(group="org.apache.spark", name="spark-sql_2.12", version= "2.4.0")

    compile("com.natpryce:konfig:1.6.10.0")

//    implementation("jp.nephy:penicillin:4.1.3-eap-2")
    implementation("io.ktor:ktor-client-cio:1.1.3")

}


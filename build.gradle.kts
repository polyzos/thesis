import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))


    implementation(project(":thesis-commons"))

    // Spark
    compile(group="org.apache.spark", name="spark-core_2.12", version= "2.4.0")
    compile(group="org.apache.spark", name="spark-sql_2.12", version= "2.4.0")

    compile("com.natpryce:konfig:1.6.10.0")

//    implementation("jp.nephy:penicillin:4.1.3-eap-2")
    implementation("io.ktor:ktor-client-cio:1.1.3")
}

allprojects {
    group = "com.ipolyzos"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.suppressWarnings = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}


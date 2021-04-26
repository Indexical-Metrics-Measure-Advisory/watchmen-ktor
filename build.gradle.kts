val ktor_version: String by project
val kotlin_version: String by project
val jna_version: String by project
val logback_version: String by project
val jackson_version: String by project
val spring_version: String by project
val mongo_driver_version: String by project
val mysql_driver_version: String by project
val oracle_driver_version: String by project
val mail_version: String by project
val trino_version: String by project

plugins {
    java
    application
    kotlin("jvm") version "1.4.30"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.imma"
version = "0.0.1"

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    @Suppress("DEPRECATION")
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "io.ktor.server.netty.EngineMain"
            )
        )
    }
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    implementation("io.trino:trino-jdbc:$trino_version")
    implementation("org.mongodb:mongodb-driver-sync:$mongo_driver_version")
    implementation("mysql:mysql-connector-java:$mysql_driver_version")
    implementation("com.oracle.database.jdbc:ojdbc8:$oracle_driver_version")
    implementation("org.springframework:spring-context-support:$spring_version")
    implementation("com.sun.mail:jakarta.mail:$mail_version")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlin_version")
    implementation("net.java.dev.jna:jna:$jna_version")
//    testImplementation("org.springframework.dataio.ktor:ktor-server-tests:$ktor_version")
}
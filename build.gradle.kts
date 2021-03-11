val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val spring_data_mango_version: String by project
val mongo_driver_version: String by project

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
    implementation("org.springframework.data:spring-data-mongodb:$spring_data_mango_version")
    implementation("org.mongodb:mongodb-driver-sync:$mongo_driver_version")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-script-util:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.4.30")
    implementation("net.java.dev.jna:jna:5.7.0")
//    testImplementation("org.springframework.dataio.ktor:ktor-server-tests:$ktor_version")
}
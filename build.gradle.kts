val ktorVersion: String by project
val kotlinVersion: String by project
val kodeinVersion: String by project
val logbackVersion: String by project
val awsSdk2Version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.21"
}

group = "com.rp199"
version = "0.0.1"
application {
    mainClass.set("com.rp199.aws.proxy.ApplicationKt")
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    implementation("org.kodein.di:kodein-di-generic-jvm:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodeinVersion")
    implementation("software.amazon.awssdk:auth:$awsSdk2Version")
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

group = "me.nat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(platform("com.typesafe.akka:akka-bom_${"2.13"}:2.6.15"))



    implementation("com.typesafe.akka:akka-persistence-typed_${"2.13"}")
    implementation("com.typesafe.akka:akka-cluster-sharding-typed_${"2.13"}")
    testImplementation("com.typesafe.akka:akka-persistence-testkit_${"2.13"}")

}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "13"
}

application {
    mainClass.set("MainKt")
}
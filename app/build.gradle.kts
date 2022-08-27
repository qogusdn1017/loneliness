import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
    idea
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server:2.1.0")
    implementation("io.ktor:ktor-server-core:2.1.0")
    implementation("io.ktor:ktor-server-netty:2.1.0")
    implementation("ch.qos.logback:logback-classic:1.3.0-beta0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
}

application {
    mainClass.set("world.komq.loneliness.LonelinessApplicationKt")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    create<Copy>("outputJar") {
        from(shadowJar)
        into(rootProject.file("out"))
    }
}

idea {
    module {
        excludeDirs.add(file("out"))
    }
}
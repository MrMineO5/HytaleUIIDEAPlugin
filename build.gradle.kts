plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "app.ultradev"
version = "1.1.1"

repositories {
    mavenCentral()
//    maven("https://mvn.ultradev.app/snapshots")
    mavenLocal()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    api("app.ultradev.hytaleui:all:3.0.1")

    intellijPlatform {
        intellijIdea("2024.3.2")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    jvmToolchain(21)
}

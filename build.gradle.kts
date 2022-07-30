import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    signing
    `maven-publish`
}

val libraryVersion: String by project

group = "com.jacobtread.shttp"
version = libraryVersion

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    val jvmTarget: String by project
    kotlinOptions.jvmTarget = jvmTarget
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "Sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val sonatypeUser: String? by project
            val sonatypeKey: String? by project
            credentials {
                username = sonatypeUser
                password = sonatypeKey
            }
        }
    }

    publications {
        register<MavenPublication>("sonatype") {
            from(components["java"])

            pom {
                name.set("Kotlin Simple HTTP")
                description.set("Simple kotlin http server and routing logic")
                url.set("https://github.com/jacobtread/kotlin-netty-http")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/jacobtread/kotlin-netty-http/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("jacobtread")
                        name.set("Jacobtread")
                        email.set("jacobtread@gmail.com")
                    }
                }

                scm {
                    url.set("https://github.com/jacobtread/kotlin-netty-http/blaze-core")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
}

allprojects {
    group = "io.github.ansonliao"
    version = "2.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    val artifactName = name
    val artifactDescription = description ?: name

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    // JUnit 4 only. Gradle defaults to the JUnit Platform, which would silently skip
    // every test in this project and still report BUILD SUCCESSFUL.
    tasks.withType<Test>().configureEach {
        useJUnit()
    }

    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    // Apache License 2.0 §4.4: ship LICENSE and NOTICE inside every artifact's META-INF.
    tasks.withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) { into("META-INF") }
        from(rootProject.file("NOTICE")) { into("META-INF") }
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(project.components["java"])
                pom {
                    name.set(artifactName)
                    description.set(artifactDescription)
                    url.set("https://github.com/ansonliao/htmlelements")
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("AlexanderTolmachev")
                            name.set("Alexander Tolmachev")
                        }
                        developer {
                            id.set("eroshenkoam")
                            name.set("Artem Eroshenko")
                        }
                        developer {
                            id.set("artkoshelev")
                            name.set("Artem Koshelev")
                        }
                        developer {
                            id.set("leonsabr")
                            name.set("Leonid Rudenko")
                        }
                        developer {
                            id.set("lanwen")
                            name.set("Kirill Merkushev")
                        }
                        developer {
                            id.set("ansonliao")
                            name.set("ansonliao")
                        }
                    }
                    scm {
                        url.set("https://github.com/ansonliao/htmlelements")
                        connection.set("scm:git:git://github.com/ansonliao/htmlelements.git")
                        developerConnection.set("scm:git:ssh://git@github.com/ansonliao/htmlelements.git")
                    }
                }
            }
        }
    }
}

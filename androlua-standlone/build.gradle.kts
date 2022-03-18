import java.util.*

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("signing")
}



ext {
    set("PUBLISH_GROUP_ID", "io.github.dingyi222666")
    set("PUBLISH_ARTIFACT_ID", "androlua-standlone")
    set("PUBLISH_VERSION", "1.0.2")
    extra["signing.keyId"] = ""
    extra["signing.password"] = ""
    extra["signing.secretKeyRingFile"] = ""
    extra["ossrhUsername"] = ""
    extra["ossrhPassword"] = ""
}

val secretPropsFile = project.rootProject.file("local.properties")

if (secretPropsFile.exists()) {
    println("Found secret props file, loading props")
    val p = Properties()
    p.load(secretPropsFile.inputStream())
    p.forEach { name, value ->
        ext[name.toString()] = value
    }
} else {
    println("No props file, loading env vars")
}


android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            ndk {
                abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a"))
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
        // ...
    }
}

dependencies {
    api(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))//libs jar
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10")
    compileOnly("androidx.appcompat:appcompat:1.4.1")
}



afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {

                groupId = project.ext["PUBLISH_GROUP_ID"].toString()
                artifactId = project.ext["PUBLISH_ARTIFACT_ID"].toString()
                version = project.ext["PUBLISH_VERSION"].toString()

                //sources jar and java doc
                from(components.getByName("release"))

                //artifact("$buildDir/outputs/aar/${project.getName()}-release.aar")

                pom {

                    name.set(project.ext["PUBLISH_ARTIFACT_ID"].toString())
                    description.set("A Androlua Standlone library")
                    // If your project has a dedicated site, use its URL here
                    url.set("https://github.com/dingyi222666/AndroLua-Standalone")


                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("dingyi222666")
                            name.set("dingyi222666")
                            email.set("dingyi222666@foxmail.com")
                        }
                    }
                    // Version control info, if you're using GitHub, follow the format as seen here
                    scm {
                        connection.set("scm:git@github.com:dingyi222666/AndroLua-Standalone.git")
                        developerConnection.set(
                            "scm:git@github.com:dingyi222666/AndroLua-Standalone.git"
                        )
                        url.set("https://github.com/dingyi222666/AndroLua-Standalone/tree/master")
                    }


                }


            }
        }
        repositories {
            // The repository to publish to, Sonatype/MavenCentral
            maven {
                // This is an arbitrary name, you may also use "mavencentral" or
                // any other name that's descriptive for you
                name = "AndroLua-Standlone"

                val releasesRepoUrl =
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl =
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                // You only need this if you want to publish snapshots, otherwise just set the URL
                // to the release repo directly
                url = if (version.toString()
                        .endsWith("SNAPSHOT")
                ) snapshotsRepoUrl else releasesRepoUrl

                // The username and password we've fetched earlier
                credentials {
                    username = project.ext["ossrhUsername"].toString()
                    password = project.ext["ossrhPassword"].toString()
                }
            }
        }
    }
}



signing {
    sign(publishing.publications)
}
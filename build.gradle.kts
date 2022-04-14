// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

plugins {
    id ("com.android.application") version ("7.1.2") apply(false)
    id ("com.android.library") version ("7.1.2") apply(false)
}


tasks.register("clean", Delete::class.java) {
    this.delete(rootProject.buildDir)
}

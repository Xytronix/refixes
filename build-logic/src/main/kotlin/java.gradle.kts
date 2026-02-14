package cc.irori.refixes.build

plugins {
    java
    id("com.diffplug.spotless")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

spotless {
    java {
        palantirJavaFormat()
    }
}

tasks {
    processResources {
        inputs.property("version", version)
        filteringCharset = "UTF-8"

        filesMatching("manifest.json") {
            expand(
                "version" to version
            )
        }
    }

    compileJava {
        dependsOn(spotlessApply)
    }

    compileJava {
        options.release.set(25)
        options.encoding = "UTF-8"
    }
}

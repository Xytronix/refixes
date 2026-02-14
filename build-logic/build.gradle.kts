plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spotless)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

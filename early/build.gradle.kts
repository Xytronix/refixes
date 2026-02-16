plugins {
    id("cc.irori.refixes.build.java")
}

repositories {
    maven("https://cursemaven.com")
}

dependencies {
    compileOnly(libs.hytale)
    compileOnly(libs.hyxin)
}

base {
    archivesName.set("refixes-early")
}

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

plugins {
    id("cc.irori.refixes.build.java") apply false
}

/* Project Properties */
val modGroup    = project.property("mod_group")     as String
val modVersion  = project.property("mod_version")   as String

allprojects {
    group = modGroup
    version = modVersion
}

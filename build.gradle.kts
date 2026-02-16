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

tasks {
    register<Delete>("cleanBundle") {
        delete(layout.buildDirectory.dir("bundle"))
    }

    register<Copy>("copyBundleManifest") {
        dependsOn("cleanBundle")
        from("bundle") {
            include(
                "manifest.json",
                "LICENSE",
                "README.md"
            )
        }
        into(layout.buildDirectory.dir("bundle"))
        expand(
            "version" to modVersion,
            "hytaleVersion" to libs.versions.hytale.get()
        )
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    register<Copy>("collectEarlyJar") {
        dependsOn("copyBundleManifest")
        val task = project(":early").tasks.named("shadowJar")
        from(task)
        into(layout.buildDirectory.dir("bundle/earlyplugins"))
    }

    register<Copy>("collectMainJar") {
        dependsOn("copyBundleManifest")
        val task = project(":plugin").tasks.named("shadowJar")
        from(task)
        into(layout.buildDirectory.dir("bundle/mods"))
    }

    register<Zip>("bundle") {
        dependsOn("collectEarlyJar", "collectMainJar")
        archiveFileName.set("${project.name}-bundle-${version}.zip")
        destinationDirectory.set(file("bundle"))
        from(layout.buildDirectory.dir("bundle"))
    }
}

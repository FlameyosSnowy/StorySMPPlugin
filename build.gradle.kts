plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    //id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "me.flame.storysmp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.10")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.10")
    implementation("dev.dejvokep:boosted-yaml:1.3.6")
}

tasks.shadowJar {
    relocate("revxrsal.commands", "me.flame.libs.lampcommands")
    relocate("dev.dejvokep.boostedyaml", "me.flame.libs.boostedyaml")
}

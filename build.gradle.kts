import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.propertykey"
version = "1.0.0"

val ideaVersion = "2025.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(ideaVersion)
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.properties")
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Property Key Support"
        version = "1.0.0"
        vendor {
            name = "Prannoy Mathew"
        }
        changeNotes = """
            <ul>
                <li>Initial release</li>
                <li>Navigation and Find Usages between braced property keys and <code>messages.properties</code></li>
                <li>Autocompletion for <code>message</code> attributes in Java annotations</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = "251"
        }
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdeaCommunity, ideaVersion)
            create(IntelliJPlatformType.IntellijIdeaUltimate, ideaVersion)
        }
    }
}

val runIdeUltimate by intellijPlatformTesting.runIde.registering {
    type = IntelliJPlatformType.IntellijIdeaUltimate
    version = ideaVersion

    plugins {
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.properties")
    }
}

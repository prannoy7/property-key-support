import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.propertykey"
version = "1.1.0"

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
        version = "1.1.0"
        vendor {
            name = "Prannoy Mathew"
        }
        changeNotes = """
            <h3>1.1.0</h3>
            <ul>
                <li>Richer code completion with message text preview and source file name</li>
                <li>Support for <code>ValidationMessages.properties</code> and <code>messages_*.properties</code> locale files</li>
                <li>Rename refactoring between Java keys and property files</li>
                <li>Quick fix to create missing keys in <code>messages.properties</code></li>
                <li>Gutter icon for unresolved property keys</li>
            </ul>
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Navigation and Find Usages between braced property keys and <code>messages.properties</code></li>
                <li>Autocompletion for <code>message</code> attributes in Java annotations</li>
                <li>Unresolved key highlighting</li>
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

    signing {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
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

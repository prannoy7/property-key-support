# Marketplace assets

Assets for publishing **Property Key Support** on the [JetBrains Marketplace](https://plugins.jetbrains.com/).

## Screenshots (`screenshots/`)

Four PNG images at **1280×800** (16:10), matching JetBrains’ recommended size:

| File | Feature shown |
|------|----------------|
| `01-go-to-declaration.png` | Split view: Java annotations ↔ `messages.properties` |
| `02-ctrl-click-navigation.png` | Ctrl+Click go to declaration on a braced key |
| `03-code-completion.png` | Autocomplete for `message` attribute values |
| `04-find-usages.png` | Find Usages from a property key |

Upload these under **Media** on your plugin page in the Marketplace admin panel.

> **Tip:** JetBrains may prefer real IDE captures for approval. Use the demo project below with **Run IDE** to replace any mockup with authentic screenshots.

## Capturing real screenshots

1. From the project root, run the sandbox IDE:
   ```bash
   ./gradlew runIde
   ```
2. In the sandbox, choose **File → Open** and select `marketplace/demo-project/`.
3. Open `UserDto.java` and `messages.properties`.
4. Capture each feature:
   - **Go to declaration** — Ctrl+Click `{user.email.required}`
   - **Completion** — type inside a `message = "..."` value and invoke completion (Ctrl+Space)
   - **Find usages** — place caret on a key in `messages.properties`, then Alt+F7
5. Export at **1280×800** (crop or resize in an image editor).

## Getting Started (Marketplace listing)

Paste the content below into the **Getting Started** field on your plugin page in the Marketplace admin panel. An HTML version is included at the end if the editor expects rich text.

### Installation

Install **Property Key Support** from the Marketplace, restart IntelliJ IDEA if prompted, and open any Java project that uses Bean Validation or Jakarta Validation with `messages.properties`. No extra configuration is required.

### What you need in your project

- Java validation annotations such as `@NotNull`, `@Size`, and `@Email`
- A `message` attribute with a braced key:

```java
@NotNull(message = "{user.email.required}")
@Size(min = 1, message = "{user.name.required}")
```

- A `messages.properties` file in your project:

```properties
user.email.required=Email is required
user.name.required=Name is required
```

### Go to declaration

Place the caret on a braced key inside a `message` attribute and press **Ctrl+B** (or **Ctrl+Click** / **Cmd+Click** on macOS) to jump to the matching entry in `messages.properties`.

### Code completion

While editing a `message` value, start typing inside the string and invoke completion with **Ctrl+Space**. The plugin suggests keys from all `messages.properties` files in the project and inserts them in braced form, for example `{user.email.required}`.

### Find usages

Open `messages.properties`, place the caret on a property key, and press **Alt+F7** to see every Java annotation that references that key.

### Unresolved keys

If a braced key does not match any entry in your `messages.properties` files, IntelliJ marks it as an unresolved reference, so missing or mistyped keys are easy to spot during development.

### Tips

- Keys must be wrapped in braces inside the annotation, for example `"{user.email.required}"`.
- All `messages.properties` files in the project are searched automatically.
- Works with both Bean Validation (`javax.validation`) and Jakarta Validation (`jakarta.validation`).

## Plugin signing

See [SIGNING.md](SIGNING.md) for certificate generation and how to sign before `publishPlugin`. Signing is configured in `build.gradle.kts` via `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and `PRIVATE_KEY_PASSWORD` environment variables.

## Plugin icon

The 40×40 SVG icons are in `src/main/resources/META-INF/`:

- `pluginIcon.svg` (light theme)
- `pluginIcon_dark.svg` (dark theme)

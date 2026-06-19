# Plugin signing

Property Key Support is configured for [JetBrains plugin signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html). Signing runs automatically before `publishPlugin` when credentials are provided.

## Generate a certificate (one-time)

```bash
# Linux / macOS / Git Bash on Windows
openssl genrsa -out private.pem 2048
openssl req -new -x509 -key private.pem -out chain.crt -days 3650
```

Register the public certificate (`chain.crt`) on your [Marketplace vendor profile](https://plugins.jetbrains.com/author/me).

## Option A — environment variables (CI / local publish)

Set these before running `./gradlew publishPlugin`:

| Variable | Value |
|----------|--------|
| `CERTIFICATE_CHAIN` | Full contents of `chain.crt` |
| `PRIVATE_KEY` | Full contents of `private.pem` |
| `PRIVATE_KEY_PASSWORD` | Password used when generating the key (empty if none) |

In IDE run configurations, multi-line PEM values are often Base64-encoded single lines.

## Option B — local files via environment

Point the environment variables at your PEM files (PowerShell example):

```powershell
$env:CERTIFICATE_CHAIN = Get-Content -Raw chain.crt
$env:PRIVATE_KEY = Get-Content -Raw private.pem
$env:PRIVATE_KEY_PASSWORD = ""
```

Bash:

```bash
export CERTIFICATE_CHAIN="$(cat chain.crt)"
export PRIVATE_KEY="$(cat private.pem)"
export PRIVATE_KEY_PASSWORD=""
```

## Verify signing

```bash
./gradlew signPlugin
```

The signed archive is produced under `build/distributions/`.

## Security

Never commit `private.pem`, `chain.crt`, or `gradle.properties` containing key paths. They are listed in `.gitignore`.

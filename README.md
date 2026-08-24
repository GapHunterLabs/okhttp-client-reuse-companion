# OkHttp Client Reuse Companion

Gutter warning icon on an `OkHttpClient` built via `new OkHttpClient()`
or `new OkHttpClient.Builder().build()` inside a regular method body
— OkHttp's own documentation states "OkHttp performs best when you
create a single OkHttpClient instance and reuse it for all of your
HTTP calls, because each client holds its own connection pool and
thread pools... creating a client for each request wastes resources
on idle pools". Building one inside a method means a brand new
connection pool (and thread pools) gets created on every call.

## Why it exists

`new OkHttpClient()` reads like a harmless local variable, but
OkHttp's own docs are explicit that it's meant to be a shared,
long-lived instance — every construction pays real, avoidable
resource cost. Nothing in the IDE flags a client built the wrong way
today.

## Why built this way

- **100% static text/PSI analysis** — matches the class name by simple
  text, so it works whether the real OkHttp jar is on the classpath or
  not. Java and Kotlin.

## v0.1 scope — stated honestly, not exhaustively

Only flags the "build from scratch" shape —
`existingClient.newBuilder()....build()`, OkHttp's own documented
pattern for customizing a shared client without duplicating its
connection pool, is never flagged (correctly, since it isn't the
anti-pattern this plugin targets). Never flags a build call inside a
constructor or a field/property initializer.

## Usage

Open any Java/Kotlin file using OkHttp. A client built inside a
regular method shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.

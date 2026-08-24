# Demo data for screenshots

`PaymentApiClient.java` — the constructor builds `sharedClient` once
(not flagged), `charge` builds a brand new client on every call
(flagged).

## How to get the screenshot

1. `./gradlew runIde` from `okhttp-client-reuse-companion`, open this
   `demo/` folder as the project.
2. Full Screen, open `PaymentApiClient.java` — a warning icon should
   appear on the `new OkHttpClient.Builder().build()` call in
   `charge` only.
3. Screenshot with both methods visible, save into
   `okhttp-client-reuse-companion/docs/screenshots/`. Close the
   sandbox.

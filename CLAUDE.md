# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Starter code for "Práctica 6" of the TC2007B course: an Android (Kotlin + Jetpack Compose) notice board ("tablón de avisos") that logs in against the course API at `https://startdroid.com/api/`. Screens, networking and DTOs are already written; the practice's topic — **session handling** (sending credentials, storing tokens encrypted, attaching/refreshing them) — is deliberately missing. The step-by-step guide lives at https://startdroid.com/practicas/avisos.html and is organized in blocks/checkpoints (Bloque A, a2, c2, …). Code comments reference those blocks; follow the guide's structure rather than inventing a different design.

All identifiers, comments and UI strings are in Spanish. Keep new code in Spanish and match the existing heavily-commented, explanatory style (this is teaching material).

## Build / run

- There is **no `gradlew` script** committed and no `gradle` on PATH; only `gradle/wrapper/gradle-wrapper.properties` (Gradle 9.7.1). The project is meant to be built and run from Android Studio. To build from the CLI you'd first need to generate the wrapper (`gradle wrapper`) — ask before adding it to the repo.
- Once a wrapper exists: `./gradlew assembleDebug`, `./gradlew installDebug`.
- No test source sets or lint config exist yet.
- Toolchain: AGP 9.3.1, Kotlin 2.4.10 (built-in Kotlin via AGP; only the compose and serialization plugins are applied), compileSdk/targetSdk 37, minSdk 24, Java 17. Dependencies are in `gradle/libs.versions.toml`.

## Architecture

Single module `:app`, package `mx.tec.avisos`, layered as `domain` → `data` → `ui`:

- **Manual DI**: `AvisosApplication` owns an `AppContainer` that lazily builds repositories. `ui/state/AppViewModelProvider.Factory` builds every ViewModel from that container (`viewModel(factory = AppViewModelProvider.Factory)`). New repositories (e.g. the session repository) are added to `AppContainer` and injected here.
- **domain/**: pure Kotlin, no Android/Retrofit. `Sesion` holds user, `Rol`, access/refresh tokens and absolute `expiraEn` (epoch seconds). `Validadores.kt` has `CredencialesValidator` used by the login UI state.
- **data/remote/**: Retrofit + kotlinx.serialization. `Network.crearApi(interceptor, authenticator)` takes an optional OkHttp `Interceptor` (to add `Authorization: Bearer …`) and `Authenticator` (to react to 401 by refreshing) — both nullable so the project compiles before they exist. The logging interceptor is added last so it sees the signed request. `AvisosApi` separates unauthenticated `auth/*` endpoints from token-protected ones. `Mappers.kt` converts `TokensDto.expiresIn` (relative) into `Sesion.expiraEn` (absolute) at receipt time.
- **data/local/Cifrador**: AES/GCM with a key held in the Android Keystore (replacement for the retired `EncryptedSharedPreferences`). `descifrar` returns `null` on any failure, meaning "no session" rather than crashing. Intended for encrypting tokens before persisting them (no DataStore dependency is declared yet).
- **data/AvisosRepository**: knows nothing about tokens; relies on the interceptor, and lets 401/403 `HttpException`s propagate.
- **ui/**: ViewModels expose Compose `mutableStateOf` state (`private set`); network screens use the sealed `UiState` (Cargando/Exito/Error), catching `IOException` and `HttpException` (translated via `mensajeDe` in `ApiErrors.kt`).
- **Navigation**: `AvisosApp` is the root; today it shows only `LoginScreen` (`LoginViewModel.enviar()` is a stub). The intended end state is a `when` over the current session: no session → login, session → `AvisosNavHost(sesion, onSalir)`, which contains the authenticated routes (`Route.AVISOS`, `Route.PUBLICAR`) and receives a non-null `Sesion`.

## Repo rules (from README)

- Commit at each guide checkpoint (e.g. `checkpoint a2`); experiments that intentionally break code go on a separate branch.
- Never commit passwords, tokens, or the professor code ("código de profesor"); the password is typed, tokens are stored encrypted on device.
- Every commit containing AI-generated code must declare it with a `Co-Authored-By` trailer.

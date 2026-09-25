# Contributing to Halo

*[Français](CONTRIBUTING.md)*

Thank you for your interest! Reports, ideas and fixes are welcome.
By taking part, you agree to the [code of conduct](CODE_OF_CONDUCT.md).

## Reporting a problem, suggesting an idea

- **A bug**: open a ["Bug" issue](https://github.com/exec-d/halo/issues/new?template=bug_report.yml)
  with the phone model, the Android version, the Halo version (*About*) and,
  if possible, a screenshot.
- **An idea for a widget or an improvement**: an ["Idea" issue](https://github.com/exec-d/halo/issues/new?template=feature_request.yml).
  A mock-up, even hand-drawn, helps a lot.
- **A security vulnerability**: never in a public issue, see
  [SECURITY.md](SECURITY.md).

## Setting up

- Flutter, stable channel; Java 17; the Android SDK.
- An Android 12+ phone or an emulator: widgets can only really be seen on a
  home screen.

```bash
git clone https://github.com/exec-d/halo.git
cd halo
flutter pub get
flutter run
```

[docs/en/architecture.md](docs/en/architecture.md) explains how the project is
organised; start there.

## Before submitting a change

The CI runs exactly this; a pull request must pass:

```bash
dart format .
flutter analyze
flutter test
flutter build apk --debug
```

- Dart screens are tested with `test/fake_platform.dart`; a new `WuxPlatform`
  method is added there too.
- The Kotlin code has no automated tests: describe in the pull request what
  you checked on a device, and attach a screenshot for any visual change.

## Conventions

- **Language**: the interface, documentation, comments and commit messages
  are in French. English translations live in the `*.en.md` files,
  `docs/en/`, `android/app/src/main/res/values-en/` and
  `lib/l10n/app_en.arb`; a change to a French text should also update its
  English counterpart.
- **Style**: `dart format` and `analysis_options.yaml` on the Dart side; on
  the Kotlin side, the official style (4 spaces, trailing commas).
- **Comments**: they say *why*, not *what* — an Android constraint, a choice,
  a limitation.
- **App interface**: [IUX](https://github.com/systm-d/IUX) components only;
  an unavailable action always gives its reason (`unavailabilityReason`).
- **Widgets**: no background or rounded corners, margins from
  `res/values/dimens.xml`, text in `Halo*` styles (`res/values/styles.xml`),
  colours `clock_core`, `clock_line`, `clock_glow` — never a hard-coded colour.
- **Case**: only labels are in capitals; values keep their case.
- **Commits**: a short subject that says what changes ("Agenda : filtre
  « toute la journée »"), a body that says why.

## Adding a widget

1. **Kotlin**: a class in `android/…/widgets/` that extends `NeonWidget`
   (or `AppWidgetProvider` + `PreviewableWidget`), with sample data for the
   preview (`sample`).
2. **Resources**: its layout `res/layout/widget_<id>.xml`, a launcher preview
   `res/layout/preview_<id>.xml`, its metadata `res/xml/widget_<id>_info.xml`
   (categories `home_screen|keyguard`), its strings in
   `res/values/strings.xml`.
3. **Manifest**: its `<receiver>` in `AndroidManifest.xml`.
4. **In-app previews**: its identifier in `WidgetPreviews.widget()`.
5. **Dart**: its description in `lib/src/home_widgets/catalog.dart` (same
   identifier), and a settings screen if needed (`lib/src/screens/`).
6. **Documentation**: a line in [docs/widgets.md](docs/widgets.md) (and its
   English counterpart [docs/en/widgets.md](docs/en/widgets.md)) and in
   [CHANGELOG.md](CHANGELOG.md).

## Graphic assets

The PNGs in `res/drawable-xxhdpi/` are generated, not drawn by hand:

```bash
pip install pillow
python3 tool/clock_assets.py <flutter>/bin/cache/artifacts/material_fonts/MaterialIcons-Regular.otf
python3 tool/sample_assets.py
```

## Publishing a version

1. Bump `version:` in `pubspec.yaml` and complete [CHANGELOG.md](CHANGELOG.md).
2. Merge into `main`.
3. Create a GitHub *release* with the tag `vX.Y.Z`: the CI attaches the APK
   to it (`.github/workflows/release.yml`).

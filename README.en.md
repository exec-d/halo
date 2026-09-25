# Halo

*[Français](README.md)*

[![CI](https://github.com/exec-d/halo/actions/workflows/ci.yml/badge.svg)](https://github.com/exec-d/halo/actions/workflows/ci.yml)
[![MIT License](https://img.shields.io/badge/licence-MIT-blue.svg)](LICENSE)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84)

**Neon widgets and an animated wallpaper to personalise an Android phone.**
The colours follow the phone's own (Material You); nothing leaves the device,
apart from the coordinates sent to Open-Meteo for the weather.

*Code name: WUX. The Android package (`dev.levilainpetit.wux`) and the code
keep that name.*

## What Halo includes

**23 widgets** for the home screen and the lock screen:

| Family | Widgets |
| --- | --- |
| Time | Clock (with the next alarm), Analog clock, World clock, Countdown, Stopwatch and timer, Month, Ephemeris |
| Agenda | Agenda, Agenda 2 columns — the current event highlighted, "all day" events can be hidden |
| Weather | Weather (dashboard and 24-hour bars), 5-day forecast, Rain, Allergies (pollen), Sun and Moon |
| System | System, Advanced system, Detailed battery, Device (console style), Earbuds and watch, Screen time, Mobile data |
| Music | Now playing (neon cover art, controls) |
| Shortcuts | Controls (flashlight, Wi-Fi, Bluetooth, sound, camera) |

**Two animated wallpapers**:

- **Circuit**: the inside of a Pixel 7 as a neon schematic, which reacts to
  tilt, follows the real battery, lights up as data flows and powers on
  component by component with the screen;
- **Tron**: the light cycle from Tron: Legacy as a technical drawing, which
  traces itself line by line when the screen turns on; its wheels spin, its
  light strip pulses.

Both take the phone's colors, like the widgets.

**Elsewhere in Android**: a screen saver (neon clock while charging), three
quick settings tiles (Weather, Halo wallpaper, Battery) and icon shortcuts
(Wallpaper, Widgets, Settings).

**The app**: gallery in three tabs (gallery, wallpapers, my setup), settings for each widget (also through
a long press on the widget), permissions, About and licences, in French and
English.

Details of each widget: [docs/en/widgets.md](docs/en/widgets.md); the
wallpapers, screen saver and tiles: [docs/en/wallpaper.md](docs/en/wallpaper.md).

## Install

Halo is not on the Play Store. Each version published on the
[Releases](https://github.com/exec-d/halo/releases) page carries an APK; the
latest build of the main branch is also an artifact of
[the CI](https://github.com/exec-d/halo/actions/workflows/ci.yml).

1. Download the APK on the phone and open it (allow installation from the
   browser or file manager if Android asks).
2. Updates install over the previous version: all builds are signed with the
   same key.
3. For **Screen time** and **Mobile data**: Android reserves access to usage
   data for Play Store apps. In *Settings → Apps → Halo*, tap **⋮ → Allow
   restricted settings**, then enable access from Halo.

Requirements: Android 8.0 (API 26); Android 12 or later for the phone's
colours, the preview in the widget list and long-press settings.

## Permissions

All are optional; each one is used only by the widgets listed.

| Permission | For | Requested |
| --- | --- | --- |
| Calendar (read) | Agenda, Agenda 2 columns, Month | On first launch |
| Approximate location | Weather, Forecast, Rain, Allergies, Sun and Moon — only if you choose "My location" | In the Weather screen |
| Nearby devices | Earbuds and watch | In the widget's screen |
| Usage data | Screen time, Mobile data | In Android settings |
| Notification access | Now playing (to see what is playing; no notification is read) | In Android settings |
| Notifications | Stopwatch and timer (alarm at the end) | In the widget's screen |
| Internet | Open-Meteo forecasts | — |

What Halo does with this data: [PRIVACY.en.md](PRIVACY.en.md).

## Build

```bash
flutter pub get
dart format .
flutter analyze
flutter test
flutter build apk --debug    # or: flutter run, on a connected device
```

You need Flutter (stable channel), Java 17 and the Android SDK. The UI library
[IUX](https://github.com/systm-d/IUX) is pulled from GitHub, pinned to a
commit in `pubspec.yaml`.

The architecture — why two halves, Flutter and Kotlin, and how they talk to
each other — is described in [docs/en/architecture.md](docs/en/architecture.md).

## Contributing

Reports and suggestions are welcome: see
[CONTRIBUTING.en.md](CONTRIBUTING.en.md), and the
[code of conduct](CODE_OF_CONDUCT.md). Security vulnerabilities are reported
privately: [SECURITY.md](SECURITY.md). Version history:
[CHANGELOG.md](CHANGELOG.md).

## License and credits

Halo is distributed under the [MIT license](LICENSE).

- Weather, rain, pollen and air quality: [Open-Meteo](https://open-meteo.com)
  (CC BY 4.0), pollen and air data from the Copernicus Atmosphere Monitoring
  Service; GeoNames geocoding (CC BY 4.0).
- Icons: Google Material Icons (Apache 2.0).
- UI: [Flutter](https://flutter.dev) and [IUX](https://github.com/systm-d/IUX).

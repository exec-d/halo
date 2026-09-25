# Privacy

*[Français](PRIVACY.md)*

Halo creates no account, contains no ads or analytics, and sends nothing to
its authors.

## What stays on the phone

Everything Halo reads is shown on the phone and never leaves it:

| Data | Read for | Permission |
| --- | --- | --- |
| Calendar (events, calendars) | Agenda, Month | Calendar, read-only |
| Next alarm | Clock | None |
| Phone status (battery, network, storage, memory, sound, location on/off) | System widgets, Circuit wallpaper | None |
| Connected Bluetooth devices (name, type, battery) | Earbuds and watch | Nearby devices |
| App usage time, unlocks, mobile data usage | Screen time, Mobile data | Usage data access |
| List of installed apps (name, icon) | Screen time | Not requested (declared) |
| Phone tilt and orientation | Circuit wallpaper | None |
| Current track (title, artist, cover art, app) | Now playing | Notification access (no notification is read) |

Halo's settings and battery history (26 hours) are kept in the app's
preferences; they are removed along with the app.

## What leaves the phone

Only weather requests go out, to [Open-Meteo](https://open-meteo.com):

- the **coordinates of the chosen place** (a city, or the approximate location
  if you choose "My location"), for forecasts, rain, pollen and air quality;
- the **text of a city search**, for geocoding.

Open-Meteo requires no account or key; its privacy policy:
<https://open-meteo.com/en/terms#privacy>.

## Deleting your data

Uninstalling Halo erases its settings and history. Permissions can be revoked
at any time in *Settings → Apps → Halo*.

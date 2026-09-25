# The widgets

*[Français](../widgets.md)*

They all share the same style: time in Roboto extra bold, mono text, a dark
shadow under text, a glow on lines and icons, the phone's colours. All of them
can be placed on the home screen and on the lock screen (recent Android); a
long press opens their settings in Halo (Android 12+).

| Widget | Size | Content | Tap |
| --- | --- | --- | --- |
| Clock | 4x2 | Time, date, next alarm; compact on a single row | Opens the clock |
| Agenda | 2x3 | Today's events, or today's and tomorrow's | Opens the event or the day |
| Agenda 2 columns | 4x3 | Same, in two columns | Same |
| System | 4x1 | Battery, network, storage | Opens the matching setting |
| Advanced system | 4x2 | Signal, Wi-Fi, Bluetooth, battery, memory, storage, location, sound | Same |
| Detailed battery | 4x2 | 24-hour curve and projected trend, estimate, temperature, voltage, cycles, health | Opens battery usage |
| Device | 4x2 | Console: time since boot, Android and security patch, model, chip, memory | Opens "About phone" |
| Earbuds and watch | 4x1 | Connected Bluetooth devices and their battery | Opens Bluetooth settings |
| Screen time | 4x2 | 24-hour dial, total, unlocks, three apps | Opens Digital Wellbeing |
| Mobile data | 4x2 | Usage for the period against the plan | Opens data usage |
| World clock | 4x1 | The time in three cities of your choice | Opens the clock |
| Countdown | 3x1 | The days until a date | Opens Halo |
| Controls | 4x1 | Flashlight, Wi-Fi, Bluetooth, sound, camera | Toggles the flashlight, opens the rest |
| Month | 4x3 | The current month, days with events marked | Opens the day in the calendar |
| Weather | 4x3 | Temperature and the day's gauge, readings, 24-hour bars | Opens Halo |
| Sun and Moon | 4x1 | Sunrise, sunset, day length, moon phase | Opens Halo |
| Rain | 4x2 | Current or upcoming rain, probabilities over 12 h | Opens Halo |
| Allergies | 4x2 | Pollen by family over three days, air quality | Opens Halo |
| 5-day forecast | 4x2 | Icon, high, low, and a capsule that places each day on the week's scale | Opens Halo |
| Analog clock | 2x2 | Neon dial, glowing hands, date | Opens alarms |
| Ephemeris | 4x2 | Name day, week and day of the year, the year in twelve months, next public holiday | Opens the calendar |
| Stopwatch and timer | 4x1 | Stopwatch, 1, 5, 10, 25 min timers that ring at the end | Buttons on the widget |
| Now playing | 4x1 | Neon cover art, title, artist, progress, controls | Opens the app that is playing |

Sizes are the sizes when placed; all of them can be resized, and adapt their
content to the space.

## Agenda

- Calendars shown as you choose; "today" or "today and tomorrow".
- "All day" events shown or hidden (per-widget setting); an event spanning
  several days counts as "all day" on the days it fully covers.
- The current event is highlighted (title in bold, lighter details), the
  following ones on the same day slightly faded.
- From 6 pm, when nothing is left in the day, the agenda moves on to the next
  day (`AgendaBuilder.END_OF_DAY_HOUR`).
- No scrolling: lines that do not fit are indicated by "•••".

## Weather, rain, allergies, sun and moon

- [Open-Meteo](https://open-meteo.com) data: hourly and 15-minute forecasts,
  air quality and pollen (Europe, CAMS model).
- The place is chosen from the approximate location or by a search.
- Pollen: the day's maximum, by family (grasses, trees, weeds), today and the
  next two days.
- The moon phase is calculated on the phone.

## Detailed system

- **Detailed battery**: a reading about every 15 minutes (and at every level
  change while the Circuit wallpaper is shown), kept for 26 hours. The
  estimate comes from Android while charging, otherwise from the pace of the
  last few hours. Cycles require Android 14.
- **Device**: the time since boot is a chronometer that ticks every second.
- **Earbuds and watch**: the battery is only known for devices that report
  it; the others show "—".
- **Screen time**: calculated from the day's usage events; it may differ
  slightly from Digital Wellbeing.
- **Mobile data**: plan (5 to 300 GB, or none) and reset day are adjustable;
  the projection follows the average pace of the period.

These last two require usage data access. Installed outside the Play Store,
Halo must first be allowed "restricted settings":
*Settings → Apps → Halo → ⋮ → Allow restricted settings*.

## Time, music

- **Stopwatch and timer**: time runs in the launcher without waking Halo.
  Tapping a number starts a timer; resetting a timer that is already at zero
  switches back to the stopwatch. At the end, a notification rings
  (notification permission, requested in the widget's screen).
- **Now playing**: Android only tells what is playing to apps that have
  notification access; Halo reads none of them. Installed outside the Play
  Store, restricted settings must first be allowed (see above).
- **Ephemeris**: name days from the French calendar of first names, public
  holidays in France (Easter calculated).

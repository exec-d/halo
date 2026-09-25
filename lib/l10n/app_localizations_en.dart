// ignore: unused_import
import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Halo';

  @override
  String get back => 'Back';

  @override
  String get pinToHome => 'Add to home screen';

  @override
  String widgetPreviewSemantics(String title) {
    return '$title widget preview';
  }

  @override
  String get catalogSettings => 'Settings';

  @override
  String get catalogAbout => 'About';

  @override
  String get catalogWallpapers => 'Live wallpapers';

  @override
  String get catalogWidgets => 'Widgets';

  @override
  String get dreamTitle => 'Screen saver';

  @override
  String get dreamDescription =>
      'While charging: a large neon clock on the Circuit scenery.';

  @override
  String get dreamOpenSettings => 'Open screen saver settings';

  @override
  String get dreamIntro =>
      'When the phone is charging, or sitting on a dock, Android can show a screen saver. Halo\'s shows the time in large type on the Circuit scenery, with the next alarm, the next event and the weather. The text moves a little every minute so as not to mark the screen; a touch wakes the phone.';

  @override
  String get dreamHowToTitle => 'To turn it on';

  @override
  String get dreamStep1 =>
      '1. In the settings that open, turn on “Use screen saver”.';

  @override
  String get dreamStep2 => '2. Choose “Halo” from the list.';

  @override
  String get dreamStep3 =>
      '3. In “When to start”, choose “While charging” (or while docked).';

  @override
  String get settingsPermissionsTitle => 'Permissions';

  @override
  String get settingsPermissionsDescription =>
      'Halo only asks for what its widgets need.';

  @override
  String get settingsCalendarTitle => 'Calendar';

  @override
  String get settingsCalendarUse =>
      'Agenda widgets and the Month widget, read only.';

  @override
  String get settingsLocationTitle => 'Approximate location';

  @override
  String get settingsLocationUse =>
      'Only for “Use my location” in Weather. A city chosen by hand is enough.';

  @override
  String get settingsBatteryTitle => 'Battery';

  @override
  String get settingsBatteryUnrestricted =>
      'Halo is not limited by battery optimization: its widgets update on time.';

  @override
  String get settingsBatteryRestricted =>
      'Android may delay widget updates to save battery. Excluding them from this optimization keeps them on time, at a minimal cost.';

  @override
  String get settingsBatteryButton => 'Manage battery optimization';

  @override
  String get settingsWeatherTitle => 'Weather data';

  @override
  String get settingsWeatherRefresh => 'Refresh now';

  @override
  String get settingsWeatherNoPlaceReason => 'No place chosen in Weather';

  @override
  String get settingsWeatherDownloading => 'Downloading';

  @override
  String get settingsWeatherUpToDate => 'Forecast up to date.';

  @override
  String get settingsWeatherFailed => 'Download failed: check your connection.';

  @override
  String get settingsWeatherNoPlace =>
      'No place chosen: open the Weather widget to choose one.';

  @override
  String settingsWeatherNoForecast(String place) {
    return 'Place: $place. No forecast yet.';
  }

  @override
  String settingsWeatherUpdatedToday(String place, String time) {
    return 'Place: $place. Updated at $time, then about every 30 minutes.';
  }

  @override
  String settingsWeatherUpdatedOn(
    String place,
    int day,
    int month,
    String time,
  ) {
    return 'Place: $place. Updated on $month/$day at $time, then about every 30 minutes.';
  }

  @override
  String get settingsAboutTitle => 'About Halo';

  @override
  String get settingsAboutSubtitle => 'Version, privacy, credits, licenses';

  @override
  String get permissionManage => 'Manage';

  @override
  String get permissionAllow => 'Allow';

  @override
  String permissionActionSemantics(String action, String permission) {
    return '$action: $permission';
  }

  @override
  String get permissionGranted => 'Granted';

  @override
  String get permissionNotGranted => 'Not granted';

  @override
  String get aboutIconSemantics => 'Halo icon';

  @override
  String aboutVersion(String version) {
    return 'Version $version';
  }

  @override
  String aboutVersionBuild(String version, int build) {
    return '$version (build $build)';
  }

  @override
  String get aboutHaloDescription =>
      'Neon-style home screen widgets in the colors of your wallpaper: clock, calendars, weather, rain, allergies, system, and more. Code name: WUX.';

  @override
  String get aboutPrivacyTitle => 'Privacy';

  @override
  String get aboutPrivacyDescription =>
      'Halo creates no account, contains no ads or analytics, and sends nothing to its authors. Your calendar, screen time, data usage and Bluetooth devices are read on the phone and never leave it. For weather, rain, pollen and air quality, only the coordinates of the chosen place are sent to Open-Meteo. Your settings stay on the phone.';

  @override
  String get aboutCreditsTitle => 'Sources and credits';

  @override
  String get aboutCreditWeatherTitle => 'Weather, rain, pollen, air quality';

  @override
  String get aboutCreditWeatherDetail =>
      'Open-Meteo.com, under the CC BY 4.0 license. Pollen and air quality: Copernicus Atmosphere Monitoring Service (CAMS), European model.';

  @override
  String get aboutCreditCitiesTitle => 'City search';

  @override
  String get aboutCreditCitiesDetail =>
      'Open-Meteo geocoding, GeoNames data (CC BY 4.0).';

  @override
  String get aboutCreditIconsTitle => 'Icons';

  @override
  String get aboutCreditIconsDetail =>
      'Material Icons by Google, Apache 2.0 license.';

  @override
  String get aboutCreditInterfaceTitle => 'Interface';

  @override
  String get aboutCreditInterfaceDetail =>
      'Flutter, and IUX for the app\'s accessible components.';

  @override
  String get aboutCreditSourceTitle => 'Source code';

  @override
  String get aboutCreditSourceDetail =>
      'Halo is free software, under the MIT license: github.com/exec-d/halo.';

  @override
  String get aboutLicenses => 'Licenses of the software used';

  @override
  String get accessTitle => 'Access';

  @override
  String get accessGranted => 'Granted';

  @override
  String get accessNotGranted => 'Not granted';

  @override
  String get accessOpenAppInfo => 'Open Halo app info';

  @override
  String get accessRestrictedHelp =>
      'If Android replies “Access to this app has been denied”: Halo, installed outside the Play Store, falls under restricted settings. In its app info, tap ⋮ then “Allow restricted settings”, confirm, and come back to turn on access.';

  @override
  String get accessBluetoothButton => 'Allow “Nearby devices”';

  @override
  String get accessBluetoothDescription =>
      'Halo reads the name, type and battery of connected Bluetooth devices. Nothing leaves the phone.';

  @override
  String get accessMediaButton => 'Open notification access';

  @override
  String get accessMediaDescription =>
      'Android only tells what is playing, and only lets it be controlled, to apps allowed to “access notifications”. Halo reads none of them: it only follows playback. Nothing leaves the phone.';

  @override
  String get accessTimerButton => 'Allow notifications';

  @override
  String get accessTimerDescription =>
      'When a timer ends, Halo rings and shows a notification. Without this permission, the widget only shows “Timer finished”.';

  @override
  String get accessUsageButton => 'Open usage access';

  @override
  String get accessUsageDescription =>
      'Android reserves app usage time and data usage to apps allowed in its settings: turn on Halo in the list. Nothing leaves the phone.';

  @override
  String get mobileDataPlanTitle => 'Data plan';

  @override
  String get mobileDataIncluded => 'Included data';

  @override
  String get mobileDataIncludedHelp =>
      'The widget draws the pace that lands exactly on your plan.';

  @override
  String get mobileDataNoPlan => 'No plan';

  @override
  String mobileDataGigabytes(int gb) {
    return '$gb GB';
  }

  @override
  String get mobileDataCycleDay => 'Reset day';

  @override
  String get mobileDataCycleDayHelp =>
      'The day of the month when the plan starts again from zero.';

  @override
  String get mobileDataFirstOfMonth => '1st of the month';

  @override
  String mobileDataDayOfMonth(int day) {
    return 'Day $day';
  }

  @override
  String get agendaDaysTitle => 'Days';

  @override
  String get agendaDaysShown => 'Days shown';

  @override
  String get agendaDaysHelp =>
      'From 6 p.m., a finished day makes way for the next one.';

  @override
  String get agendaToday => 'Today';

  @override
  String get agendaTodayTomorrow => 'Today and tomorrow';

  @override
  String get agendaEventsTitle => 'Events';

  @override
  String get agendaEventsDescription =>
      'The current event is highlighted, the following ones are slightly faded.';

  @override
  String get agendaEventsShown => 'Events shown';

  @override
  String get agendaAllDay => 'All day';

  @override
  String get agendaAllDaySemantics => 'Show all-day events';

  @override
  String get agendaAllDayHelp =>
      'Birthdays, time off, public holidays… and events spanning several days.';

  @override
  String get agendaCalendarsTitle => 'Calendars';

  @override
  String get agendaCalendarsShown => 'Calendars shown';

  @override
  String get agendaPreviewPlaceholder =>
      'The preview will appear once calendar access is allowed.';

  @override
  String get agendaAllowAccess => 'Allow calendar access';

  @override
  String get agendaOpenSettings => 'Open settings';

  @override
  String get agendaAccessTitle => 'Calendar access';

  @override
  String get agendaAccessRefused =>
      'Access was denied. If it is no longer offered, turn on “Calendar” in the app\'s permissions.';

  @override
  String get agendaAccessExplanation =>
      'Halo reads your events to display them. It never changes them and never sends them anywhere.';

  @override
  String get countdownDay => 'Day';

  @override
  String get countdownMonth => 'Month';

  @override
  String get countdownYear => 'Year';

  @override
  String get countdownEventTitle => 'Event';

  @override
  String get countdownTitleLabel => 'Title';

  @override
  String get countdownTitleHelp => 'For example “Holidays”.';

  @override
  String get countdownDateLabel => 'Date';

  @override
  String get countdownDateHelp => 'Day, month and year.';

  @override
  String get countdownDateInvalid => 'This date does not exist.';

  @override
  String get weatherUseLocation => 'Use my location';

  @override
  String get weatherSearch => 'Search';

  @override
  String get weatherSearching => 'Searching';

  @override
  String weatherNoResult(String query) {
    return 'No place found for “$query”.';
  }

  @override
  String get weatherPlaceTitle => 'Place';

  @override
  String get weatherNoPlace => 'No place chosen yet.';

  @override
  String weatherForecastFor(String place) {
    return 'Forecast for $place.';
  }

  @override
  String get weatherLocationFailed =>
      'Your location could not be obtained. Check that location is turned on and allowed for Halo.';

  @override
  String get weatherCityLabel => 'City';

  @override
  String get weatherCityHelp => 'Or search for a city by name.';

  @override
  String get weatherPlaceUnavailable =>
      'The forecast for this place is not available.';

  @override
  String get worldClockCities => 'Cities';

  @override
  String worldClockCity(int number) {
    return 'City $number';
  }

  @override
  String get wallpaperApply => 'Apply wallpaper';

  @override
  String get wallpaperActive => 'Current wallpaper';

  @override
  String get wallpaperNotApplied => 'Not applied yet';

  @override
  String wallpaperUnavailable(String title) {
    return 'This phone does not offer the apply screen. Choose “Halo · $title” in Wallpaper & style, under Live wallpapers.';
  }

  @override
  String get wallpaperIntensityTitle => 'Intensity';

  @override
  String get wallpaperIntensityLabel => 'Wallpaper intensity';

  @override
  String get wallpaperIntensityHelp =>
      'Subtle keeps widgets and icons easy to read on top.';

  @override
  String get wallpaperIntensityDiscreet => 'Subtle';

  @override
  String get wallpaperIntensityNormal => 'Normal';

  @override
  String get wallpaperIntensityVivid => 'Vivid';

  @override
  String get wallpaperFeaturesTitle => 'What moves';

  @override
  String get wallpaperBatteryTitle => 'Battery';

  @override
  String wallpaperPreviewSemantics(String title) {
    return '$title wallpaper preview';
  }

  @override
  String get wallpaperCircuitTitle => 'Circuit';

  @override
  String get wallpaperCircuitDescription =>
      'The inside of the phone in neon, in the phone\'s colors.';

  @override
  String get wallpaperCircuitTiltTitle => 'Tilt';

  @override
  String get wallpaperCircuitTiltText =>
      'The layers of the phone slide when you tilt it, and the glass reflection follows.';

  @override
  String get wallpaperCircuitBatteryTitle => 'Real battery';

  @override
  String get wallpaperCircuitBatteryText =>
      'The drawn battery shows the real level and breathes while charging.';

  @override
  String get wallpaperCircuitNetworkTitle => 'Network';

  @override
  String get wallpaperCircuitNetworkText =>
      'Pulses run from the antenna to the processor when data flows; the antennas glow with the signal strength.';

  @override
  String get wallpaperCircuitWakeTitle => 'Wake-up';

  @override
  String get wallpaperCircuitWakeText =>
      'Each time the screen turns on, the background is there right away, then components and traces light up one by one from the processor.';

  @override
  String get wallpaperCircuitBattery =>
      'The animation stops as soon as the wallpaper is no longer visible, and only runs continuously during a movement, a pulse or charging.';

  @override
  String get wallpaperHorizonTitle => 'Horizon';

  @override
  String get wallpaperHorizonDescription =>
      'A neon horizon that follows the time and the real weather of the chosen place.';

  @override
  String get wallpaperHorizonDayTitle => 'The day';

  @override
  String get wallpaperHorizonDayText =>
      'The sun rises, crosses the sky and sets at the real times; at night, the moon in its phase and the stars.';

  @override
  String get wallpaperHorizonWeatherTitle => 'The weather';

  @override
  String get wallpaperHorizonWeatherText =>
      'Clouds, rain, snow, storm or mist, depending on the weather at the place chosen in Weather.';

  @override
  String get wallpaperHorizonTiltTitle => 'Tilt';

  @override
  String get wallpaperHorizonTiltText =>
      'Mountains and the grid floor slide when you tilt the phone.';

  @override
  String get wallpaperHorizonBattery =>
      'The animation stops as soon as the wallpaper is no longer visible; it slows down in clear weather, and only runs fast in rain or snow.';

  @override
  String get wallpaperSkyTitle => 'Sky';

  @override
  String get wallpaperSkyDescription =>
      'The real stars and the real Moon above you, in the direction of the phone.';

  @override
  String get wallpaperSkyRealTitle => 'The real sky';

  @override
  String get wallpaperSkyRealText =>
      'The brightest stars and the constellations, in their place for the location chosen in Weather and for this time.';

  @override
  String get wallpaperSkyCompassTitle => 'Compass';

  @override
  String get wallpaperSkyCompassText =>
      'Turn around: the sky follows the direction of the phone. Raise it: you rise toward the zenith.';

  @override
  String get wallpaperSkyDayTitle => 'Daytime';

  @override
  String get wallpaperSkyDayText =>
      'The sky brightens and the stars fade when the sun is up.';

  @override
  String get wallpaperSkyBattery =>
      'The animation and the compass stop as soon as the wallpaper is no longer visible.';

  @override
  String get widgetClockTitle => 'Clock';

  @override
  String get widgetClockDescription =>
      'The time and date. Tap it to open the clock.';

  @override
  String get widgetOneColumnAgendaTitle => 'Agenda';

  @override
  String get widgetOneColumnAgendaDescription =>
      'The day\'s events, in one column.';

  @override
  String get widgetTwoColumnAgendaTitle => 'Agenda 2 columns';

  @override
  String get widgetTwoColumnAgendaDescription =>
      'The day\'s events, in two columns.';

  @override
  String get widgetSystemTitle => 'System';

  @override
  String get widgetSystemDescription =>
      'Battery, network and storage, in one row.';

  @override
  String get widgetAdvancedSystemTitle => 'Advanced system';

  @override
  String get widgetAdvancedSystemDescription =>
      'Signal, Wi-Fi, Bluetooth, battery, memory, storage, location and sound, in two rows.';

  @override
  String get widgetBatteryTitle => 'Detailed battery';

  @override
  String get widgetBatteryDescription =>
      'The curve of the last 24 hours and the forecast ahead, temperature, voltage and cycles.';

  @override
  String get widgetDeviceTitle => 'Device';

  @override
  String get widgetDeviceDescription =>
      'The phone\'s spec sheet, console style, and how long it has been running, to the second.';

  @override
  String get widgetBluetoothDevicesTitle => 'Earbuds and watch';

  @override
  String get widgetBluetoothDevicesDescription =>
      'The battery of connected Bluetooth devices.';

  @override
  String get widgetScreenTimeTitle => 'Screen time';

  @override
  String get widgetScreenTimeDescription =>
      'The day on a 24-hour dial: when the screen was on, unlocks and the most used apps.';

  @override
  String get widgetMobileDataTitle => 'Mobile data';

  @override
  String get widgetMobileDataDescription =>
      'The period\'s usage against your plan, with the projection at the end of the period.';

  @override
  String get widgetForecastTitle => '5-day forecast';

  @override
  String get widgetForecastDescription =>
      'Five days of weather, and a capsule placing each one within the week, from coolest to warmest.';

  @override
  String get widgetAnalogClockTitle => 'Analog clock';

  @override
  String get widgetAnalogClockDescription =>
      'A neon dial with glowing hands, and the date.';

  @override
  String get widgetEphemerisTitle => 'Ephemeris';

  @override
  String get widgetEphemerisDescription =>
      'The day\'s name day, the week, the year in twelve months and the next public holiday.';

  @override
  String get widgetTimerTitle => 'Stopwatch and timer';

  @override
  String get widgetTimerDescription =>
      'A stopwatch, and a 1, 5, 10 or 25-minute timer in one tap, which rings at the end.';

  @override
  String get widgetMediaTitle => 'Now playing';

  @override
  String get widgetMediaDescription =>
      'What is playing, its cover art in neon, and previous, play or pause, next.';

  @override
  String get widgetWorldClockTitle => 'World clock';

  @override
  String get widgetWorldClockDescription =>
      'The time in three cities, in one row.';

  @override
  String get widgetCountdownTitle => 'Countdown';

  @override
  String get widgetCountdownDescription =>
      'The days until a date that matters.';

  @override
  String get widgetControlsTitle => 'Controls';

  @override
  String get widgetControlsDescription =>
      'Flashlight in one gesture; Wi-Fi, Bluetooth, sound and camera at your fingertips.';

  @override
  String get widgetMonthTitle => 'Month';

  @override
  String get widgetMonthDescription =>
      'The current month; a dot under each day that has an event. It reads the calendar: allow it from an Agenda widget.';

  @override
  String get widgetWeatherTitle => 'Weather';

  @override
  String get widgetWeatherDescription =>
      'Dashboard style: temperature and the day\'s gauge, feels-like, wind, humidity, UV, rain, and the 24-hour curve. Open-Meteo, every hour. On one row, a single line.';

  @override
  String get widgetSunMoonTitle => 'Sun and Moon';

  @override
  String get widgetSunMoonDescription =>
      'Sunrise and sunset at the place chosen in Weather, day length and moon phase.';

  @override
  String get widgetRainTitle => 'Rain';

  @override
  String get widgetRainDescription =>
      'Whether rain is coming and when, then the probabilities for the next 12 hours, at the place chosen in Weather.';

  @override
  String get widgetAllergyTitle => 'Allergies';

  @override
  String get widgetAllergyDescription =>
      'Grasses, birch, alder, olive, mugwort, ragweed and air quality, at the place chosen in Weather. Pollen: Europe only.';
}

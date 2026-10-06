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
  String get catalogFilterLabel => 'Widget categories';

  @override
  String get catalogFilterAll => 'All';

  @override
  String get catalogFilterTime => 'Time and calendar';

  @override
  String get catalogFilterWeather => 'Weather and sky';

  @override
  String get catalogFilterSystem => 'System and devices';

  @override
  String get catalogFilterMedia => 'Media and controls';

  @override
  String get catalogActive => 'Active';

  @override
  String get catalogFeatured => 'Featured';

  @override
  String get catalogSearch => 'Search widgets';

  @override
  String get catalogSearchClose => 'Close search';

  @override
  String get catalogChange => 'Change';

  @override
  String get catalogSeeAll => 'See all';

  @override
  String catalogWidgetCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count widgets',
      one: '1 widget',
    );
    return '$_temp0';
  }

  @override
  String get catalogNew => 'New';

  @override
  String get catalogNavGallery => 'Gallery';

  @override
  String get catalogNavWallpapers => 'Wallpapers';

  @override
  String get catalogNavMine => 'My setup';

  @override
  String get catalogWallpapersIntro =>
      'They react to the tilt of the phone. Tap one to see it full size and apply it.';

  @override
  String get catalogMineWallpaper => 'Wallpaper';

  @override
  String get catalogMineNoWallpaper => 'No Halo wallpaper is applied yet.';

  @override
  String get catalogMineWidgets => 'Placed widgets';

  @override
  String get catalogMineNoWidgets =>
      'No Halo widget is placed yet. Tap a widget in the gallery to add it.';

  @override
  String get catalogHeroApplied => 'Applied on this phone';

  @override
  String get catalogNoMatch => 'No widget matches.';

  @override
  String detailOnWallpaper(String name) {
    return 'On $name';
  }

  @override
  String get detailLockScreen => 'Lock screen';

  @override
  String get detailTagHome => 'Home screen';

  @override
  String get detailTagLock => 'Lock screen';

  @override
  String get detailBackgrounds => 'Preview background';

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
      'Halo creates no account, contains no ads or analytics, and sends nothing to its authors. Your calendar, screen time, data usage, Bluetooth devices, favourite contacts, steps and sleep (Health Connect) are read on the phone and never leave it. Only a few requests leave the phone: the coordinates of the chosen place, sent to Open-Meteo for weather, rain, pollen, air quality and the sea; the Network widget\'s ping and, when you start it, its speed test, to Cloudflare\'s servers. The Moon and its phases are computed on the phone. Your settings stay on the phone.';

  @override
  String get aboutCreditsTitle => 'Sources and credits';

  @override
  String get aboutCreditWeatherTitle =>
      'Weather, rain, pollen, air quality, sea';

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
  String get accessNetworkButton => 'Allow precise location';

  @override
  String get accessNetworkDescription =>
      'Android only gives the Wi-Fi name to apps with precise location. Halo uses it for that name only: it neither reads nor keeps your location.';

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
  String get wallpaperFluxTitle => 'Back to the Future';

  @override
  String get wallpaperFluxDescription =>
      'The DeLorean\'s flux capacitor, fitted inside the phone in place of the battery.';

  @override
  String get wallpaperFluxMotion => 'The flux capacitor';

  @override
  String get wallpaperFluxMotionText =>
      'Pulses race along the three arms to the core, which flashes; faster while charging. Above the window, the gauge shows the battery level; on the motherboard, the time circuits show the time.';

  @override
  String get wallpaperFluxBattery =>
      'The animation stops as soon as the wallpaper is no longer visible; on screen, the flux capacitor runs continuously at a reduced frame rate.';

  @override
  String get wallpaperArcTitle => 'Iron Man';

  @override
  String get wallpaperArcDescription =>
      'Tony Stark\'s arc reactor, fitted inside the phone in place of the battery.';

  @override
  String get wallpaperArcMotion => 'The reactor';

  @override
  String get wallpaperArcMotionText =>
      'At start-up, the coils light up one by one, then the core flares. One coil per tenth of battery, two spinning energy tracks, a beating core; while charging, particles spiral into it, and below 15 % it flickers.';

  @override
  String get wallpaperArcBattery =>
      'The animation stops as soon as the wallpaper is no longer visible; on screen, the reactor runs continuously at a reduced frame rate.';

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
  String get widgetProgressTitle => 'Progress';

  @override
  String get widgetProgressDescription =>
      'How much of the day, week, month and year has gone by, in four bars.';

  @override
  String get widgetNetworkTitle => 'Network';

  @override
  String get widgetNetworkDescription =>
      'The current Wi-Fi or mobile network, signal strength, ping and local address. The speed test (about ten seconds, a few dozen MB) only runs when you tap “Test”.';

  @override
  String get widgetHealthTitle => 'Steps and sleep';

  @override
  String get widgetHealthDescription =>
      'Today\'s steps towards your goal, last night and its stages, and the steps of the last seven days, from Health Connect.';

  @override
  String get accessHealthButton => 'Allow Health Connect';

  @override
  String get accessHealthDescription =>
      'Halo reads your steps and sleep in Health Connect, in the background too so the widget stays up to date. It writes nothing and nothing leaves the phone.';

  @override
  String get healthGoalTitle => 'Goal';

  @override
  String get healthGoal => 'Steps per day';

  @override
  String get healthGoalHelp => 'The widget\'s ring fills up towards this goal.';

  @override
  String healthGoalSteps(int steps) {
    final intl.NumberFormat stepsNumberFormat =
        intl.NumberFormat.decimalPattern(localeName);
    final String stepsString = stepsNumberFormat.format(steps);

    return '$stepsString steps';
  }

  @override
  String get widgetContactsTitle => 'Favourite contacts';

  @override
  String get widgetContactsDescription =>
      'Up to six contacts as neon badges; one tap to call or text.';

  @override
  String get contactsTitle => 'Contacts';

  @override
  String get contactsDescription =>
      'Pick them with Android\'s contact picker: Halo only sees the contact you tap, not your address book.';

  @override
  String get contactsAdd => 'Add a contact';

  @override
  String get contactsFull => 'Six contacts: remove one to add another.';

  @override
  String get contactsActionTitle => 'On tap';

  @override
  String get contactsSms => 'Send a text';

  @override
  String get contactsSmsHelp => 'Otherwise, a tap calls the contact.';

  @override
  String get contactsCallHelp =>
      'Without permission, a tap opens the dialer with the number: you still press “Call”.';

  @override
  String get contactsCallButton => 'Call directly';

  @override
  String contactsRemove(String name) {
    return 'Remove $name';
  }

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
  String get widgetSeaTitle => 'Sea and waves';

  @override
  String get widgetSeaDescription =>
      'As close as possible to the Weather place: wave height, period and direction, water temperature, and the waves over the next 24 hours.';

  @override
  String get widgetSkyTitle => 'Moon';

  @override
  String get widgetSkyDescription =>
      'The Moon in its current phase, drawn with its seas: the countdown to the full moon, its rise and set at the Weather place, and the next four phases. Computed on the phone.';

  @override
  String get widgetAllergyTitle => 'Allergies';

  @override
  String get widgetAllergyDescription =>
      'Grasses, birch, alder, olive, mugwort, ragweed and air quality, at the place chosen in Weather. Pollen: Europe only.';

  @override
  String get catalogThemes => 'Themes';

  @override
  String get catalogThemesHint =>
      'A wallpaper, its colours, matching widgets and sounds, in one tap.';

  @override
  String get themesTitle => 'Themes';

  @override
  String get themesIntro =>
      'Pixel theme packs are reserved for Google. Halo\'s apply an animated wallpaper, its colours, matching widgets and original sounds together.';

  @override
  String get themesContents => 'Wallpaper · colours · widgets · 3 sounds';

  @override
  String get themeCircuitDescription =>
      'The phone as a neon schematic, electric blue.';

  @override
  String get themeFluxDescription => 'The flux capacitor, orange and amber.';

  @override
  String get themeArcDescription => 'The arc reactor, red and gold.';

  @override
  String get themeApply => 'Apply theme';

  @override
  String get themeActive => 'Active theme';

  @override
  String get themeNotApplied => 'Not applied yet';

  @override
  String themeUnavailable(String title) {
    return 'This phone doesn\'t offer the apply screen. Choose “Halo · $title” in Wallpaper & style, under Live wallpapers.';
  }

  @override
  String themePreviewSemantics(String title) {
    return 'Preview of the $title theme';
  }

  @override
  String get themeColorsTitle => 'Colours';

  @override
  String get themeColorsText =>
      'The wallpaper announces these colours to Android, which applies them when you confirm the wallpaper (otherwise, the next time the screen sleeps). Colours must be set to a wallpaper colour in Wallpaper & style; the system and Halo widgets then take them.';

  @override
  String get themeColorsOpen => 'Open Wallpaper & style';

  @override
  String get themeSoundsTitle => 'Sounds';

  @override
  String get themeSoundsText =>
      'Original, synthesised sounds. Choose which ones the theme replaces; they then stay available in the phone\'s sounds.';

  @override
  String get themeSoundsPermission =>
      'The first time, Android asks for the “Modify system settings” permission: turn it on for Halo, then come back.';

  @override
  String get themeSoundsAllow => 'Allow sounds';

  @override
  String get themeSoundsUnsupported => 'Sounds need Android 10 or later.';

  @override
  String themeSoundsApplied(String list) {
    return 'Sounds set: $list.';
  }

  @override
  String get themeSoundsWaiting => 'Sounds waiting for permission.';

  @override
  String get themeSoundRing => 'Ringtone';

  @override
  String get themeSoundNotification => 'Notification';

  @override
  String get themeSoundAlarm => 'Alarm';

  @override
  String themeSoundPlay(String name) {
    return 'Play $name';
  }

  @override
  String get themeSoundCircuitRing => 'Data bus';

  @override
  String get themeSoundCircuitRingText => 'Bytes running up and down the board';

  @override
  String get themeSoundCircuitNotification => 'Pulse';

  @override
  String get themeSoundCircuitNotificationText =>
      'A pulse racing to the processor';

  @override
  String get themeSoundCircuitAlarm => 'Boot';

  @override
  String get themeSoundCircuitAlarmText => 'Components light up one by one';

  @override
  String get themeSoundFluxRing => '88 mph';

  @override
  String get themeSoundFluxRingText =>
      'The engine revs, the flux crackles, then the jump';

  @override
  String get themeSoundFluxNotification => 'Flux';

  @override
  String get themeSoundFluxNotificationText =>
      'Three sparks meeting at the core';

  @override
  String get themeSoundFluxAlarm => 'Departure time';

  @override
  String get themeSoundFluxAlarmText => 'The time circuits\' beeps';

  @override
  String get themeSoundArcRing => 'Repulsor';

  @override
  String get themeSoundArcRingText => 'The charge whines, then the blast';

  @override
  String get themeSoundArcNotification => 'Interface';

  @override
  String get themeSoundArcNotificationText =>
      'Two crystal tones from the heads-up display';

  @override
  String get themeSoundArcAlarm => 'Reactor';

  @override
  String get themeSoundArcAlarmText => 'The core beats, louder and louder';

  @override
  String get themeLimitsTitle => 'What Halo can\'t change';

  @override
  String get themeLimitsText =>
      'The Pixel launcher refuses icon packs, the lock screen clock is Google\'s and Gboard has no themes for apps. Themed icons and the clock still follow the theme\'s colours.';

  @override
  String get themeColorsPreset =>
      'Colours is set to a basic colour: the system ignores the wallpaper. Choose a wallpaper colour.';

  @override
  String get wallpaperCoreBattery =>
      'The animation stops as soon as the wallpaper is hidden; on screen, it runs at full rate for 30 seconds after the screen turns on or a gesture on the home screen, then slows down until the next gesture.';

  @override
  String get wallpaperQuantumTitle => 'Quantum physics';

  @override
  String get wallpaperQuantumDescription =>
      'The whole phone becomes the bottom of a quantum computer\'s chandelier: the forest of coaxial lines and their loops, the gold plate, the copper columns covered in connectors and the chip in the middle.';

  @override
  String get wallpaperQuantumMotion => 'The chandelier';

  @override
  String get wallpaperQuantumMotionText =>
      'The columns\' rows of connectors light up from the bottom, one per tenth of battery. Control pulses run down the cables to the chip and the readout flows back up; the chip beats and its qubits twinkle. While charging, a cold wave runs down the cable loops.';

  @override
  String get themeQuantumDescription =>
      'A quantum computer\'s chandelier, violet.';

  @override
  String get themeSoundQuantumRing => 'Superposition';

  @override
  String get themeSoundQuantumRingText =>
      'Two beating tones, then a scale up and down';

  @override
  String get themeSoundQuantumNotification => 'Entanglement';

  @override
  String get themeSoundQuantumNotificationText =>
      'Two notes ringing together, twice';

  @override
  String get themeSoundQuantumAlarm => 'Collapse';

  @override
  String get themeSoundQuantumAlarmText =>
      'A hiss narrowing down to a pure note';

  @override
  String get wallpaperNeuralTitle => 'Artificial intelligence';

  @override
  String get wallpaperNeuralDescription =>
      'Circuit\'s phone, detailed down to the vias, with an AI accelerator in place of the battery: its package, four memory stacks and, on the die, a brain etched in traces.';

  @override
  String get wallpaperNeuralMotion => 'The inference';

  @override
  String get wallpaperNeuralMotionText =>
      'In a loop, the memories are read layer by layer, the data races to the brain, a wave spreads from its midline to the pads, then the answer flows back up the ribbons and the processor\'s cores light up. The lit pads follow the battery. While charging, the coils glow, the midline pulses and inferences speed up.';

  @override
  String get themeNeuralDescription =>
      'A silicon brain on its board, electric blue.';

  @override
  String get themeSoundNeuralRing => 'Inference';

  @override
  String get themeSoundNeuralRingText =>
      'A run of quick notes, like a thought taking shape';

  @override
  String get themeSoundNeuralNotification => 'Token';

  @override
  String get themeSoundNeuralNotificationText => 'Two short dry blips';

  @override
  String get themeSoundNeuralAlarm => 'Awakening';

  @override
  String get themeSoundNeuralAlarmText => 'A chord built layer by layer';

  @override
  String get wallpaperAtomTitle => 'Atomic energy';

  @override
  String get wallpaperAtomDescription =>
      'A nuclear reactor core, seen from above, fitted inside the phone in place of the battery.';

  @override
  String get wallpaperAtomMotion => 'The core';

  @override
  String get wallpaperAtomMotionText =>
      'The 37 fuel assemblies light up from the centre outwards with the battery; neutrons pass from one to the next, the chain reaction, livelier while charging; the vessel\'s glow breathes.';

  @override
  String get themeAtomDescription => 'A reactor core, yellow.';

  @override
  String get themeSoundAtomRing => 'Chain reaction';

  @override
  String get themeSoundAtomRingText => 'Clicks speeding up into a rumble';

  @override
  String get themeSoundAtomNotification => 'Neutron';

  @override
  String get themeSoundAtomNotificationText => 'A click, then a clear note';

  @override
  String get themeSoundAtomAlarm => 'Criticality';

  @override
  String get themeSoundAtomAlarmText => 'The control room\'s two-tone siren';

  @override
  String get wallpaperVaultTitle => 'Fallout';

  @override
  String get wallpaperVaultDescription =>
      'A fallout shelter door and its Geiger counter, fitted inside the phone in place of the battery.';

  @override
  String get wallpaperVaultMotion => 'The door';

  @override
  String get wallpaperVaultMotionText =>
      'One tooth of the wheel lights up per tenth of battery; while charging, the locks turn and the door opens. The counter\'s needle trembles and jumps; a scan line runs down the screen.';

  @override
  String get themeVaultDescription => 'A shelter door, phosphor green.';

  @override
  String get themeSoundVaultRing => 'Vault door';

  @override
  String get themeSoundVaultRingText =>
      'The siren, the hiss of the rams, then the thud';

  @override
  String get themeSoundVaultNotification => 'Terminal';

  @override
  String get themeSoundVaultNotificationText => 'Three keystrokes and a beep';

  @override
  String get themeSoundVaultAlarm => 'Geiger counter';

  @override
  String get themeSoundVaultAlarmText => 'A counter crackling in a hot zone';

  @override
  String get wallpaperGhostTitle => 'Ghost in the Shell';

  @override
  String get wallpaperGhostDescription =>
      'A cyberbrain in its shell, plugged into the phone in place of the battery.';

  @override
  String get wallpaperGhostMotion => 'The cyberbrain';

  @override
  String get wallpaperGhostMotionText =>
      'The ghost, a spark, wanders from trace to trace in the brain; one neck port lights up per quarter of battery; code rain falls behind, and while charging it dives: the rain speeds up.';

  @override
  String get themeGhostDescription => 'A cyberbrain and its code rain, teal.';

  @override
  String get themeSoundGhostRing => 'Dive';

  @override
  String get themeSoundGhostRingText => 'Deep drums under distant voices';

  @override
  String get themeSoundGhostNotification => 'Ghost';

  @override
  String get themeSoundGhostNotificationText => 'A breath, then a fading note';

  @override
  String get themeSoundGhostAlarm => 'Synchronisation';

  @override
  String get themeSoundGhostAlarmText => 'Drums and rising data signals';
}

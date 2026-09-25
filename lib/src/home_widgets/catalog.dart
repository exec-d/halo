import 'dart:ui';

import '../../l10n/app_localizations.dart';
import 'wux_home_widget.dart';

const _androidPackage = 'dev.levilainpetit.wux.widgets';

// Nom et description de chaque widget : des fonctions qui lisent la
// traduction (lib/l10n), pour que le catalogue reste constant.

const clockWidget = WuxHomeWidget(
  id: 'clock',
  kind: WuxWidgetKind.simple,
  title: _clockTitle,
  description: _clockDescription,
  androidProvider: '$_androidPackage.ClockWidgetProvider',
  previewSize: Size(340, 120),
);
String _clockTitle(AppLocalizations l) => l.widgetClockTitle;
String _clockDescription(AppLocalizations l) => l.widgetClockDescription;

const oneColumnAgendaWidget = WuxHomeWidget(
  id: 'agenda_one_column',
  kind: WuxWidgetKind.agenda,
  title: _oneColumnAgendaTitle,
  description: _oneColumnAgendaDescription,
  androidProvider: '$_androidPackage.OneColumnAgendaWidget',
  previewSize: Size(200, 280),
);
String _oneColumnAgendaTitle(AppLocalizations l) =>
    l.widgetOneColumnAgendaTitle;
String _oneColumnAgendaDescription(AppLocalizations l) =>
    l.widgetOneColumnAgendaDescription;

const twoColumnAgendaWidget = WuxHomeWidget(
  id: 'agenda_two_columns',
  kind: WuxWidgetKind.agenda,
  title: _twoColumnAgendaTitle,
  description: _twoColumnAgendaDescription,
  androidProvider: '$_androidPackage.TwoColumnAgendaWidget',
  previewSize: Size(340, 280),
);
String _twoColumnAgendaTitle(AppLocalizations l) =>
    l.widgetTwoColumnAgendaTitle;
String _twoColumnAgendaDescription(AppLocalizations l) =>
    l.widgetTwoColumnAgendaDescription;

const systemWidget = WuxHomeWidget(
  id: 'system',
  kind: WuxWidgetKind.simple,
  title: _systemTitle,
  description: _systemDescription,
  androidProvider: '$_androidPackage.SystemWidget',
  previewSize: Size(340, 72),
);
String _systemTitle(AppLocalizations l) => l.widgetSystemTitle;
String _systemDescription(AppLocalizations l) => l.widgetSystemDescription;

const advancedSystemWidget = WuxHomeWidget(
  id: 'system_advanced',
  kind: WuxWidgetKind.simple,
  title: _advancedSystemTitle,
  description: _advancedSystemDescription,
  androidProvider: '$_androidPackage.AdvancedSystemWidget',
  previewSize: Size(340, 150),
);
String _advancedSystemTitle(AppLocalizations l) => l.widgetAdvancedSystemTitle;
String _advancedSystemDescription(AppLocalizations l) =>
    l.widgetAdvancedSystemDescription;

const batteryWidget = WuxHomeWidget(
  id: 'battery',
  kind: WuxWidgetKind.simple,
  title: _batteryTitle,
  description: _batteryDescription,
  androidProvider: '$_androidPackage.BatteryWidget',
  previewSize: Size(340, 150),
);
String _batteryTitle(AppLocalizations l) => l.widgetBatteryTitle;
String _batteryDescription(AppLocalizations l) => l.widgetBatteryDescription;

const deviceWidget = WuxHomeWidget(
  id: 'device',
  kind: WuxWidgetKind.simple,
  title: _deviceTitle,
  description: _deviceDescription,
  androidProvider: '$_androidPackage.DeviceWidget',
  previewSize: Size(340, 150),
);
String _deviceTitle(AppLocalizations l) => l.widgetDeviceTitle;
String _deviceDescription(AppLocalizations l) => l.widgetDeviceDescription;

const bluetoothDevicesWidget = WuxHomeWidget(
  id: 'bt_devices',
  kind: WuxWidgetKind.bluetooth,
  title: _bluetoothDevicesTitle,
  description: _bluetoothDevicesDescription,
  androidProvider: '$_androidPackage.BluetoothDevicesWidget',
  previewSize: Size(340, 72),
);
String _bluetoothDevicesTitle(AppLocalizations l) =>
    l.widgetBluetoothDevicesTitle;
String _bluetoothDevicesDescription(AppLocalizations l) =>
    l.widgetBluetoothDevicesDescription;

const screenTimeWidget = WuxHomeWidget(
  id: 'screen_time',
  kind: WuxWidgetKind.usage,
  title: _screenTimeTitle,
  description: _screenTimeDescription,
  androidProvider: '$_androidPackage.ScreenTimeWidget',
  previewSize: Size(340, 150),
);
String _screenTimeTitle(AppLocalizations l) => l.widgetScreenTimeTitle;
String _screenTimeDescription(AppLocalizations l) =>
    l.widgetScreenTimeDescription;

const mobileDataWidget = WuxHomeWidget(
  id: 'mobile_data',
  kind: WuxWidgetKind.mobileData,
  title: _mobileDataTitle,
  description: _mobileDataDescription,
  androidProvider: '$_androidPackage.MobileDataWidget',
  previewSize: Size(340, 150),
);
String _mobileDataTitle(AppLocalizations l) => l.widgetMobileDataTitle;
String _mobileDataDescription(AppLocalizations l) =>
    l.widgetMobileDataDescription;

const forecastWidget = WuxHomeWidget(
  id: 'forecast',
  kind: WuxWidgetKind.weather,
  title: _forecastTitle,
  description: _forecastDescription,
  androidProvider: '$_androidPackage.ForecastWidget',
  previewSize: Size(340, 150),
);
String _forecastTitle(AppLocalizations l) => l.widgetForecastTitle;
String _forecastDescription(AppLocalizations l) => l.widgetForecastDescription;

const analogClockWidget = WuxHomeWidget(
  id: 'analog',
  kind: WuxWidgetKind.simple,
  title: _analogClockTitle,
  description: _analogClockDescription,
  androidProvider: '$_androidPackage.AnalogClockWidget',
  previewSize: Size(160, 180),
);
String _analogClockTitle(AppLocalizations l) => l.widgetAnalogClockTitle;
String _analogClockDescription(AppLocalizations l) =>
    l.widgetAnalogClockDescription;

const ephemerisWidget = WuxHomeWidget(
  id: 'ephemeris',
  kind: WuxWidgetKind.simple,
  title: _ephemerisTitle,
  description: _ephemerisDescription,
  androidProvider: '$_androidPackage.EphemerisWidget',
  previewSize: Size(340, 130),
);
String _ephemerisTitle(AppLocalizations l) => l.widgetEphemerisTitle;
String _ephemerisDescription(AppLocalizations l) =>
    l.widgetEphemerisDescription;

const timerWidget = WuxHomeWidget(
  id: 'timer',
  kind: WuxWidgetKind.timer,
  title: _timerTitle,
  description: _timerDescription,
  androidProvider: '$_androidPackage.TimerWidget',
  previewSize: Size(340, 72),
);
String _timerTitle(AppLocalizations l) => l.widgetTimerTitle;
String _timerDescription(AppLocalizations l) => l.widgetTimerDescription;

const mediaWidget = WuxHomeWidget(
  id: 'media',
  kind: WuxWidgetKind.media,
  title: _mediaTitle,
  description: _mediaDescription,
  androidProvider: '$_androidPackage.MediaWidget',
  previewSize: Size(340, 80),
);
String _mediaTitle(AppLocalizations l) => l.widgetMediaTitle;
String _mediaDescription(AppLocalizations l) => l.widgetMediaDescription;

const worldClockWidget = WuxHomeWidget(
  id: 'world_clock',
  kind: WuxWidgetKind.worldClock,
  title: _worldClockTitle,
  description: _worldClockDescription,
  androidProvider: '$_androidPackage.WorldClockWidget',
  previewSize: Size(340, 80),
);
String _worldClockTitle(AppLocalizations l) => l.widgetWorldClockTitle;
String _worldClockDescription(AppLocalizations l) =>
    l.widgetWorldClockDescription;

const countdownWidget = WuxHomeWidget(
  id: 'countdown',
  kind: WuxWidgetKind.countdown,
  title: _countdownTitle,
  description: _countdownDescription,
  androidProvider: '$_androidPackage.CountdownWidget',
  previewSize: Size(280, 72),
);
String _countdownTitle(AppLocalizations l) => l.widgetCountdownTitle;
String _countdownDescription(AppLocalizations l) =>
    l.widgetCountdownDescription;

const controlsWidget = WuxHomeWidget(
  id: 'controls',
  kind: WuxWidgetKind.simple,
  title: _controlsTitle,
  description: _controlsDescription,
  androidProvider: '$_androidPackage.ControlsWidget',
  previewSize: Size(340, 72),
);
String _controlsTitle(AppLocalizations l) => l.widgetControlsTitle;
String _controlsDescription(AppLocalizations l) => l.widgetControlsDescription;

const monthWidget = WuxHomeWidget(
  id: 'month',
  kind: WuxWidgetKind.simple,
  title: _monthTitle,
  description: _monthDescription,
  androidProvider: '$_androidPackage.MonthWidget',
  previewSize: Size(340, 250),
);
String _monthTitle(AppLocalizations l) => l.widgetMonthTitle;
String _monthDescription(AppLocalizations l) => l.widgetMonthDescription;

const weatherWidget = WuxHomeWidget(
  id: 'weather',
  kind: WuxWidgetKind.weather,
  title: _weatherTitle,
  description: _weatherDescription,
  androidProvider: '$_androidPackage.WeatherWidget',
  previewSize: Size(340, 230),
);
String _weatherTitle(AppLocalizations l) => l.widgetWeatherTitle;
String _weatherDescription(AppLocalizations l) => l.widgetWeatherDescription;

const sunMoonWidget = WuxHomeWidget(
  id: 'sun_moon',
  kind: WuxWidgetKind.simple,
  title: _sunMoonTitle,
  description: _sunMoonDescription,
  androidProvider: '$_androidPackage.SunMoonWidget',
  previewSize: Size(340, 72),
);
String _sunMoonTitle(AppLocalizations l) => l.widgetSunMoonTitle;
String _sunMoonDescription(AppLocalizations l) => l.widgetSunMoonDescription;

const rainWidget = WuxHomeWidget(
  id: 'rain',
  kind: WuxWidgetKind.simple,
  title: _rainTitle,
  description: _rainDescription,
  androidProvider: '$_androidPackage.RainWidget',
  previewSize: Size(340, 150),
);
String _rainTitle(AppLocalizations l) => l.widgetRainTitle;
String _rainDescription(AppLocalizations l) => l.widgetRainDescription;

const allergyWidget = WuxHomeWidget(
  id: 'allergy',
  kind: WuxWidgetKind.simple,
  title: _allergyTitle,
  description: _allergyDescription,
  androidProvider: '$_androidPackage.AllergyWidget',
  previewSize: Size(340, 190),
);
String _allergyTitle(AppLocalizations l) => l.widgetAllergyTitle;
String _allergyDescription(AppLocalizations l) => l.widgetAllergyDescription;

/// Tous les widgets proposés par l'application.
const wuxHomeWidgets = <WuxHomeWidget>[
  clockWidget,
  analogClockWidget,
  oneColumnAgendaWidget,
  twoColumnAgendaWidget,
  systemWidget,
  advancedSystemWidget,
  batteryWidget,
  deviceWidget,
  bluetoothDevicesWidget,
  screenTimeWidget,
  mobileDataWidget,
  mediaWidget,
  worldClockWidget,
  countdownWidget,
  timerWidget,
  ephemerisWidget,
  controlsWidget,
  monthWidget,
  weatherWidget,
  forecastWidget,
  sunMoonWidget,
  rainWidget,
  allergyWidget,
];

import 'dart:typed_data';
import 'dart:ui';

import 'package:wux/src/home_widgets/wux_home_widget.dart';
import 'package:wux/src/platform/wux_platform.dart';

/// Plateforme simulée : un stockage en mémoire, deux agendas, et un journal
/// des aperçus demandés.
class FakePlatform implements WuxPlatform {
  FakePlatform({this.permission = true, this.grantOnRequest = true});

  bool permission;
  final bool grantOnRequest;
  final data = <String, String>{};
  final pinned = <String>[];
  final renders = <String>[];
  var permissionRequests = 0;
  var finishedConfiguring = 0;

  static const personal = CalendarInfo(
    id: 1,
    name: 'Personnel',
    account: 'moi@example.com',
    color: Color(0xFFE53935),
  );
  static const work = CalendarInfo(
    id: 2,
    name: 'Travail',
    account: 'moi@example.com',
    color: Color(0xFF1E88E5),
  );

  @override
  Future<String?> read(WuxHomeWidget widget, String name) async =>
      data[widget.key(name)];

  @override
  Future<void> write(WuxHomeWidget widget, String name, String? value) async {
    if (value == null) {
      data.remove(widget.key(name));
    } else {
      data[widget.key(name)] = value;
    }
  }

  @override
  Future<bool> canPin() async => true;

  @override
  Future<void> pin(WuxHomeWidget widget) async => pinned.add(widget.id);

  final sampleRenders = <String>[];
  bool firstLaunch = false;

  @override
  Future<Uint8List?> render(
    WuxHomeWidget widget,
    Size size, {
    bool sample = false,
  }) async {
    (sample ? sampleRenders : renders).add(widget.id);
    return null;
  }

  var wallpaperActive = false;
  var dreamSettingsOpened = 0;

  @override
  Future<void> openDreamSettings() async => dreamSettingsOpened++;

  var intensity = 'discreet';

  @override
  Future<String> wallpaperIntensity() async => intensity;

  @override
  Future<void> setWallpaperIntensity(String value) async => intensity = value;

  var wallpaperApplications = 0;

  @override
  Future<Uint8List?> renderWallpaper(Size size) async => null;

  @override
  Future<bool> isWallpaperActive() async => wallpaperActive;

  @override
  Future<bool> applyWallpaper() async {
    wallpaperApplications++;
    wallpaperActive = true;
    return true;
  }

  String? target;
  void Function(String target)? openHandler;

  @override
  Future<String?> launchTarget() async {
    final value = target;
    target = null;
    return value;
  }

  @override
  void onOpen(void Function(String target) handler) => openHandler = handler;

  var mediaAccess = false;
  var notifications = false;

  @override
  Future<bool> hasMediaAccess() async => mediaAccess;

  @override
  Future<void> openMediaAccess() async => mediaAccess = true;

  @override
  Future<bool> hasNotificationPermission() async => notifications;

  @override
  Future<void> requestNotificationPermission() async => notifications = true;

  var usageAccess = false;
  var usageSettingsOpened = 0;
  var bluetooth = false;

  @override
  Future<bool> hasUsageAccess() async => usageAccess;

  @override
  Future<void> openUsageAccess() async => usageSettingsOpened++;

  @override
  Future<bool> hasBluetoothPermission() async => bluetooth;

  @override
  Future<bool> requestBluetoothPermission() async => bluetooth = true;

  @override
  Future<bool> isFirstLaunch() async => firstLaunch;

  @override
  Future<void> markLaunched() async => firstLaunch = false;

  @override
  Future<bool> hasCalendarPermission() async => permission;

  @override
  Future<bool> requestCalendarPermission() async {
    permissionRequests++;
    permission = grantOnRequest;
    return permission;
  }

  @override
  Future<void> openAppSettings() async {}

  @override
  Future<List<CalendarInfo>> calendars() async => [personal, work];

  String? place;

  @override
  Future<String?> weatherPlace() async => place;

  @override
  Future<List<WeatherPlace>> searchPlaces(String query) async => [
    WeatherPlace(name: '$query, Ain, France', latitude: 46, longitude: 5),
  ];

  @override
  Future<String?> setWeatherPlace(WeatherPlace chosen) async =>
      place = chosen.name;

  @override
  Future<String?> locateWeatherPlace() async => place = 'Ma position';

  @override
  Future<AppInfo> appInfo() async => const AppInfo(version: '1.0.0', build: 42);

  var weatherRefreshes = 0;

  @override
  Future<AppStatus> status() async => AppStatus(
    calendar: permission,
    location: false,
    batteryUnrestricted: false,
    weatherPlace: place,
    weatherUpdatedAt: place == null ? null : DateTime(2026, 9, 24, 8, 30),
  );

  @override
  Future<bool> requestLocationPermission() async => true;

  @override
  Future<void> openBatterySettings() async {}

  @override
  Future<bool> refreshWeather() async {
    weatherRefreshes++;
    return true;
  }

  @override
  Future<WuxHomeWidget?> configuringWidget() async => null;

  @override
  Future<void> finishConfiguring() async => finishedConfiguring++;
}

import 'dart:ui';

/// Un agenda du téléphone.
class CalendarInfo {
  const CalendarInfo({
    required this.id,
    required this.name,
    required this.account,
    required this.color,
  });

  factory CalendarInfo.fromMap(Map<Object?, Object?> map) => CalendarInfo(
    id: (map['id']! as num).toInt(),
    name: map['name']! as String,
    account: map['account']! as String,
    color: Color((map['color']! as num).toInt()),
  );

  final int id;
  final String name;
  final String account;
  final Color color;
}

/// Un lieu pour la météo, trouvé par recherche ou par la position.
class WeatherPlace {
  const WeatherPlace({
    required this.name,
    required this.latitude,
    required this.longitude,
  });

  factory WeatherPlace.fromMap(Map<Object?, Object?> map) => WeatherPlace(
    name: map['name']! as String,
    latitude: (map['latitude']! as num).toDouble(),
    longitude: (map['longitude']! as num).toDouble(),
  );

  final String name;
  final double latitude;
  final double longitude;
}

/// Version installée, telle qu'Android la connaît.
class AppInfo {
  const AppInfo({required this.version, required this.build});

  final String version;
  final int build;
}

/// État de ce dont dépendent les widgets.
class AppStatus {
  const AppStatus({
    required this.calendar,
    required this.location,
    required this.batteryUnrestricted,
    this.weatherPlace,
    this.weatherUpdatedAt,
  });

  final bool calendar;
  final bool location;

  /// Faux : Android peut retarder les mises à jour des widgets.
  final bool batteryUnrestricted;
  final String? weatherPlace;
  final DateTime? weatherUpdatedAt;
}

/// Les thèmes côté téléphone : celui en place, et ce que permettent les sons.
class ThemeState {
  const ThemeState({
    this.active,
    this.soundsSupported = false,
    this.canWriteSettings = false,
    this.colorSource,
  });

  factory ThemeState.fromMap(Map<Object?, Object?> map) => ThemeState(
    active: map['active'] as String?,
    soundsSupported: map['soundsSupported'] as bool? ?? false,
    canWriteSettings: map['canWriteSettings'] as bool? ?? false,
    colorSource: map['colorSource'] as String?,
  );

  /// Le thème choisi dont le fond est en place, ou `null`.
  final String? active;

  /// Android 10 ou plus : Halo peut poser ses sons.
  final bool soundsSupported;

  /// « Modifier les paramètres système » accordé.
  final bool canWriteSettings;

  /// D'où le système tire ses couleurs : `home_wallpaper`, `lock_wallpaper`,
  /// `preset` (une couleur de base, qui ignore le fond), ou `null` si
  /// Android ne le dit pas.
  final String? colorSource;

  /// Les couleurs du système suivront le fond.
  bool get colorsFollowWallpaper => colorSource != 'preset';
}

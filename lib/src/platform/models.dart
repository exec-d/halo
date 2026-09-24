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

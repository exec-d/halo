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

/// Une ligne d'événement, déjà mise en forme par le code natif.
class AgendaEventPreview {
  const AgendaEventPreview({
    required this.time,
    required this.title,
    required this.location,
    required this.color,
  });

  factory AgendaEventPreview.fromMap(Map<Object?, Object?> map) =>
      AgendaEventPreview(
        time: map['time']! as String,
        title: map['title']! as String,
        location: map['location']! as String,
        color: Color((map['color']! as num).toInt()),
      );

  final String time;
  final String title;
  final String location;
  final Color color;
}

/// Un jour de l'agenda, tel que le widget l'affichera.
class AgendaDayPreview {
  const AgendaDayPreview({required this.label, required this.events});

  factory AgendaDayPreview.fromMap(Map<Object?, Object?> map) =>
      AgendaDayPreview(
        label: map['label']! as String,
        events: [
          for (final event in map['events']! as List<Object?>)
            AgendaEventPreview.fromMap(event! as Map<Object?, Object?>),
        ],
      );

  final String label;
  final List<AgendaEventPreview> events;
}

/// L'heure et la date, formatées comme le widget horloge.
class ClockPreview {
  const ClockPreview({required this.time, required this.date});

  final String time;
  final String date;
}

/// Couleurs des widgets, résolues par Android (Material You à partir
/// d'Android 12, repli fixe avant).
class WidgetPalette {
  const WidgetPalette({
    required this.surface,
    required this.onSurface,
    required this.onSurfaceVariant,
    required this.primary,
    required this.clockAccent,
  });

  factory WidgetPalette.fromMap(Map<Object?, Object?> map) {
    Color color(String key) => Color((map[key]! as num).toInt());
    return WidgetPalette(
      surface: color('surface'),
      onSurface: color('onSurface'),
      onSurfaceVariant: color('onSurfaceVariant'),
      primary: color('primary'),
      clockAccent: color('clockAccent'),
    );
  }

  static const fallback = WidgetPalette(
    surface: Color(0xF2FFFFFF),
    onSurface: Color(0xFF1B1B1F),
    onSurfaceVariant: Color(0xFF46464F),
    primary: Color(0xFF3F51B5),
    clockAccent: Color(0xFFFFFFFF),
  );

  final Color surface;
  final Color onSurface;
  final Color onSurfaceVariant;
  final Color primary;
  final Color clockAccent;
}

/// Fond d'un widget agenda. Les clés sont relues par `AgendaSettings.kt`.
enum AgendaBackground {
  transparent('transparent'),
  surface('surface');

  const AgendaBackground(this.key);

  final String key;

  static AgendaBackground fromKey(String? key) => values.firstWhere(
    (value) => value.key == key,
    orElse: () => AgendaBackground.transparent,
  );
}

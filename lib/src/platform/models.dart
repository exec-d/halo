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
    required this.detail,
    required this.color,
  });

  factory AgendaEventPreview.fromMap(Map<Object?, Object?> map) =>
      AgendaEventPreview(
        time: map['time']! as String,
        title: map['title']! as String,
        location: map['location']! as String,
        detail: map['detail']! as String,
        color: Color((map['color']! as num).toInt()),
      );

  final String time;
  final String title;
  final String location;

  /// Seconde ligne du widget : l'horaire, puis le lieu s'il y en a un.
  final String detail;
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
  const ClockPreview({
    required this.time,
    required this.date,
    required this.dateStacked,
    this.nextAlarm,
  });

  final String time;
  final String date;

  /// La date sur deux lignes, pour la grande horloge.
  final String dateStacked;

  /// « mer. 07:00 », ou `null` si aucune alarme n'est programmée.
  final String? nextAlarm;
}

/// Couleurs des widgets, résolues par Android : l'accent du fond d'écran
/// (Material You) à partir d'Android 12, un bleu fixe avant.
class WidgetPalette {
  const WidgetPalette({
    required this.core,
    required this.glow,
    required this.line,
  });

  factory WidgetPalette.fromMap(Map<Object?, Object?> map) {
    Color color(String key) => Color((map[key]! as num).toInt());
    return WidgetPalette(
      core: color('core'),
      glow: color('glow'),
      line: color('line'),
    );
  }

  /// Mêmes valeurs que `res/values/colors.xml`.
  static const fallback = WidgetPalette(
    core: Color(0xFFE6F6FF),
    glow: Color(0xFF2F9BFF),
    line: Color(0xFF8AD2FF),
  );

  /// Cœur des chiffres et des titres.
  final Color core;

  /// Halo autour de tout ce qui est dessiné.
  final Color glow;

  /// Traits, barres, horaires.
  final Color line;
}

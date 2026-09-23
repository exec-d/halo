/// Nature d'un widget, qui décide de son écran de réglage et de son aperçu.
enum WuxWidgetKind { clock, agenda }

/// Description, côté Dart, d'un widget d'écran d'accueil déclaré côté Android.
class WuxHomeWidget {
  const WuxHomeWidget({
    required this.id,
    required this.kind,
    required this.title,
    required this.description,
    required this.androidProvider,
    this.days = 0,
    this.withClock = false,
  });

  /// Identifiant stable, utilisé comme préfixe des clés de données partagées.
  /// Le code Kotlin le répète (`AgendaWidget.widgetId`).
  final String id;

  final WuxWidgetKind kind;

  final String title;

  final String description;

  /// Nom complet de la classe Kotlin qui reçoit les mises à jour du widget.
  final String androidProvider;

  /// Nombre de jours affichés, pour un agenda.
  final int days;

  /// Agenda précédé de l'horloge.
  final bool withClock;

  /// Clé sous laquelle une donnée de ce widget est stockée pour le code natif.
  String key(String name) => '$id.$name';
}

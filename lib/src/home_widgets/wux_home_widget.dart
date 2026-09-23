import 'dart:ui';

/// Nature d'un widget, qui décide de son écran de réglage et de son aperçu.
enum WuxWidgetKind {
  /// Pas de réglage : un aperçu et le bouton d'épinglage.
  simple,

  /// Accès au calendrier, jours et agendas affichés.
  agenda,

  /// Trois villes au choix.
  worldClock,

  /// Un titre et une date.
  countdown,

  /// Le lieu des prévisions.
  weather,
}

/// Description, côté Dart, d'un widget d'écran d'accueil déclaré côté Android.
class WuxHomeWidget {
  const WuxHomeWidget({
    required this.id,
    required this.kind,
    required this.title,
    required this.description,
    required this.androidProvider,
    required this.previewSize,
  });

  /// Identifiant stable, utilisé comme préfixe des clés de données partagées.
  /// Le code Kotlin le répète (`AgendaWidget.widgetId`).
  final String id;

  final WuxWidgetKind kind;

  final String title;

  final String description;

  /// Nom complet de la classe Kotlin qui reçoit les mises à jour du widget.
  final String androidProvider;

  /// Taille (dp) à laquelle Android dessine l'aperçu : celle d'un widget
  /// posé à sa taille par défaut. L'aperçu est ensuite mis à l'échelle.
  final Size previewSize;

  /// Clé sous laquelle une donnée de ce widget est stockée pour le code natif.
  String key(String name) => '$id.$name';
}

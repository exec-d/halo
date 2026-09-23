/// Description, côté Dart, d'un widget d'écran d'accueil déclaré côté Android.
class WuxHomeWidget {
  const WuxHomeWidget({
    required this.id,
    required this.title,
    required this.description,
    required this.androidProvider,
  });

  /// Identifiant stable, utilisé comme préfixe des clés de données partagées.
  final String id;

  final String title;

  final String description;

  /// Nom complet de la classe `AppWidgetProvider` Kotlin qui dessine le widget.
  final String androidProvider;

  /// Clé sous laquelle une donnée de ce widget est stockée pour le code natif.
  String key(String name) => '$id.$name';
}

# WUX

Application Android (Flutter) qui propose des **widgets d'écran d'accueil**
pour personnaliser son téléphone.

## Principe

Flutter ne dessine pas directement sur l'écran d'accueil : un widget Android
est une vue native (`RemoteViews` ou Jetpack Glance) affichée par le lanceur.
WUX se compose donc de deux moitiés reliées par le plugin
[`home_widget`](https://pub.dev/packages/home_widget) :

| Côté | Rôle | Emplacement |
| --- | --- | --- |
| Flutter | Application compagnon : catalogue, réglages, épinglage | `lib/` |
| Android | Rendu natif de chaque widget | `android/app/src/main/kotlin/…/widgets/`, `res/` |

L'application écrit des valeurs dans un stockage partagé
(`HomeWidget.saveWidgetData`), puis demande au widget de se redessiner
(`HomeWidget.updateWidget`). Le `HomeWidgetProvider` Kotlin relit ces valeurs.

## Structure

```
lib/
  main.dart
  src/
    app.dart                      # MaterialApp
    home_widgets/
      wux_home_widget.dart        # description d'un widget
      catalog.dart                # liste des widgets proposés
      home_widget_bridge.dart     # frontière avec le plugin (mockable)
    screens/
      catalog_screen.dart         # écran provisoire
android/app/src/main/
  kotlin/dev/levilainpetit/wux/widgets/HelloWidgetProvider.kt
  res/layout/widget_hello.xml     # mise en page du widget
  res/xml/widget_hello_info.xml   # taille, redimensionnement…
  AndroidManifest.xml             # un <receiver> par widget
```

## Ajouter un widget

1. Déclarer un `WuxHomeWidget` dans `lib/src/home_widgets/catalog.dart`.
2. Créer son `HomeWidgetProvider` dans `android/…/widgets/`.
3. Ajouter sa mise en page (`res/layout/`) et ses métadonnées (`res/xml/`).
4. Déclarer son `<receiver>` dans `AndroidManifest.xml`.

Les clés partagées sont préfixées par l'`id` du widget
(`hello.message`), à relire à l'identique côté Kotlin.

## Développement

```bash
flutter pub get
dart format .
flutter analyze
flutter test
flutter run            # sur un appareil ou émulateur Android
```

La CI (`.github/workflows/ci.yml`) lance ces vérifications, construit un APK
de debug et le publie comme artefact téléchargeable.

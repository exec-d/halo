# WUX

Application Android (Flutter) qui propose des **widgets d'écran d'accueil**
pour personnaliser son téléphone.

## Widgets

| Widget | Contenu | Rendu | Toucher |
| --- | --- | --- | --- |
| Horloge | Heure (sans secondes) et date | `RemoteViews` + `TextClock` | Ouvre l'horloge |
| Agenda du jour | Événements d'aujourd'hui | Jetpack Glance | Ouvre l'événement ou le jour |
| Aujourd'hui et demain | Événements groupés par jour | Jetpack Glance | Ouvre l'événement ou le jour |

L'horloge n'est pas en Glance : `TextClock` se met à jour seul chaque minute
sans réveiller l'application, ce que Glance ne sait pas faire.

Réglages d'un agenda, appliqués immédiatement avec aperçu en direct :
agendas affichés (tous par défaut, y compris ceux ajoutés plus tard) et fond
(transparent comme la maquette, ou carte aux couleurs Material You).

## Principe

Un widget Android est une vue native affichée par le lanceur ; Flutter ne
dessine pas sur l'écran d'accueil. WUX a donc deux moitiés :

| Côté | Rôle | Emplacement |
| --- | --- | --- |
| Flutter + [IUX](https://github.com/systm-d/IUX) | Catalogue, réglages, aperçus, épinglage | `lib/` |
| Kotlin | Lecture du calendrier, rendu des widgets, rafraîchissement | `android/app/src/main/kotlin/…` |

- **Réglages** : écrits par Flutter avec `home_widget` sous la clé
  `<id du widget>.<nom>` (`agenda_today.calendars`), relus par
  `AgendaSettings.kt`.
- **Aperçus** : le canal `dev.levilainpetit.wux/native` (`MainActivity.kt`)
  renvoie les textes déjà mis en forme par `AgendaBuilder.kt`, le même code
  que le widget. Seule la mise en page est reproduite en Flutter
  (`lib/src/previews/`), et doit suivre celle de `AgendaWidget.kt`.
- **Fraîcheur des agendas** (`AgendaRefresh.kt`) : modification du calendrier
  (déclencheur de contenu WorkManager), passage à minuit, changement de réglage,
  plus la mise à jour système toutes les 30 minutes.
- **Couleurs** : `res/values-v31/colors.xml` pointe sur les couleurs système
  tirées du fond d'écran ; `res/values/colors.xml` est le repli avant Android 12.

## Décisions

| Sujet | Choix |
| --- | --- |
| Couleurs | Celles du fond d'écran (Material You, Android 12+) |
| Diffusion | Usage personnel, APK de debug issu de la CI |
| Android minimum | 8.0 (API 26) |
| Calendrier | Lecture seule (`READ_CALENDAR`) |

IUX n'est pas publié ; il est tiré de GitHub et épinglé sur un commit dans
`pubspec.yaml`. Pour monter de version, changer ce `ref` puis lancer
`flutter pub upgrade iux_flutter`.

## Ajouter un widget

1. Le déclarer dans `lib/src/home_widgets/catalog.dart`.
2. Écrire son widget Kotlin dans `android/…/widgets/`.
3. Ajouter ses métadonnées (`res/xml/`) et son `<receiver>` dans
   `AndroidManifest.xml`.
4. Lui donner un écran de réglage et un aperçu dans `lib/src/`.

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

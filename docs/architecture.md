# Architecture

*[English](en/architecture.md)*

## Deux moitiés

Un widget Android est une vue native que le lanceur affiche ; Flutter ne
dessine pas sur l'écran d'accueil. Halo a donc deux moitiés :

| Côté | Rôle | Emplacement |
| --- | --- | --- |
| Flutter + [IUX](https://github.com/systm-d/IUX) | Catalogue, réglages, aperçus, épinglage, autorisations | `lib/` |
| Kotlin | Données (agenda, météo, système), rendu des widgets et du fond d'écran, rafraîchissement | `android/app/src/main/kotlin/dev/levilainpetit/wux/` |

```
lib/
  main.dart                 point d'entrée, licences
  src/app.dart              thème IUX, écran de départ (catalogue ou réglages d'un widget)
  src/home_widgets/         description Dart des widgets (catalog.dart)
  src/platform/             WuxPlatform : la seule frontière avec Android
  src/previews/             aperçus : PNG rendus par Android
  src/screens/              catalogue, réglages de chaque type de widget, fond d'écran, réglages, à propos
android/app/src/main/kotlin/dev/levilainpetit/wux/
  MainActivity.kt           canal dev.levilainpetit.wux/native, autorisations
  calendar/                 lecture de l'agenda, mise en jours et en lignes
  weather/                  Open-Meteo : prévisions, pollens, cache, rafraîchissement
  system/                   batterie et son historique, appareil, Bluetooth, temps d'écran, données mobiles
  widgets/                  un AppWidgetProvider par widget, dessins ALPHA_8, aperçus
  wallpaper/                fond d'écran animé Circuit
```

## Comment les deux moitiés se parlent

- **Réglages** : écrits par Flutter avec `home_widget` sous la clé
  `<id du widget>.<nom>` (par exemple `agenda_one_column.calendars`), relus
  par le widget Kotlin. Chaque écriture redessine les widgets.
- **Canal `dev.levilainpetit.wux/native`** : tout le reste (autorisations,
  recherche de ville, aperçus, état). Les écrans Dart ne parlent qu'à
  l'interface `WuxPlatform`, ce qui permet de les tester avec
  `test/fake_platform.dart`.
- **Aperçus dans l'application** : Android dessine le vrai widget
  (`WidgetPreviews.kt`, chaque widget implémentant `PreviewableWidget`) et
  l'envoie en PNG ; rien n'est redessiné côté Dart. Le catalogue utilise des
  données d'exemple (`SampleData.kt`, `sample()` des sources système).
- **Aperçus dans la liste du lanceur** : `android:previewLayout`, des mises en
  page d'exemple (`res/layout/preview_*.xml`) avec des images d'exemple
  (`tool/sample_assets.py`).

## Rendu des widgets

- **`RemoteViews`**, pas Glance : Glance ne permet ni ombre ni halo sur le
  texte.
- **Pas de fond, pas de coins arrondis** dans les widgets ; le lanceur
  arrondit quand même les coins de la zone du widget, d'où une marge de
  12 dp sur les côtés et 8 dp en haut et en bas (`res/values/dimens.xml`).
- **Polices** : le lanceur n'a pas accès aux polices de l'application ; les
  textes utilisent la police mono du système et l'heure Roboto extra gras.
- **Couleurs** : `res/values-v31/colors.xml` pointe sur les couleurs d'accent
  du système (Material You) ; `res/values/colors.xml` est le repli d'avant
  Android 12.
- **Lisibilité** : tous les textes ont une ombre sombre (`text_shade`) qui les
  détache d'un fond chargé ; les traits et icônes gardent leur halo.
- **Dessins** : les graphiques (météo, batterie, cadran, données) sont des
  bitmaps `ALPHA_8` blancs, teintés par la mise en page (`android:tint`) ; les
  nuances passent par l'opacité. Les icônes lumineuses fixes sont des PNG
  générés par `tool/clock_assets.py`.
- **Tailles** : sur Android 12+, une mise en page par taille annoncée par le
  lanceur (`RemoteViews(Map<SizeF, RemoteViews>)`), pour le portrait et le
  paysage.
- **Heure qui avance seule** : `TextClock` (horloge, fuseaux) et `Chronometer`
  (durée depuis le démarrage) défilent sans réveiller l'application.

## Fraîcheur

| Données | Déclencheurs |
| --- | --- |
| Agenda | Modification du calendrier (déclencheur de contenu WorkManager), fin et début d'un événement du jour, 18 h, minuit, changement de réglage, mise à jour système (30 min) |
| Météo | WorkManager toutes les heures avec réseau ; à chaque mise à jour système, un téléchargement direct si les prévisions ont plus de 45 minutes (les tâches WorkManager d'une application en veille sont retardées) ; à l'ouverture de Halo |
| Système détaillé | WorkManager toutes les 15 minutes, mise à jour système, retour dans Halo |
| Écouteurs | Connexion et déconnexion Bluetooth |

## Décisions

| Sujet | Choix |
| --- | --- |
| Couleurs | Accent du fond d'écran (Material You, Android 12+) |
| Diffusion | APK signé avec une clé de debug commune, publié sur GitHub |
| Android minimum | 8.0 (API 26) |
| Agenda | Lecture seule (`READ_CALENDAR`) |
| Météo | Open-Meteo : gratuit, sans clé, sans compte |
| Bibliothèque d'interface | IUX, épinglée sur un commit (non publiée) |

## Signature

Toutes les builds sont signées avec `android/app/debug.keystore`, commune à la
CI et aux postes de développement : un nouvel APK s'installe par-dessus le
précédent. Cette clé est publique ; elle ne convient pas à une publication sur
un magasin d'applications.

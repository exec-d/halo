# WUX

Application Android (Flutter) qui propose des **widgets d'écran d'accueil**
pour personnaliser son téléphone.

## Widgets

Style néon d'après les maquettes : chiffres à points, texte mono, halo
lumineux, barres décoratives. Les couleurs suivent l'accent du fond d'écran
(Material You, Android 12+), un bleu fixe avant.

| Widget | Contenu | Toucher |
| --- | --- | --- |
| Horloge | Heure, date, prochaine alarme ; grande ou compacte selon la hauteur | Ouvre l'horloge |
| Agenda du jour | Événements d'aujourd'hui, en une ou deux colonnes | Ouvre l'événement ou le jour |
| Aujourd'hui et demain | Idem sur deux jours | Idem |
| Horloge et agenda | L'horloge, puis les événements d'aujourd'hui et de demain | Idem |

Tous sont en `RemoteViews` : Glance n'accepte ni police personnalisée ni
halo sur le texte. L'horloge utilise `TextClock`, qui avance seul chaque
minute sans réveiller l'application. L'agenda ne défile pas : `AgendaRenderer`
estime ce qui tient dans chaque colonne et termine par « ••• » s'il en reste.

À partir de 18 h, quand il ne reste plus d'événement à venir dans la journée,
les agendas passent au lendemain (`AgendaBuilder.END_OF_DAY_HOUR`).

Un appui long sur un widget ouvre ses réglages dans WUX (Android 12+).

### Ressources graphiques

- `res/font/wux_dots.ttf` : police à points, dessinée chiffre par chiffre
  dans `tool/wux_dots_font.py`.
- `res/font/share_tech_mono.ttf` : [Share Tech Mono](https://fonts.google.com/specimen/Share+Tech+Mono), licence OFL
  (`assets/fonts/ShareTechMono-OFL.txt`).
- `res/drawable-xxhdpi/*.png` : traits et barres avec leur halo, en blanc,
  teintés par le widget ; générés par `tool/clock_assets.py`.

Les deux polices sont aussi dans `assets/fonts/` pour les aperçus Flutter.

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
  (`lib/src/previews/`), et doit suivre les mises en page `res/layout/`.
- **Fraîcheur des agendas** (`AgendaRefresh.kt`) : modification du calendrier
  (déclencheur de contenu WorkManager), fin d'un événement du jour, 18 h,
  passage à minuit, changement de réglage,
  plus la mise à jour système toutes les 30 minutes.
- **Couleurs** : `res/values-v31/colors.xml` pointe sur les couleurs d'accent
  système tirées du fond d'écran ; `res/values/colors.xml` est le repli.

## Décisions

| Sujet | Choix |
| --- | --- |
| Couleurs | Accent du fond d'écran (Material You, Android 12+) |
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

# Halo

*Nom de code : WUX.*

Application Android (Flutter) qui propose des **widgets d'écran d'accueil**
au style néon pour personnaliser son téléphone. Nom affiché : **Halo** ; le
code, le paquet (`dev.levilainpetit.wux`) et le dépôt gardent le nom de code.

## Widgets

Style néon d'après les maquettes : heure en Roboto extra gras, texte mono,
halo lumineux. Les couleurs suivent l'accent du fond d'écran
(Material You, Android 12+), un bleu fixe avant.

| Widget | Contenu | Toucher |
| --- | --- | --- |
| Horloge | Heure, date, prochaine alarme ; grande ou compacte selon la hauteur | Ouvre l'horloge |
| Agenda | Événements d'aujourd'hui (et de demain, au choix), en une colonne | Ouvre l'événement ou le jour |
| Agenda 2 colonnes | Idem en deux colonnes | Idem |
| Système (4x1) | Batterie, réseau, stockage | Ouvre le réglage correspondant |
| Système avancé (4x2) | Signal, Wi-Fi, Bluetooth, batterie, mémoire, stockage, localisation, son | Idem |
| Fuseaux horaires (4x1) | L'heure de trois villes au choix | Ouvre l'horloge |
| Compte à rebours | Les jours jusqu'à une date | Ouvre WUX |
| Contrôles (4x1) | Lampe torche, Wi-Fi, Bluetooth, son, appareil photo | Bascule la lampe, ouvre le reste |
| Mois (4x3) | Le mois en cours, jours avec événements marqués | Ouvre le jour dans l'agenda |
| Météo (4x3) | Tableau de bord : température et jauge du jour, relevés, courbe des 24 h | Ouvre WUX |
| Soleil et Lune (4x1) | Lever, coucher, durée du jour, phase de la lune | Ouvre WUX |
| Pluie (4x2) | Pluie en cours ou à venir, probabilités sur 12 h | Ouvre WUX |
| Allergies (4x2) | Six pollens, qualité de l'air (Open-Meteo, Europe) | Ouvre WUX |

La météo vient d'[Open-Meteo](https://open-meteo.com) (gratuit, sans clé),
rafraîchie chaque heure par WorkManager quand le réseau est là. Le lieu se
choisit dans l'app, par la position (approximative, demandée une fois) ou par
une recherche ; il sert aussi à Soleil et Lune, dont la phase lunaire est
calculée sur le téléphone.

Tous sont en `RemoteViews` : Glance n'accepte pas de halo sur le texte. Un
widget est dessiné par le lanceur, qui n'a pas accès aux polices de
l'application : les textes utilisent la police mono du système et l'heure
Roboto extra gras. Heure et date sont des `TextClock`, qui avancent seuls sans
réveiller l'application.

À partir de 18 h, quand il ne reste plus d'événement à venir dans la journée,
les agendas passent au lendemain (`AgendaBuilder.END_OF_DAY_HOUR`).

Un appui long sur un widget ouvre ses réglages dans WUX (Android 12+).

### Ressources graphiques

- `res/drawable-xxhdpi/*.png` : traits et icône d'alarme avec leur halo, en blanc,
  teintés par le widget ; générés par `tool/clock_assets.py`.

## Principe

Un widget Android est une vue native affichée par le lanceur ; Flutter ne
dessine pas sur l'écran d'accueil. WUX a donc deux moitiés :

| Côté | Rôle | Emplacement |
| --- | --- | --- |
| Flutter + [IUX](https://github.com/systm-d/IUX) | Catalogue, réglages, aperçus, épinglage | `lib/` |
| Kotlin | Lecture du calendrier, rendu des widgets, rafraîchissement | `android/app/src/main/kotlin/…` |

- **Réglages** : écrits par Flutter avec `home_widget` sous la clé
  `<id du widget>.<nom>` (`agenda_one_column.calendars`), relus par
  `AgendaSettings.kt`.
- **Aperçus dans l'app** : Android dessine le vrai widget
  (`WidgetPreviews.kt`, chaque widget implémentant `PreviewableWidget`) et
  l'envoie en PNG à Flutter par le canal `dev.levilainpetit.wux/native`.
  Rien n'est redessiné côté Dart.
- **Aperçus dans la liste du lanceur** : `android:previewLayout` (Android 12+),
  la vraie mise en page pour l'horloge, des mises en page d'exemple
  (`res/layout/preview_*.xml`) pour les autres.
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
| Autorisations demandées | Agenda, position approximative (Météo) ; rien d'autre |

IUX n'est pas publié ; il est tiré de GitHub et épinglé sur un commit dans
`pubspec.yaml`. Pour monter de version, changer ce `ref` puis lancer
`flutter pub upgrade iux_flutter`.

## Ajouter un widget

1. Le déclarer dans `lib/src/home_widgets/catalog.dart`.
2. Écrire son widget Kotlin dans `android/…/widgets/`.
3. Ajouter ses métadonnées (`res/xml/`) et son `<receiver>` dans
   `AndroidManifest.xml`.
4. Lui donner un écran de réglage et un aperçu dans `lib/src/`.

## Installer une mise à jour

Toutes les builds de CI signent avec la même clé de debug
(`android/app/debug.keystore`) : un nouvel APK s'installe par-dessus le
précédent. Cette clé est publique ; elle ne doit pas servir à publier.

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

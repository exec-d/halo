# Contribuer à Halo

Merci de votre intérêt ! Signalements, idées et corrections sont bienvenus.
En participant, vous acceptez le [code de conduite](CODE_OF_CONDUCT.md).

## Signaler un problème, proposer une idée

- **Un bug** : ouvrez un [ticket « Bug »](https://github.com/exec-d/WUX/issues/new?template=bug_report.yml)
  avec le modèle du téléphone, la version d'Android, la version de Halo
  (*À propos*) et, si possible, une capture d'écran.
- **Une idée de widget ou d'amélioration** : un [ticket « Idée »](https://github.com/exec-d/WUX/issues/new?template=feature_request.yml).
  Une maquette, même dessinée à la main, aide beaucoup.
- **Une faille de sécurité** : jamais dans un ticket public, voir
  [SECURITY.md](SECURITY.md).

## Préparer son poste

- Flutter, canal stable ; Java 17 ; le SDK Android.
- Un téléphone Android 12+ ou un émulateur : les widgets ne se voient
  vraiment que sur un écran d'accueil.

```bash
git clone https://github.com/exec-d/WUX.git
cd WUX
flutter pub get
flutter run
```

[docs/architecture.md](docs/architecture.md) explique comment le projet est
organisé ; commencez par là.

## Avant d'envoyer une modification

La CI lance exactement ceci ; une demande de fusion doit passer :

```bash
dart format .
flutter analyze
flutter test
flutter build apk --debug
```

- Les écrans Dart se testent avec `test/fake_platform.dart` ; une nouvelle
  méthode de `WuxPlatform` s'y ajoute aussi.
- Le code Kotlin n'a pas de tests automatiques : décrivez dans la demande ce
  que vous avez vérifié sur un appareil, et joignez une capture pour tout
  changement visuel.

## Conventions

- **Langue** : l'interface, la documentation, les commentaires et les
  messages de commit sont en français.
- **Style** : `dart format` et `analysis_options.yaml` côté Dart ; côté
  Kotlin, le style officiel (4 espaces, virgules finales).
- **Commentaires** : ils disent *pourquoi*, pas *quoi* — une contrainte
  d'Android, un choix, une limite.
- **Interface de l'application** : composants [IUX](https://github.com/systm-d/IUX)
  uniquement ; une action indisponible donne toujours sa raison
  (`unavailabilityReason`).
- **Widgets** : pas de fond ni de coins arrondis, marges de
  `res/values/dimens.xml`, textes en styles `Halo*` (`res/values/styles.xml`),
  couleurs `clock_core`, `clock_line`, `clock_glow` — jamais de couleur en dur.
- **Casse** : seules les étiquettes sont en capitales ; les valeurs gardent
  leur casse.
- **Commits** : un sujet court qui dit ce qui change (« Agenda : filtre
  « toute la journée » »), un corps qui dit pourquoi.

## Ajouter un widget

1. **Kotlin** : une classe dans `android/…/widgets/`, qui étend `NeonWidget`
   (ou `AppWidgetProvider` + `PreviewableWidget`), avec des données d'exemple
   pour l'aperçu (`sample`).
2. **Ressources** : sa mise en page `res/layout/widget_<id>.xml`, un aperçu
   pour le lanceur `res/layout/preview_<id>.xml`, ses métadonnées
   `res/xml/widget_<id>_info.xml` (catégories `home_screen|keyguard`), ses
   textes dans `res/values/strings.xml`.
3. **Manifeste** : son `<receiver>` dans `AndroidManifest.xml`.
4. **Aperçus dans l'application** : son identifiant dans
   `WidgetPreviews.widget()`.
5. **Dart** : sa description dans `lib/src/home_widgets/catalog.dart` (même
   identifiant), et un écran de réglage si besoin (`lib/src/screens/`).
6. **Documentation** : une ligne dans [docs/widgets.md](docs/widgets.md) et
   dans [CHANGELOG.md](CHANGELOG.md).

## Ressources graphiques

Les PNG de `res/drawable-xxhdpi/` sont générés, pas dessinés à la main :

```bash
pip install pillow
python3 tool/clock_assets.py <flutter>/bin/cache/artifacts/material_fonts/MaterialIcons-Regular.otf
python3 tool/sample_assets.py
```

## Publier une version

1. Monter `version:` dans `pubspec.yaml` et compléter [CHANGELOG.md](CHANGELOG.md).
2. Fusionner sur `main`.
3. Créer une *release* GitHub avec l'étiquette `vX.Y.Z` : la CI y joint
   l'APK (`.github/workflows/release.yml`).

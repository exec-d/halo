// ignore: unused_import
import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for French (`fr`).
class AppLocalizationsFr extends AppLocalizations {
  AppLocalizationsFr([String locale = 'fr']) : super(locale);

  @override
  String get appTitle => 'Halo';

  @override
  String get back => 'Retour';

  @override
  String get pinToHome => 'Ajouter à l\'écran d\'accueil';

  @override
  String widgetPreviewSemantics(String title) {
    return 'Aperçu du widget $title';
  }

  @override
  String get catalogSettings => 'Réglages';

  @override
  String get catalogAbout => 'À propos';

  @override
  String get catalogWallpapers => 'Fonds d\'écran animés';

  @override
  String get catalogWidgets => 'Widgets';

  @override
  String get catalogFilterLabel => 'Catégories de widgets';

  @override
  String get catalogFilterAll => 'Tout';

  @override
  String get catalogFilterTime => 'Heure et agenda';

  @override
  String get catalogFilterWeather => 'Météo et ciel';

  @override
  String get catalogFilterSystem => 'Système et appareils';

  @override
  String get catalogFilterMedia => 'Médias et contrôles';

  @override
  String get catalogActive => 'Actif';

  @override
  String get catalogFeatured => 'À découvrir';

  @override
  String get dreamTitle => 'Écran de veille';

  @override
  String get dreamDescription =>
      'Pendant la charge : une grande horloge néon sur le décor Circuit.';

  @override
  String get dreamOpenSettings => 'Ouvrir les réglages de l\'écran de veille';

  @override
  String get dreamIntro =>
      'Quand le téléphone charge, ou est posé sur un socle, Android peut afficher un écran de veille. Celui de Halo montre l\'heure en grand sur le décor Circuit, avec la prochaine alarme, le prochain événement et la météo. Le texte se déplace un peu chaque minute pour ne pas marquer l\'écran ; un toucher réveille le téléphone.';

  @override
  String get dreamHowToTitle => 'Pour l\'activer';

  @override
  String get dreamStep1 =>
      '1. Dans les réglages qui s\'ouvrent, activez « Utiliser l\'écran de veille ».';

  @override
  String get dreamStep2 => '2. Choisissez « Halo » dans la liste.';

  @override
  String get dreamStep3 =>
      '3. Dans « Quand l\'activer », choisissez « Pendant la charge » (ou sur un socle).';

  @override
  String get settingsPermissionsTitle => 'Autorisations';

  @override
  String get settingsPermissionsDescription =>
      'Halo ne demande que ce dont ses widgets ont besoin.';

  @override
  String get settingsCalendarTitle => 'Agenda';

  @override
  String get settingsCalendarUse => 'Agendas et widget Mois, en lecture seule.';

  @override
  String get settingsLocationTitle => 'Position approximative';

  @override
  String get settingsLocationUse =>
      'Seulement pour « Utiliser ma position » dans Météo. Une ville choisie à la main suffit.';

  @override
  String get settingsBatteryTitle => 'Batterie';

  @override
  String get settingsBatteryUnrestricted =>
      'Halo n\'est pas limité par l\'optimisation de la batterie : ses widgets se mettent à jour à l\'heure.';

  @override
  String get settingsBatteryRestricted =>
      'Android peut retarder les mises à jour des widgets pour économiser la batterie. Les exclure de cette optimisation les garde à l\'heure, pour un coût minime.';

  @override
  String get settingsBatteryButton => 'Gérer l\'optimisation de la batterie';

  @override
  String get settingsWeatherTitle => 'Données météo';

  @override
  String get settingsWeatherRefresh => 'Actualiser maintenant';

  @override
  String get settingsWeatherNoPlaceReason => 'Aucun lieu choisi dans Météo';

  @override
  String get settingsWeatherDownloading => 'Téléchargement en cours';

  @override
  String get settingsWeatherUpToDate => 'Prévisions à jour.';

  @override
  String get settingsWeatherFailed =>
      'Échec du téléchargement : vérifiez la connexion.';

  @override
  String get settingsWeatherNoPlace =>
      'Aucun lieu choisi : ouvrez le widget Météo pour en choisir un.';

  @override
  String settingsWeatherNoForecast(String place) {
    return 'Lieu : $place. Pas encore de prévisions.';
  }

  @override
  String settingsWeatherUpdatedToday(String place, String time) {
    return 'Lieu : $place. Mises à jour à $time, puis toutes les 30 minutes environ.';
  }

  @override
  String settingsWeatherUpdatedOn(
    String place,
    int day,
    int month,
    String time,
  ) {
    return 'Lieu : $place. Mises à jour le $day/$month à $time, puis toutes les 30 minutes environ.';
  }

  @override
  String get settingsAboutTitle => 'À propos de Halo';

  @override
  String get settingsAboutSubtitle =>
      'Version, confidentialité, crédits, licences';

  @override
  String get permissionManage => 'Gérer';

  @override
  String get permissionAllow => 'Autoriser';

  @override
  String permissionActionSemantics(String action, String permission) {
    return '$action : $permission';
  }

  @override
  String get permissionGranted => 'Accordée';

  @override
  String get permissionNotGranted => 'Non accordée';

  @override
  String get aboutIconSemantics => 'Icône de Halo';

  @override
  String aboutVersion(String version) {
    return 'Version $version';
  }

  @override
  String aboutVersionBuild(String version, int build) {
    return '$version (build $build)';
  }

  @override
  String get aboutHaloDescription =>
      'Des widgets d\'écran d\'accueil au style néon, aux couleurs de votre fond d\'écran : horloge, agendas, météo, pluie, allergies, système, et plus. Nom de code : WUX.';

  @override
  String get aboutPrivacyTitle => 'Confidentialité';

  @override
  String get aboutPrivacyDescription =>
      'Halo ne crée aucun compte, ne contient ni publicité ni mesure d\'audience, et n\'envoie rien à ses auteurs. Votre agenda, le temps d\'écran, la consommation de données et les appareils Bluetooth sont lus sur le téléphone et n\'en sortent pas. Pour la météo, la pluie, les pollens et la qualité de l\'air, seules les coordonnées du lieu choisi sont envoyées à Open-Meteo. Vos réglages restent sur le téléphone.';

  @override
  String get aboutCreditsTitle => 'Sources et crédits';

  @override
  String get aboutCreditWeatherTitle =>
      'Météo, pluie, pollens, qualité de l\'air';

  @override
  String get aboutCreditWeatherDetail =>
      'Open-Meteo.com, sous licence CC BY 4.0. Pollens et qualité de l\'air : Copernicus Atmosphere Monitoring Service (CAMS), modèle européen.';

  @override
  String get aboutCreditCitiesTitle => 'Recherche de villes';

  @override
  String get aboutCreditCitiesDetail =>
      'Géocodage Open-Meteo, données GeoNames (CC BY 4.0).';

  @override
  String get aboutCreditIconsTitle => 'Icônes';

  @override
  String get aboutCreditIconsDetail =>
      'Material Icons de Google, licence Apache 2.0.';

  @override
  String get aboutCreditInterfaceTitle => 'Interface';

  @override
  String get aboutCreditInterfaceDetail =>
      'Flutter, et IUX pour les composants accessibles de l\'application.';

  @override
  String get aboutCreditSourceTitle => 'Code source';

  @override
  String get aboutCreditSourceDetail =>
      'Halo est un logiciel libre, sous licence MIT : github.com/exec-d/halo.';

  @override
  String get aboutLicenses => 'Licences des logiciels utilisés';

  @override
  String get accessTitle => 'Accès';

  @override
  String get accessGranted => 'Accordé';

  @override
  String get accessNotGranted => 'Non accordé';

  @override
  String get accessOpenAppInfo => 'Ouvrir la fiche de Halo';

  @override
  String get accessRestrictedHelp =>
      'Si Android répond « L\'accès a été refusé à cette appli » : Halo, installé hors du Play Store, relève des paramètres restreints. Dans sa fiche, touchez ⋮ puis « Autoriser les paramètres restreints », confirmez, et revenez activer l\'accès.';

  @override
  String get accessBluetoothButton => 'Autoriser « Appareils à proximité »';

  @override
  String get accessBluetoothDescription =>
      'Halo lit le nom, le type et la batterie des appareils Bluetooth connectés. Rien ne quitte le téléphone.';

  @override
  String get accessMediaButton => 'Ouvrir l\'accès aux notifications';

  @override
  String get accessMediaDescription =>
      'Android ne dit ce qui joue, et ne laisse le piloter, qu\'aux applis autorisées à « accéder aux notifications ». Halo n\'en lit aucune : il suit seulement la lecture. Rien ne quitte le téléphone.';

  @override
  String get accessTimerButton => 'Autoriser les notifications';

  @override
  String get accessTimerDescription =>
      'À la fin d\'un minuteur, Halo sonne et affiche une notification. Sans cette autorisation, le widget indique seulement « Minuteur terminé ».';

  @override
  String get accessUsageButton => 'Ouvrir l\'accès aux données d\'utilisation';

  @override
  String get accessUsageDescription =>
      'Android réserve la durée d\'utilisation des applis et la consommation de données aux applis autorisées dans ses réglages : activez Halo dans la liste. Rien ne quitte le téléphone.';

  @override
  String get mobileDataPlanTitle => 'Forfait';

  @override
  String get mobileDataIncluded => 'Données incluses';

  @override
  String get mobileDataIncludedHelp =>
      'Le widget trace le rythme qui mène pile au forfait.';

  @override
  String get mobileDataNoPlan => 'Pas de forfait';

  @override
  String mobileDataGigabytes(int gb) {
    return '$gb Go';
  }

  @override
  String get mobileDataCycleDay => 'Jour de reprise';

  @override
  String get mobileDataCycleDayHelp =>
      'Le jour du mois où le forfait repart à zéro.';

  @override
  String get mobileDataFirstOfMonth => '1er du mois';

  @override
  String mobileDataDayOfMonth(int day) {
    return 'Le $day';
  }

  @override
  String get agendaDaysTitle => 'Jours';

  @override
  String get agendaDaysShown => 'Jours affichés';

  @override
  String get agendaDaysHelp =>
      'À partir de 18 h, une journée terminée laisse place au lendemain.';

  @override
  String get agendaToday => 'Aujourd\'hui';

  @override
  String get agendaTodayTomorrow => 'Aujourd\'hui et demain';

  @override
  String get agendaEventsTitle => 'Événements';

  @override
  String get agendaEventsDescription =>
      'L\'événement en cours est mis en avant, les suivants sont légèrement estompés.';

  @override
  String get agendaEventsShown => 'Événements affichés';

  @override
  String get agendaAllDay => 'Toute la journée';

  @override
  String get agendaAllDaySemantics =>
      'Afficher les événements toute la journée';

  @override
  String get agendaAllDayHelp =>
      'Anniversaires, congés, jours fériés… et les événements sur plusieurs jours.';

  @override
  String get agendaCalendarsTitle => 'Agendas';

  @override
  String get agendaCalendarsShown => 'Agendas affichés';

  @override
  String get agendaPreviewPlaceholder =>
      'L\'aperçu apparaîtra une fois l\'agenda autorisé.';

  @override
  String get agendaAllowAccess => 'Autoriser l\'accès à l\'agenda';

  @override
  String get agendaOpenSettings => 'Ouvrir les réglages';

  @override
  String get agendaAccessTitle => 'Accès à l\'agenda';

  @override
  String get agendaAccessRefused =>
      'L\'accès a été refusé. S\'il ne vous est plus proposé, activez « Agenda » dans les autorisations de l\'application.';

  @override
  String get agendaAccessExplanation =>
      'Halo lit vos événements pour les afficher. Il ne les modifie jamais et ne les envoie nulle part.';

  @override
  String get countdownDay => 'Jour';

  @override
  String get countdownMonth => 'Mois';

  @override
  String get countdownYear => 'Année';

  @override
  String get countdownEventTitle => 'Évènement';

  @override
  String get countdownTitleLabel => 'Titre';

  @override
  String get countdownTitleHelp => 'Par exemple « Vacances ».';

  @override
  String get countdownDateLabel => 'Date';

  @override
  String get countdownDateHelp => 'Jour, mois et année.';

  @override
  String get countdownDateInvalid => 'Cette date n\'existe pas.';

  @override
  String get weatherUseLocation => 'Utiliser ma position';

  @override
  String get weatherSearch => 'Rechercher';

  @override
  String get weatherSearching => 'Recherche en cours';

  @override
  String weatherNoResult(String query) {
    return 'Aucun lieu trouvé pour « $query ».';
  }

  @override
  String get weatherPlaceTitle => 'Lieu';

  @override
  String get weatherNoPlace => 'Aucun lieu choisi pour le moment.';

  @override
  String weatherForecastFor(String place) {
    return 'Prévisions pour $place.';
  }

  @override
  String get weatherLocationFailed =>
      'La position n\'a pas pu être obtenue. Vérifiez que la localisation est activée et autorisée pour Halo.';

  @override
  String get weatherCityLabel => 'Ville';

  @override
  String get weatherCityHelp => 'Ou cherchez une ville par son nom.';

  @override
  String get weatherPlaceUnavailable =>
      'Les prévisions de ce lieu ne sont pas disponibles.';

  @override
  String get worldClockCities => 'Villes';

  @override
  String worldClockCity(int number) {
    return 'Ville $number';
  }

  @override
  String get wallpaperApply => 'Appliquer le fond d\'écran';

  @override
  String get wallpaperActive => 'Fond d\'écran actuel';

  @override
  String get wallpaperNotApplied => 'Pas encore appliqué';

  @override
  String wallpaperUnavailable(String title) {
    return 'Ce téléphone ne propose pas l\'écran d\'application. Choisissez « Halo · $title » dans Fond d\'écran et style, rubrique Fonds d\'écran animés.';
  }

  @override
  String get wallpaperIntensityTitle => 'Intensité';

  @override
  String get wallpaperIntensityLabel => 'Intensité du fond';

  @override
  String get wallpaperIntensityHelp =>
      'Discret garde les widgets et les icônes bien lisibles par-dessus.';

  @override
  String get wallpaperIntensityDiscreet => 'Discret';

  @override
  String get wallpaperIntensityNormal => 'Normal';

  @override
  String get wallpaperIntensityVivid => 'Vif';

  @override
  String get wallpaperFeaturesTitle => 'Ce qui bouge';

  @override
  String get wallpaperBatteryTitle => 'Batterie';

  @override
  String wallpaperPreviewSemantics(String title) {
    return 'Aperçu du fond d\'écran $title';
  }

  @override
  String get wallpaperCircuitTitle => 'Circuit';

  @override
  String get wallpaperCircuitDescription =>
      'L\'intérieur du téléphone en néon, aux couleurs du téléphone.';

  @override
  String get wallpaperCircuitTiltTitle => 'Inclinaison';

  @override
  String get wallpaperCircuitTiltText =>
      'Les plans du téléphone glissent quand vous le penchez, et le reflet du verre suit.';

  @override
  String get wallpaperCircuitBatteryTitle => 'Vraie batterie';

  @override
  String get wallpaperCircuitBatteryText =>
      'La batterie dessinée affiche le niveau réel et respire pendant la charge.';

  @override
  String get wallpaperCircuitNetworkTitle => 'Réseau';

  @override
  String get wallpaperCircuitNetworkText =>
      'Des impulsions courent de l\'antenne au processeur quand des données passent ; les antennes brillent selon la force du signal.';

  @override
  String get wallpaperCircuitWakeTitle => 'Allumage';

  @override
  String get wallpaperCircuitWakeText =>
      'À chaque allumage de l\'écran, le fond est là tout de suite, puis composants et pistes s\'illuminent un à un depuis le processeur.';

  @override
  String get wallpaperCircuitBattery =>
      'L\'animation s\'arrête dès que le fond n\'est plus visible, et ne tourne en continu que pendant un mouvement, une impulsion ou une charge.';

  @override
  String get wallpaperSceneBattery =>
      'L\'animation s\'arrête dès que le fond n\'est plus visible. Ce fond garde ses propres couleurs, pas celles du téléphone.';

  @override
  String get wallpaperGridTitle => 'Grille';

  @override
  String get wallpaperGridDescription =>
      'Une grille lumineuse à perte de vue, où deux motos de lumière tracent leurs murs.';

  @override
  String get wallpaperGridATitle => 'Motos de lumière';

  @override
  String get wallpaperGridAText =>
      'Deux motos, cyan et orange, filent sur la grille et virent à angle droit ; leurs murs lumineux s\'effacent derrière elles.';

  @override
  String get wallpaperGridBTitle => 'Perspective';

  @override
  String get wallpaperGridBText =>
      'La grille défile doucement vers vous jusqu\'à l\'horizon.';

  @override
  String get wallpaperGridCTitle => 'Inclinaison';

  @override
  String get wallpaperGridCText =>
      'Penchez le téléphone : le sol et les murs glissent plus que l\'horizon lointain.';

  @override
  String get wallpaperMegacityTitle => 'Mégapole';

  @override
  String get wallpaperMegacityDescription =>
      'Une ville immense sous la pluie, la nuit, dans un smog orangé.';

  @override
  String get wallpaperMegacityATitle => 'La ville';

  @override
  String get wallpaperMegacityAText =>
      'Trois plans de tours aux fenêtres allumées, une pyramide au loin et un grand panneau lumineux qui change de couleur.';

  @override
  String get wallpaperMegacityBTitle => 'La vie';

  @override
  String get wallpaperMegacityBText =>
      'Des torchères crachent leur flamme, des voitures volantes passent, deux projecteurs balaient le ciel.';

  @override
  String get wallpaperMegacityCTitle => 'Pluie et inclinaison';

  @override
  String get wallpaperMegacityCText =>
      'Il pleut sans cesse ; penchez le téléphone, les tours proches bougent plus que les lointaines, et la pluie penche.';

  @override
  String get wallpaperCodeTitle => 'Code';

  @override
  String get wallpaperCodeDescription =>
      'Une pluie de caractères verts, en deux plans.';

  @override
  String get wallpaperCodeATitle => 'La pluie';

  @override
  String get wallpaperCodeAText =>
      'Chaque colonne tombe à sa vitesse, sa tête blanche en avant, sa traîne qui s\'éteint ; les caractères changent au passage.';

  @override
  String get wallpaperCodeBTitle => 'Profondeur';

  @override
  String get wallpaperCodeBText =>
      'Un plan de petits caractères, plus sombres, derrière le premier.';

  @override
  String get wallpaperCodeCTitle => 'Gravité';

  @override
  String get wallpaperCodeCText =>
      'La pluie penche quand vous inclinez le téléphone.';

  @override
  String get wallpaperNeonTitle => 'Néon';

  @override
  String get wallpaperNeonDescription =>
      'Une rue du futur, la nuit, bordée d\'enseignes néon.';

  @override
  String get wallpaperNeonATitle => 'Enseignes';

  @override
  String get wallpaperNeonAText =>
      'Jaunes, cyan, magenta et rouges, de chaque côté de la rue ; certaines grésillent, toutes se reflètent sur le sol mouillé.';

  @override
  String get wallpaperNeonBTitle => 'Bugs';

  @override
  String get wallpaperNeonBText =>
      'De temps en temps, l\'image décroche : bandes décalées, couleurs séparées, une barre jaune d\'interface.';

  @override
  String get wallpaperNeonCTitle => 'Inclinaison';

  @override
  String get wallpaperNeonCText =>
      'Penchez le téléphone : les enseignes proches bougent plus que le fond de la rue.';

  @override
  String get wallpaperSentinelTitle => 'Sentinelle';

  @override
  String get wallpaperSentinelDescription =>
      'L\'œil rouge d\'une intelligence artificielle, dans son boîtier de métal.';

  @override
  String get wallpaperSentinelATitle => 'Il vous regarde';

  @override
  String get wallpaperSentinelAText =>
      'Le cœur de l\'œil suit l\'inclinaison du téléphone, et son reflet glisse à l\'opposé sur le verre.';

  @override
  String get wallpaperSentinelBTitle => 'Il respire';

  @override
  String get wallpaperSentinelBText =>
      'Le cœur pulse lentement ; un anneau de balayage part du centre de temps en temps.';

  @override
  String get wallpaperSentinelCTitle => 'Il s\'éveille';

  @override
  String get wallpaperSentinelCText =>
      'L\'œil s\'avive quand vous déverrouillez le téléphone.';

  @override
  String get widgetClockTitle => 'Horloge';

  @override
  String get widgetClockDescription =>
      'L\'heure et la date. Touchez-le pour ouvrir l\'horloge.';

  @override
  String get widgetOneColumnAgendaTitle => 'Agenda';

  @override
  String get widgetOneColumnAgendaDescription =>
      'Les événements du jour, en une colonne.';

  @override
  String get widgetTwoColumnAgendaTitle => 'Agenda 2 colonnes';

  @override
  String get widgetTwoColumnAgendaDescription =>
      'Les événements du jour, en deux colonnes.';

  @override
  String get widgetSystemTitle => 'Système';

  @override
  String get widgetSystemDescription =>
      'Batterie, réseau et stockage, sur une rangée.';

  @override
  String get widgetAdvancedSystemTitle => 'Système avancé';

  @override
  String get widgetAdvancedSystemDescription =>
      'Signal, Wi-Fi, Bluetooth, batterie, mémoire, stockage, localisation et son, sur deux rangées.';

  @override
  String get widgetBatteryTitle => 'Batterie détaillée';

  @override
  String get widgetBatteryDescription =>
      'La courbe des 24 dernières heures et la suite prévue, la température, la tension et les cycles.';

  @override
  String get widgetDeviceTitle => 'Appareil';

  @override
  String get widgetDeviceDescription =>
      'La fiche du téléphone façon console, et depuis combien de temps il tourne, à la seconde.';

  @override
  String get widgetBluetoothDevicesTitle => 'Écouteurs et montre';

  @override
  String get widgetBluetoothDevicesDescription =>
      'La batterie des appareils Bluetooth connectés.';

  @override
  String get widgetScreenTimeTitle => 'Temps d\'écran';

  @override
  String get widgetScreenTimeDescription =>
      'La journée en cadran de 24 heures : quand l\'écran était allumé, les déverrouillages et les applis les plus utilisées.';

  @override
  String get widgetMobileDataTitle => 'Données mobiles';

  @override
  String get widgetMobileDataDescription =>
      'La consommation de la période face au forfait, avec la projection en fin de période.';

  @override
  String get widgetForecastTitle => 'Prévisions 5 jours';

  @override
  String get widgetForecastDescription =>
      'Cinq jours de météo, et une capsule qui place chacun dans la semaine, du plus frais au plus chaud.';

  @override
  String get widgetAnalogClockTitle => 'Horloge analogique';

  @override
  String get widgetAnalogClockDescription =>
      'Un cadran néon aux aiguilles lumineuses, et la date.';

  @override
  String get widgetEphemerisTitle => 'Éphéméride';

  @override
  String get widgetEphemerisDescription =>
      'La fête du jour, la semaine, l\'année en douze mois et le prochain jour férié.';

  @override
  String get widgetTimerTitle => 'Chronomètre et minuteur';

  @override
  String get widgetTimerDescription =>
      'Un chronomètre, et un minuteur de 1, 5, 10 ou 25 minutes d\'un toucher, qui sonne à la fin.';

  @override
  String get widgetMediaTitle => 'Lecture en cours';

  @override
  String get widgetMediaDescription =>
      'Ce qui joue, sa pochette en néon, et précédent, lecture ou pause, suivant.';

  @override
  String get widgetWorldClockTitle => 'Fuseaux horaires';

  @override
  String get widgetWorldClockDescription =>
      'L\'heure de trois villes, sur une rangée.';

  @override
  String get widgetCountdownTitle => 'Compte à rebours';

  @override
  String get widgetCountdownDescription =>
      'Les jours jusqu\'à une date qui compte.';

  @override
  String get widgetControlsTitle => 'Contrôles';

  @override
  String get widgetControlsDescription =>
      'Lampe torche en un geste ; Wi-Fi, Bluetooth, son et appareil photo à portée de doigt.';

  @override
  String get widgetMonthTitle => 'Mois';

  @override
  String get widgetMonthDescription =>
      'Le mois en cours ; un point sous chaque jour qui a un événement. Il lit l\'agenda : autorisez-le depuis un widget Agenda.';

  @override
  String get widgetWeatherTitle => 'Météo';

  @override
  String get widgetWeatherDescription =>
      'Façon tableau de bord : température et jauge du jour, ressenti, vent, humidité, UV, pluie, et la courbe des 24 heures. Open-Meteo, chaque heure. Sur une rangée, une seule ligne.';

  @override
  String get widgetSunMoonTitle => 'Soleil et Lune';

  @override
  String get widgetSunMoonDescription =>
      'Lever et coucher du soleil au lieu choisi dans Météo, durée du jour et phase de la lune.';

  @override
  String get widgetRainTitle => 'Pluie';

  @override
  String get widgetRainDescription =>
      'Si la pluie arrive et quand, puis les probabilités des 12 prochaines heures, au lieu choisi dans Météo.';

  @override
  String get widgetAllergyTitle => 'Allergies';

  @override
  String get widgetAllergyDescription =>
      'Graminées, bouleau, aulne, olivier, armoise, ambroisie et qualité de l\'air, au lieu choisi dans Météo. Pollens : Europe seulement.';
}

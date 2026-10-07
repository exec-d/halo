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
  String get catalogSearch => 'Rechercher un widget';

  @override
  String get catalogSearchClose => 'Fermer la recherche';

  @override
  String get catalogChange => 'Changer';

  @override
  String get catalogSeeAll => 'Tout voir';

  @override
  String catalogWidgetCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count widgets',
      one: '1 widget',
    );
    return '$_temp0';
  }

  @override
  String get catalogNew => 'Nouveau';

  @override
  String get catalogNavGallery => 'Galerie';

  @override
  String get catalogNavWallpapers => 'Fonds';

  @override
  String get catalogNavMine => 'Mes ajouts';

  @override
  String get catalogWallpapersIntro =>
      'Ils réagissent à l\'inclinaison du téléphone. Touchez-en un pour le voir en grand et l\'appliquer.';

  @override
  String get catalogMineWallpaper => 'Fond d\'écran';

  @override
  String get catalogMineNoWallpaper =>
      'Aucun fond Halo n\'est appliqué pour l\'instant.';

  @override
  String get catalogMineWidgets => 'Widgets posés';

  @override
  String get catalogMineNoWidgets =>
      'Aucun widget Halo n\'est posé pour l\'instant. Touchez un widget dans la galerie pour l\'ajouter.';

  @override
  String get catalogHeroApplied => 'Appliqué sur ce téléphone';

  @override
  String get catalogNoMatch => 'Aucun widget ne correspond.';

  @override
  String detailOnWallpaper(String name) {
    return 'Sur $name';
  }

  @override
  String get detailLockScreen => 'Verrouillage';

  @override
  String get detailTagHome => 'Accueil';

  @override
  String get detailTagLock => 'Verrouillage';

  @override
  String get detailBackgrounds => 'Fond de l\'aperçu';

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
      'Halo ne crée aucun compte, ne contient ni publicité ni mesure d\'audience, et n\'envoie rien à ses auteurs. Votre agenda, le temps d\'écran, la consommation de données, les appareils Bluetooth, vos contacts favoris, vos pas et votre sommeil (Santé Connect) sont lus sur le téléphone et n\'en sortent pas. Seules quelques requêtes quittent le téléphone : les coordonnées du lieu choisi, envoyées à Open-Meteo pour la météo, la pluie, les pollens, la qualité de l\'air et la mer ; le ping et, quand vous le lancez, le test de débit du widget Réseau, vers les serveurs de Cloudflare. La Lune et ses phases sont calculées sur le téléphone. Vos réglages restent sur le téléphone.';

  @override
  String get aboutCreditsTitle => 'Sources et crédits';

  @override
  String get aboutCreditWeatherTitle =>
      'Météo, pluie, pollens, qualité de l\'air, mer';

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
  String get accessNetworkButton => 'Autoriser la position précise';

  @override
  String get accessNetworkDescription =>
      'Android ne donne le nom du Wi-Fi qu\'aux applis qui ont la position précise. Halo ne s\'en sert que pour ce nom : il ne lit ni ne garde votre position.';

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
  String get wallpaperFluxTitle => 'Retour vers le futur';

  @override
  String get wallpaperFluxDescription =>
      'Le convecteur temporel de la DeLorean, installé dans le téléphone à la place de la batterie.';

  @override
  String get wallpaperFluxMotion => 'Le convecteur';

  @override
  String get wallpaperFluxMotionText =>
      'Les impulsions courent le long des trois bras jusqu\'au cœur, qui s\'illumine ; plus vite pendant la charge. Au-dessus du hublot, la jauge montre le niveau de batterie ; sur la carte mère, les circuits temporels affichent l\'heure.';

  @override
  String get wallpaperFluxBattery =>
      'L\'animation s\'arrête dès que le fond n\'est plus visible ; à l\'écran, le convecteur tourne sans cesse, à cadence réduite.';

  @override
  String get wallpaperArcTitle => 'Iron Man';

  @override
  String get wallpaperArcDescription =>
      'Le réacteur arc de Tony Stark, installé dans le téléphone à la place de la batterie.';

  @override
  String get wallpaperArcMotion => 'Le réacteur';

  @override
  String get wallpaperArcMotionText =>
      'À l\'allumage, les bobines s\'allument une à une puis le cœur s\'embrase. Une bobine par dixième de batterie, deux pistes d\'énergie qui tournent, un cœur qui bat ; en charge, des particules spiralent vers lui, et sous 15 % il vacille.';

  @override
  String get wallpaperArcBattery =>
      'L\'animation s\'arrête dès que le fond n\'est plus visible ; à l\'écran, le réacteur tourne sans cesse, à cadence réduite.';

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
  String get widgetProgressTitle => 'Progression';

  @override
  String get widgetProgressDescription =>
      'La part écoulée du jour, de la semaine, du mois et de l\'année, en quatre barres.';

  @override
  String get widgetNetworkTitle => 'Réseau';

  @override
  String get widgetNetworkDescription =>
      'Le Wi-Fi ou le réseau mobile en cours, la force du signal, le ping et l\'adresse locale. Le test de débit (une dizaine de secondes, quelques dizaines de Mo) ne part qu\'au toucher de « Tester ».';

  @override
  String get widgetHealthTitle => 'Pas et sommeil';

  @override
  String get widgetHealthDescription =>
      'Les pas du jour vers votre objectif, la dernière nuit et ses phases, et les pas des sept derniers jours, par Santé Connect.';

  @override
  String get accessHealthButton => 'Autoriser Santé Connect';

  @override
  String get accessHealthDescription =>
      'Halo lit vos pas et votre sommeil dans Santé Connect, y compris en arrière-plan pour que le widget reste à jour. Il n\'écrit rien et rien ne quitte le téléphone.';

  @override
  String get healthGoalTitle => 'Objectif';

  @override
  String get healthGoal => 'Pas par jour';

  @override
  String get healthGoalHelp =>
      'L\'anneau du widget se remplit vers cet objectif.';

  @override
  String healthGoalSteps(int steps) {
    final intl.NumberFormat stepsNumberFormat =
        intl.NumberFormat.decimalPattern(localeName);
    final String stepsString = stepsNumberFormat.format(steps);

    return '$stepsString pas';
  }

  @override
  String get widgetContactsTitle => 'Contacts favoris';

  @override
  String get widgetContactsDescription =>
      'Jusqu\'à six contacts en pastilles néon ; un toucher pour appeler ou écrire.';

  @override
  String get contactsTitle => 'Contacts';

  @override
  String get contactsDescription =>
      'Choisissez-les avec le sélecteur d\'Android : Halo ne voit que le contact touché, pas votre carnet d\'adresses.';

  @override
  String get contactsAdd => 'Ajouter un contact';

  @override
  String get contactsFull =>
      'Six contacts : retirez-en un pour en ajouter un autre.';

  @override
  String get contactsActionTitle => 'Au toucher';

  @override
  String get contactsSms => 'Écrire un SMS';

  @override
  String get contactsSmsHelp => 'Sinon, un toucher appelle le contact.';

  @override
  String get contactsCallHelp =>
      'Sans autorisation, un toucher ouvre le clavier avec le numéro : il reste à appuyer sur « Appeler ».';

  @override
  String get contactsCallButton => 'Appeler directement';

  @override
  String contactsRemove(String name) {
    return 'Retirer $name';
  }

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
  String get widgetSeaTitle => 'Mer et vagues';

  @override
  String get widgetSeaDescription =>
      'Au plus près du lieu de Météo : la hauteur, la période et la direction des vagues, la température de l\'eau et les vagues des 24 prochaines heures.';

  @override
  String get widgetSkyTitle => 'Lune';

  @override
  String get widgetSkyDescription =>
      'La Lune dans sa phase du moment, dessinée avec ses mers : le compte à rebours jusqu\'à la pleine lune, son lever et son coucher au lieu de Météo, et les quatre phases à venir. Calculé sur le téléphone.';

  @override
  String get widgetAllergyTitle => 'Allergies';

  @override
  String get widgetAllergyDescription =>
      'Graminées, bouleau, aulne, olivier, armoise, ambroisie et qualité de l\'air, au lieu choisi dans Météo. Pollens : Europe seulement.';

  @override
  String get catalogThemes => 'Thèmes';

  @override
  String get catalogThemesHint =>
      'Un fond, ses couleurs, des widgets assortis et des sons, en un toucher.';

  @override
  String get themesTitle => 'Thèmes';

  @override
  String get themesIntro =>
      'Les packs de thème des Pixel sont réservés à Google. Ceux de Halo appliquent ensemble un fond animé, ses couleurs, des widgets assortis et des sons originaux.';

  @override
  String get themesContents => 'Fond · couleurs · widgets · 3 sons';

  @override
  String get themeCircuitDescription =>
      'Le téléphone en schéma néon, bleu électrique.';

  @override
  String get themeFluxDescription => 'Le convecteur temporel, orange et ambre.';

  @override
  String get themeArcDescription => 'Le réacteur arc, rouge et or.';

  @override
  String get themeApply => 'Appliquer le thème';

  @override
  String get themeActive => 'Thème actif';

  @override
  String get themeNotApplied => 'Pas encore appliqué';

  @override
  String themeUnavailable(String title) {
    return 'Ce téléphone ne propose pas l\'écran d\'application. Choisissez « Halo · $title » dans Fond d\'écran et style, rubrique Fonds d\'écran animés.';
  }

  @override
  String themePreviewSemantics(String title) {
    return 'Aperçu du thème $title';
  }

  @override
  String get themeColorsTitle => 'Couleurs';

  @override
  String get themeColorsText =>
      'Le fond annonce ces couleurs à Android, qui les applique quand vous confirmez le fond (sinon, à la prochaine mise en veille de l\'écran). Il faut que Couleurs soit réglé sur une couleur du fond d\'écran dans Fond d\'écran et style ; le système et les widgets Halo les prennent alors.';

  @override
  String get themeColorsOpen => 'Ouvrir Fond d\'écran et style';

  @override
  String get themeSoundsTitle => 'Sons';

  @override
  String get themeSoundsText =>
      'Des sons originaux, synthétisés. Choisissez ceux que le thème remplace ; ils restent ensuite au choix dans les sons du téléphone.';

  @override
  String get themeSoundsPermission =>
      'La première fois, Android demande l\'autorisation « Modifier les paramètres système » : activez-la pour Halo, puis revenez.';

  @override
  String get themeSoundsAllow => 'Autoriser les sons';

  @override
  String get themeSoundsUnsupported => 'Les sons demandent Android 10 ou plus.';

  @override
  String themeSoundsApplied(String list) {
    return 'Sons réglés : $list.';
  }

  @override
  String get themeSoundsWaiting => 'Sons en attente de l\'autorisation.';

  @override
  String get themeSoundRing => 'Sonnerie';

  @override
  String get themeSoundNotification => 'Notification';

  @override
  String get themeSoundAlarm => 'Alarme';

  @override
  String themeSoundPlay(String name) {
    return 'Écouter $name';
  }

  @override
  String get themeSoundCircuitRing => 'Bus de données';

  @override
  String get themeSoundCircuitRingText =>
      'Des octets qui montent et descendent la carte';

  @override
  String get themeSoundCircuitNotification => 'Impulsion';

  @override
  String get themeSoundCircuitNotificationText =>
      'Une impulsion file vers le processeur';

  @override
  String get themeSoundCircuitAlarm => 'Démarrage';

  @override
  String get themeSoundCircuitAlarmText => 'Les composants s\'allument un à un';

  @override
  String get themeSoundFluxRing => '88 mph';

  @override
  String get themeSoundFluxRingText =>
      'Le moteur monte, le flux crépite, puis le saut';

  @override
  String get themeSoundFluxNotification => 'Flux';

  @override
  String get themeSoundFluxNotificationText =>
      'Trois éclairs qui se rejoignent au cœur';

  @override
  String get themeSoundFluxAlarm => 'Heure de départ';

  @override
  String get themeSoundFluxAlarmText => 'Les bips des circuits temporels';

  @override
  String get themeSoundArcRing => 'Répulseur';

  @override
  String get themeSoundArcRingText => 'La charge siffle, puis le tir';

  @override
  String get themeSoundArcNotification => 'Interface';

  @override
  String get themeSoundArcNotificationText =>
      'Deux tons cristallins de l\'affichage tête haute';

  @override
  String get themeSoundArcAlarm => 'Réacteur';

  @override
  String get themeSoundArcAlarmText => 'Le cœur bat, de plus en plus fort';

  @override
  String get themeLimitsTitle => 'Ce que Halo ne peut pas changer';

  @override
  String get themeLimitsText =>
      'Le lanceur Pixel refuse les packs d\'icônes, l\'horloge de l\'écran verrouillé est celle de Google et Gboard n\'a pas de thème pour les applications. Les icônes à thème et l\'horloge suivent quand même les couleurs du thème.';

  @override
  String get themeColorsPreset =>
      'Couleurs est réglé sur une couleur de base : le système ignore le fond. Choisissez une couleur du fond d\'écran.';

  @override
  String get wallpaperCoreBattery =>
      'L\'animation s\'arrête dès que le fond n\'est plus visible ; à l\'écran, elle tourne à pleine cadence 30 secondes après l\'allumage ou un geste sur l\'accueil, puis ralentit jusqu\'au geste suivant.';

  @override
  String get wallpaperQuantumTitle => 'Physique quantique';

  @override
  String get wallpaperQuantumDescription =>
      'Tout le téléphone devient le bas du lustre d\'un ordinateur quantique : la forêt de câbles coaxiaux et leurs boucles, le plateau doré, les colonnes de cuivre couvertes de connecteurs et la puce au centre.';

  @override
  String get wallpaperQuantumMotion => 'Le lustre';

  @override
  String get wallpaperQuantumMotionText =>
      'Les rangées de connecteurs des colonnes s\'allument de bas en haut, une par dixième de batterie. Des impulsions de commande descendent les câbles jusqu\'à la puce et la lecture remonte ; la puce bat et ses qubits scintillent. En charge, une vague de froid descend les boucles des câbles.';

  @override
  String get themeQuantumDescription =>
      'Le lustre d\'un ordinateur quantique, violet.';

  @override
  String get themeSoundQuantumRing => 'Superposition';

  @override
  String get themeSoundQuantumRingText =>
      'Deux tons qui battent, puis une gamme qui monte et redescend';

  @override
  String get themeSoundQuantumNotification => 'Intrication';

  @override
  String get themeSoundQuantumNotificationText =>
      'Deux notes qui sonnent ensemble, deux fois';

  @override
  String get themeSoundQuantumAlarm => 'Effondrement';

  @override
  String get themeSoundQuantumAlarmText =>
      'Un souffle qui se resserre jusqu\'à une note pure';

  @override
  String get wallpaperNeuralTitle => 'Intelligence artificielle';

  @override
  String get wallpaperNeuralDescription =>
      'Le téléphone de Circuit, détaillé jusqu\'aux vias, avec à la place de la batterie un accélérateur d\'IA : son boîtier, quatre piles de mémoire et, sur la puce, un cerveau gravé en pistes.';

  @override
  String get wallpaperNeuralMotion => 'L\'inférence';

  @override
  String get wallpaperNeuralMotionText =>
      'En boucle, les mémoires sont lues couche après couche, les données filent jusqu\'au cerveau, une vague part de sa ligne médiane jusqu\'aux plots, puis la réponse remonte par les nappes et les cœurs du processeur s\'allument. Les plots allumés suivent la batterie. En charge, les bobines s\'éclairent, la ligne médiane pulse et les inférences s\'accélèrent.';

  @override
  String get themeNeuralDescription =>
      'Un cerveau de silicium sur sa carte, bleu électrique.';

  @override
  String get themeSoundNeuralRing => 'Inférence';

  @override
  String get themeSoundNeuralRingText =>
      'Une suite de notes rapides, comme une pensée qui se forme';

  @override
  String get themeSoundNeuralNotification => 'Jeton';

  @override
  String get themeSoundNeuralNotificationText => 'Deux petits sons secs';

  @override
  String get themeSoundNeuralAlarm => 'Éveil';

  @override
  String get themeSoundNeuralAlarmText =>
      'Un accord qui se construit couche par couche';

  @override
  String get wallpaperAtomTitle => 'Énergie atomique';

  @override
  String get wallpaperAtomDescription =>
      'Tout le téléphone devient la cuve d\'un réacteur à eau pressurisée, en coupe : les mécanismes des barres de commande sur le couvercle, la bride et ses goujons, le cœur et ses onze assemblages de combustible, les tubulures d\'entrée et de sortie.';

  @override
  String get wallpaperAtomMotion => 'Le réacteur';

  @override
  String get wallpaperAtomMotionText =>
      'Les barres de commande sortent du cœur d\'autant que de batterie, et le cœur brille d\'autant plus ; l\'indicateur de chaque mécanisme allume un segment par dixième. Des fissions s\'allument dans les crayons et en allument d\'autres. L\'eau entre froide, descend le long de la cuve, traverse le cœur en s\'éclairant et ressort chaude. En charge, les bobines des mécanismes s\'allument tour à tour et le débit s\'accélère.';

  @override
  String get themeAtomDescription => 'La cuve d\'un réacteur, jaune.';

  @override
  String get themeSoundAtomRing => 'Réaction en chaîne';

  @override
  String get themeSoundAtomRingText =>
      'Des clics qui s\'accélèrent jusqu\'au grondement';

  @override
  String get themeSoundAtomNotification => 'Neutron';

  @override
  String get themeSoundAtomNotificationText => 'Un clic, puis une note claire';

  @override
  String get themeSoundAtomAlarm => 'Criticité';

  @override
  String get themeSoundAtomAlarmText =>
      'La sirène à deux tons de la salle de contrôle';

  @override
  String get wallpaperVaultTitle => 'Fallout';

  @override
  String get wallpaperVaultDescription =>
      'Tout le téléphone devient le Pip-Boy : son boîtier, ses verrous, son compteur Geiger et sa molette, et le grand écran cathodique vert qui fait défiler STAT, INV, DATA, MAP et RADIO.';

  @override
  String get wallpaperVaultMotion => 'Le Pip-Boy';

  @override
  String get wallpaperVaultMotionText =>
      'Toutes les 7 secondes, un parasite et l\'onglet suivant : le Vault Boy qui arrive en rebondissant, un Stimpak qui tourne en fil de fer, la vraie date (en 2287) et l\'heure, la carte et sa boussole, l\'oscilloscope de la radio. HEALTH suit la batterie ; en charge, STIMPAK s\'allume et la barre se remplit en vague. Avec du réseau, le compteur Geiger s\'affole. À l\'allumage, l\'écran s\'ouvre comme un tube.';

  @override
  String get themeVaultDescription => 'Le Pip-Boy, vert phosphore.';

  @override
  String get themeSoundVaultRing => 'Porte de l\'abri';

  @override
  String get themeSoundVaultRingText =>
      'La sirène, le sifflement des vérins, puis le choc';

  @override
  String get themeSoundVaultNotification => 'Terminal';

  @override
  String get themeSoundVaultNotificationText =>
      'Trois frappes de clavier et un bip';

  @override
  String get themeSoundVaultAlarm => 'Compteur Geiger';

  @override
  String get themeSoundVaultAlarmText =>
      'Le crépitement d\'un compteur en zone chaude';

  @override
  String get wallpaperGhostTitle => 'Ghost in the Shell';

  @override
  String get wallpaperGhostDescription =>
      'Un cyber-cerveau dans sa coque, branché dans le téléphone à la place de la batterie.';

  @override
  String get wallpaperGhostMotion => 'Le cyber-cerveau';

  @override
  String get wallpaperGhostMotionText =>
      'Le ghost, une étincelle, erre de piste en piste dans le cerveau ; une prise de nuque s\'allume par quart de batterie ; une pluie de code tombe derrière, et en charge, c\'est la plongée : elle accélère.';

  @override
  String get themeGhostDescription =>
      'Un cyber-cerveau et sa pluie de code, turquoise.';

  @override
  String get themeSoundGhostRing => 'Plongée';

  @override
  String get themeSoundGhostRingText =>
      'Des tambours graves sous des voix lointaines';

  @override
  String get themeSoundGhostNotification => 'Ghost';

  @override
  String get themeSoundGhostNotificationText =>
      'Un souffle, puis une note qui s\'efface';

  @override
  String get themeSoundGhostAlarm => 'Synchronisation';

  @override
  String get themeSoundGhostAlarmText =>
      'Des tambours et des signaux de données qui montent';
}

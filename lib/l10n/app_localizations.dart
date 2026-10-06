import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_fr.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('fr'),
  ];

  /// Nom de l'application.
  ///
  /// In fr, this message translates to:
  /// **'Halo'**
  String get appTitle;

  /// Bouton retour de la barre d'application.
  ///
  /// In fr, this message translates to:
  /// **'Retour'**
  String get back;

  /// Bouton qui épingle un widget sur l'écran d'accueil.
  ///
  /// In fr, this message translates to:
  /// **'Ajouter à l\'écran d\'accueil'**
  String get pinToHome;

  /// Libellé d'accessibilité de l'aperçu d'un widget.
  ///
  /// In fr, this message translates to:
  /// **'Aperçu du widget {title}'**
  String widgetPreviewSemantics(String title);

  /// Bouton et titre des réglages.
  ///
  /// In fr, this message translates to:
  /// **'Réglages'**
  String get catalogSettings;

  /// Bouton et titre de « À propos ».
  ///
  /// In fr, this message translates to:
  /// **'À propos'**
  String get catalogAbout;

  /// Section des fonds d'écran animés du catalogue.
  ///
  /// In fr, this message translates to:
  /// **'Fonds d\'écran animés'**
  String get catalogWallpapers;

  /// Section des widgets du catalogue.
  ///
  /// In fr, this message translates to:
  /// **'Widgets'**
  String get catalogWidgets;

  /// No description provided for @catalogFilterLabel.
  ///
  /// In fr, this message translates to:
  /// **'Catégories de widgets'**
  String get catalogFilterLabel;

  /// No description provided for @catalogFilterAll.
  ///
  /// In fr, this message translates to:
  /// **'Tout'**
  String get catalogFilterAll;

  /// No description provided for @catalogFilterTime.
  ///
  /// In fr, this message translates to:
  /// **'Heure et agenda'**
  String get catalogFilterTime;

  /// No description provided for @catalogFilterWeather.
  ///
  /// In fr, this message translates to:
  /// **'Météo et ciel'**
  String get catalogFilterWeather;

  /// No description provided for @catalogFilterSystem.
  ///
  /// In fr, this message translates to:
  /// **'Système et appareils'**
  String get catalogFilterSystem;

  /// No description provided for @catalogFilterMedia.
  ///
  /// In fr, this message translates to:
  /// **'Médias et contrôles'**
  String get catalogFilterMedia;

  /// No description provided for @catalogActive.
  ///
  /// In fr, this message translates to:
  /// **'Actif'**
  String get catalogActive;

  /// No description provided for @catalogFeatured.
  ///
  /// In fr, this message translates to:
  /// **'À découvrir'**
  String get catalogFeatured;

  /// No description provided for @catalogSearch.
  ///
  /// In fr, this message translates to:
  /// **'Rechercher un widget'**
  String get catalogSearch;

  /// No description provided for @catalogSearchClose.
  ///
  /// In fr, this message translates to:
  /// **'Fermer la recherche'**
  String get catalogSearchClose;

  /// No description provided for @catalogChange.
  ///
  /// In fr, this message translates to:
  /// **'Changer'**
  String get catalogChange;

  /// No description provided for @catalogSeeAll.
  ///
  /// In fr, this message translates to:
  /// **'Tout voir'**
  String get catalogSeeAll;

  /// No description provided for @catalogWidgetCount.
  ///
  /// In fr, this message translates to:
  /// **'{count, plural, =1{1 widget} other{{count} widgets}}'**
  String catalogWidgetCount(int count);

  /// No description provided for @catalogNew.
  ///
  /// In fr, this message translates to:
  /// **'Nouveau'**
  String get catalogNew;

  /// No description provided for @catalogNavGallery.
  ///
  /// In fr, this message translates to:
  /// **'Galerie'**
  String get catalogNavGallery;

  /// No description provided for @catalogNavWallpapers.
  ///
  /// In fr, this message translates to:
  /// **'Fonds'**
  String get catalogNavWallpapers;

  /// No description provided for @catalogNavMine.
  ///
  /// In fr, this message translates to:
  /// **'Mes ajouts'**
  String get catalogNavMine;

  /// No description provided for @catalogWallpapersIntro.
  ///
  /// In fr, this message translates to:
  /// **'Ils réagissent à l\'inclinaison du téléphone. Touchez-en un pour le voir en grand et l\'appliquer.'**
  String get catalogWallpapersIntro;

  /// No description provided for @catalogMineWallpaper.
  ///
  /// In fr, this message translates to:
  /// **'Fond d\'écran'**
  String get catalogMineWallpaper;

  /// No description provided for @catalogMineNoWallpaper.
  ///
  /// In fr, this message translates to:
  /// **'Aucun fond Halo n\'est appliqué pour l\'instant.'**
  String get catalogMineNoWallpaper;

  /// No description provided for @catalogMineWidgets.
  ///
  /// In fr, this message translates to:
  /// **'Widgets posés'**
  String get catalogMineWidgets;

  /// No description provided for @catalogMineNoWidgets.
  ///
  /// In fr, this message translates to:
  /// **'Aucun widget Halo n\'est posé pour l\'instant. Touchez un widget dans la galerie pour l\'ajouter.'**
  String get catalogMineNoWidgets;

  /// No description provided for @catalogHeroApplied.
  ///
  /// In fr, this message translates to:
  /// **'Appliqué sur ce téléphone'**
  String get catalogHeroApplied;

  /// No description provided for @catalogNoMatch.
  ///
  /// In fr, this message translates to:
  /// **'Aucun widget ne correspond.'**
  String get catalogNoMatch;

  /// No description provided for @detailOnWallpaper.
  ///
  /// In fr, this message translates to:
  /// **'Sur {name}'**
  String detailOnWallpaper(String name);

  /// No description provided for @detailLockScreen.
  ///
  /// In fr, this message translates to:
  /// **'Verrouillage'**
  String get detailLockScreen;

  /// No description provided for @detailTagHome.
  ///
  /// In fr, this message translates to:
  /// **'Accueil'**
  String get detailTagHome;

  /// No description provided for @detailTagLock.
  ///
  /// In fr, this message translates to:
  /// **'Verrouillage'**
  String get detailTagLock;

  /// No description provided for @detailBackgrounds.
  ///
  /// In fr, this message translates to:
  /// **'Fond de l\'aperçu'**
  String get detailBackgrounds;

  /// Nom de l'écran de veille.
  ///
  /// In fr, this message translates to:
  /// **'Écran de veille'**
  String get dreamTitle;

  /// Description courte de l'écran de veille, dans le catalogue.
  ///
  /// In fr, this message translates to:
  /// **'Pendant la charge : une grande horloge néon sur le décor Circuit.'**
  String get dreamDescription;

  /// No description provided for @dreamOpenSettings.
  ///
  /// In fr, this message translates to:
  /// **'Ouvrir les réglages de l\'écran de veille'**
  String get dreamOpenSettings;

  /// No description provided for @dreamIntro.
  ///
  /// In fr, this message translates to:
  /// **'Quand le téléphone charge, ou est posé sur un socle, Android peut afficher un écran de veille. Celui de Halo montre l\'heure en grand sur le décor Circuit, avec la prochaine alarme, le prochain événement et la météo. Le texte se déplace un peu chaque minute pour ne pas marquer l\'écran ; un toucher réveille le téléphone.'**
  String get dreamIntro;

  /// No description provided for @dreamHowToTitle.
  ///
  /// In fr, this message translates to:
  /// **'Pour l\'activer'**
  String get dreamHowToTitle;

  /// No description provided for @dreamStep1.
  ///
  /// In fr, this message translates to:
  /// **'1. Dans les réglages qui s\'ouvrent, activez « Utiliser l\'écran de veille ».'**
  String get dreamStep1;

  /// No description provided for @dreamStep2.
  ///
  /// In fr, this message translates to:
  /// **'2. Choisissez « Halo » dans la liste.'**
  String get dreamStep2;

  /// No description provided for @dreamStep3.
  ///
  /// In fr, this message translates to:
  /// **'3. Dans « Quand l\'activer », choisissez « Pendant la charge » (ou sur un socle).'**
  String get dreamStep3;

  /// No description provided for @settingsPermissionsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Autorisations'**
  String get settingsPermissionsTitle;

  /// No description provided for @settingsPermissionsDescription.
  ///
  /// In fr, this message translates to:
  /// **'Halo ne demande que ce dont ses widgets ont besoin.'**
  String get settingsPermissionsDescription;

  /// L'autorisation d'accès à l'agenda.
  ///
  /// In fr, this message translates to:
  /// **'Agenda'**
  String get settingsCalendarTitle;

  /// No description provided for @settingsCalendarUse.
  ///
  /// In fr, this message translates to:
  /// **'Agendas et widget Mois, en lecture seule.'**
  String get settingsCalendarUse;

  /// No description provided for @settingsLocationTitle.
  ///
  /// In fr, this message translates to:
  /// **'Position approximative'**
  String get settingsLocationTitle;

  /// No description provided for @settingsLocationUse.
  ///
  /// In fr, this message translates to:
  /// **'Seulement pour « Utiliser ma position » dans Météo. Une ville choisie à la main suffit.'**
  String get settingsLocationUse;

  /// No description provided for @settingsBatteryTitle.
  ///
  /// In fr, this message translates to:
  /// **'Batterie'**
  String get settingsBatteryTitle;

  /// No description provided for @settingsBatteryUnrestricted.
  ///
  /// In fr, this message translates to:
  /// **'Halo n\'est pas limité par l\'optimisation de la batterie : ses widgets se mettent à jour à l\'heure.'**
  String get settingsBatteryUnrestricted;

  /// No description provided for @settingsBatteryRestricted.
  ///
  /// In fr, this message translates to:
  /// **'Android peut retarder les mises à jour des widgets pour économiser la batterie. Les exclure de cette optimisation les garde à l\'heure, pour un coût minime.'**
  String get settingsBatteryRestricted;

  /// No description provided for @settingsBatteryButton.
  ///
  /// In fr, this message translates to:
  /// **'Gérer l\'optimisation de la batterie'**
  String get settingsBatteryButton;

  /// No description provided for @settingsWeatherTitle.
  ///
  /// In fr, this message translates to:
  /// **'Données météo'**
  String get settingsWeatherTitle;

  /// No description provided for @settingsWeatherRefresh.
  ///
  /// In fr, this message translates to:
  /// **'Actualiser maintenant'**
  String get settingsWeatherRefresh;

  /// Pourquoi l'actualisation est indisponible.
  ///
  /// In fr, this message translates to:
  /// **'Aucun lieu choisi dans Météo'**
  String get settingsWeatherNoPlaceReason;

  /// Pourquoi l'actualisation est indisponible.
  ///
  /// In fr, this message translates to:
  /// **'Téléchargement en cours'**
  String get settingsWeatherDownloading;

  /// No description provided for @settingsWeatherUpToDate.
  ///
  /// In fr, this message translates to:
  /// **'Prévisions à jour.'**
  String get settingsWeatherUpToDate;

  /// No description provided for @settingsWeatherFailed.
  ///
  /// In fr, this message translates to:
  /// **'Échec du téléchargement : vérifiez la connexion.'**
  String get settingsWeatherFailed;

  /// No description provided for @settingsWeatherNoPlace.
  ///
  /// In fr, this message translates to:
  /// **'Aucun lieu choisi : ouvrez le widget Météo pour en choisir un.'**
  String get settingsWeatherNoPlace;

  /// No description provided for @settingsWeatherNoForecast.
  ///
  /// In fr, this message translates to:
  /// **'Lieu : {place}. Pas encore de prévisions.'**
  String settingsWeatherNoForecast(String place);

  /// Dernière mise à jour aujourd'hui ; {time} est déjà formatée.
  ///
  /// In fr, this message translates to:
  /// **'Lieu : {place}. Mises à jour à {time}, puis toutes les 30 minutes environ.'**
  String settingsWeatherUpdatedToday(String place, String time);

  /// Dernière mise à jour un autre jour.
  ///
  /// In fr, this message translates to:
  /// **'Lieu : {place}. Mises à jour le {day}/{month} à {time}, puis toutes les 30 minutes environ.'**
  String settingsWeatherUpdatedOn(
    String place,
    int day,
    int month,
    String time,
  );

  /// No description provided for @settingsAboutTitle.
  ///
  /// In fr, this message translates to:
  /// **'À propos de Halo'**
  String get settingsAboutTitle;

  /// No description provided for @settingsAboutSubtitle.
  ///
  /// In fr, this message translates to:
  /// **'Version, confidentialité, crédits, licences'**
  String get settingsAboutSubtitle;

  /// No description provided for @permissionManage.
  ///
  /// In fr, this message translates to:
  /// **'Gérer'**
  String get permissionManage;

  /// No description provided for @permissionAllow.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser'**
  String get permissionAllow;

  /// Libellé d'accessibilité du bouton d'une autorisation.
  ///
  /// In fr, this message translates to:
  /// **'{action} : {permission}'**
  String permissionActionSemantics(String action, String permission);

  /// Une autorisation (féminin).
  ///
  /// In fr, this message translates to:
  /// **'Accordée'**
  String get permissionGranted;

  /// Une autorisation (féminin).
  ///
  /// In fr, this message translates to:
  /// **'Non accordée'**
  String get permissionNotGranted;

  /// No description provided for @aboutIconSemantics.
  ///
  /// In fr, this message translates to:
  /// **'Icône de Halo'**
  String get aboutIconSemantics;

  /// No description provided for @aboutVersion.
  ///
  /// In fr, this message translates to:
  /// **'Version {version}'**
  String aboutVersion(String version);

  /// Version et numéro de build.
  ///
  /// In fr, this message translates to:
  /// **'{version} (build {build})'**
  String aboutVersionBuild(String version, int build);

  /// No description provided for @aboutHaloDescription.
  ///
  /// In fr, this message translates to:
  /// **'Des widgets d\'écran d\'accueil au style néon, aux couleurs de votre fond d\'écran : horloge, agendas, météo, pluie, allergies, système, et plus. Nom de code : WUX.'**
  String get aboutHaloDescription;

  /// No description provided for @aboutPrivacyTitle.
  ///
  /// In fr, this message translates to:
  /// **'Confidentialité'**
  String get aboutPrivacyTitle;

  /// No description provided for @aboutPrivacyDescription.
  ///
  /// In fr, this message translates to:
  /// **'Halo ne crée aucun compte, ne contient ni publicité ni mesure d\'audience, et n\'envoie rien à ses auteurs. Votre agenda, le temps d\'écran, la consommation de données, les appareils Bluetooth, vos contacts favoris, vos pas et votre sommeil (Santé Connect) sont lus sur le téléphone et n\'en sortent pas. Seules quelques requêtes quittent le téléphone : les coordonnées du lieu choisi, envoyées à Open-Meteo pour la météo, la pluie, les pollens, la qualité de l\'air et la mer ; le ping et, quand vous le lancez, le test de débit du widget Réseau, vers les serveurs de Cloudflare. La Lune et ses phases sont calculées sur le téléphone. Vos réglages restent sur le téléphone.'**
  String get aboutPrivacyDescription;

  /// No description provided for @aboutCreditsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Sources et crédits'**
  String get aboutCreditsTitle;

  /// No description provided for @aboutCreditWeatherTitle.
  ///
  /// In fr, this message translates to:
  /// **'Météo, pluie, pollens, qualité de l\'air, mer'**
  String get aboutCreditWeatherTitle;

  /// No description provided for @aboutCreditWeatherDetail.
  ///
  /// In fr, this message translates to:
  /// **'Open-Meteo.com, sous licence CC BY 4.0. Pollens et qualité de l\'air : Copernicus Atmosphere Monitoring Service (CAMS), modèle européen.'**
  String get aboutCreditWeatherDetail;

  /// No description provided for @aboutCreditCitiesTitle.
  ///
  /// In fr, this message translates to:
  /// **'Recherche de villes'**
  String get aboutCreditCitiesTitle;

  /// No description provided for @aboutCreditCitiesDetail.
  ///
  /// In fr, this message translates to:
  /// **'Géocodage Open-Meteo, données GeoNames (CC BY 4.0).'**
  String get aboutCreditCitiesDetail;

  /// No description provided for @aboutCreditIconsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Icônes'**
  String get aboutCreditIconsTitle;

  /// No description provided for @aboutCreditIconsDetail.
  ///
  /// In fr, this message translates to:
  /// **'Material Icons de Google, licence Apache 2.0.'**
  String get aboutCreditIconsDetail;

  /// No description provided for @aboutCreditInterfaceTitle.
  ///
  /// In fr, this message translates to:
  /// **'Interface'**
  String get aboutCreditInterfaceTitle;

  /// No description provided for @aboutCreditInterfaceDetail.
  ///
  /// In fr, this message translates to:
  /// **'Flutter, et IUX pour les composants accessibles de l\'application.'**
  String get aboutCreditInterfaceDetail;

  /// No description provided for @aboutCreditSourceTitle.
  ///
  /// In fr, this message translates to:
  /// **'Code source'**
  String get aboutCreditSourceTitle;

  /// No description provided for @aboutCreditSourceDetail.
  ///
  /// In fr, this message translates to:
  /// **'Halo est un logiciel libre, sous licence MIT : github.com/exec-d/halo.'**
  String get aboutCreditSourceDetail;

  /// No description provided for @aboutLicenses.
  ///
  /// In fr, this message translates to:
  /// **'Licences des logiciels utilisés'**
  String get aboutLicenses;

  /// No description provided for @accessTitle.
  ///
  /// In fr, this message translates to:
  /// **'Accès'**
  String get accessTitle;

  /// Un accès (masculin).
  ///
  /// In fr, this message translates to:
  /// **'Accordé'**
  String get accessGranted;

  /// Un accès (masculin).
  ///
  /// In fr, this message translates to:
  /// **'Non accordé'**
  String get accessNotGranted;

  /// No description provided for @accessOpenAppInfo.
  ///
  /// In fr, this message translates to:
  /// **'Ouvrir la fiche de Halo'**
  String get accessOpenAppInfo;

  /// No description provided for @accessRestrictedHelp.
  ///
  /// In fr, this message translates to:
  /// **'Si Android répond « L\'accès a été refusé à cette appli » : Halo, installé hors du Play Store, relève des paramètres restreints. Dans sa fiche, touchez ⋮ puis « Autoriser les paramètres restreints », confirmez, et revenez activer l\'accès.'**
  String get accessRestrictedHelp;

  /// No description provided for @accessBluetoothButton.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser « Appareils à proximité »'**
  String get accessBluetoothButton;

  /// No description provided for @accessBluetoothDescription.
  ///
  /// In fr, this message translates to:
  /// **'Halo lit le nom, le type et la batterie des appareils Bluetooth connectés. Rien ne quitte le téléphone.'**
  String get accessBluetoothDescription;

  /// No description provided for @accessNetworkButton.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser la position précise'**
  String get accessNetworkButton;

  /// No description provided for @accessNetworkDescription.
  ///
  /// In fr, this message translates to:
  /// **'Android ne donne le nom du Wi-Fi qu\'aux applis qui ont la position précise. Halo ne s\'en sert que pour ce nom : il ne lit ni ne garde votre position.'**
  String get accessNetworkDescription;

  /// No description provided for @accessMediaButton.
  ///
  /// In fr, this message translates to:
  /// **'Ouvrir l\'accès aux notifications'**
  String get accessMediaButton;

  /// No description provided for @accessMediaDescription.
  ///
  /// In fr, this message translates to:
  /// **'Android ne dit ce qui joue, et ne laisse le piloter, qu\'aux applis autorisées à « accéder aux notifications ». Halo n\'en lit aucune : il suit seulement la lecture. Rien ne quitte le téléphone.'**
  String get accessMediaDescription;

  /// No description provided for @accessTimerButton.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser les notifications'**
  String get accessTimerButton;

  /// No description provided for @accessTimerDescription.
  ///
  /// In fr, this message translates to:
  /// **'À la fin d\'un minuteur, Halo sonne et affiche une notification. Sans cette autorisation, le widget indique seulement « Minuteur terminé ».'**
  String get accessTimerDescription;

  /// No description provided for @accessUsageButton.
  ///
  /// In fr, this message translates to:
  /// **'Ouvrir l\'accès aux données d\'utilisation'**
  String get accessUsageButton;

  /// No description provided for @accessUsageDescription.
  ///
  /// In fr, this message translates to:
  /// **'Android réserve la durée d\'utilisation des applis et la consommation de données aux applis autorisées dans ses réglages : activez Halo dans la liste. Rien ne quitte le téléphone.'**
  String get accessUsageDescription;

  /// No description provided for @mobileDataPlanTitle.
  ///
  /// In fr, this message translates to:
  /// **'Forfait'**
  String get mobileDataPlanTitle;

  /// No description provided for @mobileDataIncluded.
  ///
  /// In fr, this message translates to:
  /// **'Données incluses'**
  String get mobileDataIncluded;

  /// No description provided for @mobileDataIncludedHelp.
  ///
  /// In fr, this message translates to:
  /// **'Le widget trace le rythme qui mène pile au forfait.'**
  String get mobileDataIncludedHelp;

  /// No description provided for @mobileDataNoPlan.
  ///
  /// In fr, this message translates to:
  /// **'Pas de forfait'**
  String get mobileDataNoPlan;

  /// Taille de forfait, en gigaoctets.
  ///
  /// In fr, this message translates to:
  /// **'{gb} Go'**
  String mobileDataGigabytes(int gb);

  /// No description provided for @mobileDataCycleDay.
  ///
  /// In fr, this message translates to:
  /// **'Jour de reprise'**
  String get mobileDataCycleDay;

  /// No description provided for @mobileDataCycleDayHelp.
  ///
  /// In fr, this message translates to:
  /// **'Le jour du mois où le forfait repart à zéro.'**
  String get mobileDataCycleDayHelp;

  /// No description provided for @mobileDataFirstOfMonth.
  ///
  /// In fr, this message translates to:
  /// **'1er du mois'**
  String get mobileDataFirstOfMonth;

  /// Jour de reprise du forfait, du 2 au 28.
  ///
  /// In fr, this message translates to:
  /// **'Le {day}'**
  String mobileDataDayOfMonth(int day);

  /// No description provided for @agendaDaysTitle.
  ///
  /// In fr, this message translates to:
  /// **'Jours'**
  String get agendaDaysTitle;

  /// No description provided for @agendaDaysShown.
  ///
  /// In fr, this message translates to:
  /// **'Jours affichés'**
  String get agendaDaysShown;

  /// No description provided for @agendaDaysHelp.
  ///
  /// In fr, this message translates to:
  /// **'À partir de 18 h, une journée terminée laisse place au lendemain.'**
  String get agendaDaysHelp;

  /// No description provided for @agendaToday.
  ///
  /// In fr, this message translates to:
  /// **'Aujourd\'hui'**
  String get agendaToday;

  /// No description provided for @agendaTodayTomorrow.
  ///
  /// In fr, this message translates to:
  /// **'Aujourd\'hui et demain'**
  String get agendaTodayTomorrow;

  /// No description provided for @agendaEventsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Événements'**
  String get agendaEventsTitle;

  /// No description provided for @agendaEventsDescription.
  ///
  /// In fr, this message translates to:
  /// **'L\'événement en cours est mis en avant, les suivants sont légèrement estompés.'**
  String get agendaEventsDescription;

  /// No description provided for @agendaEventsShown.
  ///
  /// In fr, this message translates to:
  /// **'Événements affichés'**
  String get agendaEventsShown;

  /// No description provided for @agendaAllDay.
  ///
  /// In fr, this message translates to:
  /// **'Toute la journée'**
  String get agendaAllDay;

  /// No description provided for @agendaAllDaySemantics.
  ///
  /// In fr, this message translates to:
  /// **'Afficher les événements toute la journée'**
  String get agendaAllDaySemantics;

  /// No description provided for @agendaAllDayHelp.
  ///
  /// In fr, this message translates to:
  /// **'Anniversaires, congés, jours fériés… et les événements sur plusieurs jours.'**
  String get agendaAllDayHelp;

  /// No description provided for @agendaCalendarsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Agendas'**
  String get agendaCalendarsTitle;

  /// No description provided for @agendaCalendarsShown.
  ///
  /// In fr, this message translates to:
  /// **'Agendas affichés'**
  String get agendaCalendarsShown;

  /// No description provided for @agendaPreviewPlaceholder.
  ///
  /// In fr, this message translates to:
  /// **'L\'aperçu apparaîtra une fois l\'agenda autorisé.'**
  String get agendaPreviewPlaceholder;

  /// No description provided for @agendaAllowAccess.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser l\'accès à l\'agenda'**
  String get agendaAllowAccess;

  /// No description provided for @agendaOpenSettings.
  ///
  /// In fr, this message translates to:
  /// **'Ouvrir les réglages'**
  String get agendaOpenSettings;

  /// No description provided for @agendaAccessTitle.
  ///
  /// In fr, this message translates to:
  /// **'Accès à l\'agenda'**
  String get agendaAccessTitle;

  /// No description provided for @agendaAccessRefused.
  ///
  /// In fr, this message translates to:
  /// **'L\'accès a été refusé. S\'il ne vous est plus proposé, activez « Agenda » dans les autorisations de l\'application.'**
  String get agendaAccessRefused;

  /// No description provided for @agendaAccessExplanation.
  ///
  /// In fr, this message translates to:
  /// **'Halo lit vos événements pour les afficher. Il ne les modifie jamais et ne les envoie nulle part.'**
  String get agendaAccessExplanation;

  /// No description provided for @countdownDay.
  ///
  /// In fr, this message translates to:
  /// **'Jour'**
  String get countdownDay;

  /// No description provided for @countdownMonth.
  ///
  /// In fr, this message translates to:
  /// **'Mois'**
  String get countdownMonth;

  /// No description provided for @countdownYear.
  ///
  /// In fr, this message translates to:
  /// **'Année'**
  String get countdownYear;

  /// No description provided for @countdownEventTitle.
  ///
  /// In fr, this message translates to:
  /// **'Évènement'**
  String get countdownEventTitle;

  /// No description provided for @countdownTitleLabel.
  ///
  /// In fr, this message translates to:
  /// **'Titre'**
  String get countdownTitleLabel;

  /// No description provided for @countdownTitleHelp.
  ///
  /// In fr, this message translates to:
  /// **'Par exemple « Vacances ».'**
  String get countdownTitleHelp;

  /// No description provided for @countdownDateLabel.
  ///
  /// In fr, this message translates to:
  /// **'Date'**
  String get countdownDateLabel;

  /// No description provided for @countdownDateHelp.
  ///
  /// In fr, this message translates to:
  /// **'Jour, mois et année.'**
  String get countdownDateHelp;

  /// No description provided for @countdownDateInvalid.
  ///
  /// In fr, this message translates to:
  /// **'Cette date n\'existe pas.'**
  String get countdownDateInvalid;

  /// No description provided for @weatherUseLocation.
  ///
  /// In fr, this message translates to:
  /// **'Utiliser ma position'**
  String get weatherUseLocation;

  /// No description provided for @weatherSearch.
  ///
  /// In fr, this message translates to:
  /// **'Rechercher'**
  String get weatherSearch;

  /// No description provided for @weatherSearching.
  ///
  /// In fr, this message translates to:
  /// **'Recherche en cours'**
  String get weatherSearching;

  /// No description provided for @weatherNoResult.
  ///
  /// In fr, this message translates to:
  /// **'Aucun lieu trouvé pour « {query} ».'**
  String weatherNoResult(String query);

  /// No description provided for @weatherPlaceTitle.
  ///
  /// In fr, this message translates to:
  /// **'Lieu'**
  String get weatherPlaceTitle;

  /// No description provided for @weatherNoPlace.
  ///
  /// In fr, this message translates to:
  /// **'Aucun lieu choisi pour le moment.'**
  String get weatherNoPlace;

  /// No description provided for @weatherForecastFor.
  ///
  /// In fr, this message translates to:
  /// **'Prévisions pour {place}.'**
  String weatherForecastFor(String place);

  /// No description provided for @weatherLocationFailed.
  ///
  /// In fr, this message translates to:
  /// **'La position n\'a pas pu être obtenue. Vérifiez que la localisation est activée et autorisée pour Halo.'**
  String get weatherLocationFailed;

  /// No description provided for @weatherCityLabel.
  ///
  /// In fr, this message translates to:
  /// **'Ville'**
  String get weatherCityLabel;

  /// No description provided for @weatherCityHelp.
  ///
  /// In fr, this message translates to:
  /// **'Ou cherchez une ville par son nom.'**
  String get weatherCityHelp;

  /// No description provided for @weatherPlaceUnavailable.
  ///
  /// In fr, this message translates to:
  /// **'Les prévisions de ce lieu ne sont pas disponibles.'**
  String get weatherPlaceUnavailable;

  /// No description provided for @worldClockCities.
  ///
  /// In fr, this message translates to:
  /// **'Villes'**
  String get worldClockCities;

  /// No description provided for @worldClockCity.
  ///
  /// In fr, this message translates to:
  /// **'Ville {number}'**
  String worldClockCity(int number);

  /// No description provided for @wallpaperApply.
  ///
  /// In fr, this message translates to:
  /// **'Appliquer le fond d\'écran'**
  String get wallpaperApply;

  /// No description provided for @wallpaperActive.
  ///
  /// In fr, this message translates to:
  /// **'Fond d\'écran actuel'**
  String get wallpaperActive;

  /// No description provided for @wallpaperNotApplied.
  ///
  /// In fr, this message translates to:
  /// **'Pas encore appliqué'**
  String get wallpaperNotApplied;

  /// No description provided for @wallpaperUnavailable.
  ///
  /// In fr, this message translates to:
  /// **'Ce téléphone ne propose pas l\'écran d\'application. Choisissez « Halo · {title} » dans Fond d\'écran et style, rubrique Fonds d\'écran animés.'**
  String wallpaperUnavailable(String title);

  /// No description provided for @wallpaperIntensityTitle.
  ///
  /// In fr, this message translates to:
  /// **'Intensité'**
  String get wallpaperIntensityTitle;

  /// No description provided for @wallpaperIntensityLabel.
  ///
  /// In fr, this message translates to:
  /// **'Intensité du fond'**
  String get wallpaperIntensityLabel;

  /// No description provided for @wallpaperIntensityHelp.
  ///
  /// In fr, this message translates to:
  /// **'Discret garde les widgets et les icônes bien lisibles par-dessus.'**
  String get wallpaperIntensityHelp;

  /// No description provided for @wallpaperIntensityDiscreet.
  ///
  /// In fr, this message translates to:
  /// **'Discret'**
  String get wallpaperIntensityDiscreet;

  /// No description provided for @wallpaperIntensityNormal.
  ///
  /// In fr, this message translates to:
  /// **'Normal'**
  String get wallpaperIntensityNormal;

  /// No description provided for @wallpaperIntensityVivid.
  ///
  /// In fr, this message translates to:
  /// **'Vif'**
  String get wallpaperIntensityVivid;

  /// No description provided for @wallpaperFeaturesTitle.
  ///
  /// In fr, this message translates to:
  /// **'Ce qui bouge'**
  String get wallpaperFeaturesTitle;

  /// No description provided for @wallpaperBatteryTitle.
  ///
  /// In fr, this message translates to:
  /// **'Batterie'**
  String get wallpaperBatteryTitle;

  /// No description provided for @wallpaperPreviewSemantics.
  ///
  /// In fr, this message translates to:
  /// **'Aperçu du fond d\'écran {title}'**
  String wallpaperPreviewSemantics(String title);

  /// No description provided for @wallpaperCircuitTitle.
  ///
  /// In fr, this message translates to:
  /// **'Circuit'**
  String get wallpaperCircuitTitle;

  /// No description provided for @wallpaperCircuitDescription.
  ///
  /// In fr, this message translates to:
  /// **'L\'intérieur du téléphone en néon, aux couleurs du téléphone.'**
  String get wallpaperCircuitDescription;

  /// No description provided for @wallpaperCircuitTiltTitle.
  ///
  /// In fr, this message translates to:
  /// **'Inclinaison'**
  String get wallpaperCircuitTiltTitle;

  /// No description provided for @wallpaperCircuitTiltText.
  ///
  /// In fr, this message translates to:
  /// **'Les plans du téléphone glissent quand vous le penchez, et le reflet du verre suit.'**
  String get wallpaperCircuitTiltText;

  /// No description provided for @wallpaperCircuitBatteryTitle.
  ///
  /// In fr, this message translates to:
  /// **'Vraie batterie'**
  String get wallpaperCircuitBatteryTitle;

  /// No description provided for @wallpaperCircuitBatteryText.
  ///
  /// In fr, this message translates to:
  /// **'La batterie dessinée affiche le niveau réel et respire pendant la charge.'**
  String get wallpaperCircuitBatteryText;

  /// No description provided for @wallpaperCircuitNetworkTitle.
  ///
  /// In fr, this message translates to:
  /// **'Réseau'**
  String get wallpaperCircuitNetworkTitle;

  /// No description provided for @wallpaperCircuitNetworkText.
  ///
  /// In fr, this message translates to:
  /// **'Des impulsions courent de l\'antenne au processeur quand des données passent ; les antennes brillent selon la force du signal.'**
  String get wallpaperCircuitNetworkText;

  /// No description provided for @wallpaperCircuitWakeTitle.
  ///
  /// In fr, this message translates to:
  /// **'Allumage'**
  String get wallpaperCircuitWakeTitle;

  /// No description provided for @wallpaperCircuitWakeText.
  ///
  /// In fr, this message translates to:
  /// **'À chaque allumage de l\'écran, le fond est là tout de suite, puis composants et pistes s\'illuminent un à un depuis le processeur.'**
  String get wallpaperCircuitWakeText;

  /// No description provided for @wallpaperCircuitBattery.
  ///
  /// In fr, this message translates to:
  /// **'L\'animation s\'arrête dès que le fond n\'est plus visible, et ne tourne en continu que pendant un mouvement, une impulsion ou une charge.'**
  String get wallpaperCircuitBattery;

  /// No description provided for @wallpaperFluxTitle.
  ///
  /// In fr, this message translates to:
  /// **'Retour vers le futur'**
  String get wallpaperFluxTitle;

  /// No description provided for @wallpaperFluxDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le convecteur temporel de la DeLorean, installé dans le téléphone à la place de la batterie.'**
  String get wallpaperFluxDescription;

  /// No description provided for @wallpaperFluxMotion.
  ///
  /// In fr, this message translates to:
  /// **'Le convecteur'**
  String get wallpaperFluxMotion;

  /// No description provided for @wallpaperFluxMotionText.
  ///
  /// In fr, this message translates to:
  /// **'Les impulsions courent le long des trois bras jusqu\'au cœur, qui s\'illumine ; plus vite pendant la charge. Au-dessus du hublot, la jauge montre le niveau de batterie ; sur la carte mère, les circuits temporels affichent l\'heure.'**
  String get wallpaperFluxMotionText;

  /// No description provided for @wallpaperFluxBattery.
  ///
  /// In fr, this message translates to:
  /// **'L\'animation s\'arrête dès que le fond n\'est plus visible ; à l\'écran, le convecteur tourne sans cesse, à cadence réduite.'**
  String get wallpaperFluxBattery;

  /// No description provided for @wallpaperArcTitle.
  ///
  /// In fr, this message translates to:
  /// **'Iron Man'**
  String get wallpaperArcTitle;

  /// No description provided for @wallpaperArcDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le réacteur arc de Tony Stark, installé dans le téléphone à la place de la batterie.'**
  String get wallpaperArcDescription;

  /// No description provided for @wallpaperArcMotion.
  ///
  /// In fr, this message translates to:
  /// **'Le réacteur'**
  String get wallpaperArcMotion;

  /// No description provided for @wallpaperArcMotionText.
  ///
  /// In fr, this message translates to:
  /// **'À l\'allumage, les bobines s\'allument une à une puis le cœur s\'embrase. Une bobine par dixième de batterie, deux pistes d\'énergie qui tournent, un cœur qui bat ; en charge, des particules spiralent vers lui, et sous 15 % il vacille.'**
  String get wallpaperArcMotionText;

  /// No description provided for @wallpaperArcBattery.
  ///
  /// In fr, this message translates to:
  /// **'L\'animation s\'arrête dès que le fond n\'est plus visible ; à l\'écran, le réacteur tourne sans cesse, à cadence réduite.'**
  String get wallpaperArcBattery;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Horloge'**
  String get widgetClockTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'L\'heure et la date. Touchez-le pour ouvrir l\'horloge.'**
  String get widgetClockDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Agenda'**
  String get widgetOneColumnAgendaTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Les événements du jour, en une colonne.'**
  String get widgetOneColumnAgendaDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Agenda 2 colonnes'**
  String get widgetTwoColumnAgendaTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Les événements du jour, en deux colonnes.'**
  String get widgetTwoColumnAgendaDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Système'**
  String get widgetSystemTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Batterie, réseau et stockage, sur une rangée.'**
  String get widgetSystemDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Système avancé'**
  String get widgetAdvancedSystemTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Signal, Wi-Fi, Bluetooth, batterie, mémoire, stockage, localisation et son, sur deux rangées.'**
  String get widgetAdvancedSystemDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Batterie détaillée'**
  String get widgetBatteryTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'La courbe des 24 dernières heures et la suite prévue, la température, la tension et les cycles.'**
  String get widgetBatteryDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Appareil'**
  String get widgetDeviceTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'La fiche du téléphone façon console, et depuis combien de temps il tourne, à la seconde.'**
  String get widgetDeviceDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Écouteurs et montre'**
  String get widgetBluetoothDevicesTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'La batterie des appareils Bluetooth connectés.'**
  String get widgetBluetoothDevicesDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Temps d\'écran'**
  String get widgetScreenTimeTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'La journée en cadran de 24 heures : quand l\'écran était allumé, les déverrouillages et les applis les plus utilisées.'**
  String get widgetScreenTimeDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Données mobiles'**
  String get widgetMobileDataTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'La consommation de la période face au forfait, avec la projection en fin de période.'**
  String get widgetMobileDataDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Prévisions 5 jours'**
  String get widgetForecastTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Cinq jours de météo, et une capsule qui place chacun dans la semaine, du plus frais au plus chaud.'**
  String get widgetForecastDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Horloge analogique'**
  String get widgetAnalogClockTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Un cadran néon aux aiguilles lumineuses, et la date.'**
  String get widgetAnalogClockDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Éphéméride'**
  String get widgetEphemerisTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'La fête du jour, la semaine, l\'année en douze mois et le prochain jour férié.'**
  String get widgetEphemerisDescription;

  /// No description provided for @widgetProgressTitle.
  ///
  /// In fr, this message translates to:
  /// **'Progression'**
  String get widgetProgressTitle;

  /// No description provided for @widgetProgressDescription.
  ///
  /// In fr, this message translates to:
  /// **'La part écoulée du jour, de la semaine, du mois et de l\'année, en quatre barres.'**
  String get widgetProgressDescription;

  /// No description provided for @widgetNetworkTitle.
  ///
  /// In fr, this message translates to:
  /// **'Réseau'**
  String get widgetNetworkTitle;

  /// No description provided for @widgetNetworkDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le Wi-Fi ou le réseau mobile en cours, la force du signal, le ping et l\'adresse locale. Le test de débit (une dizaine de secondes, quelques dizaines de Mo) ne part qu\'au toucher de « Tester ».'**
  String get widgetNetworkDescription;

  /// No description provided for @widgetHealthTitle.
  ///
  /// In fr, this message translates to:
  /// **'Pas et sommeil'**
  String get widgetHealthTitle;

  /// No description provided for @widgetHealthDescription.
  ///
  /// In fr, this message translates to:
  /// **'Les pas du jour vers votre objectif, la dernière nuit et ses phases, et les pas des sept derniers jours, par Santé Connect.'**
  String get widgetHealthDescription;

  /// No description provided for @accessHealthButton.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser Santé Connect'**
  String get accessHealthButton;

  /// No description provided for @accessHealthDescription.
  ///
  /// In fr, this message translates to:
  /// **'Halo lit vos pas et votre sommeil dans Santé Connect, y compris en arrière-plan pour que le widget reste à jour. Il n\'écrit rien et rien ne quitte le téléphone.'**
  String get accessHealthDescription;

  /// No description provided for @healthGoalTitle.
  ///
  /// In fr, this message translates to:
  /// **'Objectif'**
  String get healthGoalTitle;

  /// No description provided for @healthGoal.
  ///
  /// In fr, this message translates to:
  /// **'Pas par jour'**
  String get healthGoal;

  /// No description provided for @healthGoalHelp.
  ///
  /// In fr, this message translates to:
  /// **'L\'anneau du widget se remplit vers cet objectif.'**
  String get healthGoalHelp;

  /// No description provided for @healthGoalSteps.
  ///
  /// In fr, this message translates to:
  /// **'{steps} pas'**
  String healthGoalSteps(int steps);

  /// No description provided for @widgetContactsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Contacts favoris'**
  String get widgetContactsTitle;

  /// No description provided for @widgetContactsDescription.
  ///
  /// In fr, this message translates to:
  /// **'Jusqu\'à six contacts en pastilles néon ; un toucher pour appeler ou écrire.'**
  String get widgetContactsDescription;

  /// No description provided for @contactsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Contacts'**
  String get contactsTitle;

  /// No description provided for @contactsDescription.
  ///
  /// In fr, this message translates to:
  /// **'Choisissez-les avec le sélecteur d\'Android : Halo ne voit que le contact touché, pas votre carnet d\'adresses.'**
  String get contactsDescription;

  /// No description provided for @contactsAdd.
  ///
  /// In fr, this message translates to:
  /// **'Ajouter un contact'**
  String get contactsAdd;

  /// No description provided for @contactsFull.
  ///
  /// In fr, this message translates to:
  /// **'Six contacts : retirez-en un pour en ajouter un autre.'**
  String get contactsFull;

  /// No description provided for @contactsActionTitle.
  ///
  /// In fr, this message translates to:
  /// **'Au toucher'**
  String get contactsActionTitle;

  /// No description provided for @contactsSms.
  ///
  /// In fr, this message translates to:
  /// **'Écrire un SMS'**
  String get contactsSms;

  /// No description provided for @contactsSmsHelp.
  ///
  /// In fr, this message translates to:
  /// **'Sinon, un toucher appelle le contact.'**
  String get contactsSmsHelp;

  /// No description provided for @contactsCallHelp.
  ///
  /// In fr, this message translates to:
  /// **'Sans autorisation, un toucher ouvre le clavier avec le numéro : il reste à appuyer sur « Appeler ».'**
  String get contactsCallHelp;

  /// No description provided for @contactsCallButton.
  ///
  /// In fr, this message translates to:
  /// **'Appeler directement'**
  String get contactsCallButton;

  /// No description provided for @contactsRemove.
  ///
  /// In fr, this message translates to:
  /// **'Retirer {name}'**
  String contactsRemove(String name);

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Chronomètre et minuteur'**
  String get widgetTimerTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Un chronomètre, et un minuteur de 1, 5, 10 ou 25 minutes d\'un toucher, qui sonne à la fin.'**
  String get widgetTimerDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Lecture en cours'**
  String get widgetMediaTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Ce qui joue, sa pochette en néon, et précédent, lecture ou pause, suivant.'**
  String get widgetMediaDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Fuseaux horaires'**
  String get widgetWorldClockTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'L\'heure de trois villes, sur une rangée.'**
  String get widgetWorldClockDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Compte à rebours'**
  String get widgetCountdownTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Les jours jusqu\'à une date qui compte.'**
  String get widgetCountdownDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Contrôles'**
  String get widgetControlsTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Lampe torche en un geste ; Wi-Fi, Bluetooth, son et appareil photo à portée de doigt.'**
  String get widgetControlsDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Mois'**
  String get widgetMonthTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Le mois en cours ; un point sous chaque jour qui a un événement. Il lit l\'agenda : autorisez-le depuis un widget Agenda.'**
  String get widgetMonthDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Météo'**
  String get widgetWeatherTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Façon tableau de bord : température et jauge du jour, ressenti, vent, humidité, UV, pluie, et la courbe des 24 heures. Open-Meteo, chaque heure. Sur une rangée, une seule ligne.'**
  String get widgetWeatherDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Soleil et Lune'**
  String get widgetSunMoonTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Lever et coucher du soleil au lieu choisi dans Météo, durée du jour et phase de la lune.'**
  String get widgetSunMoonDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Pluie'**
  String get widgetRainTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Si la pluie arrive et quand, puis les probabilités des 12 prochaines heures, au lieu choisi dans Météo.'**
  String get widgetRainDescription;

  /// No description provided for @widgetSeaTitle.
  ///
  /// In fr, this message translates to:
  /// **'Mer et vagues'**
  String get widgetSeaTitle;

  /// No description provided for @widgetSeaDescription.
  ///
  /// In fr, this message translates to:
  /// **'Au plus près du lieu de Météo : la hauteur, la période et la direction des vagues, la température de l\'eau et les vagues des 24 prochaines heures.'**
  String get widgetSeaDescription;

  /// No description provided for @widgetSkyTitle.
  ///
  /// In fr, this message translates to:
  /// **'Lune'**
  String get widgetSkyTitle;

  /// No description provided for @widgetSkyDescription.
  ///
  /// In fr, this message translates to:
  /// **'La Lune dans sa phase du moment, dessinée avec ses mers : le compte à rebours jusqu\'à la pleine lune, son lever et son coucher au lieu de Météo, et les quatre phases à venir. Calculé sur le téléphone.'**
  String get widgetSkyDescription;

  /// Nom du widget, dans le catalogue et en titre de son écran.
  ///
  /// In fr, this message translates to:
  /// **'Allergies'**
  String get widgetAllergyTitle;

  /// Description du widget.
  ///
  /// In fr, this message translates to:
  /// **'Graminées, bouleau, aulne, olivier, armoise, ambroisie et qualité de l\'air, au lieu choisi dans Météo. Pollens : Europe seulement.'**
  String get widgetAllergyDescription;

  /// No description provided for @catalogThemes.
  ///
  /// In fr, this message translates to:
  /// **'Thèmes'**
  String get catalogThemes;

  /// No description provided for @catalogThemesHint.
  ///
  /// In fr, this message translates to:
  /// **'Un fond, ses couleurs, des widgets assortis et des sons, en un toucher.'**
  String get catalogThemesHint;

  /// No description provided for @themesTitle.
  ///
  /// In fr, this message translates to:
  /// **'Thèmes'**
  String get themesTitle;

  /// No description provided for @themesIntro.
  ///
  /// In fr, this message translates to:
  /// **'Les packs de thème des Pixel sont réservés à Google. Ceux de Halo appliquent ensemble un fond animé, ses couleurs, des widgets assortis et des sons originaux.'**
  String get themesIntro;

  /// No description provided for @themesContents.
  ///
  /// In fr, this message translates to:
  /// **'Fond · couleurs · widgets · 3 sons'**
  String get themesContents;

  /// No description provided for @themeCircuitDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le téléphone en schéma néon, bleu électrique.'**
  String get themeCircuitDescription;

  /// No description provided for @themeFluxDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le convecteur temporel, orange et ambre.'**
  String get themeFluxDescription;

  /// No description provided for @themeArcDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le réacteur arc, rouge et or.'**
  String get themeArcDescription;

  /// No description provided for @themeApply.
  ///
  /// In fr, this message translates to:
  /// **'Appliquer le thème'**
  String get themeApply;

  /// No description provided for @themeActive.
  ///
  /// In fr, this message translates to:
  /// **'Thème actif'**
  String get themeActive;

  /// No description provided for @themeNotApplied.
  ///
  /// In fr, this message translates to:
  /// **'Pas encore appliqué'**
  String get themeNotApplied;

  /// No description provided for @themeUnavailable.
  ///
  /// In fr, this message translates to:
  /// **'Ce téléphone ne propose pas l\'écran d\'application. Choisissez « Halo · {title} » dans Fond d\'écran et style, rubrique Fonds d\'écran animés.'**
  String themeUnavailable(String title);

  /// No description provided for @themePreviewSemantics.
  ///
  /// In fr, this message translates to:
  /// **'Aperçu du thème {title}'**
  String themePreviewSemantics(String title);

  /// No description provided for @themeColorsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Couleurs'**
  String get themeColorsTitle;

  /// No description provided for @themeColorsText.
  ///
  /// In fr, this message translates to:
  /// **'Le fond annonce ces couleurs à Android, qui les applique quand vous confirmez le fond (sinon, à la prochaine mise en veille de l\'écran). Il faut que Couleurs soit réglé sur une couleur du fond d\'écran dans Fond d\'écran et style ; le système et les widgets Halo les prennent alors.'**
  String get themeColorsText;

  /// No description provided for @themeColorsOpen.
  ///
  /// In fr, this message translates to:
  /// **'Ouvrir Fond d\'écran et style'**
  String get themeColorsOpen;

  /// No description provided for @themeSoundsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Sons'**
  String get themeSoundsTitle;

  /// No description provided for @themeSoundsText.
  ///
  /// In fr, this message translates to:
  /// **'Des sons originaux, synthétisés. Choisissez ceux que le thème remplace ; ils restent ensuite au choix dans les sons du téléphone.'**
  String get themeSoundsText;

  /// No description provided for @themeSoundsPermission.
  ///
  /// In fr, this message translates to:
  /// **'La première fois, Android demande l\'autorisation « Modifier les paramètres système » : activez-la pour Halo, puis revenez.'**
  String get themeSoundsPermission;

  /// No description provided for @themeSoundsAllow.
  ///
  /// In fr, this message translates to:
  /// **'Autoriser les sons'**
  String get themeSoundsAllow;

  /// No description provided for @themeSoundsUnsupported.
  ///
  /// In fr, this message translates to:
  /// **'Les sons demandent Android 10 ou plus.'**
  String get themeSoundsUnsupported;

  /// No description provided for @themeSoundsApplied.
  ///
  /// In fr, this message translates to:
  /// **'Sons réglés : {list}.'**
  String themeSoundsApplied(String list);

  /// No description provided for @themeSoundsWaiting.
  ///
  /// In fr, this message translates to:
  /// **'Sons en attente de l\'autorisation.'**
  String get themeSoundsWaiting;

  /// No description provided for @themeSoundRing.
  ///
  /// In fr, this message translates to:
  /// **'Sonnerie'**
  String get themeSoundRing;

  /// No description provided for @themeSoundNotification.
  ///
  /// In fr, this message translates to:
  /// **'Notification'**
  String get themeSoundNotification;

  /// No description provided for @themeSoundAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Alarme'**
  String get themeSoundAlarm;

  /// No description provided for @themeSoundPlay.
  ///
  /// In fr, this message translates to:
  /// **'Écouter {name}'**
  String themeSoundPlay(String name);

  /// No description provided for @themeSoundCircuitRing.
  ///
  /// In fr, this message translates to:
  /// **'Bus de données'**
  String get themeSoundCircuitRing;

  /// No description provided for @themeSoundCircuitRingText.
  ///
  /// In fr, this message translates to:
  /// **'Des octets qui montent et descendent la carte'**
  String get themeSoundCircuitRingText;

  /// No description provided for @themeSoundCircuitNotification.
  ///
  /// In fr, this message translates to:
  /// **'Impulsion'**
  String get themeSoundCircuitNotification;

  /// No description provided for @themeSoundCircuitNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Une impulsion file vers le processeur'**
  String get themeSoundCircuitNotificationText;

  /// No description provided for @themeSoundCircuitAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Démarrage'**
  String get themeSoundCircuitAlarm;

  /// No description provided for @themeSoundCircuitAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Les composants s\'allument un à un'**
  String get themeSoundCircuitAlarmText;

  /// No description provided for @themeSoundFluxRing.
  ///
  /// In fr, this message translates to:
  /// **'88 mph'**
  String get themeSoundFluxRing;

  /// No description provided for @themeSoundFluxRingText.
  ///
  /// In fr, this message translates to:
  /// **'Le moteur monte, le flux crépite, puis le saut'**
  String get themeSoundFluxRingText;

  /// No description provided for @themeSoundFluxNotification.
  ///
  /// In fr, this message translates to:
  /// **'Flux'**
  String get themeSoundFluxNotification;

  /// No description provided for @themeSoundFluxNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Trois éclairs qui se rejoignent au cœur'**
  String get themeSoundFluxNotificationText;

  /// No description provided for @themeSoundFluxAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Heure de départ'**
  String get themeSoundFluxAlarm;

  /// No description provided for @themeSoundFluxAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Les bips des circuits temporels'**
  String get themeSoundFluxAlarmText;

  /// No description provided for @themeSoundArcRing.
  ///
  /// In fr, this message translates to:
  /// **'Répulseur'**
  String get themeSoundArcRing;

  /// No description provided for @themeSoundArcRingText.
  ///
  /// In fr, this message translates to:
  /// **'La charge siffle, puis le tir'**
  String get themeSoundArcRingText;

  /// No description provided for @themeSoundArcNotification.
  ///
  /// In fr, this message translates to:
  /// **'Interface'**
  String get themeSoundArcNotification;

  /// No description provided for @themeSoundArcNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Deux tons cristallins de l\'affichage tête haute'**
  String get themeSoundArcNotificationText;

  /// No description provided for @themeSoundArcAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Réacteur'**
  String get themeSoundArcAlarm;

  /// No description provided for @themeSoundArcAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Le cœur bat, de plus en plus fort'**
  String get themeSoundArcAlarmText;

  /// No description provided for @themeLimitsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Ce que Halo ne peut pas changer'**
  String get themeLimitsTitle;

  /// No description provided for @themeLimitsText.
  ///
  /// In fr, this message translates to:
  /// **'Le lanceur Pixel refuse les packs d\'icônes, l\'horloge de l\'écran verrouillé est celle de Google et Gboard n\'a pas de thème pour les applications. Les icônes à thème et l\'horloge suivent quand même les couleurs du thème.'**
  String get themeLimitsText;

  /// No description provided for @themeColorsPreset.
  ///
  /// In fr, this message translates to:
  /// **'Couleurs est réglé sur une couleur de base : le système ignore le fond. Choisissez une couleur du fond d\'écran.'**
  String get themeColorsPreset;

  /// No description provided for @wallpaperCoreBattery.
  ///
  /// In fr, this message translates to:
  /// **'L\'animation s\'arrête dès que le fond n\'est plus visible ; à l\'écran, elle tourne à pleine cadence 30 secondes après l\'allumage ou un geste sur l\'accueil, puis ralentit jusqu\'au geste suivant.'**
  String get wallpaperCoreBattery;

  /// No description provided for @wallpaperQuantumTitle.
  ///
  /// In fr, this message translates to:
  /// **'Physique quantique'**
  String get wallpaperQuantumTitle;

  /// No description provided for @wallpaperQuantumDescription.
  ///
  /// In fr, this message translates to:
  /// **'Tout le téléphone devient le bas du lustre d\'un ordinateur quantique : la forêt de câbles coaxiaux et leurs boucles, le plateau doré, les colonnes de cuivre couvertes de connecteurs et la puce au centre.'**
  String get wallpaperQuantumDescription;

  /// No description provided for @wallpaperQuantumMotion.
  ///
  /// In fr, this message translates to:
  /// **'Le lustre'**
  String get wallpaperQuantumMotion;

  /// No description provided for @wallpaperQuantumMotionText.
  ///
  /// In fr, this message translates to:
  /// **'Les rangées de connecteurs des colonnes s\'allument de bas en haut, une par dixième de batterie. Des impulsions de commande descendent les câbles jusqu\'à la puce et la lecture remonte ; la puce bat et ses qubits scintillent. En charge, une vague de froid descend les boucles des câbles.'**
  String get wallpaperQuantumMotionText;

  /// No description provided for @themeQuantumDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le lustre d\'un ordinateur quantique, violet.'**
  String get themeQuantumDescription;

  /// No description provided for @themeSoundQuantumRing.
  ///
  /// In fr, this message translates to:
  /// **'Superposition'**
  String get themeSoundQuantumRing;

  /// No description provided for @themeSoundQuantumRingText.
  ///
  /// In fr, this message translates to:
  /// **'Deux tons qui battent, puis une gamme qui monte et redescend'**
  String get themeSoundQuantumRingText;

  /// No description provided for @themeSoundQuantumNotification.
  ///
  /// In fr, this message translates to:
  /// **'Intrication'**
  String get themeSoundQuantumNotification;

  /// No description provided for @themeSoundQuantumNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Deux notes qui sonnent ensemble, deux fois'**
  String get themeSoundQuantumNotificationText;

  /// No description provided for @themeSoundQuantumAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Effondrement'**
  String get themeSoundQuantumAlarm;

  /// No description provided for @themeSoundQuantumAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Un souffle qui se resserre jusqu\'à une note pure'**
  String get themeSoundQuantumAlarmText;

  /// No description provided for @wallpaperNeuralTitle.
  ///
  /// In fr, this message translates to:
  /// **'Intelligence artificielle'**
  String get wallpaperNeuralTitle;

  /// No description provided for @wallpaperNeuralDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le téléphone de Circuit, détaillé jusqu\'aux vias, avec à la place de la batterie un accélérateur d\'IA : son boîtier, quatre piles de mémoire et, sur la puce, un cerveau gravé en pistes.'**
  String get wallpaperNeuralDescription;

  /// No description provided for @wallpaperNeuralMotion.
  ///
  /// In fr, this message translates to:
  /// **'L\'inférence'**
  String get wallpaperNeuralMotion;

  /// No description provided for @wallpaperNeuralMotionText.
  ///
  /// In fr, this message translates to:
  /// **'En boucle, les mémoires sont lues couche après couche, les données filent jusqu\'au cerveau, une vague part de sa ligne médiane jusqu\'aux plots, puis la réponse remonte par les nappes et les cœurs du processeur s\'allument. Les plots allumés suivent la batterie. En charge, les bobines s\'éclairent, la ligne médiane pulse et les inférences s\'accélèrent.'**
  String get wallpaperNeuralMotionText;

  /// No description provided for @themeNeuralDescription.
  ///
  /// In fr, this message translates to:
  /// **'Un cerveau de silicium sur sa carte, bleu électrique.'**
  String get themeNeuralDescription;

  /// No description provided for @themeSoundNeuralRing.
  ///
  /// In fr, this message translates to:
  /// **'Inférence'**
  String get themeSoundNeuralRing;

  /// No description provided for @themeSoundNeuralRingText.
  ///
  /// In fr, this message translates to:
  /// **'Une suite de notes rapides, comme une pensée qui se forme'**
  String get themeSoundNeuralRingText;

  /// No description provided for @themeSoundNeuralNotification.
  ///
  /// In fr, this message translates to:
  /// **'Jeton'**
  String get themeSoundNeuralNotification;

  /// No description provided for @themeSoundNeuralNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Deux petits sons secs'**
  String get themeSoundNeuralNotificationText;

  /// No description provided for @themeSoundNeuralAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Éveil'**
  String get themeSoundNeuralAlarm;

  /// No description provided for @themeSoundNeuralAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Un accord qui se construit couche par couche'**
  String get themeSoundNeuralAlarmText;

  /// No description provided for @wallpaperAtomTitle.
  ///
  /// In fr, this message translates to:
  /// **'Énergie atomique'**
  String get wallpaperAtomTitle;

  /// No description provided for @wallpaperAtomDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le cœur d\'un réacteur nucléaire, vu de dessus, installé dans le téléphone à la place de la batterie.'**
  String get wallpaperAtomDescription;

  /// No description provided for @wallpaperAtomMotion.
  ///
  /// In fr, this message translates to:
  /// **'Le cœur'**
  String get wallpaperAtomMotion;

  /// No description provided for @wallpaperAtomMotionText.
  ///
  /// In fr, this message translates to:
  /// **'Les 37 assemblages s\'allument du centre vers le bord selon la batterie ; des neutrons passent de l\'un à l\'autre, la réaction en chaîne, plus vive en charge ; la lueur de la cuve respire.'**
  String get wallpaperAtomMotionText;

  /// No description provided for @themeAtomDescription.
  ///
  /// In fr, this message translates to:
  /// **'Le cœur d\'un réacteur, jaune.'**
  String get themeAtomDescription;

  /// No description provided for @themeSoundAtomRing.
  ///
  /// In fr, this message translates to:
  /// **'Réaction en chaîne'**
  String get themeSoundAtomRing;

  /// No description provided for @themeSoundAtomRingText.
  ///
  /// In fr, this message translates to:
  /// **'Des clics qui s\'accélèrent jusqu\'au grondement'**
  String get themeSoundAtomRingText;

  /// No description provided for @themeSoundAtomNotification.
  ///
  /// In fr, this message translates to:
  /// **'Neutron'**
  String get themeSoundAtomNotification;

  /// No description provided for @themeSoundAtomNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Un clic, puis une note claire'**
  String get themeSoundAtomNotificationText;

  /// No description provided for @themeSoundAtomAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Criticité'**
  String get themeSoundAtomAlarm;

  /// No description provided for @themeSoundAtomAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'La sirène à deux tons de la salle de contrôle'**
  String get themeSoundAtomAlarmText;

  /// No description provided for @wallpaperVaultTitle.
  ///
  /// In fr, this message translates to:
  /// **'Fallout'**
  String get wallpaperVaultTitle;

  /// No description provided for @wallpaperVaultDescription.
  ///
  /// In fr, this message translates to:
  /// **'Une porte d\'abri antiatomique et son compteur Geiger, installés dans le téléphone à la place de la batterie.'**
  String get wallpaperVaultDescription;

  /// No description provided for @wallpaperVaultMotion.
  ///
  /// In fr, this message translates to:
  /// **'La porte'**
  String get wallpaperVaultMotion;

  /// No description provided for @wallpaperVaultMotionText.
  ///
  /// In fr, this message translates to:
  /// **'Une dent de la roue s\'allume par dixième de batterie ; en charge, les verrous tournent et la porte s\'ouvre. L\'aiguille du compteur tremble et sursaute aux coups ; une ligne de balayage descend l\'écran.'**
  String get wallpaperVaultMotionText;

  /// No description provided for @themeVaultDescription.
  ///
  /// In fr, this message translates to:
  /// **'Une porte d\'abri, vert phosphore.'**
  String get themeVaultDescription;

  /// No description provided for @themeSoundVaultRing.
  ///
  /// In fr, this message translates to:
  /// **'Porte de l\'abri'**
  String get themeSoundVaultRing;

  /// No description provided for @themeSoundVaultRingText.
  ///
  /// In fr, this message translates to:
  /// **'La sirène, le sifflement des vérins, puis le choc'**
  String get themeSoundVaultRingText;

  /// No description provided for @themeSoundVaultNotification.
  ///
  /// In fr, this message translates to:
  /// **'Terminal'**
  String get themeSoundVaultNotification;

  /// No description provided for @themeSoundVaultNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Trois frappes de clavier et un bip'**
  String get themeSoundVaultNotificationText;

  /// No description provided for @themeSoundVaultAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Compteur Geiger'**
  String get themeSoundVaultAlarm;

  /// No description provided for @themeSoundVaultAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Le crépitement d\'un compteur en zone chaude'**
  String get themeSoundVaultAlarmText;

  /// No description provided for @wallpaperGhostTitle.
  ///
  /// In fr, this message translates to:
  /// **'Ghost in the Shell'**
  String get wallpaperGhostTitle;

  /// No description provided for @wallpaperGhostDescription.
  ///
  /// In fr, this message translates to:
  /// **'Un cyber-cerveau dans sa coque, branché dans le téléphone à la place de la batterie.'**
  String get wallpaperGhostDescription;

  /// No description provided for @wallpaperGhostMotion.
  ///
  /// In fr, this message translates to:
  /// **'Le cyber-cerveau'**
  String get wallpaperGhostMotion;

  /// No description provided for @wallpaperGhostMotionText.
  ///
  /// In fr, this message translates to:
  /// **'Le ghost, une étincelle, erre de piste en piste dans le cerveau ; une prise de nuque s\'allume par quart de batterie ; une pluie de code tombe derrière, et en charge, c\'est la plongée : elle accélère.'**
  String get wallpaperGhostMotionText;

  /// No description provided for @themeGhostDescription.
  ///
  /// In fr, this message translates to:
  /// **'Un cyber-cerveau et sa pluie de code, turquoise.'**
  String get themeGhostDescription;

  /// No description provided for @themeSoundGhostRing.
  ///
  /// In fr, this message translates to:
  /// **'Plongée'**
  String get themeSoundGhostRing;

  /// No description provided for @themeSoundGhostRingText.
  ///
  /// In fr, this message translates to:
  /// **'Des tambours graves sous des voix lointaines'**
  String get themeSoundGhostRingText;

  /// No description provided for @themeSoundGhostNotification.
  ///
  /// In fr, this message translates to:
  /// **'Ghost'**
  String get themeSoundGhostNotification;

  /// No description provided for @themeSoundGhostNotificationText.
  ///
  /// In fr, this message translates to:
  /// **'Un souffle, puis une note qui s\'efface'**
  String get themeSoundGhostNotificationText;

  /// No description provided for @themeSoundGhostAlarm.
  ///
  /// In fr, this message translates to:
  /// **'Synchronisation'**
  String get themeSoundGhostAlarm;

  /// No description provided for @themeSoundGhostAlarmText.
  ///
  /// In fr, this message translates to:
  /// **'Des tambours et des signaux de données qui montent'**
  String get themeSoundGhostAlarmText;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'fr'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'fr':
      return AppLocalizationsFr();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}

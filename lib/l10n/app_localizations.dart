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
  /// **'Halo ne crée aucun compte, ne contient ni publicité ni mesure d\'audience, et n\'envoie rien à ses auteurs. Votre agenda, le temps d\'écran, la consommation de données et les appareils Bluetooth sont lus sur le téléphone et n\'en sortent pas. Pour la météo, la pluie, les pollens et la qualité de l\'air, seules les coordonnées du lieu choisi sont envoyées à Open-Meteo. Vos réglages restent sur le téléphone.'**
  String get aboutPrivacyDescription;

  /// No description provided for @aboutCreditsTitle.
  ///
  /// In fr, this message translates to:
  /// **'Sources et crédits'**
  String get aboutCreditsTitle;

  /// No description provided for @aboutCreditWeatherTitle.
  ///
  /// In fr, this message translates to:
  /// **'Météo, pluie, pollens, qualité de l\'air'**
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

  /// No description provided for @wallpaperSceneBattery.
  ///
  /// In fr, this message translates to:
  /// **'L\'animation s\'arrête dès que le fond n\'est plus visible. Ce fond garde ses propres couleurs, pas celles du téléphone.'**
  String get wallpaperSceneBattery;

  /// No description provided for @wallpaperGridTitle.
  ///
  /// In fr, this message translates to:
  /// **'Grille'**
  String get wallpaperGridTitle;

  /// No description provided for @wallpaperGridDescription.
  ///
  /// In fr, this message translates to:
  /// **'Une grille lumineuse à perte de vue, où deux motos de lumière tracent leurs murs.'**
  String get wallpaperGridDescription;

  /// No description provided for @wallpaperGridATitle.
  ///
  /// In fr, this message translates to:
  /// **'Motos de lumière'**
  String get wallpaperGridATitle;

  /// No description provided for @wallpaperGridAText.
  ///
  /// In fr, this message translates to:
  /// **'Deux motos, cyan et orange, filent sur la grille et virent à angle droit ; leurs murs lumineux s\'effacent derrière elles.'**
  String get wallpaperGridAText;

  /// No description provided for @wallpaperGridBTitle.
  ///
  /// In fr, this message translates to:
  /// **'Perspective'**
  String get wallpaperGridBTitle;

  /// No description provided for @wallpaperGridBText.
  ///
  /// In fr, this message translates to:
  /// **'La grille défile doucement vers vous jusqu\'à l\'horizon.'**
  String get wallpaperGridBText;

  /// No description provided for @wallpaperGridCTitle.
  ///
  /// In fr, this message translates to:
  /// **'Inclinaison'**
  String get wallpaperGridCTitle;

  /// No description provided for @wallpaperGridCText.
  ///
  /// In fr, this message translates to:
  /// **'Penchez le téléphone : le sol et les murs glissent plus que l\'horizon lointain.'**
  String get wallpaperGridCText;

  /// No description provided for @wallpaperMegacityTitle.
  ///
  /// In fr, this message translates to:
  /// **'Mégapole'**
  String get wallpaperMegacityTitle;

  /// No description provided for @wallpaperMegacityDescription.
  ///
  /// In fr, this message translates to:
  /// **'Une ville immense sous la pluie, la nuit, dans un smog orangé.'**
  String get wallpaperMegacityDescription;

  /// No description provided for @wallpaperMegacityATitle.
  ///
  /// In fr, this message translates to:
  /// **'La ville'**
  String get wallpaperMegacityATitle;

  /// No description provided for @wallpaperMegacityAText.
  ///
  /// In fr, this message translates to:
  /// **'Trois plans de tours aux fenêtres allumées, une pyramide au loin et un grand panneau lumineux qui change de couleur.'**
  String get wallpaperMegacityAText;

  /// No description provided for @wallpaperMegacityBTitle.
  ///
  /// In fr, this message translates to:
  /// **'La vie'**
  String get wallpaperMegacityBTitle;

  /// No description provided for @wallpaperMegacityBText.
  ///
  /// In fr, this message translates to:
  /// **'Des torchères crachent leur flamme, des voitures volantes passent, deux projecteurs balaient le ciel.'**
  String get wallpaperMegacityBText;

  /// No description provided for @wallpaperMegacityCTitle.
  ///
  /// In fr, this message translates to:
  /// **'Pluie et inclinaison'**
  String get wallpaperMegacityCTitle;

  /// No description provided for @wallpaperMegacityCText.
  ///
  /// In fr, this message translates to:
  /// **'Il pleut sans cesse ; penchez le téléphone, les tours proches bougent plus que les lointaines, et la pluie penche.'**
  String get wallpaperMegacityCText;

  /// No description provided for @wallpaperCodeTitle.
  ///
  /// In fr, this message translates to:
  /// **'Code'**
  String get wallpaperCodeTitle;

  /// No description provided for @wallpaperCodeDescription.
  ///
  /// In fr, this message translates to:
  /// **'Une pluie de caractères verts, en deux plans.'**
  String get wallpaperCodeDescription;

  /// No description provided for @wallpaperCodeATitle.
  ///
  /// In fr, this message translates to:
  /// **'La pluie'**
  String get wallpaperCodeATitle;

  /// No description provided for @wallpaperCodeAText.
  ///
  /// In fr, this message translates to:
  /// **'Chaque colonne tombe à sa vitesse, sa tête blanche en avant, sa traîne qui s\'éteint ; les caractères changent au passage.'**
  String get wallpaperCodeAText;

  /// No description provided for @wallpaperCodeBTitle.
  ///
  /// In fr, this message translates to:
  /// **'Profondeur'**
  String get wallpaperCodeBTitle;

  /// No description provided for @wallpaperCodeBText.
  ///
  /// In fr, this message translates to:
  /// **'Un plan de petits caractères, plus sombres, derrière le premier.'**
  String get wallpaperCodeBText;

  /// No description provided for @wallpaperCodeCTitle.
  ///
  /// In fr, this message translates to:
  /// **'Gravité'**
  String get wallpaperCodeCTitle;

  /// No description provided for @wallpaperCodeCText.
  ///
  /// In fr, this message translates to:
  /// **'La pluie penche quand vous inclinez le téléphone.'**
  String get wallpaperCodeCText;

  /// No description provided for @wallpaperNeonTitle.
  ///
  /// In fr, this message translates to:
  /// **'Néon'**
  String get wallpaperNeonTitle;

  /// No description provided for @wallpaperNeonDescription.
  ///
  /// In fr, this message translates to:
  /// **'Une rue du futur, la nuit, bordée d\'enseignes néon.'**
  String get wallpaperNeonDescription;

  /// No description provided for @wallpaperNeonATitle.
  ///
  /// In fr, this message translates to:
  /// **'Enseignes'**
  String get wallpaperNeonATitle;

  /// No description provided for @wallpaperNeonAText.
  ///
  /// In fr, this message translates to:
  /// **'Jaunes, cyan, magenta et rouges, de chaque côté de la rue ; certaines grésillent, toutes se reflètent sur le sol mouillé.'**
  String get wallpaperNeonAText;

  /// No description provided for @wallpaperNeonBTitle.
  ///
  /// In fr, this message translates to:
  /// **'Bugs'**
  String get wallpaperNeonBTitle;

  /// No description provided for @wallpaperNeonBText.
  ///
  /// In fr, this message translates to:
  /// **'De temps en temps, l\'image décroche : bandes décalées, couleurs séparées, une barre jaune d\'interface.'**
  String get wallpaperNeonBText;

  /// No description provided for @wallpaperNeonCTitle.
  ///
  /// In fr, this message translates to:
  /// **'Inclinaison'**
  String get wallpaperNeonCTitle;

  /// No description provided for @wallpaperNeonCText.
  ///
  /// In fr, this message translates to:
  /// **'Penchez le téléphone : les enseignes proches bougent plus que le fond de la rue.'**
  String get wallpaperNeonCText;

  /// No description provided for @wallpaperSentinelTitle.
  ///
  /// In fr, this message translates to:
  /// **'Sentinelle'**
  String get wallpaperSentinelTitle;

  /// No description provided for @wallpaperSentinelDescription.
  ///
  /// In fr, this message translates to:
  /// **'L\'œil rouge d\'une intelligence artificielle, dans son boîtier de métal.'**
  String get wallpaperSentinelDescription;

  /// No description provided for @wallpaperSentinelATitle.
  ///
  /// In fr, this message translates to:
  /// **'Il vous regarde'**
  String get wallpaperSentinelATitle;

  /// No description provided for @wallpaperSentinelAText.
  ///
  /// In fr, this message translates to:
  /// **'Le cœur de l\'œil suit l\'inclinaison du téléphone, et son reflet glisse à l\'opposé sur le verre.'**
  String get wallpaperSentinelAText;

  /// No description provided for @wallpaperSentinelBTitle.
  ///
  /// In fr, this message translates to:
  /// **'Il respire'**
  String get wallpaperSentinelBTitle;

  /// No description provided for @wallpaperSentinelBText.
  ///
  /// In fr, this message translates to:
  /// **'Le cœur pulse lentement ; un anneau de balayage part du centre de temps en temps.'**
  String get wallpaperSentinelBText;

  /// No description provided for @wallpaperSentinelCTitle.
  ///
  /// In fr, this message translates to:
  /// **'Il s\'éveille'**
  String get wallpaperSentinelCTitle;

  /// No description provided for @wallpaperSentinelCText.
  ///
  /// In fr, this message translates to:
  /// **'L\'œil s\'avive quand vous déverrouillez le téléphone.'**
  String get wallpaperSentinelCText;

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

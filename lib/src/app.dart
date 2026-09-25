import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../l10n/app_localizations.dart';

import 'home_widgets/catalog.dart';
import 'home_widgets/wux_home_widget.dart';
import 'platform/wux_platform.dart';
import 'screens/catalog_screen.dart';
import 'screens/widget_screen.dart';

/// Application compagnon : elle présente les widgets d'écran d'accueil
/// disponibles et leur transmet les réglages choisis.
///
/// IUX exige deux ancêtres : son thème, et `IuxFeedbackScope` dès qu'un
/// composant émet un retour (haptique, annonce).
class WuxApp extends StatelessWidget {
  const WuxApp({
    super.key,
    required this.platform,
    this.configuring,
    this.locale,
  });

  final WuxPlatform platform;

  /// Widget dont le lanceur a demandé les réglages, ou `null` pour un
  /// lancement normal.
  final WuxHomeWidget? configuring;

  /// Langue imposée (les tests) ; `null` : celle du téléphone, le français
  /// à défaut d'une langue traduite.
  final Locale? locale;

  /// Les langues traduites, le français en tête : c'est lui qu'Android
  /// obtient quand la langue du téléphone n'est pas traduite.
  static final supportedLocales = [
    const Locale('fr'),
    for (final locale in AppLocalizations.supportedLocales)
      if (locale.languageCode != 'fr') locale,
  ];

  @override
  Widget build(BuildContext context) {
    final configuring = this.configuring;
    return IuxFeedbackScope(
      child: MaterialApp(
        onGenerateTitle: (context) => AppLocalizations.of(context).appTitle,
        locale: locale,
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: supportedLocales,
        debugShowCheckedModeBanner: false,
        theme: IuxTheme.light(),
        darkTheme: IuxTheme.dark(),
        // Halo est une appli de néons : sombre, comme ses fonds et ses widgets.
        themeMode: ThemeMode.dark,
        home: configuring == null
            ? CatalogScreen(widgets: wuxHomeWidgets, platform: platform)
            : WidgetScreen(
                homeWidget: configuring,
                platform: platform,
                configuring: true,
              ),
      ),
    );
  }
}

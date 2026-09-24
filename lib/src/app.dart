import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

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
  const WuxApp({super.key, required this.platform, this.configuring});

  final WuxPlatform platform;

  /// Widget dont le lanceur a demandé les réglages, ou `null` pour un
  /// lancement normal.
  final WuxHomeWidget? configuring;

  @override
  Widget build(BuildContext context) {
    final configuring = this.configuring;
    return IuxFeedbackScope(
      child: MaterialApp(
        title: 'Halo',
        debugShowCheckedModeBanner: false,
        theme: IuxTheme.light(),
        darkTheme: IuxTheme.dark(),
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

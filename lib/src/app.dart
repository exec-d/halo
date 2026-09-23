import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import 'home_widgets/catalog.dart';
import 'platform/wux_platform.dart';
import 'screens/catalog_screen.dart';

/// Application compagnon : elle présente les widgets d'écran d'accueil
/// disponibles et leur transmet les réglages choisis.
///
/// IUX exige deux ancêtres : son thème, et `IuxFeedbackScope` dès qu'un
/// composant émet un retour (haptique, annonce).
class WuxApp extends StatelessWidget {
  const WuxApp({super.key, required this.platform});

  final WuxPlatform platform;

  @override
  Widget build(BuildContext context) {
    return IuxFeedbackScope(
      child: MaterialApp(
        title: 'WUX',
        debugShowCheckedModeBanner: false,
        theme: IuxTheme.light(),
        darkTheme: IuxTheme.dark(),
        home: CatalogScreen(widgets: wuxHomeWidgets, platform: platform),
      ),
    );
  }
}

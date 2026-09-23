import 'package:flutter/material.dart';

import 'home_widgets/catalog.dart';
import 'home_widgets/home_widget_bridge.dart';
import 'screens/catalog_screen.dart';

/// Application compagnon : elle présente les widgets d'écran d'accueil
/// disponibles et leur transmet les réglages choisis.
class WuxApp extends StatelessWidget {
  const WuxApp({super.key, required this.bridge});

  final HomeWidgetBridge bridge;

  @override
  Widget build(BuildContext context) {
    const seed = Color(0xFF3F51B5);
    return MaterialApp(
      title: 'WUX',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(colorSchemeSeed: seed),
      darkTheme: ThemeData(colorSchemeSeed: seed, brightness: Brightness.dark),
      home: CatalogScreen(widgets: wuxHomeWidgets, bridge: bridge),
    );
  }
}

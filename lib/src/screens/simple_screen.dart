import 'package:flutter/material.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'settings_scaffold.dart';

/// Widget sans réglage : son aperçu et le bouton d'épinglage.
class SimpleScreen extends StatelessWidget {
  const SimpleScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.allowPin = true,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;

  /// Faux quand l'écran est ouvert depuis un widget déjà posé.
  final bool allowPin;

  @override
  Widget build(BuildContext context) => SettingsScaffold(
    homeWidget: homeWidget,
    platform: platform,
    allowPin: allowPin,
  );
}

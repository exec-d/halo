import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

import '../platform/wux_platform.dart';
import 'screen_frame.dart';

/// L'écran de veille Halo : ce qu'il montre et comment l'activer. Android le
/// choisit dans ses propres réglages.
class DreamScreen extends StatelessWidget {
  const DreamScreen({super.key, required this.platform});

  final WuxPlatform platform;

  @override
  Widget build(BuildContext context) {
    final typography = IuxTypographyTheme.of(context);
    final l10n = AppLocalizations.of(context);
    final open = l10n.dreamOpenSettings;
    return Scaffold(
      body: ScreenFrame(
        title: l10n.dreamTitle,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: l10n.dreamIntro,
              children: [
                IuxButton(
                  label: open,
                  action: IuxActionDescriptor.primary(
                    semantics: IuxActionSemantics(label: open),
                    role: IuxActionRole.custom,
                  ),
                  expand: true,
                  onActivate: platform.openDreamSettings,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.dreamHowToTitle,
              children: [
                for (final step in [
                  l10n.dreamStep1,
                  l10n.dreamStep2,
                  l10n.dreamStep3,
                ]) ...[
                  Text(step, style: typography.body),
                  const IuxGap.standard(),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }
}

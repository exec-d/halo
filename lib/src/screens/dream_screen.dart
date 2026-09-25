import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../platform/wux_platform.dart';
import 'screen_frame.dart';

/// Nom et description de l'écran de veille, partagés avec le catalogue.
const dreamTitle = 'Écran de veille';
const dreamDescription =
    'Pendant la charge : une grande horloge néon sur le décor Circuit.';

/// L'écran de veille Halo : ce qu'il montre et comment l'activer. Android le
/// choisit dans ses propres réglages.
class DreamScreen extends StatelessWidget {
  const DreamScreen({super.key, required this.platform});

  final WuxPlatform platform;

  static const _open = "Ouvrir les réglages de l'écran de veille";

  @override
  Widget build(BuildContext context) {
    final typography = IuxTypographyTheme.of(context);
    return Scaffold(
      body: ScreenFrame(
        title: dreamTitle,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description:
                  'Quand le téléphone charge, ou est posé sur un socle, '
                  'Android peut afficher un écran de veille. Celui de Halo '
                  "montre l'heure en grand sur le décor Circuit, avec la "
                  'prochaine alarme, le prochain événement et la météo. Le '
                  'texte se déplace un peu chaque minute pour ne pas marquer '
                  "l'écran ; un toucher réveille le téléphone.",
              children: [
                IuxButton(
                  label: _open,
                  action: const IuxActionDescriptor.primary(
                    semantics: IuxActionSemantics(label: _open),
                    role: IuxActionRole.custom,
                  ),
                  expand: true,
                  onActivate: platform.openDreamSettings,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Pour l\'activer',
              children: [
                for (final step in _steps) ...[
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

  static const _steps = [
    '1. Dans les réglages qui s\'ouvrent, activez « Utiliser l\'écran de '
        'veille ».',
    '2. Choisissez « Halo » dans la liste.',
    '3. Dans « Quand l\'activer », choisissez « Pendant la charge » (ou sur '
        'un socle).',
  ];
}

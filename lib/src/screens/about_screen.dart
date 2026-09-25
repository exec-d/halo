import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../platform/wux_platform.dart';
import 'screen_frame.dart';

/// À propos : version, ce que fait Halo, confidentialité, sources et
/// licences.
class AboutScreen extends StatefulWidget {
  const AboutScreen({super.key, required this.platform});

  final WuxPlatform platform;

  @override
  State<AboutScreen> createState() => _AboutScreenState();
}

class _AboutScreenState extends State<AboutScreen> {
  AppInfo? _info;

  @override
  void initState() {
    super.initState();
    widget.platform.appInfo().then((info) {
      if (mounted) setState(() => _info = info);
    });
  }

  String get _version {
    final info = _info;
    return info == null ? '' : '${info.version} (build ${info.build})';
  }

  @override
  Widget build(BuildContext context) {
    final type = IuxTypographyTheme.of(context);
    return Scaffold(
      body: ScreenFrame(
        title: 'À propos',
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                ClipOval(
                  child: Image.asset(
                    'assets/icon.png',
                    width: 72,
                    height: 72,
                    semanticLabel: 'Icône de Halo',
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Halo', style: type.title),
                      Text('Version $_version', style: type.body),
                    ],
                  ),
                ),
              ],
            ),
            const IuxGap.between(),
            const IuxSection(
              title: 'Halo',
              description:
                  "Des widgets d'écran d'accueil au style néon, aux couleurs "
                  'de votre fond d\'écran : horloge, agendas, météo, pluie, '
                  'allergies, système, et plus. Nom de code : WUX.',
              children: [],
            ),
            const IuxGap.between(),
            const IuxSection(
              title: 'Confidentialité',
              description:
                  'Halo ne crée aucun compte, ne contient ni publicité ni '
                  "mesure d'audience, et n'envoie rien à ses auteurs. Votre "
                  'agenda, le temps d\'écran, la consommation de données et '
                  'les appareils Bluetooth sont lus sur le téléphone et n\'en '
                  'sortent pas. Pour la '
                  'météo, la pluie, les pollens et la qualité de l\'air, '
                  'seules les coordonnées du lieu choisi sont envoyées à '
                  'Open-Meteo. Vos réglages restent sur le téléphone.',
              children: [],
            ),
            const IuxGap.between(),
            const IuxSection(
              title: 'Sources et crédits',
              children: [
                _Credit(
                  title: 'Météo, pluie, pollens, qualité de l\'air',
                  detail:
                      'Open-Meteo.com, sous licence CC BY 4.0. Pollens et '
                      "qualité de l'air : Copernicus Atmosphere Monitoring "
                      'Service (CAMS), modèle européen.',
                ),
                _Credit(
                  title: 'Recherche de villes',
                  detail: 'Géocodage Open-Meteo, données GeoNames (CC BY 4.0).',
                ),
                _Credit(
                  title: 'Icônes',
                  detail: 'Material Icons de Google, licence Apache 2.0.',
                ),
                _Credit(
                  title: 'Interface',
                  detail:
                      'Flutter, et IUX pour les composants accessibles de '
                      "l'application.",
                ),
                _Credit(
                  title: 'Code source',
                  detail:
                      'Halo est un logiciel libre, sous licence MIT : '
                      'github.com/exec-d/WUX.',
                ),
              ],
            ),
            const IuxGap.between(),
            IuxButton(
              label: _licenses,
              action: const IuxActionDescriptor(
                semantics: IuxActionSemantics(label: _licenses),
              ),
              expand: true,
              onActivate: () => showLicensePage(
                context: context,
                applicationName: 'Halo',
                applicationVersion: _version,
                applicationIcon: Padding(
                  padding: const EdgeInsets.all(8),
                  child: ClipOval(
                    child: Image.asset('assets/icon.png', width: 48),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  static const _licenses = 'Licences des logiciels utilisés';
}

class _Credit extends StatelessWidget {
  const _Credit({required this.title, required this.detail});

  final String title;
  final String detail;

  @override
  Widget build(BuildContext context) {
    final type = IuxTypographyTheme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: type.label),
          Text(detail, style: type.body),
        ],
      ),
    );
  }
}

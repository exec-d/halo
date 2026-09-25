import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

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

  String _version(AppLocalizations l10n) {
    final info = _info;
    return info == null ? '' : l10n.aboutVersionBuild(info.version, info.build);
  }

  @override
  Widget build(BuildContext context) {
    final type = IuxTypographyTheme.of(context);
    final l10n = AppLocalizations.of(context);
    final version = _version(l10n);
    final licenses = l10n.aboutLicenses;
    return Scaffold(
      body: ScreenFrame(
        title: l10n.catalogAbout,
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
                    semanticLabel: l10n.aboutIconSemantics,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(l10n.appTitle, style: type.title),
                      Text(l10n.aboutVersion(version), style: type.body),
                    ],
                  ),
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.appTitle,
              description: l10n.aboutHaloDescription,
              children: [],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.aboutPrivacyTitle,
              description: l10n.aboutPrivacyDescription,
              children: [],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.aboutCreditsTitle,
              children: [
                _Credit(
                  title: l10n.aboutCreditWeatherTitle,
                  detail: l10n.aboutCreditWeatherDetail,
                ),
                _Credit(
                  title: l10n.aboutCreditCitiesTitle,
                  detail: l10n.aboutCreditCitiesDetail,
                ),
                _Credit(
                  title: l10n.aboutCreditIconsTitle,
                  detail: l10n.aboutCreditIconsDetail,
                ),
                _Credit(
                  title: l10n.aboutCreditInterfaceTitle,
                  detail: l10n.aboutCreditInterfaceDetail,
                ),
                _Credit(
                  title: l10n.aboutCreditSourceTitle,
                  detail: l10n.aboutCreditSourceDetail,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxButton(
              label: licenses,
              action: IuxActionDescriptor(
                semantics: IuxActionSemantics(label: licenses),
              ),
              expand: true,
              onActivate: () => showLicensePage(
                context: context,
                applicationName: l10n.appTitle,
                applicationVersion: version,
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

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'about_screen.dart';
import 'screen_frame.dart';
import 'settings_screen.dart';
import 'widget_screen.dart';

/// Liste des widgets disponibles, chacun avec un aperçu d'exemple ; toucher
/// une carte ouvre ses réglages, avec l'aperçu de ses vraies données.
///
/// Au premier lancement, demande l'accès à l'agenda, dont dépendent trois
/// widgets. La position, qui ne sert qu'à la météo, est demandée là-bas.
class CatalogScreen extends StatefulWidget {
  const CatalogScreen({
    super.key,
    required this.widgets,
    required this.platform,
  });

  final List<WuxHomeWidget> widgets;
  final WuxPlatform platform;

  @override
  State<CatalogScreen> createState() => _CatalogScreenState();
}

class _CatalogScreenState extends State<CatalogScreen> {
  List<WuxHomeWidget> get widgets => widget.widgets;
  WuxPlatform get platform => widget.platform;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _firstLaunch());
  }

  Future<void> _firstLaunch() async {
    if (!await platform.isFirstLaunch()) return;
    await platform.markLaunched();
    if (!await platform.hasCalendarPermission()) {
      await platform.requestCalendarPermission();
    }
  }

  void _open(BuildContext context, WuxHomeWidget homeWidget) {
    Navigator.of(context).push<void>(
      MaterialPageRoute<void>(
        builder: (context) =>
            WidgetScreen(homeWidget: homeWidget, platform: platform),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ScreenFrame(
        title: 'Mes widgets',
        actions: [
          IuxIconButton(
            icon: Icons.settings_outlined,
            action: const IuxActionDescriptor(
              semantics: IuxActionSemantics(label: 'Réglages'),
            ),
            onActivate: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(
                builder: (_) => SettingsScreen(platform: platform),
              ),
            ),
          ),
          IuxIconButton(
            icon: Icons.info_outline,
            action: const IuxActionDescriptor(
              semantics: IuxActionSemantics(label: 'À propos'),
            ),
            onActivate: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(
                builder: (_) => AboutScreen(platform: platform),
              ),
            ),
          ),
        ],
        child: IuxSection(
          children: [
            for (final homeWidget in widgets) ...[
              IuxCard.tappable(
                semanticLabel: homeWidget.title,
                hint: homeWidget.description,
                onActivate: () => _open(context, homeWidget),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    WallpaperFrame(
                      child: WidgetPreview(
                        homeWidget: homeWidget,
                        platform: platform,
                        sample: true,
                      ),
                    ),
                    const IuxGap.standard(),
                    Text(
                      homeWidget.title,
                      style: IuxTypographyTheme.of(context).title,
                    ),
                    Text(
                      homeWidget.description,
                      style: IuxTypographyTheme.of(context).body,
                    ),
                  ],
                ),
              ),
              const IuxGap.between(),
            ],
          ],
        ),
      ),
    );
  }
}

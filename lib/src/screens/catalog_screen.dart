import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';
import 'widget_screen.dart';

/// Liste des widgets disponibles, chacun avec son aperçu ; toucher une carte
/// ouvre ses réglages.
class CatalogScreen extends StatelessWidget {
  const CatalogScreen({
    super.key,
    required this.widgets,
    required this.platform,
  });

  final List<WuxHomeWidget> widgets;
  final WuxPlatform platform;

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

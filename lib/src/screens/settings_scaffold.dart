import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';

/// Squelette d'un écran de widget : l'aperçu, le bouton d'épinglage, puis
/// les sections de réglage [sections].
class SettingsScaffold extends StatefulWidget {
  const SettingsScaffold({
    super.key,
    required this.homeWidget,
    required this.platform,
    required this.allowPin,
    this.revision = 0,
    this.sections = const [],
    this.preview,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final bool allowPin;

  /// À augmenter après chaque réglage, pour redessiner l'aperçu.
  final int revision;
  final List<Widget> sections;

  /// Remplace l'aperçu (par exemple tant qu'une autorisation manque).
  final Widget? preview;

  @override
  State<SettingsScaffold> createState() => _SettingsScaffoldState();
}

class _SettingsScaffoldState extends State<SettingsScaffold> {
  bool _canPin = false;

  @override
  void initState() {
    super.initState();
    widget.platform.canPin().then((value) {
      if (mounted) setState(() => _canPin = value);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ScreenFrame(
        title: widget.homeWidget.title,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: widget.homeWidget.description,
              children: [
                WallpaperFrame(
                  child:
                      widget.preview ??
                      WidgetPreview(
                        homeWidget: widget.homeWidget,
                        platform: widget.platform,
                        revision: widget.revision,
                      ),
                ),
                const IuxGap.between(),
                PinButton(
                  visible: _canPin && widget.allowPin,
                  onPin: () => widget.platform.pin(widget.homeWidget),
                ),
              ],
            ),
            for (final section in widget.sections) ...[
              const IuxGap.between(),
              section,
            ],
          ],
        ),
      ),
    );
  }
}

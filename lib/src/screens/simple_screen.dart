import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';

/// Widget sans réglage : son aperçu et le bouton d'épinglage.
class SimpleScreen extends StatefulWidget {
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
  State<SimpleScreen> createState() => _SimpleScreenState();
}

class _SimpleScreenState extends State<SimpleScreen> {
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
        child: IuxSection(
          description: widget.homeWidget.description,
          children: [
            WallpaperFrame(
              child: WidgetPreview(
                homeWidget: widget.homeWidget,
                platform: widget.platform,
              ),
            ),
            const IuxGap.between(),
            PinButton(
              visible: _canPin && widget.allowPin,
              onPin: () => widget.platform.pin(widget.homeWidget),
            ),
          ],
        ),
      ),
    );
  }
}

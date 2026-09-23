import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';

/// Widget système : un aperçu avec l'état réel du téléphone, sans réglage.
class SystemScreen extends StatefulWidget {
  const SystemScreen({
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
  State<SystemScreen> createState() => _SystemScreenState();
}

class _SystemScreenState extends State<SystemScreen> {
  List<SystemTilePreview> _tiles = const [];
  WidgetPalette _palette = WidgetPalette.fallback;
  bool _canPin = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final tiles = await widget.platform.systemPreview();
    final palette = await widget.platform.palette();
    final canPin = await widget.platform.canPin();
    if (!mounted) return;
    setState(() {
      _tiles = tiles;
      _palette = palette;
      _canPin = canPin;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ScreenFrame(
        title: widget.homeWidget.title,
        canGoBack: true,
        child: IuxSection(
          description:
              '${widget.homeWidget.description} Le réseau se met à jour dès '
              "qu'il change ; la batterie et le stockage toutes les 5 minutes, "
              'écran allumé. Touchez une case pour ouvrir le réglage '
              'correspondant.',
          children: [
            WallpaperFrame(
              child: SystemWidgetPreview(tiles: _tiles, palette: _palette),
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

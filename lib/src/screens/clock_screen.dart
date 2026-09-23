import 'dart:async';

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';

class ClockScreen extends StatefulWidget {
  const ClockScreen({
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
  State<ClockScreen> createState() => _ClockScreenState();
}

class _ClockScreenState extends State<ClockScreen> {
  ClockPreview? _clock;
  WidgetPalette _palette = WidgetPalette.fallback;
  bool _canPin = false;
  Timer? _tick;

  @override
  void initState() {
    super.initState();
    _load();
    // L'aperçu n'affiche pas les secondes : un rafraîchissement toutes les
    // quinze secondes le tient à l'heure à moins d'un quart de minute près.
    _tick = Timer.periodic(const Duration(seconds: 15), (_) => _refresh());
  }

  @override
  void dispose() {
    _tick?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    final palette = await widget.platform.palette();
    final canPin = await widget.platform.canPin();
    if (!mounted) return;
    setState(() {
      _palette = palette;
      _canPin = canPin;
    });
    await _refresh();
  }

  Future<void> _refresh() async {
    final clock = await widget.platform.clockPreview();
    if (mounted) setState(() => _clock = clock);
  }

  @override
  Widget build(BuildContext context) {
    final clock = _clock;
    return Scaffold(
      body: ScreenFrame(
        title: widget.homeWidget.title,
        canGoBack: true,
        child: IuxSection(
          description: widget.homeWidget.description,
          children: [
            WallpaperFrame(
              child: clock == null
                  ? const SizedBox(height: 120)
                  : ClockWidgetPreview(clock: clock, palette: _palette),
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

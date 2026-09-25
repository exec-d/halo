import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';

/// Fond sombre derrière un aperçu : le style néon est pensé pour un fond
/// d'écran foncé.
class WallpaperFrame extends StatelessWidget {
  const WallpaperFrame({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF05070D), Color(0xFF0B1224)],
        ),
      ),
      child: Padding(padding: const EdgeInsets.all(12), child: child),
    );
  }
}

/// Le vrai widget, dessiné par Android (`WidgetPreviews.kt`) et affiché en
/// image, à l'échelle de la place disponible.
///
/// Redessiné quand [revision] change (un réglage vient de bouger) et toutes
/// les 30 secondes, pour que l'heure et l'état du téléphone restent justes.
class WidgetPreview extends StatefulWidget {
  const WidgetPreview({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.revision = 0,
    this.sample = false,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final int revision;

  /// Données d'exemple : pour la liste des widgets, qui doit être belle même
  /// sans autorisation ni données.
  final bool sample;

  @override
  State<WidgetPreview> createState() => _WidgetPreviewState();
}

class _WidgetPreviewState extends State<WidgetPreview> {
  Uint8List? _image;
  Timer? _tick;

  @override
  void initState() {
    super.initState();
    _render();
    _tick = Timer.periodic(const Duration(seconds: 30), (_) => _render());
  }

  @override
  void didUpdateWidget(WidgetPreview old) {
    super.didUpdateWidget(old);
    if (old.revision != widget.revision ||
        old.homeWidget != widget.homeWidget) {
      _render();
    }
  }

  @override
  void dispose() {
    _tick?.cancel();
    super.dispose();
  }

  Future<void> _render() async {
    final image = await widget.platform.render(
      widget.homeWidget,
      widget.homeWidget.previewSize,
      sample: widget.sample,
    );
    if (mounted) setState(() => _image = image);
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.homeWidget.previewSize;
    final image = _image;
    final l10n = AppLocalizations.of(context);
    return Semantics(
      image: true,
      label: l10n.widgetPreviewSemantics(widget.homeWidget.title(l10n)),
      // Taille réelle du widget, réduite seulement si la place manque :
      // agrandi, un widget étroit aurait un texte démesuré.
      child: FittedBox(
        fit: BoxFit.scaleDown,
        child: SizedBox(
          width: size.width,
          height: size.height,
          child: image == null
              ? null
              : Image.memory(image, fit: BoxFit.fill, gaplessPlayback: true),
        ),
      ),
    );
  }
}

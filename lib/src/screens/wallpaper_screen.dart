import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../platform/wux_platform.dart';
import 'screen_frame.dart';

/// Nom et description du fond d'écran animé, partagés par le catalogue et son
/// écran.
const wallpaperTitle = 'Circuit';
const wallpaperDescription =
    "L'intérieur du téléphone en néon, aux couleurs du téléphone.";

/// Le fond d'écran animé « Circuit » : aperçu, ce qu'il montre, et son
/// application à l'accueil et à l'écran de verrouillage.
class WallpaperScreen extends StatefulWidget {
  const WallpaperScreen({super.key, required this.platform});

  final WuxPlatform platform;

  @override
  State<WallpaperScreen> createState() => _WallpaperScreenState();
}

class _WallpaperScreenState extends State<WallpaperScreen>
    with WidgetsBindingObserver {
  static const _apply = "Appliquer le fond d'écran";

  bool? _active;

  /// L'écran système d'application manque (rare, sur certains téléphones).
  bool _unavailable = false;

  WuxPlatform get _platform => widget.platform;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // De retour de l'écran système : le fond vient peut-être d'être appliqué.
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    final active = await _platform.isWallpaperActive();
    if (mounted) setState(() => _active = active);
  }

  Future<void> _applyWallpaper() async {
    final opened = await _platform.applyWallpaper();
    if (!mounted) return;
    setState(() => _unavailable = !opened);
    await _refresh();
  }

  @override
  Widget build(BuildContext context) {
    final typography = IuxTypographyTheme.of(context);
    return Scaffold(
      body: ScreenFrame(
        title: wallpaperTitle,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: wallpaperDescription,
              children: [
                Center(child: WallpaperPreview(platform: _platform)),
                const IuxGap.between(),
                if (_active != null)
                  IuxStatusIndicator(
                    status: _active!
                        ? const IuxStatus.success('Fond d\'écran actuel')
                        : const IuxStatus.neutral('Pas encore appliqué'),
                  ),
                const IuxGap.standard(),
                IuxButton(
                  label: _apply,
                  action: const IuxActionDescriptor.primary(
                    semantics: IuxActionSemantics(label: _apply),
                    role: IuxActionRole.custom,
                  ),
                  expand: true,
                  onActivate: _applyWallpaper,
                ),
                if (_unavailable) ...[
                  const IuxGap.standard(),
                  Text(
                    "Ce téléphone ne propose pas l'écran d'application. "
                    "Choisissez « Halo · Circuit » dans Fond d'écran et "
                    'style, rubrique Fonds d\'écran animés.',
                    style: typography.body,
                  ),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Ce qui bouge',
              children: [
                for (final (title, text) in _features) ...[
                  Text(title, style: typography.title),
                  Text(text, style: typography.body),
                  const IuxGap.standard(),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Batterie',
              description:
                  "L'animation s'arrête dès que le fond n'est plus visible, "
                  'et ne tourne en continu que pendant un mouvement, une '
                  'impulsion ou une charge.',
              children: const [],
            ),
            const IuxGap.between(),
          ],
        ),
      ),
    );
  }

  static const _features = [
    (
      'Inclinaison',
      'Les plans du téléphone glissent quand vous le penchez, et le reflet '
          'du verre suit.',
    ),
    (
      'Vraie batterie',
      'La batterie dessinée affiche le niveau réel et respire pendant la '
          'charge.',
    ),
    (
      'Réseau',
      'Des impulsions courent de l\'antenne au processeur quand des données '
          'passent ; les antennes brillent selon la force du signal.',
    ),
    (
      'Allumage',
      "À chaque allumage de l'écran, les circuits s'éclairent depuis le "
          'processeur.',
    ),
  ];
}

/// Le fond d'écran dessiné par Android, en image fixe, au format d'un
/// téléphone.
class WallpaperPreview extends StatefulWidget {
  const WallpaperPreview({super.key, required this.platform, this.width = 220});

  final WuxPlatform platform;

  /// Largeur affichée ; la hauteur suit le format 9:20.
  final double width;

  @override
  State<WallpaperPreview> createState() => _WallpaperPreviewState();
}

class _WallpaperPreviewState extends State<WallpaperPreview> {
  Uint8List? _image;
  bool _requested = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_requested) return;
    _requested = true;
    final ratio = MediaQuery.devicePixelRatioOf(context);
    final size = Size(widget.width * ratio, widget.width * 20 / 9 * ratio);
    widget.platform.renderWallpaper(size).then((image) {
      if (mounted) setState(() => _image = image);
    });
  }

  @override
  Widget build(BuildContext context) {
    final image = _image;
    return Semantics(
      image: true,
      label: "Aperçu du fond d'écran $wallpaperTitle",
      child: SizedBox(
        width: widget.width,
        height: widget.width * 20 / 9,
        child: DecoratedBox(
          decoration: const BoxDecoration(color: Color(0xFF05070D)),
          child: image == null
              ? null
              : Image.memory(image, fit: BoxFit.cover, gaplessPlayback: true),
        ),
      ),
    );
  }
}

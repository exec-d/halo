import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../platform/wux_platform.dart';
import 'screen_frame.dart';

/// Un fond d'écran animé de Halo : son identifiant (partagé avec
/// `WallpaperPreview.kt`), son nom, et ce qui bouge.
class HaloWallpaper {
  const HaloWallpaper({
    required this.id,
    required this.title,
    required this.description,
    required this.features,
    required this.battery,
  });

  final String id;
  final String title;
  final String description;
  final List<(String, String)> features;
  final String battery;
}

const circuitWallpaper = HaloWallpaper(
  id: 'circuit',
  title: 'Circuit',
  description: "L'intérieur du téléphone en néon, aux couleurs du téléphone.",
  features: [
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
      "Des impulsions courent de l'antenne au processeur quand des données "
          'passent ; les antennes brillent selon la force du signal.',
    ),
    (
      'Allumage',
      "À chaque allumage de l'écran, le fond est là tout de suite, puis "
          "composants et pistes s'illuminent un à un depuis le processeur.",
    ),
  ],
  battery:
      "L'animation s'arrête dès que le fond n'est plus visible, et ne tourne "
      'en continu que pendant un mouvement, une impulsion ou une charge.',
);

const horizonWallpaper = HaloWallpaper(
  id: 'horizon',
  title: 'Horizon',
  description:
      "Un horizon néon qui suit l'heure et la vraie météo du lieu choisi.",
  features: [
    (
      'La journée',
      'Le soleil se lève, traverse le ciel et se couche aux vraies heures ; '
          'la nuit, la lune dans sa phase et les étoiles.',
    ),
    (
      'La météo',
      'Nuages, pluie, neige, orage ou brume, selon le temps qu\'il fait au '
          'lieu choisi dans Météo.',
    ),
    (
      'Inclinaison',
      'Montagnes et sol quadrillé glissent quand vous penchez le téléphone.',
    ),
  ],
  battery:
      "L'animation s'arrête dès que le fond n'est plus visible ; elle "
      'ralentit par temps clair, et ne va vite que sous la pluie ou la neige.',
);

const skyWallpaper = HaloWallpaper(
  id: 'sky',
  title: 'Ciel',
  description:
      'Les vraies étoiles et la vraie Lune au-dessus de vous, dans la '
      'direction du téléphone.',
  features: [
    (
      'Le vrai ciel',
      'Les étoiles les plus brillantes et les constellations, à leur place '
          'pour le lieu choisi dans Météo et pour cette heure.',
    ),
    (
      'Boussole',
      'Tournez-vous : le ciel suit la direction du téléphone. Levez-le : '
          'vous montez vers le zénith.',
    ),
    (
      'Le jour',
      'Le ciel s\'éclaircit et les étoiles pâlissent quand le soleil est '
          'levé.',
    ),
  ],
  battery:
      "L'animation et la boussole s'arrêtent dès que le fond n'est plus "
      'visible.',
);

/// Les fonds d'écran animés, dans l'ordre du catalogue.
const haloWallpapers = [circuitWallpaper, horizonWallpaper, skyWallpaper];

/// Un fond d'écran animé : aperçu, ce qu'il montre, son intensité et son
/// application à l'accueil et à l'écran de verrouillage.
class WallpaperScreen extends StatefulWidget {
  const WallpaperScreen({
    super.key,
    required this.platform,
    this.wallpaper = circuitWallpaper,
  });

  final WuxPlatform platform;
  final HaloWallpaper wallpaper;

  @override
  State<WallpaperScreen> createState() => _WallpaperScreenState();
}

class _WallpaperScreenState extends State<WallpaperScreen>
    with WidgetsBindingObserver {
  static const _apply = "Appliquer le fond d'écran";

  bool? _active;
  String _intensity = 'discreet';

  /// Augmentée à chaque réglage, pour redessiner l'aperçu.
  int _revision = 0;

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
    final active = await _platform.isWallpaperActive(kind: widget.wallpaper.id);
    final intensity = await _platform.wallpaperIntensity();
    if (mounted) {
      setState(() {
        _active = active;
        _intensity = intensity;
      });
    }
  }

  Future<void> _setIntensity(String value) async {
    setState(() => _intensity = value);
    await _platform.setWallpaperIntensity(value);
    if (mounted) setState(() => _revision++);
  }

  Future<void> _applyWallpaper() async {
    final opened = await _platform.applyWallpaper(kind: widget.wallpaper.id);
    if (!mounted) return;
    setState(() => _unavailable = !opened);
    await _refresh();
  }

  @override
  Widget build(BuildContext context) {
    final typography = IuxTypographyTheme.of(context);
    return Scaffold(
      body: ScreenFrame(
        title: widget.wallpaper.title,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: widget.wallpaper.description,
              children: [
                Center(
                  child: WallpaperPreview(
                    platform: _platform,
                    wallpaper: widget.wallpaper,
                    revision: _revision,
                  ),
                ),
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
                    'Choisissez « Halo · ${widget.wallpaper.title} » dans '
                    "Fond d'écran et "
                    'style, rubrique Fonds d\'écran animés.',
                    style: typography.body,
                  ),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Intensité',
              children: [
                IuxRadioGroup<String>(
                  label: 'Intensité du fond',
                  input: const IuxInputDescriptor(
                    semantics: IuxInputSemantics(label: 'Intensité du fond'),
                    helpText:
                        'Discret garde les widgets et les icônes bien '
                        'lisibles par-dessus.',
                  ),
                  value: _intensity,
                  options: const [
                    IuxRadioOption(value: 'discreet', label: 'Discret'),
                    IuxRadioOption(value: 'normal', label: 'Normal'),
                    IuxRadioOption(value: 'vivid', label: 'Vif'),
                  ],
                  onChanged: _setIntensity,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Ce qui bouge',
              children: [
                for (final (title, text) in widget.wallpaper.features) ...[
                  Text(title, style: typography.title),
                  Text(text, style: typography.body),
                  const IuxGap.standard(),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Batterie',
              description: widget.wallpaper.battery,
              children: const [],
            ),
            const IuxGap.between(),
          ],
        ),
      ),
    );
  }
}

/// Le fond d'écran dessiné par Android, en image fixe, au format d'un
/// téléphone.
class WallpaperPreview extends StatefulWidget {
  const WallpaperPreview({
    super.key,
    required this.platform,
    this.wallpaper = circuitWallpaper,
    this.width = 220,
    this.revision = 0,
  });

  final WuxPlatform platform;
  final HaloWallpaper wallpaper;

  /// Redessiné quand elle change (un réglage vient de bouger).
  final int revision;

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
    _render();
  }

  @override
  void didUpdateWidget(WallpaperPreview old) {
    super.didUpdateWidget(old);
    if (old.revision != widget.revision) _render();
  }

  void _render() {
    final ratio = MediaQuery.devicePixelRatioOf(context);
    final size = Size(widget.width * ratio, widget.width * 20 / 9 * ratio);
    widget.platform.renderWallpaper(size, kind: widget.wallpaper.id).then((
      image,
    ) {
      if (mounted) setState(() => _image = image);
    });
  }

  @override
  Widget build(BuildContext context) {
    final image = _image;
    return Semantics(
      image: true,
      label: "Aperçu du fond d'écran ${widget.wallpaper.title}",
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

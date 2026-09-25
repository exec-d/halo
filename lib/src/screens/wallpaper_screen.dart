import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

import '../platform/wux_platform.dart';
import 'screen_frame.dart';

/// Un fond d'écran animé de Halo : son identifiant (partagé avec
/// `WallpaperPreview.kt`), son nom, et ce qui bouge. Les textes sont lus
/// dans la traduction (lib/l10n).
class HaloWallpaper {
  const HaloWallpaper({
    required this.id,
    required this.title,
    required this.description,
    required this.features,
    required this.battery,
  });

  final String id;
  final String Function(AppLocalizations l10n) title;
  final String Function(AppLocalizations l10n) description;

  /// Ce qui bouge : un titre et son explication.
  final List<(String, String)> Function(AppLocalizations l10n) features;
  final String Function(AppLocalizations l10n) battery;
}

const circuitWallpaper = HaloWallpaper(
  id: 'circuit',
  title: _circuitTitle,
  description: _circuitDescription,
  features: _circuitFeatures,
  battery: _circuitBattery,
);
String _circuitTitle(AppLocalizations l) => l.wallpaperCircuitTitle;
String _circuitDescription(AppLocalizations l) => l.wallpaperCircuitDescription;
List<(String, String)> _circuitFeatures(AppLocalizations l) => [
  (l.wallpaperCircuitTiltTitle, l.wallpaperCircuitTiltText),
  (l.wallpaperCircuitBatteryTitle, l.wallpaperCircuitBatteryText),
  (l.wallpaperCircuitNetworkTitle, l.wallpaperCircuitNetworkText),
  (l.wallpaperCircuitWakeTitle, l.wallpaperCircuitWakeText),
];
String _circuitBattery(AppLocalizations l) => l.wallpaperCircuitBattery;

const horizonWallpaper = HaloWallpaper(
  id: 'horizon',
  title: _horizonTitle,
  description: _horizonDescription,
  features: _horizonFeatures,
  battery: _horizonBattery,
);
String _horizonTitle(AppLocalizations l) => l.wallpaperHorizonTitle;
String _horizonDescription(AppLocalizations l) => l.wallpaperHorizonDescription;
List<(String, String)> _horizonFeatures(AppLocalizations l) => [
  (l.wallpaperHorizonDayTitle, l.wallpaperHorizonDayText),
  (l.wallpaperHorizonWeatherTitle, l.wallpaperHorizonWeatherText),
  (l.wallpaperHorizonTiltTitle, l.wallpaperHorizonTiltText),
];
String _horizonBattery(AppLocalizations l) => l.wallpaperHorizonBattery;

const skyWallpaper = HaloWallpaper(
  id: 'sky',
  title: _skyTitle,
  description: _skyDescription,
  features: _skyFeatures,
  battery: _skyBattery,
);
String _skyTitle(AppLocalizations l) => l.wallpaperSkyTitle;
String _skyDescription(AppLocalizations l) => l.wallpaperSkyDescription;
List<(String, String)> _skyFeatures(AppLocalizations l) => [
  (l.wallpaperSkyRealTitle, l.wallpaperSkyRealText),
  (l.wallpaperSkyCompassTitle, l.wallpaperSkyCompassText),
  (l.wallpaperSkyDayTitle, l.wallpaperSkyDayText),
];
String _skyBattery(AppLocalizations l) => l.wallpaperSkyBattery;

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
    final l10n = AppLocalizations.of(context);
    final title = widget.wallpaper.title(l10n);
    final apply = l10n.wallpaperApply;
    return Scaffold(
      body: ScreenFrame(
        title: title,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: widget.wallpaper.description(l10n),
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
                        ? IuxStatus.success(l10n.wallpaperActive)
                        : IuxStatus.neutral(l10n.wallpaperNotApplied),
                  ),
                const IuxGap.standard(),
                IuxButton(
                  label: apply,
                  action: IuxActionDescriptor.primary(
                    semantics: IuxActionSemantics(label: apply),
                    role: IuxActionRole.custom,
                  ),
                  expand: true,
                  onActivate: _applyWallpaper,
                ),
                if (_unavailable) ...[
                  const IuxGap.standard(),
                  Text(
                    l10n.wallpaperUnavailable(title),
                    style: typography.body,
                  ),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.wallpaperIntensityTitle,
              children: [
                IuxRadioGroup<String>(
                  label: l10n.wallpaperIntensityLabel,
                  input: IuxInputDescriptor(
                    semantics: IuxInputSemantics(
                      label: l10n.wallpaperIntensityLabel,
                    ),
                    helpText: l10n.wallpaperIntensityHelp,
                  ),
                  value: _intensity,
                  options: [
                    IuxRadioOption(
                      value: 'discreet',
                      label: l10n.wallpaperIntensityDiscreet,
                    ),
                    IuxRadioOption(
                      value: 'normal',
                      label: l10n.wallpaperIntensityNormal,
                    ),
                    IuxRadioOption(
                      value: 'vivid',
                      label: l10n.wallpaperIntensityVivid,
                    ),
                  ],
                  onChanged: _setIntensity,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.wallpaperFeaturesTitle,
              children: [
                for (final (title, text) in widget.wallpaper.features(
                  l10n,
                )) ...[
                  Text(title, style: typography.title),
                  Text(text, style: typography.body),
                  const IuxGap.standard(),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.wallpaperBatteryTitle,
              description: widget.wallpaper.battery(l10n),
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
    final l10n = AppLocalizations.of(context);
    return Semantics(
      image: true,
      label: l10n.wallpaperPreviewSemantics(widget.wallpaper.title(l10n)),
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

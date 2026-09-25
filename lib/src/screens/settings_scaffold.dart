import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'neon.dart';
import 'wallpaper_screen.dart';

/// La fiche d'un widget : son aperçu en grand, posé sur un fond Halo au
/// choix (ou sur l'écran de verrouillage), son nom et ses étiquettes, les
/// sections de réglage [sections], et le bouton d'épinglage en bas.
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

  /// Le fond sous l'aperçu : celui qui est appliqué, Circuit à défaut.
  HaloWallpaper _background = circuitWallpaper;

  /// L'aperçu sur l'écran de verrouillage, assombri, sous l'horloge.
  bool _locked = false;

  @override
  void initState() {
    super.initState();
    widget.platform.canPin().then((value) {
      if (mounted) setState(() => _canPin = value);
    });
    _findActive();
  }

  Future<void> _findActive() async {
    for (final wallpaper in haloWallpapers) {
      if (await widget.platform.isWallpaperActive(kind: wallpaper.id)) {
        if (mounted) setState(() => _background = wallpaper);
        return;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final homeWidget = widget.homeWidget;
    final pin = _canPin && widget.allowPin;
    return Scaffold(
      backgroundColor: Neon.background,
      body: SingleChildScrollView(
        padding: EdgeInsets.zero,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _Stage(
              platform: widget.platform,
              background: _background,
              locked: _locked,
              onBack: () => Navigator.of(context).maybePop(),
              onBackground: (wallpaper) => setState(() {
                _background = wallpaper;
                _locked = false;
              }),
              onLocked: () => setState(() => _locked = true),
              child:
                  widget.preview ??
                  WidgetPreview(
                    homeWidget: homeWidget,
                    platform: widget.platform,
                    revision: widget.revision,
                  ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 22, 20, 0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Semantics(
                    header: true,
                    child: Text(
                      homeWidget.title(l10n),
                      style: Neon.display(28),
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(homeWidget.description(l10n), style: Neon.body(14)),
                  const SizedBox(height: 14),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      _Tag(_category(l10n, homeWidget.category)),
                      _Tag(l10n.detailTagHome),
                      _Tag(l10n.detailTagLock),
                    ],
                  ),
                ],
              ),
            ),
            for (final section in widget.sections)
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 20, 16, 0),
                child: section,
              ),
            const SizedBox(height: 24),
          ],
        ),
      ),
      bottomNavigationBar: pin
          ? DecoratedBox(
              decoration: const BoxDecoration(
                color: Neon.background,
                border: Border(top: BorderSide(color: Neon.line)),
              ),
              child: SafeArea(
                top: false,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 12, 20, 12),
                  child: _PinButton(
                    onPin: () => widget.platform.pin(homeWidget),
                  ),
                ),
              ),
            )
          : null,
    );
  }

  static String _category(AppLocalizations l10n, WuxWidgetCategory category) =>
      switch (category) {
        WuxWidgetCategory.time => l10n.catalogFilterTime,
        WuxWidgetCategory.weather => l10n.catalogFilterWeather,
        WuxWidgetCategory.system => l10n.catalogFilterSystem,
        WuxWidgetCategory.media => l10n.catalogFilterMedia,
      };
}

/// Le haut de la fiche : le fond choisi, l'aperçu du widget au milieu, le
/// retour et le choix du fond.
class _Stage extends StatelessWidget {
  const _Stage({
    required this.platform,
    required this.background,
    required this.locked,
    required this.onBack,
    required this.onBackground,
    required this.onLocked,
    required this.child,
  });

  final WuxPlatform platform;
  final HaloWallpaper background;
  final bool locked;
  final VoidCallback onBack;
  final ValueChanged<HaloWallpaper> onBackground;
  final VoidCallback onLocked;
  final Widget child;

  static const _height = 440.0;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final top = MediaQuery.paddingOf(context).top;
    final now = TimeOfDay.now();
    return SizedBox(
      height: _height + top,
      // L'aperçu du fond, au format du téléphone, déborde : on le recadre.
      child: ClipRect(
        child: LayoutBuilder(
          builder: (context, constraints) => Stack(
            fit: StackFit.expand,
            children: [
              ExcludeSemantics(
                child: ColoredBox(
                  color: Neon.stage,
                  child: OverflowBox(
                    maxHeight: constraints.maxWidth * 20 / 9,
                    child: WallpaperPreview(
                      key: ValueKey(background.id),
                      platform: platform,
                      wallpaper: background,
                      width: constraints.maxWidth,
                    ),
                  ),
                ),
              ),
              // Un voile, plus sombre sur l'écran de verrouillage.
              ColoredBox(
                color: Colors.black.withValues(alpha: locked ? 0.45 : 0.15),
              ),
              if (locked)
                Positioned(
                  top: top + 70,
                  left: 0,
                  right: 0,
                  child: ExcludeSemantics(
                    child: Text(
                      '${now.hour.toString().padLeft(2, '0')}:'
                      '${now.minute.toString().padLeft(2, '0')}',
                      textAlign: TextAlign.center,
                      style: Neon.display(64, color: Neon.text),
                    ),
                  ),
                ),
              Positioned(
                left: 24,
                right: 24,
                top: top + (locked ? 180 : 90),
                bottom: 72,
                child: Center(child: child),
              ),
              Positioned(
                top: top + 12,
                left: 12,
                child: Material(
                  color: Neon.surface.withValues(alpha: 0.8),
                  shape: const CircleBorder(),
                  child: NeonTappable(
                    label: l10n.back,
                    radius: 24,
                    onTap: onBack,
                    child: const SizedBox(
                      width: 48,
                      height: 48,
                      child: Icon(Icons.arrow_back, color: Neon.text),
                    ),
                  ),
                ),
              ),
              Positioned(
                left: 0,
                right: 0,
                bottom: 14,
                child: Semantics(
                  label: l10n.detailBackgrounds,
                  container: true,
                  child: SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Row(
                      children: [
                        for (final wallpaper in haloWallpapers) ...[
                          _StageChip(
                            label: l10n.detailOnWallpaper(
                              wallpaper.title(l10n),
                            ),
                            selected: !locked && wallpaper == background,
                            onSelect: () => onBackground(wallpaper),
                          ),
                          const SizedBox(width: 8),
                        ],
                        _StageChip(
                          label: l10n.detailLockScreen,
                          selected: locked,
                          onSelect: onLocked,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Un choix de fond, lisible sur n'importe quel fond.
class _StageChip extends StatelessWidget {
  const _StageChip({
    required this.label,
    required this.selected,
    required this.onSelect,
  });

  final String label;
  final bool selected;
  final VoidCallback onSelect;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: selected ? Neon.amber : Neon.background.withValues(alpha: 0.7),
      shape: StadiumBorder(
        side: selected
            ? BorderSide.none
            : const BorderSide(color: Neon.outline),
      ),
      child: NeonTappable(
        label: label,
        selected: selected,
        radius: 22,
        onTap: onSelect,
        child: Container(
          height: 40,
          padding: const EdgeInsets.symmetric(horizontal: 14),
          alignment: Alignment.center,
          child: Text(
            label,
            style: Neon.body(
              13,
              weight: selected ? FontWeight.w600 : FontWeight.w400,
              color: selected ? Neon.onAmber : Neon.text,
            ),
          ),
        ),
      ),
    );
  }
}

/// Une étiquette : ce que le widget est, pas une commande.
class _Tag extends StatelessWidget {
  const _Tag(this.label);

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Neon.outline),
      ),
      child: Text(
        label.toUpperCase(),
        style: Neon.mono(11, color: const Color(0xFFD5DBE6), spacing: 0.6),
      ),
    );
  }
}

/// « Ajouter à l'écran d'accueil », le grand bouton ambre du bas.
class _PinButton extends StatelessWidget {
  const _PinButton({required this.onPin});

  final VoidCallback onPin;

  @override
  Widget build(BuildContext context) {
    final label = AppLocalizations.of(context).pinToHome;
    return Material(
      color: Neon.amber,
      borderRadius: BorderRadius.circular(26),
      child: NeonTappable(
        label: label,
        radius: 26,
        onTap: onPin,
        child: Container(
          height: 52,
          alignment: Alignment.center,
          child: Text(
            label,
            style: Neon.body(15, weight: FontWeight.w600, color: Neon.onAmber),
          ),
        ),
      ),
    );
  }
}

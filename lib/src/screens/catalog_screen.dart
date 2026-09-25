import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

import '../home_widgets/catalog.dart';
import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'about_screen.dart';
import 'dream_screen.dart';
import 'screen_frame.dart';
import 'settings_screen.dart';
import 'wallpaper_screen.dart';
import 'widget_screen.dart';

/// La galerie de Halo : le fond d'écran du moment en grand, les fonds animés
/// en carrousel, puis les widgets, filtrables par famille, chacun avec un
/// aperçu d'exemple ; toucher une carte ouvre ses réglages, avec l'aperçu de
/// ses vraies données. L'écran de veille ferme la page.
///
/// Au premier lancement, demande l'accès à l'agenda, dont dépendent trois
/// widgets. La position, qui ne sert qu'à la météo, est demandée là-bas.
class CatalogScreen extends StatefulWidget {
  const CatalogScreen({
    super.key,
    required this.widgets,
    required this.platform,
  });

  final List<WuxHomeWidget> widgets;
  final WuxPlatform platform;

  @override
  State<CatalogScreen> createState() => _CatalogScreenState();
}

class _CatalogScreenState extends State<CatalogScreen> {
  List<WuxHomeWidget> get widgets => widget.widgets;
  WuxPlatform get platform => widget.platform;

  /// Famille affichée, `null` : tous les widgets.
  WuxWidgetCategory? _category;

  /// Le fond Halo appliqué, s'il y en a un.
  HaloWallpaper? _active;

  @override
  void initState() {
    super.initState();
    _findActive();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await _firstLaunch();
      final target = await platform.launchTarget();
      if (target != null) _openTarget(target);
    });
    platform.onOpen(_openTarget);
  }

  final _widgetsKey = GlobalKey();

  /// Un raccourci de l'icône ou une tuile des réglages rapides : le fond
  /// d'écran, les réglages, la météo, ou la liste des widgets.
  void _openTarget(String target) {
    if (!mounted) return;
    final navigator = Navigator.of(context);
    navigator.popUntil((route) => route.isFirst);
    switch (target) {
      case 'wallpaper':
        navigator.push<void>(
          MaterialPageRoute<void>(
            builder: (_) => WallpaperScreen(platform: platform),
          ),
        );
      case 'settings':
        navigator.push<void>(
          MaterialPageRoute<void>(
            builder: (_) => SettingsScreen(platform: platform),
          ),
        );
      case 'weather':
        navigator.push<void>(
          MaterialPageRoute<void>(
            builder: (_) =>
                WidgetScreen(homeWidget: weatherWidget, platform: platform),
          ),
        );
      case 'widgets':
        final section = _widgetsKey.currentContext;
        if (section != null) {
          Scrollable.ensureVisible(
            section,
            duration: const Duration(milliseconds: 300),
          );
        }
    }
  }

  Future<void> _findActive() async {
    for (final wallpaper in haloWallpapers) {
      if (await platform.isWallpaperActive(kind: wallpaper.id)) {
        if (mounted) setState(() => _active = wallpaper);
        return;
      }
    }
    if (mounted) setState(() => _active = null);
  }

  void _openWallpaper(HaloWallpaper wallpaper) {
    Navigator.of(context)
        .push<void>(
          MaterialPageRoute<void>(
            builder: (_) =>
                WallpaperScreen(platform: platform, wallpaper: wallpaper),
          ),
        )
        // De retour : le fond vient peut-être d'être appliqué.
        .then((_) => _findActive());
  }

  Future<void> _firstLaunch() async {
    if (!await platform.isFirstLaunch()) return;
    await platform.markLaunched();
    if (!await platform.hasCalendarPermission()) {
      await platform.requestCalendarPermission();
    }
  }

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
    final l10n = AppLocalizations.of(context);
    final shown = [
      for (final homeWidget in widgets)
        if (_category == null || homeWidget.category == _category) homeWidget,
    ];
    final categories = <(WuxWidgetCategory?, String)>[
      (null, l10n.catalogFilterAll),
      (WuxWidgetCategory.time, l10n.catalogFilterTime),
      (WuxWidgetCategory.weather, l10n.catalogFilterWeather),
      (WuxWidgetCategory.system, l10n.catalogFilterSystem),
      (WuxWidgetCategory.media, l10n.catalogFilterMedia),
    ];
    return Scaffold(
      body: ScreenFrame(
        title: l10n.appTitle,
        actions: [
          IuxIconButton(
            icon: Icons.settings_outlined,
            action: IuxActionDescriptor(
              semantics: IuxActionSemantics(label: l10n.catalogSettings),
            ),
            onActivate: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(
                builder: (_) => SettingsScreen(platform: platform),
              ),
            ),
          ),
          IuxIconButton(
            icon: Icons.info_outline,
            action: IuxActionDescriptor(
              semantics: IuxActionSemantics(label: l10n.catalogAbout),
            ),
            onActivate: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(
                builder: (_) => AboutScreen(platform: platform),
              ),
            ),
          ),
        ],
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _WallpaperHero(
              platform: platform,
              wallpaper: _active ?? circuitWallpaper,
              active: _active != null,
              onOpen: () => _openWallpaper(_active ?? circuitWallpaper),
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.catalogWallpapers,
              children: [
                // Tous construits (pas de liste paresseuse) : six fonds.
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  clipBehavior: Clip.none,
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      for (final (i, wallpaper) in haloWallpapers.indexed) ...[
                        if (i > 0) const SizedBox(width: 12),
                        _WallpaperTile(
                          platform: platform,
                          wallpaper: wallpaper,
                          active: wallpaper == _active,
                          onOpen: () => _openWallpaper(wallpaper),
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              key: _widgetsKey,
              title: l10n.catalogWidgets,
              children: [
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  clipBehavior: Clip.none,
                  child: IuxChipGroup(
                    label: l10n.catalogFilterLabel,
                    chips: [
                      for (final (category, label) in categories)
                        IuxFilterChip(
                          label: label,
                          selected: _category == category,
                          onSelectionChanged: (_) =>
                              setState(() => _category = category),
                        ),
                    ],
                  ),
                ),
                const IuxGap.standard(),
                for (final row in _rows(shown)) ...[
                  if (row.length == 1)
                    _WidgetTile(
                      homeWidget: row.single,
                      platform: platform,
                      onOpen: () => _open(context, row.single),
                    )
                  else
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        for (final (i, homeWidget) in row.indexed) ...[
                          if (i > 0) const SizedBox(width: 12),
                          Expanded(
                            child: _WidgetTile(
                              homeWidget: homeWidget,
                              platform: platform,
                              onOpen: () => _open(context, homeWidget),
                            ),
                          ),
                        ],
                      ],
                    ),
                  const IuxGap.standard(),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.dreamTitle,
              children: [
                IuxCard.tappable(
                  semanticLabel: l10n.dreamTitle,
                  hint: l10n.dreamDescription,
                  onActivate: () => Navigator.of(context).push<void>(
                    MaterialPageRoute<void>(
                      builder: (_) => DreamScreen(platform: platform),
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        l10n.dreamTitle,
                        style: IuxTypographyTheme.of(context).title,
                      ),
                      Text(
                        l10n.dreamDescription,
                        style: IuxTypographyTheme.of(context).body,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  /// Les widgets en rangées : les étroits deux par deux, les larges seuls,
  /// pour que chacun garde une taille lisible.
  static List<List<WuxHomeWidget>> _rows(List<WuxHomeWidget> shown) {
    final rows = <List<WuxHomeWidget>>[];
    WuxHomeWidget? waiting;
    for (final homeWidget in shown) {
      if (homeWidget.previewSize.width > _narrow) {
        rows.add([homeWidget]);
      } else if (waiting == null) {
        waiting = homeWidget;
        rows.add([homeWidget]);
      } else {
        rows[rows.indexWhere((row) => row.first == waiting)] = [
          waiting,
          homeWidget,
        ];
        waiting = null;
      }
    }
    return rows;
  }

  /// Largeur (dp) sous laquelle un widget partage sa rangée.
  static const _narrow = 220;
}

/// Le fond d'écran du moment, en grand : son aperçu, son nom, et s'il est
/// appliqué.
class _WallpaperHero extends StatelessWidget {
  const _WallpaperHero({
    required this.platform,
    required this.wallpaper,
    required this.active,
    required this.onOpen,
  });

  final WuxPlatform platform;
  final HaloWallpaper wallpaper;
  final bool active;
  final VoidCallback onOpen;

  static const _height = 280.0;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final typography = IuxTypographyTheme.of(context);
    final title = wallpaper.title(l10n);
    return IuxCard.tappable(
      semanticLabel: title,
      hint: wallpaper.description(l10n),
      onActivate: onOpen,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(20),
        child: SizedBox(
          height: _height,
          child: LayoutBuilder(
            builder: (context, constraints) => Stack(
              fit: StackFit.expand,
              children: [
                // L'aperçu au format du téléphone, recadré au milieu.
                OverflowBox(
                  maxHeight: constraints.maxWidth * 20 / 9,
                  child: WallpaperPreview(
                    key: ValueKey(wallpaper.id),
                    platform: platform,
                    wallpaper: wallpaper,
                    width: constraints.maxWidth,
                  ),
                ),
                const DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      stops: [0.45, 1],
                      colors: [Color(0x0005070D), Color(0xF005070D)],
                    ),
                  ),
                ),
                Positioned(
                  left: 20,
                  right: 20,
                  bottom: 18,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        (active ? l10n.catalogActive : l10n.catalogFeatured)
                            .toUpperCase(),
                        style: typography.overline.copyWith(
                          color: _amber,
                          letterSpacing: 1.6,
                        ),
                      ),
                      Text(
                        title,
                        style: typography.headline.copyWith(
                          color: Colors.white,
                        ),
                      ),
                      Text(
                        wallpaper.description(l10n),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: typography.body.copyWith(
                          color: const Color(0xFFD5DBE6),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Un fond d'écran du carrousel : son aperçu en portrait et son nom.
class _WallpaperTile extends StatelessWidget {
  const _WallpaperTile({
    required this.platform,
    required this.wallpaper,
    required this.active,
    required this.onOpen,
  });

  final WuxPlatform platform;
  final HaloWallpaper wallpaper;
  final bool active;
  final VoidCallback onOpen;

  static const _width = 116.0;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final typography = IuxTypographyTheme.of(context);
    return SizedBox(
      width: _width + 32,
      child: IuxCard.tappable(
        semanticLabel: wallpaper.title(l10n),
        hint: wallpaper.description(l10n),
        onActivate: onOpen,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            DecoratedBox(
              position: DecorationPosition.foreground,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                border: active ? Border.all(color: _amber, width: 2) : null,
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: WallpaperPreview(
                  platform: platform,
                  wallpaper: wallpaper,
                  width: _width,
                ),
              ),
            ),
            const IuxGap.standard(),
            Text(
              wallpaper.title(l10n),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: typography.title,
            ),
            if (active) Text(l10n.catalogActive, style: typography.supporting),
          ],
        ),
      ),
    );
  }
}

/// Un widget de la galerie : son aperçu sur un fond sombre, et son nom.
class _WidgetTile extends StatelessWidget {
  const _WidgetTile({
    required this.homeWidget,
    required this.platform,
    required this.onOpen,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final typography = IuxTypographyTheme.of(context);
    return IuxCard.tappable(
      semanticLabel: homeWidget.title(l10n),
      hint: homeWidget.description(l10n),
      onActivate: onOpen,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(16),
            child: WallpaperFrame(
              child: WidgetPreview(
                homeWidget: homeWidget,
                platform: platform,
                sample: true,
              ),
            ),
          ),
          const IuxGap.standard(),
          Text(homeWidget.title(l10n), style: typography.title),
          Text(
            homeWidget.description(l10n),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: typography.supporting,
          ),
        ],
      ),
    );
  }
}

/// L'ambre de Halo, sur les fonds sombres des aperçus.
const _amber = Color(0xFFF2C178);

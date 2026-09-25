import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';

import '../home_widgets/catalog.dart';
import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'dream_screen.dart';
import 'neon.dart';
import 'settings_screen.dart';
import 'wallpaper_screen.dart';
import 'widget_screen.dart';

/// L'accueil de Halo, en trois onglets : la galerie (le fond du moment en
/// grand, les fonds animés en carrousel, les widgets en mosaïque, filtrables
/// par famille), les fonds animés en grand avec l'écran de veille, et ce qui
/// est déjà posé sur le téléphone. Toucher un widget ouvre ses réglages, avec
/// l'aperçu de ses vraies données.
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

class _CatalogScreenState extends State<CatalogScreen>
    with WidgetsBindingObserver {
  List<WuxHomeWidget> get widgets => widget.widgets;
  WuxPlatform get platform => widget.platform;

  /// Onglet affiché : galerie, fonds, mes ajouts.
  int _tab = 0;

  /// Famille affichée, `null` : tous les widgets.
  WuxWidgetCategory? _category;

  /// Texte cherché, `null` : pas de recherche ouverte.
  String? _query;

  /// Le fond Halo appliqué, s'il y en a un.
  HaloWallpaper? _active;

  /// Les classes Android des widgets posés.
  Set<String> _placed = const {};

  final _widgetsKey = GlobalKey();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refresh();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await _firstLaunch();
      final target = await platform.launchTarget();
      if (target != null) _openTarget(target);
    });
    platform.onOpen(_openTarget);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // De retour sur Halo : un fond ou un widget vient peut-être d'être posé.
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    HaloWallpaper? active;
    for (final wallpaper in haloWallpapers) {
      if (await platform.isWallpaperActive(kind: wallpaper.id)) {
        active = wallpaper;
        break;
      }
    }
    final placed = await platform.placedWidgets();
    if (!mounted) return;
    setState(() {
      _active = active;
      _placed = placed;
    });
  }

  /// Un raccourci de l'icône ou une tuile des réglages rapides : le fond
  /// d'écran, les réglages, la météo, ou la liste des widgets.
  void _openTarget(String target) {
    if (!mounted) return;
    final navigator = Navigator.of(context);
    navigator.popUntil((route) => route.isFirst);
    switch (target) {
      case 'wallpaper':
        _openWallpaper(_active ?? circuitWallpaper);
      case 'settings':
        _push(SettingsScreen(platform: platform));
      case 'weather':
        _openWidget(weatherWidget);
      case 'widgets':
        setState(() => _tab = 0);
        WidgetsBinding.instance.addPostFrameCallback((_) {
          final section = _widgetsKey.currentContext;
          if (section != null) {
            Scrollable.ensureVisible(
              section,
              duration: const Duration(milliseconds: 300),
            );
          }
        });
    }
  }

  Future<void> _firstLaunch() async {
    if (!await platform.isFirstLaunch()) return;
    await platform.markLaunched();
    if (!await platform.hasCalendarPermission()) {
      await platform.requestCalendarPermission();
    }
  }

  Future<void> _push(Widget screen) async {
    await Navigator.of(context)
        .push<void>(MaterialPageRoute<void>(builder: (_) => screen));
    _refresh();
  }

  void _openWallpaper(HaloWallpaper wallpaper) =>
      _push(WallpaperScreen(platform: platform, wallpaper: wallpaper));

  void _openWidget(WuxHomeWidget homeWidget) =>
      _push(WidgetScreen(homeWidget: homeWidget, platform: platform));

  @override
  Widget build(BuildContext context) {
    return Theme(
      data: Theme.of(context).copyWith(
        textSelectionTheme: const TextSelectionThemeData(
          cursorColor: Neon.amber,
        ),
      ),
      child: Scaffold(
        backgroundColor: Neon.background,
        body: SafeArea(
          bottom: false,
          child: switch (_tab) {
            0 => _gallery(context),
            1 => _WallpapersTab(
              platform: platform,
              active: _active,
              onOpen: _openWallpaper,
              onDream: () => _push(DreamScreen(platform: platform)),
            ),
            _ => _MineTab(
              platform: platform,
              active: _active,
              placed: [
                for (final homeWidget in widgets)
                  if (_placed.contains(homeWidget.androidProvider)) homeWidget,
              ],
              onOpenWallpaper: _openWallpaper,
              onOpenWidget: _openWidget,
            ),
          },
        ),
        bottomNavigationBar: _NavigationBar(
          selected: _tab,
          onSelect: (tab) {
            setState(() => _tab = tab);
            if (tab == 2) _refresh();
          },
        ),
      ),
    );
  }

  Widget _gallery(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final query = _query?.trim().toLowerCase() ?? '';
    final shown = [
      for (final homeWidget in widgets)
        if ((_category == null || homeWidget.category == _category) &&
            (query.isEmpty ||
                homeWidget.title(l10n).toLowerCase().contains(query) ||
                homeWidget.description(l10n).toLowerCase().contains(query)))
          homeWidget,
    ];
    final categories = <(WuxWidgetCategory?, String)>[
      (null, l10n.catalogFilterAll),
      (WuxWidgetCategory.time, l10n.catalogFilterTime),
      (WuxWidgetCategory.weather, l10n.catalogFilterWeather),
      (WuxWidgetCategory.system, l10n.catalogFilterSystem),
      (WuxWidgetCategory.media, l10n.catalogFilterMedia),
    ];
    final active = _active ?? circuitWallpaper;
    return SingleChildScrollView(
      padding: const EdgeInsets.only(bottom: 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _Header(
            searching: _query != null,
            onSearch: () => setState(() => _query = _query == null ? '' : null),
            onSettings: () => _push(SettingsScreen(platform: platform)),
          ),
          if (_query != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 4, 16, 8),
              child: _SearchField(
                onChanged: (value) => setState(() => _query = value),
              ),
            ),
          // Pendant une recherche, seuls les widgets restent : c'est eux
          // qu'elle filtre.
          if (_query == null) ...[
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
              child: _Hero(
                platform: platform,
                wallpaper: active,
                active: _active != null,
                onOpen: () => _openWallpaper(active),
                onChange: () => setState(() => _tab = 1),
              ),
            ),
            _SectionHeader(
              title: l10n.catalogWallpapers,
              trailing: _TextLink(
                label: l10n.catalogSeeAll,
                onTap: () => setState(() => _tab = 1),
              ),
            ),
            // Tous construits (pas de liste paresseuse) : six fonds.
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  for (final (i, wallpaper) in haloWallpapers.indexed) ...[
                    if (i > 0) const SizedBox(width: 12),
                    _WallpaperTile(
                      platform: platform,
                      wallpaper: wallpaper,
                      active: wallpaper == _active,
                      width: 118,
                      height: 200,
                      onOpen: () => _openWallpaper(wallpaper),
                    ),
                  ],
                ],
              ),
            ),
          ],
          _SectionHeader(
            key: _widgetsKey,
            title: l10n.catalogWidgets,
            trailing: Text(
              l10n.catalogWidgetCount(shown.length),
              style: Neon.mono(12, color: Neon.faint),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
            child: Semantics(
              label: l10n.catalogFilterLabel,
              container: true,
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                clipBehavior: Clip.none,
                child: Row(
                  children: [
                    for (final (i, (category, label))
                        in categories.indexed) ...[
                      if (i > 0) const SizedBox(width: 8),
                      NeonChip(
                        label: label,
                        selected: _category == category,
                        onSelect: () => setState(() => _category = category),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: shown.isEmpty
                ? Text(l10n.catalogNoMatch, style: Neon.body(14))
                : _Mosaic(
                    widgets: shown,
                    platform: platform,
                    onOpen: _openWidget,
                  ),
          ),
        ],
      ),
    );
  }
}

/// Le logo, le nom, la recherche et les réglages.
class _Header extends StatelessWidget {
  const _Header({
    required this.searching,
    required this.onSearch,
    required this.onSettings,
  });

  final bool searching;
  final VoidCallback onSearch;
  final VoidCallback onSettings;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 12, 4),
      child: Row(
        children: [
          ExcludeSemantics(
            child: Container(
              width: 26,
              height: 26,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: Neon.amber, width: 2.4),
              ),
              alignment: Alignment.center,
              child: Container(
                width: 8,
                height: 8,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  color: Neon.amber,
                ),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Semantics(
              header: true,
              label: l10n.appTitle,
              excludeSemantics: true,
              child: Text(
                'HALO',
                style: Neon.display(
                  26,
                  weight: FontWeight.w700,
                ).copyWith(letterSpacing: 1),
              ),
            ),
          ),
          _IconButton(
            icon: searching ? Icons.close : Icons.search,
            label: searching ? l10n.catalogSearchClose : l10n.catalogSearch,
            onTap: onSearch,
          ),
          _IconButton(
            icon: Icons.settings_outlined,
            label: l10n.catalogSettings,
            onTap: onSettings,
          ),
        ],
      ),
    );
  }
}

class _IconButton extends StatelessWidget {
  const _IconButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return NeonTappable(
      label: label,
      onTap: onTap,
      radius: 24,
      child: SizedBox(
        width: 48,
        height: 48,
        child: Icon(icon, color: Neon.text, size: 22),
      ),
    );
  }
}

class _SearchField extends StatelessWidget {
  const _SearchField({required this.onChanged});

  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return TextField(
      autofocus: true,
      onChanged: onChanged,
      style: Neon.body(15, color: Neon.text),
      decoration: InputDecoration(
        hintText: l10n.catalogSearch,
        hintStyle: Neon.body(15, color: Neon.faint),
        prefixIcon: const Icon(Icons.search, color: Neon.faint),
        filled: true,
        fillColor: Neon.surface,
        contentPadding: const EdgeInsets.symmetric(vertical: 14),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(22),
          borderSide: const BorderSide(color: Neon.outline),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(22),
          borderSide: const BorderSide(color: Neon.amber, width: 1.5),
        ),
      ),
    );
  }
}

/// Le fond d'écran du moment, en grand, recadré au milieu.
class _Hero extends StatelessWidget {
  const _Hero({
    required this.platform,
    required this.wallpaper,
    required this.active,
    required this.onOpen,
    required this.onChange,
  });

  final WuxPlatform platform;
  final HaloWallpaper wallpaper;
  final bool active;
  final VoidCallback onOpen;
  final VoidCallback onChange;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final title = wallpaper.title(l10n);
    return ClipRRect(
      borderRadius: BorderRadius.circular(28),
      child: DecoratedBox(
        position: DecorationPosition.foreground,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(28),
          border: Border.all(color: Neon.line),
        ),
        child: SizedBox(
          height: 300,
          child: LayoutBuilder(
            builder: (context, constraints) => Stack(
              fit: StackFit.expand,
              children: [
                NeonTappable(
                  label: title,
                  hint: wallpaper.description(l10n),
                  radius: 28,
                  onTap: onOpen,
                  child: ColoredBox(
                    color: Neon.stage,
                    child: OverflowBox(
                      maxHeight: constraints.maxWidth * 20 / 9,
                      child: WallpaperPreview(
                        key: ValueKey(wallpaper.id),
                        platform: platform,
                        wallpaper: wallpaper,
                        width: constraints.maxWidth,
                      ),
                    ),
                  ),
                ),
                // Le texte laisse passer le toucher jusqu'à l'aperçu.
                Positioned(
                  left: 0,
                  right: 0,
                  bottom: 0,
                  child: IgnorePointer(
                    child: Container(
                      padding: const EdgeInsets.fromLTRB(20, 40, 124, 18),
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.bottomCenter,
                          end: Alignment.topCenter,
                          colors: [Color(0xF005070D), Color(0x0005070D)],
                        ),
                      ),
                      child: ExcludeSemantics(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              '● ${(active ? l10n.catalogActive : l10n.catalogFeatured).toUpperCase()}',
                              style: Neon.mono(11, spacing: 1.8),
                            ),
                            const SizedBox(height: 4),
                            Text(title, style: Neon.display(28)),
                            const SizedBox(height: 2),
                            Text(
                              active
                                  ? l10n.catalogHeroApplied
                                  : wallpaper.description(l10n),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: Neon.body(13),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
                Positioned(
                  right: 20,
                  bottom: 18,
                  child: NeonPill(label: l10n.catalogChange, onTap: onChange),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({super.key, required this.title, this.trailing});

  final String title;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 22, 20, 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Expanded(
            child: Semantics(
              header: true,
              child: Text(title, style: Neon.display(20)),
            ),
          ),
          ?trailing,
        ],
      ),
    );
  }
}

class _TextLink extends StatelessWidget {
  const _TextLink({required this.label, required this.onTap});

  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return NeonTappable(
      label: label,
      onTap: onTap,
      radius: 8,
      child: ConstrainedBox(
        constraints: const BoxConstraints(minHeight: 44),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 6),
          child: Center(
            widthFactor: 1,
            child: Text(
              label,
              style: Neon.body(14, weight: FontWeight.w500, color: Neon.amber),
            ),
          ),
        ),
      ),
    );
  }
}

/// Les identifiants des fonds marqués « Nouveau ».
const _newWallpapers = {'tron'};

/// Un fond d'écran en portrait : son aperçu recadré et son nom.
class _WallpaperTile extends StatelessWidget {
  const _WallpaperTile({
    required this.platform,
    required this.wallpaper,
    required this.active,
    required this.width,
    required this.height,
    required this.onOpen,
    this.subtitle,
  });

  final WuxPlatform platform;
  final HaloWallpaper wallpaper;
  final bool active;
  final double width;
  final double height;
  final String? subtitle;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final badge = active
        ? (l10n.catalogActive, Neon.amber, Neon.onAmber)
        : _newWallpapers.contains(wallpaper.id)
        ? (l10n.catalogNew, Neon.cyan, Neon.onCyan)
        : null;
    return SizedBox(
      width: width,
      child: NeonTappable(
        label: wallpaper.title(l10n),
        hint: wallpaper.description(l10n),
        onTap: onOpen,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            DecoratedBox(
              position: DecorationPosition.foreground,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                border: active
                    ? Border.all(color: Neon.amber, width: 2)
                    : Border.all(color: Neon.line),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(20),
                child: SizedBox(
                  width: width,
                  height: height,
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      ColoredBox(
                        color: Neon.stage,
                        child: OverflowBox(
                          maxHeight: width * 20 / 9,
                          child: WallpaperPreview(
                            platform: platform,
                            wallpaper: wallpaper,
                            width: width,
                          ),
                        ),
                      ),
                      if (badge != null)
                        Positioned(
                          top: 8,
                          right: 8,
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 7,
                              vertical: 3,
                            ),
                            decoration: BoxDecoration(
                              color: badge.$2,
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              badge.$1.toUpperCase(),
                              style: Neon.mono(
                                10,
                                weight: FontWeight.w600,
                                color: badge.$3,
                              ),
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              wallpaper.title(l10n),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: Neon.body(
                subtitle == null ? 14 : 15,
                weight: subtitle == null ? FontWeight.w500 : FontWeight.w600,
                color: Neon.text,
              ),
            ),
            if (subtitle != null)
              Text(
                subtitle!,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: Neon.body(12),
              ),
          ],
        ),
      ),
    );
  }
}

/// Les widgets en deux colonnes, chacun dans la plus courte, dans l'ordre
/// du catalogue.
class _Mosaic extends StatelessWidget {
  const _Mosaic({
    required this.widgets,
    required this.platform,
    required this.onOpen,
  });

  final List<WuxHomeWidget> widgets;
  final WuxPlatform platform;
  final ValueChanged<WuxHomeWidget> onOpen;

  static const _gap = 12.0;
  static const _padding = 10.0;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final column = (constraints.maxWidth - _gap) / 2;
        final columns = [<WuxHomeWidget>[], <WuxHomeWidget>[]];
        final heights = [0.0, 0.0];
        for (final homeWidget in widgets) {
          final size = homeWidget.previewSize;
          final inner = column - _padding * 2;
          final scale = (inner / size.width).clamp(0.0, 1.0);
          // Aperçu, marges et nom.
          final height = size.height * scale + _padding * 2 + 34 + _gap;
          final shortest = heights[0] <= heights[1] ? 0 : 1;
          columns[shortest].add(homeWidget);
          heights[shortest] += height;
        }
        return Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            for (final (i, list) in columns.indexed) ...[
              if (i > 0) const SizedBox(width: _gap),
              Expanded(
                child: Column(
                  children: [
                    for (final homeWidget in list)
                      Padding(
                        padding: const EdgeInsets.only(bottom: _gap),
                        child: _WidgetTile(
                          homeWidget: homeWidget,
                          platform: platform,
                          onOpen: () => onOpen(homeWidget),
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ],
        );
      },
    );
  }
}

/// Un widget de la mosaïque : son aperçu sur une carte sombre, son nom.
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
    return NeonTappable(
      label: homeWidget.title(l10n),
      hint: homeWidget.description(l10n),
      onTap: () => onOpen(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Container(
            padding: const EdgeInsets.all(_Mosaic._padding),
            decoration: BoxDecoration(
              color: Neon.surface,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: Neon.line),
            ),
            child: Center(
              child: WidgetPreview(
                homeWidget: homeWidget,
                platform: platform,
                sample: true,
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            homeWidget.title(l10n),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: Neon.body(14, weight: FontWeight.w500, color: Neon.text),
          ),
          const SizedBox(height: 4),
        ],
      ),
    );
  }
}

/// Les fonds animés en grand, deux par rangée, et l'écran de veille.
class _WallpapersTab extends StatelessWidget {
  const _WallpapersTab({
    required this.platform,
    required this.active,
    required this.onOpen,
    required this.onDream,
  });

  final WuxPlatform platform;
  final HaloWallpaper? active;
  final ValueChanged<HaloWallpaper> onOpen;
  final VoidCallback onDream;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(16, 20, 16, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Semantics(
            header: true,
            child: Text(l10n.catalogWallpapers, style: Neon.display(24)),
          ),
          const SizedBox(height: 6),
          Text(l10n.catalogWallpapersIntro, style: Neon.body(14)),
          const SizedBox(height: 16),
          LayoutBuilder(
            builder: (context, constraints) {
              final width = (constraints.maxWidth - 14) / 2;
              return Wrap(
                spacing: 14,
                runSpacing: 16,
                children: [
                  for (final wallpaper in haloWallpapers)
                    _WallpaperTile(
                      platform: platform,
                      wallpaper: wallpaper,
                      active: wallpaper == active,
                      width: width,
                      height: width * 1.75,
                      subtitle: wallpaper.description(l10n),
                      onOpen: () => onOpen(wallpaper),
                    ),
                ],
              );
            },
          ),
          const SizedBox(height: 28),
          Semantics(
            header: true,
            child: Text(l10n.dreamTitle, style: Neon.display(20)),
          ),
          const SizedBox(height: 10),
          NeonTappable(
            label: l10n.dreamTitle,
            hint: l10n.dreamDescription,
            onTap: onDream,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Neon.surface,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: Neon.line),
              ),
              child: Row(
                children: [
                  const Icon(Icons.nightlight_outlined, color: Neon.amber),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          l10n.dreamTitle,
                          style: Neon.body(
                            15,
                            weight: FontWeight.w600,
                            color: Neon.text,
                          ),
                        ),
                        Text(l10n.dreamDescription, style: Neon.body(13)),
                      ],
                    ),
                  ),
                  const Icon(Icons.chevron_right, color: Neon.faint),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Ce qui est déjà sur le téléphone : le fond Halo appliqué et les widgets
/// posés.
class _MineTab extends StatelessWidget {
  const _MineTab({
    required this.platform,
    required this.active,
    required this.placed,
    required this.onOpenWallpaper,
    required this.onOpenWidget,
  });

  final WuxPlatform platform;
  final HaloWallpaper? active;
  final List<WuxHomeWidget> placed;
  final ValueChanged<HaloWallpaper> onOpenWallpaper;
  final ValueChanged<WuxHomeWidget> onOpenWidget;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final active = this.active;
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(16, 20, 16, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Semantics(
            header: true,
            child: Text(l10n.catalogNavMine, style: Neon.display(24)),
          ),
          const SizedBox(height: 20),
          Text(l10n.catalogMineWallpaper, style: Neon.display(18)),
          const SizedBox(height: 10),
          if (active == null)
            Text(l10n.catalogMineNoWallpaper, style: Neon.body(14))
          else
            Align(
              alignment: Alignment.centerLeft,
              child: _WallpaperTile(
                platform: platform,
                wallpaper: active,
                active: true,
                width: 150,
                height: 260,
                subtitle: active.description(l10n),
                onOpen: () => onOpenWallpaper(active),
              ),
            ),
          const SizedBox(height: 28),
          Text(l10n.catalogMineWidgets, style: Neon.display(18)),
          const SizedBox(height: 10),
          if (placed.isEmpty)
            Text(l10n.catalogMineNoWidgets, style: Neon.body(14))
          else
            _Mosaic(widgets: placed, platform: platform, onOpen: onOpenWidget),
        ],
      ),
    );
  }
}

/// La barre du bas : galerie, fonds, mes ajouts.
class _NavigationBar extends StatelessWidget {
  const _NavigationBar({required this.selected, required this.onSelect});

  final int selected;
  final ValueChanged<int> onSelect;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final items = [
      (Icons.grid_view_rounded, l10n.catalogNavGallery),
      (Icons.smartphone_outlined, l10n.catalogNavWallpapers),
      (Icons.add_circle_outline, l10n.catalogNavMine),
    ];
    return DecoratedBox(
      decoration: const BoxDecoration(
        color: Neon.navigation,
        border: Border(top: BorderSide(color: Neon.line)),
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 72,
          child: Row(
            children: [
              for (final (i, (icon, label)) in items.indexed)
                Expanded(
                  child: NeonTappable(
                    label: label,
                    selected: i == selected,
                    radius: 16,
                    onTap: () => onSelect(i),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          width: 56,
                          height: 30,
                          decoration: BoxDecoration(
                            color: i == selected
                                ? Neon.amber.withValues(alpha: 0.18)
                                : Colors.transparent,
                            borderRadius: BorderRadius.circular(15),
                          ),
                          child: Icon(
                            icon,
                            size: 20,
                            color: i == selected ? Neon.amber : Neon.muted,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          label,
                          style: Neon.body(
                            12,
                            weight: i == selected
                                ? FontWeight.w600
                                : FontWeight.w400,
                            color: i == selected ? Neon.amber : Neon.muted,
                          ),
                        ),
                      ],
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

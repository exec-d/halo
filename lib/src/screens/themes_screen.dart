import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

import '../platform/wux_platform.dart';
import 'neon.dart';
import 'screen_frame.dart';
import 'wallpaper_screen.dart';

/// Un son d'un thème : son rôle (`ring`, `notification`, `alarm`, partagé
/// avec `ThemeSounds.kt`), son nom et ce qu'on y entend.
typedef ThemeSound = (
  String kind,
  String Function(AppLocalizations l10n) name,
  String Function(AppLocalizations l10n) text,
);

/// Un thème Halo : un fond animé (même identifiant, partagé avec
/// `HaloThemes.kt`), les couleurs qu'Android en tirera, et trois sons.
class HaloTheme {
  const HaloTheme({
    required this.wallpaper,
    required this.description,
    required this.accent,
    required this.onAccent,
    required this.swatches,
    required this.sounds,
  });

  final HaloWallpaper wallpaper;
  final String Function(AppLocalizations l10n) description;

  /// La couleur principale qu'Android tirera du fond, et le texte posé dessus.
  final Color accent;
  final Color onAccent;

  /// Principale, secondaire, tertiaire, surface : ce que montrera le système.
  final List<Color> swatches;
  final List<ThemeSound> sounds;

  String get id => wallpaper.id;
  String title(AppLocalizations l10n) => wallpaper.title(l10n);
}

const circuitTheme = HaloTheme(
  wallpaper: circuitWallpaper,
  description: _circuitDescription,
  accent: Color(0xFF8FCDFF),
  onAccent: Color(0xFF00344F),
  swatches: [
    Color(0xFF8FCDFF),
    Color(0xFFB6C9D8),
    Color(0xFFCDC0E9),
    Color(0xFF0F1418),
  ],
  sounds: [
    ('ring', _circuitRing, _circuitRingText),
    ('notification', _circuitNotification, _circuitNotificationText),
    ('alarm', _circuitAlarm, _circuitAlarmText),
  ],
);
String _circuitDescription(AppLocalizations l) => l.themeCircuitDescription;
String _circuitRing(AppLocalizations l) => l.themeSoundCircuitRing;
String _circuitRingText(AppLocalizations l) => l.themeSoundCircuitRingText;
String _circuitNotification(AppLocalizations l) =>
    l.themeSoundCircuitNotification;
String _circuitNotificationText(AppLocalizations l) =>
    l.themeSoundCircuitNotificationText;
String _circuitAlarm(AppLocalizations l) => l.themeSoundCircuitAlarm;
String _circuitAlarmText(AppLocalizations l) => l.themeSoundCircuitAlarmText;

const fluxTheme = HaloTheme(
  wallpaper: fluxWallpaper,
  description: _fluxDescription,
  accent: Color(0xFFFFB77C),
  onAccent: Color(0xFF4C2700),
  swatches: [
    Color(0xFFFFB77C),
    Color(0xFFE3C0A5),
    Color(0xFFC4CB97),
    Color(0xFF19120C),
  ],
  sounds: [
    ('ring', _fluxRing, _fluxRingText),
    ('notification', _fluxNotification, _fluxNotificationText),
    ('alarm', _fluxAlarm, _fluxAlarmText),
  ],
);
String _fluxDescription(AppLocalizations l) => l.themeFluxDescription;
String _fluxRing(AppLocalizations l) => l.themeSoundFluxRing;
String _fluxRingText(AppLocalizations l) => l.themeSoundFluxRingText;
String _fluxNotification(AppLocalizations l) => l.themeSoundFluxNotification;
String _fluxNotificationText(AppLocalizations l) =>
    l.themeSoundFluxNotificationText;
String _fluxAlarm(AppLocalizations l) => l.themeSoundFluxAlarm;
String _fluxAlarmText(AppLocalizations l) => l.themeSoundFluxAlarmText;

const arcTheme = HaloTheme(
  wallpaper: arcWallpaper,
  description: _arcDescription,
  accent: Color(0xFFFFB4A8),
  onAccent: Color(0xFF690003),
  swatches: [
    Color(0xFFFFB4A8),
    Color(0xFFE7BDB6),
    Color(0xFFE2C46D),
    Color(0xFF1A1110),
  ],
  sounds: [
    ('ring', _arcRing, _arcRingText),
    ('notification', _arcNotification, _arcNotificationText),
    ('alarm', _arcAlarm, _arcAlarmText),
  ],
);
String _arcDescription(AppLocalizations l) => l.themeArcDescription;
String _arcRing(AppLocalizations l) => l.themeSoundArcRing;
String _arcRingText(AppLocalizations l) => l.themeSoundArcRingText;
String _arcNotification(AppLocalizations l) => l.themeSoundArcNotification;
String _arcNotificationText(AppLocalizations l) =>
    l.themeSoundArcNotificationText;
String _arcAlarm(AppLocalizations l) => l.themeSoundArcAlarm;
String _arcAlarmText(AppLocalizations l) => l.themeSoundArcAlarmText;

/// Les thèmes, dans l'ordre des fonds.
const haloThemes = [circuitTheme, fluxTheme, arcTheme];

/// Le nom d'un rôle de son (`ring`…), pour l'écran.
String themeSoundRole(AppLocalizations l10n, String kind) => switch (kind) {
  'ring' => l10n.themeSoundRing,
  'notification' => l10n.themeSoundNotification,
  _ => l10n.themeSoundAlarm,
};

/// La liste des thèmes : chacun avec son fond dans sa palette, ses couleurs
/// et ce qu'il contient.
class ThemesScreen extends StatefulWidget {
  const ThemesScreen({super.key, required this.platform});

  final WuxPlatform platform;

  @override
  State<ThemesScreen> createState() => _ThemesScreenState();
}

class _ThemesScreenState extends State<ThemesScreen>
    with WidgetsBindingObserver {
  String? _active;

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
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    final state = await widget.platform.themeState();
    if (mounted) setState(() => _active = state.active);
  }

  Future<void> _open(HaloTheme theme) async {
    await Navigator.of(context).push<void>(
      MaterialPageRoute<void>(
        builder: (_) => ThemeScreen(platform: widget.platform, theme: theme),
      ),
    );
    _refresh();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      body: ScreenFrame(
        title: l10n.themesTitle,
        canGoBack: true,
        child: IuxSection(
          description: l10n.themesIntro,
          children: [
            for (final theme in haloThemes) ...[
              ThemeCard(
                platform: widget.platform,
                theme: theme,
                active: theme.id == _active,
                onOpen: () => _open(theme),
              ),
              const IuxGap.standard(),
            ],
          ],
        ),
      ),
    );
  }
}

/// Un thème dans une liste : son fond, son nom, ses couleurs, son contenu.
class ThemeCard extends StatelessWidget {
  const ThemeCard({
    super.key,
    required this.platform,
    required this.theme,
    required this.active,
    required this.onOpen,
  });

  final WuxPlatform platform;
  final HaloTheme theme;
  final bool active;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Material(
      color: active
          ? Color.alphaBlend(theme.accent.withValues(alpha: 0.1), Neon.surface)
          : Neon.surface,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: BorderSide(color: active ? theme.accent : Neon.line),
      ),
      child: NeonTappable(
        label: theme.title(l10n),
        hint: theme.description(l10n),
        selected: active,
        onTap: onOpen,
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: ThemePreview(
                  platform: platform,
                  theme: theme,
                  width: 72,
                  height: 112,
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Wrap(
                      spacing: 8,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        Text(
                          theme.title(l10n),
                          style: Neon.display(17, color: Neon.text),
                        ),
                        if (active)
                          Text(
                            l10n.catalogActive.toUpperCase(),
                            style: Neon.mono(
                              10,
                              weight: FontWeight.w600,
                              color: theme.accent,
                              spacing: 1,
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Row(
                      children: [
                        for (final color in theme.swatches.take(3)) ...[
                          _Dot(color: color),
                          const SizedBox(width: 5),
                        ],
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text(theme.description(l10n), style: Neon.body(13)),
                    const SizedBox(height: 2),
                    Text(
                      l10n.themesContents,
                      style: Neon.mono(10.5, color: Neon.faint),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 14,
      height: 14,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        border: Border.all(color: Colors.white.withValues(alpha: 0.08)),
      ),
    );
  }
}

/// Le fond d'un thème dessiné par Android dans la palette du thème, recadré
/// à [width] × [height].
class ThemePreview extends StatefulWidget {
  const ThemePreview({
    super.key,
    required this.platform,
    required this.theme,
    required this.width,
    required this.height,
  });

  final WuxPlatform platform;
  final HaloTheme theme;
  final double width;
  final double height;

  @override
  State<ThemePreview> createState() => _ThemePreviewState();
}

class _ThemePreviewState extends State<ThemePreview> {
  Uint8List? _image;
  bool _requested = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_requested) return;
    _requested = true;
    final ratio = MediaQuery.devicePixelRatioOf(context);
    // Au format du téléphone, recadré au milieu ensuite.
    final width = widget.width * ratio;
    widget.platform
        .renderTheme(widget.theme.id, Size(width, width * 20 / 9))
        .then((image) {
          if (mounted) setState(() => _image = image);
        });
  }

  @override
  Widget build(BuildContext context) {
    final image = _image;
    final l10n = AppLocalizations.of(context);
    return Semantics(
      image: true,
      label: l10n.themePreviewSemantics(widget.theme.title(l10n)),
      child: SizedBox(
        width: widget.width,
        height: widget.height,
        child: DecoratedBox(
          decoration: const BoxDecoration(color: Neon.stage),
          child: image == null
              ? null
              : Image.memory(
                  image,
                  fit: BoxFit.cover,
                  alignment: const Alignment(0, 0.1),
                  gaplessPlayback: true,
                ),
        ),
      ),
    );
  }
}

/// Un thème : son aperçu, son application, ses couleurs et ses sons.
class ThemeScreen extends StatefulWidget {
  const ThemeScreen({super.key, required this.platform, required this.theme});

  final WuxPlatform platform;
  final HaloTheme theme;

  @override
  State<ThemeScreen> createState() => _ThemeScreenState();
}

class _ThemeScreenState extends State<ThemeScreen> with WidgetsBindingObserver {
  ThemeState _state = const ThemeState();
  bool _loaded = false;

  /// Les sons à régler avec le thème ; tous, par défaut.
  late final Set<String> _sounds = {
    for (final (kind, _, _) in widget.theme.sounds) kind,
  };

  /// Sons choisis à l'application, en attente de l'autorisation d'Android.
  List<String>? _pending;
  bool _askedPermission = false;
  List<String>? _applied;
  bool _unavailable = false;

  WuxPlatform get _platform => widget.platform;
  HaloTheme get _theme => widget.theme;

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
    // De retour de l'écran du fond ou du réglage des sons.
    if (state == AppLifecycleState.resumed) _resume();
  }

  Future<void> _refresh() async {
    final state = await _platform.themeState();
    if (mounted) {
      setState(() {
        _state = state;
        _loaded = true;
      });
    }
  }

  Future<void> _resume() async {
    await _refresh();
    final pending = _pending;
    if (pending == null || !mounted) return;
    if (_state.canWriteSettings) {
      await _installSounds(pending);
    } else if (!_askedPermission) {
      // Le fond vient d'être confirmé : au tour des sons.
      _askedPermission = true;
      await _platform.requestWriteSettings();
    }
  }

  Future<void> _apply() async {
    final already = await _platform.isWallpaperActive(kind: _theme.id);
    final opened = await _platform.applyTheme(_theme.id);
    if (!mounted) return;
    setState(() {
      _unavailable = !opened;
      _applied = null;
    });
    final kinds = [
      for (final (kind, _, _) in _theme.sounds)
        if (_sounds.contains(kind)) kind,
    ];
    if (kinds.isEmpty || !_state.soundsSupported) {
      await _refresh();
      return;
    }
    if (_state.canWriteSettings) {
      await _installSounds(kinds);
    } else {
      setState(() {
        _pending = kinds;
        _askedPermission = false;
      });
      // Rien ne s'est ouvert pour le fond : on demande tout de suite.
      if (already) {
        _askedPermission = true;
        await _platform.requestWriteSettings();
      }
    }
    await _refresh();
  }

  Future<void> _installSounds(List<String> kinds) async {
    final applied = await _platform.applyThemeSounds(_theme.id, kinds);
    if (!mounted) return;
    setState(() {
      _applied = applied;
      _pending = null;
    });
  }

  Future<void> _allowSounds() async {
    _askedPermission = true;
    await _platform.requestWriteSettings();
  }

  @override
  Widget build(BuildContext context) {
    final typography = IuxTypographyTheme.of(context);
    final l10n = AppLocalizations.of(context);
    final title = _theme.title(l10n);
    final active = _state.active == _theme.id;
    final apply = l10n.themeApply;
    final applied = _applied;
    return Scaffold(
      body: ScreenFrame(
        title: title,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: _theme.description(l10n),
              children: [
                Center(
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(18),
                    child: ThemePreview(
                      platform: _platform,
                      theme: _theme,
                      width: 200,
                      height: 200 * 20 / 9,
                    ),
                  ),
                ),
                const IuxGap.between(),
                if (_loaded)
                  IuxStatusIndicator(
                    status: active
                        ? IuxStatus.success(l10n.themeActive)
                        : IuxStatus.neutral(l10n.themeNotApplied),
                  ),
                const IuxGap.standard(),
                IuxButton(
                  label: apply,
                  action: IuxActionDescriptor.primary(
                    semantics: IuxActionSemantics(label: apply),
                    role: IuxActionRole.custom,
                  ),
                  expand: true,
                  onActivate: _apply,
                ),
                if (_unavailable) ...[
                  const IuxGap.standard(),
                  Text(l10n.themeUnavailable(title), style: typography.body),
                ],
                if (applied != null && applied.isNotEmpty) ...[
                  const IuxGap.standard(),
                  Text(
                    l10n.themeSoundsApplied(
                      applied.map((k) => themeSoundRole(l10n, k)).join(', '),
                    ),
                    style: typography.body,
                  ),
                ],
                if (_pending != null) ...[
                  const IuxGap.standard(),
                  Text(l10n.themeSoundsWaiting, style: typography.body),
                  const IuxGap.standard(),
                  IuxButton(
                    label: l10n.themeSoundsAllow,
                    action: IuxActionDescriptor(
                      semantics: IuxActionSemantics(
                        label: l10n.themeSoundsAllow,
                      ),
                    ),
                    expand: true,
                    onActivate: _allowSounds,
                  ),
                ],
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.themeColorsTitle,
              description: l10n.themeColorsText,
              children: [
                Row(
                  children: [
                    for (final color in _theme.swatches) ...[
                      Container(
                        width: 46,
                        height: 30,
                        decoration: BoxDecoration(
                          color: color,
                          borderRadius: BorderRadius.circular(9),
                          border: Border.all(
                            color: Colors.white.withValues(alpha: 0.1),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                    ],
                  ],
                ),
                const IuxGap.standard(),
                IuxButton(
                  label: l10n.themeColorsOpen,
                  action: IuxActionDescriptor(
                    semantics: IuxActionSemantics(label: l10n.themeColorsOpen),
                  ),
                  expand: true,
                  onActivate: _platform.openColorSettings,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.themeSoundsTitle,
              description: !_state.soundsSupported && _loaded
                  ? l10n.themeSoundsUnsupported
                  : l10n.themeSoundsText,
              children: [
                for (final (kind, name, text) in _theme.sounds) ...[
                  Row(
                    children: [
                      IuxIconButton(
                        icon: Icons.play_arrow_rounded,
                        action: IuxActionDescriptor(
                          semantics: IuxActionSemantics(
                            label: l10n.themeSoundPlay(name(l10n)),
                          ),
                        ),
                        onActivate: () =>
                            _platform.playThemeSound(_theme.id, kind),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(name(l10n), style: typography.title),
                            Text(text(l10n), style: typography.body),
                          ],
                        ),
                      ),
                    ],
                  ),
                  if (_state.soundsSupported)
                    IuxCheckbox(
                      label: themeSoundRole(l10n, kind),
                      input: IuxInputDescriptor(
                        semantics: IuxInputSemantics(
                          label:
                              '${themeSoundRole(l10n, kind)} · ${name(l10n)}',
                        ),
                      ),
                      value: IuxSelectionState.fromSelected(
                        _sounds.contains(kind),
                      ),
                      onChanged: (on) => setState(() {
                        if (on) {
                          _sounds.add(kind);
                        } else {
                          _sounds.remove(kind);
                        }
                      }),
                    ),
                  const IuxGap.standard(),
                ],
                if (_state.soundsSupported && !_state.canWriteSettings)
                  Text(l10n.themeSoundsPermission, style: typography.body),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: l10n.themeLimitsTitle,
              description: l10n.themeLimitsText,
              children: const [],
            ),
            const IuxGap.between(),
          ],
        ),
      ),
    );
  }
}

/// L'entrée des thèmes dans la galerie : leurs trois fonds côte à côte.
class ThemesBanner extends StatelessWidget {
  const ThemesBanner({super.key, required this.platform, required this.onOpen});

  final WuxPlatform platform;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Material(
      color: Neon.surface,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: const BorderSide(color: Neon.line),
      ),
      child: NeonTappable(
        label: l10n.catalogThemes,
        hint: l10n.catalogThemesHint,
        onTap: onOpen,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              for (final theme in haloThemes) ...[
                ClipRRect(
                  borderRadius: BorderRadius.circular(10),
                  child: ThemePreview(
                    platform: platform,
                    theme: theme,
                    width: 40,
                    height: 64,
                  ),
                ),
                const SizedBox(width: 6),
              ],
              const SizedBox(width: 8),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.catalogThemes,
                      style: Neon.body(
                        15,
                        weight: FontWeight.w600,
                        color: Neon.text,
                      ),
                    ),
                    Text(l10n.catalogThemesHint, style: Neon.body(13)),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right, color: Neon.faint),
            ],
          ),
        ),
      ),
    );
  }
}

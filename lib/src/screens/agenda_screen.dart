import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';

/// Réglages d'un widget agenda : accès au calendrier, agendas affichés, fond.
///
/// Chaque changement s'applique tout de suite au widget et à l'aperçu ; il n'y
/// a pas de bouton « Enregistrer ».
class AgendaScreen extends StatefulWidget {
  const AgendaScreen({
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
  State<AgendaScreen> createState() => _AgendaScreenState();
}

class _AgendaScreenState extends State<AgendaScreen> {
  static const _calendarsKey = 'calendars';
  static const _backgroundKey = 'background';

  bool? _permission;

  /// La demande système a été refusée : Android ne la représentera peut-être
  /// plus, il faut alors passer par la fiche de l'application.
  bool _refused = false;

  bool _canPin = false;
  List<CalendarInfo> _calendars = const [];

  /// `null` : tous les agendas, y compris ceux ajoutés plus tard.
  Set<int>? _selected;
  AgendaBackground _background = AgendaBackground.transparent;
  WidgetPalette _palette = WidgetPalette.fallback;
  List<AgendaDayPreview> _preview = const [];

  WuxPlatform get _platform => widget.platform;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final permission = await _platform.hasCalendarPermission();
    final canPin = await _platform.canPin();
    final palette = await _platform.palette();
    final stored = await _platform.read(widget.homeWidget, _calendarsKey);
    final background = await _platform.read(widget.homeWidget, _backgroundKey);
    if (!mounted) return;
    setState(() {
      _permission = permission;
      _canPin = canPin;
      _palette = palette;
      _selected = stored == null
          ? null
          : {for (final part in stored.split(',')) ?int.tryParse(part)};
      _background = AgendaBackground.fromKey(background);
    });
    if (permission) await _loadCalendar();
  }

  Future<void> _loadCalendar() async {
    final calendars = await _platform.calendars();
    if (!mounted) return;
    setState(() => _calendars = calendars);
    await _refreshPreview();
  }

  Future<void> _refreshPreview() async {
    final preview = await _platform.agendaPreview(
      days: widget.homeWidget.days,
      calendarIds: _selected,
    );
    if (mounted) setState(() => _preview = preview);
  }

  Future<void> _requestPermission() async {
    final granted = await _platform.requestCalendarPermission();
    if (!mounted) return;
    setState(() {
      _permission = granted;
      _refused = !granted;
    });
    if (granted) await _loadCalendar();
  }

  bool _isShown(CalendarInfo calendar) =>
      _selected?.contains(calendar.id) ?? true;

  Future<void> _toggle(CalendarInfo calendar, bool shown) async {
    final selected = {
      for (final c in _calendars)
        if (c.id == calendar.id ? shown : _isShown(c)) c.id,
    };
    final all = selected.length == _calendars.length;
    setState(() => _selected = all ? null : selected);
    await _platform.write(
      widget.homeWidget,
      _calendarsKey,
      all ? null : selected.join(','),
    );
    await _refreshPreview();
  }

  Future<void> _setBackground(AgendaBackground background) async {
    setState(() => _background = background);
    await _platform.write(widget.homeWidget, _backgroundKey, background.key);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ScreenFrame(
        title: widget.homeWidget.title,
        canGoBack: true,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            IuxSection(
              description: widget.homeWidget.description,
              children: [
                WallpaperFrame(
                  child: _permission ?? false
                      ? AgendaWidgetPreview(
                          days: _preview,
                          background: _background,
                          palette: _palette,
                        )
                      : const _PreviewPlaceholder(),
                ),
                const IuxGap.between(),
                PinButton(
                  visible: _canPin && widget.allowPin,
                  onPin: () => _platform.pin(widget.homeWidget),
                ),
              ],
            ),
            const IuxGap.between(),
            if (_permission == false) ...[
              _PermissionSection(
                refused: _refused,
                onRequest: _requestPermission,
                onOpenSettings: _platform.openAppSettings,
              ),
              const IuxGap.between(),
            ],
            if (_calendars.isNotEmpty) ...[
              IuxSection(
                title: 'Agendas',
                children: [
                  IuxSelectionGroup(
                    label: 'Agendas affichés',
                    children: [
                      for (final calendar in _calendars)
                        IuxSwitch(
                          label: calendar.name,
                          input: IuxInputDescriptor(
                            semantics: IuxInputSemantics(label: calendar.name),
                            helpText: calendar.account,
                          ),
                          value: IuxSelectionState.fromSelected(
                            _isShown(calendar),
                          ),
                          onChanged: (shown) => _toggle(calendar, shown),
                        ),
                    ],
                  ),
                ],
              ),
              const IuxGap.between(),
            ],
            IuxSection(
              title: 'Apparence',
              children: [
                IuxRadioGroup<AgendaBackground>(
                  label: 'Fond du widget',
                  input: const IuxInputDescriptor(
                    semantics: IuxInputSemantics(label: 'Fond du widget'),
                  ),
                  value: _background,
                  options: const [
                    IuxRadioOption(
                      value: AgendaBackground.transparent,
                      label: 'Transparent',
                      helpText: "Texte clair posé sur le fond d'écran.",
                    ),
                    IuxRadioOption(
                      value: AgendaBackground.surface,
                      label: 'Couleurs du fond d’écran',
                      helpText: 'Carte aux teintes Material You (Android 12+).',
                    ),
                  ],
                  onChanged: _setBackground,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _PreviewPlaceholder extends StatelessWidget {
  const _PreviewPlaceholder();

  @override
  Widget build(BuildContext context) {
    return const SizedBox(
      height: 120,
      child: Center(
        child: Text(
          "L'aperçu apparaîtra une fois l'agenda autorisé.",
          textAlign: TextAlign.center,
          style: TextStyle(color: Colors.white, fontSize: 15),
        ),
      ),
    );
  }
}

class _PermissionSection extends StatelessWidget {
  const _PermissionSection({
    required this.refused,
    required this.onRequest,
    required this.onOpenSettings,
  });

  final bool refused;
  final VoidCallback onRequest;
  final VoidCallback onOpenSettings;

  static const _allow = "Autoriser l'accès à l'agenda";
  static const _settings = 'Ouvrir les réglages';

  @override
  Widget build(BuildContext context) {
    return IuxSection(
      title: "Accès à l'agenda",
      description: refused
          ? "L'accès a été refusé. S'il ne vous est plus proposé, activez "
                "« Agenda » dans les autorisations de l'application."
          : 'WUX lit vos événements pour les afficher. Il ne les modifie '
                'jamais et ne les envoie nulle part.',
      children: [
        IuxButton(
          label: _allow,
          action: const IuxActionDescriptor.primary(
            semantics: IuxActionSemantics(label: _allow),
            role: IuxActionRole.custom,
          ),
          expand: true,
          onActivate: onRequest,
        ),
        if (refused) ...[
          const IuxGap.standard(),
          IuxButton(
            label: _settings,
            action: const IuxActionDescriptor(
              semantics: IuxActionSemantics(label: _settings),
            ),
            expand: true,
            onActivate: onOpenSettings,
          ),
        ],
      ],
    );
  }
}

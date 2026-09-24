import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import '../previews/widget_previews.dart';
import 'screen_frame.dart';

/// Réglages d'un widget agenda : accès au calendrier, jours, événements
/// « toute la journée » et agendas affichés.
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
  static const _daysKey = 'days';
  static const _allDayKey = 'allDay';

  bool? _permission;

  /// La demande système a été refusée : Android ne la représentera peut-être
  /// plus, il faut alors passer par la fiche de l'application.
  bool _refused = false;

  bool _canPin = false;
  List<CalendarInfo> _calendars = const [];

  /// `null` : tous les agendas, y compris ceux ajoutés plus tard.
  Set<int>? _selected;

  /// Augmentée à chaque réglage, pour redessiner l'aperçu.
  int _revision = 0;

  /// 1 : aujourd'hui ; 2 : aujourd'hui et demain. Relu par `AgendaSettings.kt`.
  int _days = 2;

  /// Faux : les événements « toute la journée » sont masqués. Relu par
  /// `AgendaSettings.kt` (`'0'` pour les masquer).
  bool _showAllDay = true;

  WuxPlatform get _platform => widget.platform;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final permission = await _platform.hasCalendarPermission();
    final canPin = await _platform.canPin();
    final stored = await _platform.read(widget.homeWidget, _calendarsKey);
    final days = await _platform.read(widget.homeWidget, _daysKey);
    final allDay = await _platform.read(widget.homeWidget, _allDayKey);
    if (!mounted) return;
    setState(() {
      _permission = permission;
      _canPin = canPin;
      _days = days == '1' ? 1 : 2;
      _showAllDay = allDay != '0';
      _selected = stored == null
          ? null
          : {for (final part in stored.split(',')) ?int.tryParse(part)};
    });
    if (permission) await _loadCalendar();
  }

  Future<void> _loadCalendar() async {
    final calendars = await _platform.calendars();
    if (!mounted) return;
    setState(() => _calendars = calendars);
    _refreshPreview();
  }

  void _refreshPreview() => setState(() => _revision++);

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
    _refreshPreview();
  }

  Future<void> _setDays(int days) async {
    setState(() => _days = days);
    await _platform.write(widget.homeWidget, _daysKey, '$days');
    _refreshPreview();
  }

  Future<void> _setShowAllDay(bool shown) async {
    setState(() => _showAllDay = shown);
    await _platform.write(widget.homeWidget, _allDayKey, shown ? null : '0');
    _refreshPreview();
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
                      ? WidgetPreview(
                          homeWidget: widget.homeWidget,
                          platform: _platform,
                          revision: _revision,
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
            IuxSection(
              title: 'Jours',
              children: [
                IuxRadioGroup<int>(
                  label: 'Jours affichés',
                  input: const IuxInputDescriptor(
                    semantics: IuxInputSemantics(label: 'Jours affichés'),
                    helpText:
                        'À partir de 18 h, une journée terminée laisse '
                        'place au lendemain.',
                  ),
                  value: _days,
                  options: const [
                    IuxRadioOption(value: 1, label: "Aujourd'hui"),
                    IuxRadioOption(value: 2, label: "Aujourd'hui et demain"),
                  ],
                  onChanged: _setDays,
                ),
              ],
            ),
            const IuxGap.between(),
            IuxSection(
              title: 'Événements',
              description:
                  "L'événement en cours est mis en avant, les suivants "
                  'sont légèrement estompés.',
              children: [
                IuxSelectionGroup(
                  label: 'Événements affichés',
                  children: [
                    IuxSwitch(
                      label: 'Toute la journée',
                      input: const IuxInputDescriptor(
                        semantics: IuxInputSemantics(
                          label: 'Afficher les événements toute la journée',
                        ),
                        helpText:
                            'Anniversaires, congés, jours fériés… et les '
                            'événements sur plusieurs jours.',
                      ),
                      value: IuxSelectionState.fromSelected(_showAllDay),
                      onChanged: _setShowAllDay,
                    ),
                  ],
                ),
              ],
            ),
            const IuxGap.between(),
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
          : 'Halo lit vos événements pour les afficher. Il ne les modifie '
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

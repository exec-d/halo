import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/home_widget_bridge.dart';
import '../home_widgets/wux_home_widget.dart';

/// Liste des widgets disponibles. Écran provisoire, en attendant les maquettes.
class CatalogScreen extends StatefulWidget {
  const CatalogScreen({super.key, required this.widgets, required this.bridge});

  final List<WuxHomeWidget> widgets;
  final HomeWidgetBridge bridge;

  @override
  State<CatalogScreen> createState() => _CatalogScreenState();
}

class _CatalogScreenState extends State<CatalogScreen> {
  bool _canPin = false;

  /// L'unique emplacement de notice de l'écran : `IuxTransientLayer` n'en
  /// affiche qu'une à la fois.
  IuxTransientMessage? _notice;

  @override
  void initState() {
    super.initState();
    widget.bridge.canPin().then((value) {
      if (mounted) setState(() => _canPin = value);
    });
  }

  void _notify(String text) {
    setState(
      () => _notice = IuxTransientMessage(
        text: text,
        dismissLabel: 'Fermer',
        tone: IuxTransientTone.success,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IuxTransientLayer(
        message: _notice,
        onDismissed: () => setState(() => _notice = null),
        child: _ScreenFrame(
          title: 'Mes widgets',
          child: IuxSection(
            children: [
              for (final homeWidget in widget.widgets) ...[
                _WidgetCard(
                  homeWidget: homeWidget,
                  bridge: widget.bridge,
                  canPin: _canPin,
                  onApplied: () => _notify('Widget mis à jour'),
                ),
                const IuxGap.between(),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// Barre d'application au-dessus d'une page, défilant ensemble.
///
/// Reprend `apps/pilot/lib/screen_frame.dart` d'IUX : `IuxPage` et
/// `IuxAppBar` ne se composent pas seuls (double marge haute, débordement à
/// fort grossissement du texte).
class _ScreenFrame extends StatelessWidget {
  const _ScreenFrame({required this.title, required this.child});

  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      physics: const AlwaysScrollableScrollPhysics(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          IuxAppBar(title: title),
          MediaQuery.removePadding(
            context: context,
            removeTop: true,
            child: IuxPage(scrollable: false, child: child),
          ),
        ],
      ),
    );
  }
}

class _WidgetCard extends StatefulWidget {
  const _WidgetCard({
    required this.homeWidget,
    required this.bridge,
    required this.canPin,
    required this.onApplied,
  });

  final WuxHomeWidget homeWidget;
  final HomeWidgetBridge bridge;
  final bool canPin;
  final VoidCallback onApplied;

  @override
  State<_WidgetCard> createState() => _WidgetCardState();
}

class _WidgetCardState extends State<_WidgetCard> {
  final _message = TextEditingController();

  static const _messageInput = IuxInputDescriptor(
    semantics: IuxInputSemantics(label: 'Message'),
    helpText: 'Texte affiché par le widget.',
  );

  @override
  void initState() {
    super.initState();
    widget.bridge.read(widget.homeWidget, 'message').then((value) {
      if (mounted && value != null) _message.text = value;
    });
  }

  @override
  void dispose() {
    _message.dispose();
    super.dispose();
  }

  Future<void> _apply() async {
    await widget.bridge.write(widget.homeWidget, 'message', _message.text);
    widget.onApplied();
  }

  @override
  Widget build(BuildContext context) {
    return IuxCard(
      actions: [
        IuxButton(
          label: 'Appliquer',
          action: const IuxActionDescriptor.primary(
            semantics: IuxActionSemantics(label: 'Appliquer'),
          ),
          onActivate: _apply,
        ),
        if (widget.canPin)
          IuxButton(
            label: "Ajouter à l'accueil",
            action: const IuxActionDescriptor(
              semantics: IuxActionSemantics(label: "Ajouter à l'accueil"),
            ),
            onActivate: () => widget.bridge.pin(widget.homeWidget),
          ),
      ],
      child: IuxSection(
        title: widget.homeWidget.title,
        description: widget.homeWidget.description,
        children: [
          IuxTextField(
            input: _messageInput,
            controller: _message,
            onChanged: (_) {},
          ),
        ],
      ),
    );
  }
}

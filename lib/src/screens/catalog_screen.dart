import 'package:flutter/material.dart';

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

  @override
  void initState() {
    super.initState();
    widget.bridge.canPin().then((value) {
      if (mounted) setState(() => _canPin = value);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mes widgets')),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: widget.widgets.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) => _WidgetCard(
          homeWidget: widget.widgets[index],
          bridge: widget.bridge,
          canPin: _canPin,
        ),
      ),
    );
  }
}

class _WidgetCard extends StatefulWidget {
  const _WidgetCard({
    required this.homeWidget,
    required this.bridge,
    required this.canPin,
  });

  final WuxHomeWidget homeWidget;
  final HomeWidgetBridge bridge;
  final bool canPin;

  @override
  State<_WidgetCard> createState() => _WidgetCardState();
}

class _WidgetCardState extends State<_WidgetCard> {
  final _message = TextEditingController();

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

  Future<void> _save() async {
    final messenger = ScaffoldMessenger.of(context);
    await widget.bridge.write(widget.homeWidget, 'message', _message.text);
    messenger.showSnackBar(const SnackBar(content: Text('Widget mis à jour')));
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(widget.homeWidget.title, style: textTheme.titleMedium),
            const SizedBox(height: 4),
            Text(widget.homeWidget.description, style: textTheme.bodyMedium),
            const SizedBox(height: 16),
            TextField(
              controller: _message,
              decoration: const InputDecoration(
                labelText: 'Message',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                FilledButton(onPressed: _save, child: const Text('Appliquer')),
                if (widget.canPin)
                  OutlinedButton(
                    onPressed: () => widget.bridge.pin(widget.homeWidget),
                    child: const Text("Ajouter à l'accueil"),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

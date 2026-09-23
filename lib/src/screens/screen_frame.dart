import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

/// Barre d'application au-dessus d'une page, défilant ensemble.
///
/// Reprend `apps/pilot/lib/screen_frame.dart` d'IUX : `IuxPage` et
/// `IuxAppBar` ne se composent pas seuls (double marge haute, débordement à
/// fort grossissement du texte).
class ScreenFrame extends StatelessWidget {
  const ScreenFrame({
    super.key,
    required this.title,
    required this.child,
    this.canGoBack = false,
  });

  final String title;
  final Widget child;

  /// Affiche le bouton retour, pour un écran ouvert depuis le catalogue.
  final bool canGoBack;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      physics: const AlwaysScrollableScrollPhysics(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          IuxAppBar(
            title: title,
            leading: canGoBack
                ? IuxAppBarLeading.back(
                    label: 'Retour',
                    // maybePop : laisse un PopScope décider (écran de configuration).
                    onActivate: () => Navigator.of(context).maybePop(),
                  )
                : null,
          ),
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

/// Bouton « Ajouter à l'accueil », masqué si le lanceur ne le permet pas.
class PinButton extends StatelessWidget {
  const PinButton({super.key, required this.visible, required this.onPin});

  final bool visible;
  final VoidCallback onPin;

  static const _label = "Ajouter à l'écran d'accueil";

  @override
  Widget build(BuildContext context) {
    if (!visible) return const SizedBox.shrink();
    return IuxButton(
      label: _label,
      action: const IuxActionDescriptor.primary(
        semantics: IuxActionSemantics(label: _label),
        role: IuxActionRole.custom,
      ),
      expand: true,
      onActivate: onPin,
    );
  }
}

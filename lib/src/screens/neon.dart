import 'package:flutter/material.dart';

/// Les couleurs et les polices de la galerie : fond nuit, ambre de Halo,
/// cyan pour les nouveautés.
abstract final class Neon {
  static const background = Color(0xFF05070D);
  static const surface = Color(0xFF0C111C);
  static const stage = Color(0xFF070B14);
  static const line = Color(0xFF1B2333);
  static const outline = Color(0xFF2A3346);
  static const navigation = Color(0xFF0A0E18);
  static const text = Color(0xFFE8ECF4);
  static const muted = Color(0xFFB4BCCC);
  static const faint = Color(0xFF9AA3B5);
  static const amber = Color(0xFFF2C178);
  static const onAmber = Color(0xFF1A1206);
  static const cyan = Color(0xFF5CE1E6);
  static const onCyan = Color(0xFF03181A);

  static TextStyle display(
    double size, {
    FontWeight weight = FontWeight.w600,
    Color color = text,
  }) => TextStyle(
    fontFamily: 'ChakraPetch',
    fontSize: size,
    fontWeight: weight,
    color: color,
    height: 1.15,
  );

  static TextStyle body(
    double size, {
    FontWeight weight = FontWeight.w400,
    Color color = muted,
  }) => TextStyle(
    fontFamily: 'IBMPlexSans',
    fontSize: size,
    fontWeight: weight,
    color: color,
    height: 1.35,
  );

  static TextStyle mono(
    double size, {
    FontWeight weight = FontWeight.w400,
    Color color = amber,
    double spacing = 0,
  }) => TextStyle(
    fontFamily: 'JetBrainsMono',
    fontSize: size,
    fontWeight: weight,
    color: color,
    letterSpacing: spacing,
  );
}

/// Une zone qu'on touche : un bouton pour les lecteurs d'écran, un reflet au
/// toucher.
class NeonTappable extends StatelessWidget {
  const NeonTappable({
    super.key,
    required this.label,
    required this.onTap,
    required this.child,
    this.hint,
    this.radius = 20,
    this.selected,
  });

  final String label;
  final String? hint;
  final VoidCallback onTap;
  final Widget child;
  final double radius;
  final bool? selected;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      selected: selected,
      label: label,
      hint: hint,
      excludeSemantics: true,
      child: Material(
        type: MaterialType.transparency,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(radius),
          splashColor: Neon.amber.withValues(alpha: 0.12),
          highlightColor: Neon.amber.withValues(alpha: 0.06),
          child: child,
        ),
      ),
    );
  }
}

/// Le bouton ambre de la galerie.
class NeonPill extends StatelessWidget {
  const NeonPill({super.key, required this.label, required this.onTap});

  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Neon.amber,
      borderRadius: BorderRadius.circular(22),
      child: NeonTappable(
        label: label,
        onTap: onTap,
        radius: 22,
        child: Container(
          height: 44,
          padding: const EdgeInsets.symmetric(horizontal: 18),
          alignment: Alignment.center,
          child: Text(
            label,
            style: Neon.body(14, weight: FontWeight.w600, color: Neon.onAmber),
          ),
        ),
      ),
    );
  }
}

/// Un filtre : plein et ambré quand il est choisi.
class NeonChip extends StatelessWidget {
  const NeonChip({
    super.key,
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
      color: selected ? Neon.amber : Colors.transparent,
      shape: StadiumBorder(
        side: selected
            ? BorderSide.none
            : const BorderSide(color: Neon.outline),
      ),
      child: NeonTappable(
        label: label,
        selected: selected,
        onTap: onSelect,
        radius: 22,
        child: Container(
          height: 44,
          padding: const EdgeInsets.symmetric(horizontal: 16),
          alignment: Alignment.center,
          child: Text(
            label,
            style: Neon.body(
              14,
              weight: selected ? FontWeight.w600 : FontWeight.w400,
              color: selected ? Neon.onAmber : Neon.text,
            ),
          ),
        ),
      ),
    );
  }
}

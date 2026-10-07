package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ce que le téléphone de Circuit offre à un cœur qui remplace sa batterie :
 * la place de la batterie et le bas de la carte mère, vus de face (comme à
 * l'écran), et l'unité (un centième de la largeur).
 */
class CoreKit(
    val u: Float,
    val density: Float,
    /** La place de la batterie. */
    val area: RectF,
    /** Le bas de la carte mère, où arrivent les câbles. */
    val boardBottom: Float,
    /** Hauteur de l'écran, en unités (≈ 216 sur un téléphone 20:9). */
    val h: Float = 216f,
) {
    /** Un point de [area], en fractions de sa largeur et de sa hauteur. */
    fun at(fx: Float, fy: Float) = PointF(area.left + fx * area.width(), area.top + fy * area.height())

    /** Un point à [radius] de [center], à [degrees] (0 à droite, sens horaire). */
    fun around(center: PointF, radius: Float, degrees: Float): PointF {
        val a = Math.toRadians(degrees.toDouble())
        return PointF(center.x + cos(a).toFloat() * radius, center.y + sin(a).toFloat() * radius)
    }

    fun polygon(points: List<PointF>) = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
}

/** Les pinceaux et le halo du peintre, prêtés aux cœurs pour s'animer. */
interface Ink {
    val stroke: Paint
    val fill: Paint
    val palette: CircuitPalette

    /** Un halo rond de la couleur du téléphone, du centre vers rien. */
    fun halo(canvas: Canvas, at: PointF, radius: Float, alpha: Int)
}

/**
 * Un cœur à la place de la batterie : son dessin fixe (rendu une fois dans le
 * plan de la batterie) et ce qui s'y anime à chaque image. Tout est en
 * coordonnées de face.
 */
abstract class CoreArt(protected val kit: CoreKit) {
    protected val u = kit.u
    protected val density = kit.density

    /** Le dessin fixe, en traits blancs ([line], [thin]) et aplats ([fill]). */
    abstract fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint)

    /** Ce qui vit : appelé à chaque image, dans le plan de la batterie. */
    abstract fun drawLive(canvas: Canvas, state: FrameState, ink: Ink)

    /** Les contours qui s'illuminent à l'allumage, du centre vers le bord. */
    open fun outlines(): List<Path> = emptyList()

    /**
     * Le cœur redessine tout le téléphone : le fond ([drawChassis], sous le
     * cadre seul), la batterie ([drawStatic]) et les cartes ([drawBoard]), avec
     * leurs animations et ses propres trajets d'impulsions ([routes]).
     */
    open val wholePhone: Boolean get() = false

    open fun drawChassis(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) = Unit
    open fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) = Unit
    open fun drawLiveChassis(canvas: Canvas, state: FrameState, ink: Ink) = Unit
    open fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) = Unit

    /** Les trajets des impulsions du réseau, de la charge et du battement, de face ; `null` : ceux de Circuit. */
    open fun routes(): Triple<List<CircuitScene.Route>, List<CircuitScene.Route>, List<CircuitScene.Route>>? = null

    /** D'où part l'allumage, de face ; `null` : le processeur de Circuit. */
    open val origin: PointF? get() = null

    /** L'animation reste visible en « Discret », comme les impulsions. */
    protected fun strength(state: FrameState) = 0.4f + 0.6f * state.intensity

    /** Une vis cruciforme. */
    protected fun screw(canvas: Canvas, at: PointF, thin: Paint) {
        canvas.drawCircle(at.x, at.y, 1.3f * u, thin)
        canvas.drawLine(at.x - 0.7f * u, at.y, at.x + 0.7f * u, at.y, thin)
        canvas.drawLine(at.x, at.y - 0.7f * u, at.x, at.y + 0.7f * u, thin)
    }

    /** La platine qui remplace la batterie : son cadre et ses quatre vis. */
    protected fun plate(canvas: Canvas, line: Paint, thin: Paint) {
        val a = kit.area
        canvas.drawRoundRect(a, 2f * u, 2f * u, line)
        canvas.drawRoundRect(RectF(a).apply { inset(1.4f * u, 1.4f * u) }, 1.4f * u, 1.4f * u, thin)
        listOf(
            PointF(a.left + 3.5f * u, a.top + 3.5f * u), PointF(a.right - 3.5f * u, a.top + 3.5f * u),
            PointF(a.left + 3.5f * u, a.bottom - 3.5f * u), PointF(a.right - 3.5f * u, a.bottom - 3.5f * u),
        ).forEach { screw(canvas, it, thin) }
    }

    /** Une nappe de la carte mère jusqu'à [to] : deux fils et leurs barrettes. */
    protected fun ribbon(canvas: Canvas, x: Float, to: Float, thin: Paint) {
        canvas.drawRect(RectF(x - 3f * u, to - 2f * u, x + 3f * u, to), thin)
        canvas.drawLine(x - 1.6f * u, to - 2f * u, x - 1.6f * u, kit.boardBottom, thin)
        canvas.drawLine(x + 1.6f * u, to - 2f * u, x + 1.6f * u, kit.boardBottom, thin)
        var y = to - 3.4f * u
        while (y > kit.boardBottom + 0.6f * u) {
            canvas.drawLine(x - 1.6f * u, y, x + 1.6f * u, y, thin)
            y -= 1.4f * u
        }
    }

    /** Un bruit pseudo-aléatoire stable, de 0 à 1. */
    protected fun noise(seed: Int): Float {
        var h = seed * 374_761_393 + 668_265_263
        h = (h xor (h ushr 13)) * 1_274_126_177
        return ((h xor (h ushr 16)) and 0xFFFF) / 65_535f
    }

    companion object {
        /** Le cœur de [core], ou `null` pour ceux que [CircuitScene] dessine lui-même. */
        fun of(core: CircuitScene.Core, kit: CoreKit): CoreArt? = when (core) {
            CircuitScene.Core.ARC -> ArcCore(kit)
            CircuitScene.Core.QUANTUM -> QuantumCore(kit)
            CircuitScene.Core.NEURAL -> NeuralCore(kit)
            CircuitScene.Core.ATOM -> AtomCore(kit)
            CircuitScene.Core.VAULT -> VaultCore(kit)
            CircuitScene.Core.GHOST -> GhostCore(kit)
            else -> null
        }
    }
}

// Une imitation d'android.graphics faite avec Java2D, pour dessiner les scènes sur
// l'ordinateur (tool/scenes/render.sh). Seul ce dont les scènes se servent existe.
@file:Suppress("unused", "UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package android.graphics

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Font
import java.awt.MultipleGradientPaint
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.Raster
import kotlin.math.PI
import kotlin.math.atan2

object Color {
    const val BLACK = -0x1000000
    const val WHITE = -0x1
    const val TRANSPARENT = 0
    fun rgb(r: Int, g: Int, b: Int) = argb(255, r, g, b)
    fun argb(a: Int, r: Int, g: Int, b: Int) =
        (a.coerceIn(0, 255) shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)
    fun alpha(c: Int) = (c ushr 24) and 0xFF
    fun red(c: Int) = (c shr 16) and 0xFF
    fun green(c: Int) = (c shr 8) and 0xFF
    fun blue(c: Int) = c and 0xFF
    fun colorToHSV(c: Int, hsv: FloatArray) {
        val out = java.awt.Color.RGBtoHSB(red(c), green(c), blue(c), null)
        hsv[0] = out[0] * 360f; hsv[1] = out[1]; hsv[2] = out[2]
    }
    fun HSVToColor(hsv: FloatArray): Int = HSVToColor(255, hsv)
    fun HSVToColor(alpha: Int, hsv: FloatArray): Int {
        val rgb = java.awt.Color.HSBtoRGB(((hsv[0] % 360f) + 360f) % 360f / 360f, hsv[1].coerceIn(0f, 1f), hsv[2].coerceIn(0f, 1f))
        return (alpha shl 24) or (rgb and 0xFFFFFF)
    }
    internal fun awt(c: Int) = java.awt.Color(red(c), green(c), blue(c), alpha(c))
}

class RectF(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f) {
    constructor(r: RectF) : this(r.left, r.top, r.right, r.bottom)
    fun union(x: Float, y: Float) { left = minOf(left, x); top = minOf(top, y); right = maxOf(right, x); bottom = maxOf(bottom, y) }
    companion object {
        fun intersects(a: RectF, b: RectF) = a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
    }
    fun set(l: Float, t: Float, r: Float, b: Float) { left = l; top = t; right = r; bottom = b }
    fun width() = right - left
    fun height() = bottom - top
    fun centerX() = (left + right) / 2
    fun centerY() = (top + bottom) / 2
    fun inset(dx: Float, dy: Float) { left += dx; top += dy; right -= dx; bottom -= dy }
}

class Typeface private constructor(val family: String, val bold: Boolean) {
    companion object {
        val MONOSPACE = Typeface(Font.MONOSPACED, false)
        val DEFAULT = Typeface(Font.SANS_SERIF, false)
        val DEFAULT_BOLD = Typeface(Font.SANS_SERIF, true)
        val SANS_SERIF = DEFAULT
        const val NORMAL = 0
        const val BOLD = 1
        fun create(t: Typeface?, style: Int) = Typeface(t?.family ?: Font.SANS_SERIF, style == BOLD)
        /** La famille demandée est approchée par la police sans empattement de Java. */
        fun create(family: String?, style: Int) = Typeface(Font.SANS_SERIF, style == BOLD)
    }
}

open class Shader {
    enum class TileMode { CLAMP, REPEAT, MIRROR }
    internal open fun awt(alpha: Float): java.awt.Paint? = null
    internal var local = AffineTransform()
    fun setLocalMatrix(m: Matrix?) { local = AffineTransform(m?.t ?: AffineTransform()) }
}

class PointF(var x: Float = 0f, var y: Float = 0f) {
    fun set(x: Float, y: Float) { this.x = x; this.y = y }
}

class Matrix {
    internal var t = AffineTransform()
    fun reset() { t = AffineTransform() }
    fun setScale(sx: Float, sy: Float) { t = AffineTransform.getScaleInstance(sx.toDouble(), sy.toDouble()) }
    fun setScale(sx: Float, sy: Float, px: Float, py: Float) {
        t = AffineTransform().apply { translate(px.toDouble(), py.toDouble()); scale(sx.toDouble(), sy.toDouble()); translate(-px.toDouble(), -py.toDouble()) }
    }
    fun setTranslate(dx: Float, dy: Float) { t = AffineTransform.getTranslateInstance(dx.toDouble(), dy.toDouble()) }
    fun postTranslate(dx: Float, dy: Float): Boolean { t.preConcatenate(AffineTransform.getTranslateInstance(dx.toDouble(), dy.toDouble())); return true }
}

class Rect(var left: Int = 0, var top: Int = 0, var right: Int = 0, var bottom: Int = 0)

private fun fractions(n: Int, positions: FloatArray?): FloatArray {
    val f = positions?.copyOf() ?: FloatArray(n) { if (n == 1) 0f else it / (n - 1f) }
    for (i in 1 until f.size) if (f[i] <= f[i - 1]) f[i] = (f[i - 1] + 1e-4f).coerceAtMost(1f)
    for (i in f.size - 2 downTo 0) if (f[i] >= f[i + 1]) f[i] = f[i + 1] - 1e-4f
    return f
}

private fun awtColors(colors: IntArray, alpha: Float) = Array(colors.size) {
    val c = colors[it]
    java.awt.Color(Color.red(c), Color.green(c), Color.blue(c), (Color.alpha(c) * alpha).toInt().coerceIn(0, 255))
}

private fun cycle(t: Shader.TileMode) = when (t) {
    Shader.TileMode.CLAMP -> MultipleGradientPaint.CycleMethod.NO_CYCLE
    Shader.TileMode.REPEAT -> MultipleGradientPaint.CycleMethod.REPEAT
    Shader.TileMode.MIRROR -> MultipleGradientPaint.CycleMethod.REFLECT
}

class LinearGradient(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val colors: IntArray, val positions: FloatArray?, val tile: TileMode) : Shader() {
    constructor(x0: Float, y0: Float, x1: Float, y1: Float, c0: Int, c1: Int, tile: TileMode) : this(x0, y0, x1, y1, intArrayOf(c0, c1), null, tile)
    override fun awt(alpha: Float): java.awt.Paint {
        val same = x0 == x1 && y0 == y1
        return java.awt.LinearGradientPaint(Point2D.Float(x0, y0), Point2D.Float(x1, if (same) y1 + 0.01f else y1), fractions(colors.size, positions), awtColors(colors, alpha), cycle(tile), MultipleGradientPaint.ColorSpaceType.SRGB, local)
    }
}

class RadialGradient(val cx: Float, val cy: Float, val r: Float, val colors: IntArray, val positions: FloatArray?, val tile: TileMode) : Shader() {
    constructor(cx: Float, cy: Float, r: Float, c0: Int, c1: Int, tile: TileMode) : this(cx, cy, r, intArrayOf(c0, c1), null, tile)
    override fun awt(alpha: Float): java.awt.Paint =
        java.awt.RadialGradientPaint(Point2D.Float(cx, cy), r.coerceAtLeast(0.01f), Point2D.Float(cx, cy), fractions(colors.size, positions), awtColors(colors, alpha), cycle(tile), MultipleGradientPaint.ColorSpaceType.SRGB, local)
}

class SweepGradient(val cx: Float, val cy: Float, val colors: IntArray, val positions: FloatArray?) : Shader() {
    constructor(cx: Float, cy: Float, c0: Int, c1: Int) : this(cx, cy, intArrayOf(c0, c1), null)
    override fun awt(alpha: Float): java.awt.Paint = SweepPaint(this, alpha)
}

private class SweepPaint(val s: SweepGradient, val alpha: Float) : java.awt.Paint {
    override fun getTransparency() = java.awt.Transparency.TRANSLUCENT
    override fun createContext(cm: ColorModel?, db: java.awt.Rectangle, ub: Rectangle2D?, xform: AffineTransform, hints: RenderingHints?): java.awt.PaintContext {
        val inv = xform.createInverse()
        val f = fractions(s.colors.size, s.positions)
        return object : java.awt.PaintContext {
            override fun dispose() {}
            override fun getColorModel(): ColorModel = ColorModel.getRGBdefault()
            override fun getRaster(x: Int, y: Int, w: Int, h: Int): Raster {
                val img = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
                val p = Point2D.Float()
                for (j in 0 until h) for (i in 0 until w) {
                    inv.transform(Point2D.Float((x + i).toFloat(), (y + j).toFloat()), p)
                    var t = (atan2(p.y - s.cy, p.x - s.cx) / (2 * PI)).toFloat()
                    if (t < 0) t += 1f
                    var k = 0
                    while (k < f.size - 2 && t > f[k + 1]) k++
                    val u = ((t - f[k]) / (f[k + 1] - f[k])).coerceIn(0f, 1f)
                    val a = s.colors[k]; val b = s.colors[k + 1]
                    fun mix(ca: Int, cb: Int) = (ca + (cb - ca) * u).toInt()
                    val c = Color.argb((mix(Color.alpha(a), Color.alpha(b)) * alpha).toInt(), mix(Color.red(a), Color.red(b)), mix(Color.green(a), Color.green(b)), mix(Color.blue(a), Color.blue(b)))
                    img.setRGB(i, j, c)
                }
                return img.raster
            }
        }
    }
}

class Bitmap private constructor(val image: BufferedImage, val alpha8: Boolean = false) {
    enum class Config { ARGB_8888, ALPHA_8, RGB_565 }
    enum class CompressFormat { PNG }
    val width get() = image.width
    val height get() = image.height
    fun setPixel(x: Int, y: Int, c: Int) = image.setRGB(x, y, c)
    fun getPixel(x: Int, y: Int) = image.getRGB(x, y)
    fun recycle() {}
    /** Le canal alpha seul, en masque ALPHA_8. */
    fun extractAlpha(): Bitmap {
        val out = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until height) for (x in 0 until width) out.setRGB(x, y, (image.getRGB(x, y) and 0xFF000000.toInt()) or 0xFFFFFF)
        return Bitmap(out, true)
    }
    fun compress(format: CompressFormat, quality: Int, out: java.io.OutputStream): Boolean = javax.imageio.ImageIO.write(image, "png", out)

    /** Le masque flouté (flou gaussien approché par trois flous en boîte), décalé de -rayon. */
    fun extractAlpha(paint: Paint?, offset: IntArray): Bitmap {
        val r = ((paint?.maskFilter as? BlurMaskFilter)?.radius ?: 0f).toInt().coerceAtLeast(1)
        val w = width + 2 * r
        val h = height + 2 * r
        var a = FloatArray(w * h)
        for (y in 0 until height) for (x in 0 until width) a[(y + r) * w + x + r] = Color.alpha(image.getRGB(x, y)).toFloat()
        val k = (r / 1.7f).toInt().coerceAtLeast(1)
        repeat(3) {
            val b = FloatArray(w * h)
            for (y in 0 until h) { var sum = 0f; for (x in -k until w + k) { if (x + k < w) sum += a[y * w + x + k]; if (x - k - 1 >= 0) sum -= a[y * w + x - k - 1]; if (x in 0 until w) b[y * w + x] = sum / (2 * k + 1) } }
            val c = FloatArray(w * h)
            for (x in 0 until w) { var sum = 0f; for (y in -k until h + k) { if (y + k < h) sum += b[(y + k) * w + x]; if (y - k - 1 >= 0) sum -= b[(y - k - 1) * w + x]; if (y in 0 until h) c[y * w + x] = sum / (2 * k + 1) } }
            a = c
        }
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        for (i in a.indices) out.setRGB(i % w, i / w, (a[i].toInt().coerceIn(0, 255) shl 24) or 0xFFFFFF)
        offset[0] = -r; offset[1] = -r
        return Bitmap(out, true)
    }
    companion object {
        fun createBitmap(w: Int, h: Int, c: Config) = Bitmap(BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB), c == Config.ALPHA_8)
        fun createScaledBitmap(src: Bitmap, w: Int, h: Int, filter: Boolean): Bitmap {
            val out = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
            out.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                drawImage(src.image, 0, 0, w, h, null); dispose()
            }
            return Bitmap(out, src.alpha8)
        }
    }
}

class BitmapShader(val bitmap: Bitmap, val tx: TileMode, val ty: TileMode) : Shader() {
    override fun awt(alpha: Float): java.awt.Paint = java.awt.TexturePaint(bitmap.image, Rectangle2D.Float(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()))
}

class ColorMatrix {
    val array = floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)
    fun setScale(r: Float, g: Float, b: Float, a: Float) {
        array.fill(0f); array[0] = r; array[6] = g; array[12] = b; array[18] = a
    }
}

open class ColorFilter
class ColorMatrixColorFilter(val matrix: ColorMatrix) : ColorFilter()
class PorterDuffColorFilter(val color: Int, val mode: PorterDuff.Mode) : ColorFilter()
object PorterDuff { enum class Mode { SRC_IN, SRC_ATOP, MULTIPLY, SRC_OVER, ADD, SCREEN, CLEAR } }
open class Xfermode
class PorterDuffXfermode(val mode: PorterDuff.Mode) : Xfermode()

open class MaskFilter
open class PathEffect
class DashPathEffect(val intervals: FloatArray, val phase: Float) : PathEffect()
class BlurMaskFilter(val radius: Float, val style: Blur) : MaskFilter() {
    enum class Blur { NORMAL, SOLID, OUTER, INNER }
}

class Paint(flags: Int = 0) {
    constructor(other: Paint) : this() {
        color = other.color; style = other.style; strokeWidth = other.strokeWidth; strokeCap = other.strokeCap
        strokeJoin = other.strokeJoin; shader = other.shader; typeface = other.typeface; textAlign = other.textAlign
        textSize = other.textSize; colorFilter = other.colorFilter; letterSpacing = other.letterSpacing; xfermode = other.xfermode
    }
    enum class Style { FILL, STROKE, FILL_AND_STROKE }
    enum class Cap { BUTT, ROUND, SQUARE }
    enum class Join { MITER, ROUND, BEVEL }
    enum class Align { LEFT, CENTER, RIGHT }
    companion object {
        const val ANTI_ALIAS_FLAG = 1
        const val FILTER_BITMAP_FLAG = 2
        const val DITHER_FLAG = 4
    }
    var isAntiAlias = flags and ANTI_ALIAS_FLAG != 0
    var isFilterBitmap = flags and FILTER_BITMAP_FLAG != 0
    var xfermode: Xfermode? = null
    var color: Int = Color.BLACK
    var alpha: Int
        get() = Color.alpha(color)
        set(v) { color = (color and 0xFFFFFF) or (v.coerceIn(0, 255) shl 24) }
    var style = Style.FILL
    var strokeWidth = 0f
    var strokeCap = Cap.BUTT
    var strokeJoin = Join.MITER
    var shader: Shader? = null
    var typeface: Typeface? = null
    var textAlign = Align.LEFT
    var textSize = 12f
    var letterSpacing = 0f
    var colorFilter: ColorFilter? = null
    var maskFilter: MaskFilter? = null
    var pathEffect: PathEffect? = null
    internal var shadowRadius = 0f
    internal var shadowColor = 0
    fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) { shadowRadius = radius; shadowColor = color }
    fun clearShadowLayer() { shadowRadius = 0f }
    fun setARGB(a: Int, r: Int, g: Int, b: Int) { color = Color.argb(a, r, g, b) }
    fun measureText(text: String): Float = font().let { f -> java.awt.Canvas().getFontMetrics(f).stringWidth(text).toFloat() }
    internal fun font(): Font {
        val t = typeface ?: Typeface.DEFAULT
        return Font(t.family, if (t.bold) Font.BOLD else Font.PLAIN, 1).deriveFont(textSize)
    }
}

class Path() {
    enum class Direction { CW, CCW }
    enum class Op { DIFFERENCE, INTERSECT, UNION, XOR, REVERSE_DIFFERENCE }
    enum class FillType { WINDING, EVEN_ODD }
    internal var p = Path2D.Float()
    constructor(src: Path) : this() { p = Path2D.Float(src.p) }
    var fillType = FillType.WINDING
    fun moveTo(x: Float, y: Float) = p.moveTo(x, y)
    fun lineTo(x: Float, y: Float) { if (p.currentPoint == null) p.moveTo(x, y) else p.lineTo(x, y) }
    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) = p.quadTo(x1, y1, x2, y2)
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) = p.curveTo(x1, y1, x2, y2, x3, y3)
    fun close() { if (p.currentPoint != null) p.closePath() }
    fun rewind() { p = Path2D.Float() }
    fun reset() { p = Path2D.Float() }
    fun addCircle(x: Float, y: Float, r: Float, d: Direction) = p.append(Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r), false)
    fun addOval(r: RectF, d: Direction) = p.append(Ellipse2D.Float(r.left, r.top, r.width(), r.height()), false)
    /** Comme Android : relie le point courant au début de l'arc. */
    fun arcTo(oval: RectF, start: Float, sweep: Float) =
        p.append(java.awt.geom.Arc2D.Float(oval.left, oval.top, oval.width(), oval.height(), -start, -sweep, java.awt.geom.Arc2D.OPEN), true)
    fun addArc(oval: RectF, start: Float, sweep: Float) =
        p.append(java.awt.geom.Arc2D.Float(oval.left, oval.top, oval.width(), oval.height(), -start, -sweep, java.awt.geom.Arc2D.OPEN), false)
    fun addRect(l: Float, t: Float, r: Float, b: Float, d: Direction) = p.append(Rectangle2D.Float(l, t, r - l, b - t), false)
    fun addRoundRect(rect: RectF, rx: Float, ry: Float, d: Direction) = p.append(RoundRectangle2D.Float(rect.left, rect.top, rect.width(), rect.height(), rx * 2, ry * 2), false)
    fun op(other: Path, op: Op): Boolean {
        val a = Area(p); val b = Area(other.p)
        when (op) {
            Op.DIFFERENCE -> a.subtract(b)
            Op.INTERSECT -> a.intersect(b)
            Op.UNION -> a.add(b)
            Op.XOR -> a.exclusiveOr(b)
            Op.REVERSE_DIFFERENCE -> { b.subtract(a); p = Path2D.Float(b); return true }
        }
        p = Path2D.Float(a); return true
    }
    fun offset(dx: Float, dy: Float) = p.transform(AffineTransform.getTranslateInstance(dx.toDouble(), dy.toDouble()))
    fun transform(m: Matrix) = p.transform(m.t)
    fun computeBounds(r: RectF, exact: Boolean) { val b = p.bounds2D; r.set(b.minX.toFloat(), b.minY.toFloat(), b.maxX.toFloat(), b.maxY.toFloat()) }
    val isEmpty get() = p.currentPoint == null
}

class Canvas(private val bitmap: Bitmap) {
    private class Layer(val image: BufferedImage, val g: java.awt.Graphics2D, val paint: Paint?, val parent: java.awt.Graphics2D?)
    private var g: java.awt.Graphics2D = setup(bitmap.image.createGraphics())
    private val stack = ArrayDeque<Pair<AffineTransform, java.awt.Shape?>>()
    private val layers = ArrayDeque<Pair<Int, Layer>>()
    val width get() = bitmap.width
    val height get() = bitmap.height

    private fun setup(g: java.awt.Graphics2D) = g.apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    }

    fun save(): Int { stack.addLast(g.transform to g.clip); return stack.size }
    fun saveLayer(bounds: RectF?, paint: Paint?): Int {
        val depth = save()
        val img = BufferedImage(bitmap.width, bitmap.height, BufferedImage.TYPE_INT_ARGB)
        val lg = setup(img.createGraphics())
        lg.transform = g.transform; lg.clip = g.clip
        layers.addLast(depth to Layer(img, lg, paint, g))
        g = lg
        return depth
    }
    fun saveLayer(l: Float, t: Float, r: Float, b: Float, paint: Paint?) = saveLayer(RectF(l, t, r, b), paint)
    fun restore() {
        val depth = stack.size
        val top = layers.lastOrNull()
        if (top != null && top.first == depth) {
            layers.removeLast()
            val layer = top.second
            layer.g.dispose()
            val filter = layer.paint?.colorFilter
            if (filter is ColorMatrixColorFilter) {
                val m = filter.matrix.array
                for (y in 0 until layer.image.height) for (x in 0 until layer.image.width) {
                    val c = layer.image.getRGB(x, y)
                    if (c == 0) continue
                    layer.image.setRGB(x, y, Color.argb((Color.alpha(c) * m[18]).toInt(), (Color.red(c) * m[0]).toInt(), (Color.green(c) * m[6]).toInt(), (Color.blue(c) * m[12]).toInt()))
                }
            }
            g = layer.parent!!
            val (t, c) = stack.removeLast()
            val saved = g.transform
            g.transform = AffineTransform()
            g.clip = null
            val a = (layer.paint?.alpha ?: 255) / 255f
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a)
            g.drawImage(layer.image, 0, 0, null)
            g.composite = AlphaComposite.SrcOver
            g.transform = t; g.clip = c
            return
        }
        val (t, c) = stack.removeLast()
        g.transform = t; g.clip = c
    }
    fun restoreToCount(count: Int) { while (stack.size >= count) restore() }
    fun translate(dx: Float, dy: Float) = g.translate(dx.toDouble(), dy.toDouble())
    fun scale(sx: Float, sy: Float) = g.scale(sx.toDouble(), sy.toDouble())
    fun scale(sx: Float, sy: Float, px: Float, py: Float) { translate(px, py); scale(sx, sy); translate(-px, -py) }
    fun rotate(deg: Float) = g.rotate(Math.toRadians(deg.toDouble()))
    fun rotate(deg: Float, px: Float, py: Float) = g.rotate(Math.toRadians(deg.toDouble()), px.toDouble(), py.toDouble())
    fun clipRect(l: Float, t: Float, r: Float, b: Float): Boolean { g.clip(Rectangle2D.Float(l, t, r - l, b - t)); return true }
    fun clipRect(r: RectF) = clipRect(r.left, r.top, r.right, r.bottom)
    fun clipPath(path: Path): Boolean { g.clip(path.p); return true }

    fun drawColor(color: Int) {
        val t = g.transform; g.transform = AffineTransform()
        g.color = Color.awt(color); g.fillRect(0, 0, bitmap.width, bitmap.height); g.transform = t
    }
    fun drawARGB(a: Int, r: Int, gg: Int, b: Int) = drawColor(Color.argb(a, r, gg, b))
    fun drawPaint(paint: Paint) {
        val t = g.transform; g.transform = AffineTransform()
        fill(Rectangle2D.Float(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), paint, t)
        g.transform = t
    }

    private fun prepare(paint: Paint, shaderTransform: AffineTransform? = null) {
        val sh = paint.shader
        if (sh != null) {
            g.paint = sh.awt(paint.alpha / 255f)
        } else {
            g.color = Color.awt(paint.color)
        }
        val cap = when (paint.strokeCap) { Paint.Cap.BUTT -> BasicStroke.CAP_BUTT; Paint.Cap.ROUND -> BasicStroke.CAP_ROUND; Paint.Cap.SQUARE -> BasicStroke.CAP_SQUARE }
        val join = when (paint.strokeJoin) { Paint.Join.MITER -> BasicStroke.JOIN_MITER; Paint.Join.ROUND -> BasicStroke.JOIN_ROUND; Paint.Join.BEVEL -> BasicStroke.JOIN_BEVEL }
        g.stroke = BasicStroke(paint.strokeWidth.coerceAtLeast(1f), cap, join)
    }

    private fun blurred(paint: Paint): Float = (paint.maskFilter as? BlurMaskFilter)?.radius ?: 0f

    private fun fill(shape: java.awt.Shape, paint: Paint, t: AffineTransform? = null) {
        val blur = blurred(paint)
        if (blur > 0f) { glow(shape, paint, blur, fill = true); return }
        prepare(paint); g.fill(shape)
    }

    private fun shape(shape: java.awt.Shape, paint: Paint) {
        if ((paint.xfermode as? PorterDuffXfermode)?.mode == PorterDuff.Mode.CLEAR) {
            // Efface : le masque redevient transparent sous la forme.
            g.composite = AlphaComposite.Clear
            if (paint.style == Paint.Style.STROKE) {
                g.stroke = BasicStroke(paint.strokeWidth.coerceAtLeast(1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                g.draw(shape)
            } else {
                g.fill(shape)
            }
            g.composite = AlphaComposite.SrcOver
            return
        }
        val blur = blurred(paint)
        if (blur > 0f) {
            glow(shape, paint, blur, fill = paint.style != Paint.Style.STROKE)
            return
        }
        prepare(paint)
        when (paint.style) {
            Paint.Style.FILL -> g.fill(shape)
            Paint.Style.STROKE -> g.draw(shape)
            Paint.Style.FILL_AND_STROKE -> { g.fill(shape); g.draw(shape) }
        }
    }

    /** Un flou approché : la forme élargie plusieurs fois, de moins en moins opaque. */
    private fun glow(shape: java.awt.Shape, paint: Paint, radius: Float, fill: Boolean) {
        val steps = 6
        val base = paint.alpha
        for (i in steps downTo 1) {
            val w = radius * 2 * i / steps + (if (fill) 0f else paint.strokeWidth)
            val p = Paint(paint).apply { alpha = (base / steps.toFloat() * 1.3f).toInt(); style = Paint.Style.STROKE; strokeWidth = w; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
            prepare(p); g.draw(shape)
        }
        if (fill) { prepare(Paint(paint).apply { alpha = base / 2 }); g.fill(shape) }
    }

    fun drawRect(l: Float, t: Float, r: Float, b: Float, paint: Paint) = shape(Rectangle2D.Float(minOf(l, r), minOf(t, b), kotlin.math.abs(r - l), kotlin.math.abs(b - t)), paint)
    fun drawRect(r: RectF, paint: Paint) = drawRect(r.left, r.top, r.right, r.bottom, paint)
    fun drawRoundRect(r: RectF, rx: Float, ry: Float, paint: Paint) = shape(RoundRectangle2D.Float(r.left, r.top, r.width(), r.height(), rx * 2, ry * 2), paint)
    fun drawRoundRect(l: Float, t: Float, r: Float, b: Float, rx: Float, ry: Float, paint: Paint) = drawRoundRect(RectF(l, t, r, b), rx, ry, paint)
    fun drawCircle(x: Float, y: Float, r: Float, paint: Paint) = shape(Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r), paint)
    fun drawOval(r: RectF, paint: Paint) = shape(Ellipse2D.Float(r.left, r.top, r.width(), r.height()), paint)
    fun drawArc(r: RectF, start: Float, sweep: Float, useCenter: Boolean, paint: Paint) =
        shape(java.awt.geom.Arc2D.Float(r.left, r.top, r.width(), r.height(), -start, -sweep, if (useCenter) java.awt.geom.Arc2D.PIE else java.awt.geom.Arc2D.OPEN), paint)
    fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) {
        val blur = blurred(paint)
        if (blur > 0f) { glow(Line2D.Float(x0, y0, x1, y1), paint, blur, fill = false); return }
        prepare(paint); g.draw(Line2D.Float(x0, y0, x1, y1))
    }
    fun drawLines(pts: FloatArray, paint: Paint) { var i = 0; while (i + 3 < pts.size) { drawLine(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], paint); i += 4 } }
    fun drawPoint(x: Float, y: Float, paint: Paint) = drawCircle(x, y, paint.strokeWidth / 2, Paint(paint).apply { style = Paint.Style.FILL })
    fun drawPath(path: Path, paint: Paint) = shape(path.p, paint)
    fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        prepare(paint)
        g.font = paint.font()
        val w = g.fontMetrics.stringWidth(text)
        val dx = when (paint.textAlign) { Paint.Align.LEFT -> 0f; Paint.Align.CENTER -> -w / 2f; Paint.Align.RIGHT -> -w.toFloat() }
        if (paint.shadowRadius > 0f) {
            val saved = g.paint
            val sc = paint.shadowColor
            for (r in listOf(paint.shadowRadius, paint.shadowRadius / 2)) {
                g.color = Color.awt(Color.argb(Color.alpha(sc) * Color.alpha(paint.color) / 255 / 3, Color.red(sc), Color.green(sc), Color.blue(sc)))
                for ((ox, oy) in listOf(-r to 0f, r to 0f, 0f to -r, 0f to r)) g.drawString(text, x + dx + ox, y + oy)
            }
            g.paint = saved
        }
        g.drawString(text, x + dx, y)
    }
    fun drawText(chars: CharArray, index: Int, count: Int, x: Float, y: Float, paint: Paint) = drawText(String(chars, index, count), x, y, paint)
    fun drawBitmap(b: Bitmap, x: Float, y: Float, paint: Paint?) = drawBitmap(b, null, RectF(x, y, x + b.width, y + b.height), paint)
    fun drawBitmap(b: Bitmap, src: Rect?, dst: RectF, paint: Paint?) {
        val img = if (b.alpha8 && paint != null) tint(b.image, paint.color) else b.image
        val a = if (b.alpha8 || paint == null) 1f else paint.alpha / 255f
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(img, dst.left.toInt(), dst.top.toInt(), dst.width().toInt(), dst.height().toInt(), null)
        g.composite = AlphaComposite.SrcOver
    }

    /** Un masque ALPHA_8 dessiné avec une peinture : la couleur de la peinture, l'alpha du masque. */
    private fun tint(image: BufferedImage, color: Int): BufferedImage {
        val out = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val rgb = color and 0xFFFFFF
        val pa = Color.alpha(color)
        val row = IntArray(image.width)
        for (y in 0 until image.height) {
            image.getRGB(0, y, image.width, 1, row, 0, image.width)
            for (x in row.indices) row[x] = ((Color.alpha(row[x]) * pa / 255) shl 24) or rgb
            out.setRGB(0, y, image.width, 1, row, 0, image.width)
        }
        return out
    }
}

object BitmapFactory {
    fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap? {
        val img = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(data, offset, length)) ?: return null
        val out = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB)
        out.createGraphics().apply { drawImage(img, 0, 0, null); dispose() }
        val bitmap = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until img.height) for (x in 0 until img.width) bitmap.setPixel(x, y, out.getRGB(x, y))
        return bitmap
    }
}

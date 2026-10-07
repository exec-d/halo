package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Fallout : tout le téléphone devient le Pip-Boy.
 *
 * - devant, le boîtier : ses verrous, sa plaque, son bouton, ses jointures et
 *   ses vis ; le grand écran cathodique bombé dans son cadre épais et son
 *   voyant ; en bas, le compteur Geiger à aiguille, la molette et les
 *   rainures ;
 * - au fond de l'écran, la lueur du phosphore.
 *
 * L'écran fait défiler les cinq onglets du jeu, sept secondes chacun, avec
 * un parasite à chaque changement : STAT (S.P.E.C.I.A.L. et le Vault Boy qui
 * arrive en rebondissant), INV (les objets de soin, un Stimpak qui tourne en
 * fil de fer), DATA (la vraie date, en 2287, l'heure et les objectifs qui
 * s'écrivent), MAP (la carte qui défile, le joueur, la boussole) et RADIO
 * (les stations et l'oscilloscope). HEALTH suit la batterie ; en charge,
 * STIMPAK s'allume et la barre se remplit en vague. Avec du réseau,
 * l'oscilloscope s'agite et le compteur Geiger s'affole. À l'allumage,
 * l'écran s'ouvre comme un tube.
 *
 * Les cotes sont celles de la maquette : un centième de la largeur, et une
 * hauteur de 216 unités ramenée à celle de l'écran par [y].
 */
class VaultCore(kit: CoreKit) : CoreArt(kit) {

    override val wholePhone get() = true

    private val scale = kit.h / 216f
    private fun x(v: Float) = v * u
    private fun y(v: Float) = v * scale * u
    private fun p(px: Float, py: Float) = PointF(x(px), y(py))
    private fun r(l: Float, t: Float, rt: Float, b: Float) = RectF(x(l), y(t), x(rt), y(b))

    // ——— Les cotes, en unités ———

    private val screen = floatArrayOf(13f, 47f, 87f, 160f)
    private val bezel = floatArrayOf(8f, 41f, 92f, 166f)
    private val screenRect = r(screen[0], screen[1], screen[2], screen[3])
    private val screenPath = Path().apply { addRoundRect(screenRect, x(7f), x(7f), Path.Direction.CW) }
    private val screenCenter = p(50f, 103f)
    private val dial = p(22f, 192f)
    private val knob = p(78f, 192f)
    private val led = p(85.5f, 44f)
    private val ring = Triple(68f, 99f, 14f)

    private val tabs = listOf("STAT" to 27f, "INV" to 38.5f, "DATA" to 49.5f, "MAP" to 61f, "RADIO" to 72.5f)
    private val tabHalf = floatArrayOf(5f, 4f, 5f, 4.5f, 5.6f)
    private val subTabs = listOf(
        listOf("STATUS", "SPECIAL", "PERKS"), listOf("WEAPONS", "APPAREL", "AID", "MISC", "JUNK"),
        listOf("QUESTS", "WORKSHOPS"), listOf("WORLD MAP", "LOCAL MAP"), emptyList(),
    )
    private val subOn = intArrayOf(1, 2, 0, 1, -1)
    private val special = listOf("Strength" to 6, "Perception" to 7, "Endurance" to 5, "Charisma" to 4, "Intelligence" to 8, "Agility" to 6, "Luck" to 7)

    private class Item(val name: String, val count: Int, val stats: List<Pair<String, String>>)

    private val items = listOf(
        Item("Stimpak", 4, listOf("HP" to "+40", "WG" to "0.1", "VAL" to "50")),
        Item("RadAway", 2, listOf("RADS" to "-300", "WG" to "0.1", "VAL" to "80")),
        Item("Rad-X", 3, listOf("RAD RES" to "+100", "WG" to "0.1", "VAL" to "40")),
        Item("Nuka-Cola", 6, listOf("HP" to "+10", "AP" to "+5", "VAL" to "20")),
        Item("Purified Water", 5, listOf("HP" to "+20", "WG" to "0.5", "VAL" to "20")),
        Item("Sugar Bombs", 1, listOf("HP" to "+15", "RADS" to "+5", "VAL" to "11")),
        Item("Fancy Lads Cakes", 2, listOf("HP" to "+20", "WG" to "0.5", "VAL" to "12")),
    )
    private val quests = listOf(
        "Out of Time" to listOf("Talk to Preston", "Find Kellogg", "Reach Diamond City"),
        "Unlikely Valentine" to listOf("Find Nick Valentine", "Escort Nick to his office"),
        "Reunions" to listOf("Search Kellogg's house", "Follow Dogmeat", "Find Kellogg in Fort Hagen"),
        "Dangerous Minds" to listOf("Talk to Dr. Amari", "Enter the Memory Lounger"),
        "The Molecular Level" to listOf("Find a Courser", "Decode the chip"),
    )
    private val stations = listOf("Classical Radio", "Diamond City Radio", "Distress Signal", "Freedom Radio", "Military Frequency AF95")
    private val places = listOf(Triple(26f, 84f, "Sanctuary"), Triple(64f, 108f, "Red Rocket"), Triple(70f, 70f, "Concord"), Triple(80f, 116f, "Corvega"))
    private val compass = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    /** Les déclics du compteur Geiger : position et rythme. */
    private val clicks = (0 until 24).map { floatArrayOf(noise(it * 3), noise(it * 3 + 1), noise(it * 3 + 2)) }

    private fun seg(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) =
        canvas.drawLine(x(x0), y(y0), x(x1), y(y1), paint)

    private fun round(canvas: Canvas, l: Float, t: Float, rt: Float, b: Float, radius: Float, paint: Paint) =
        canvas.drawRoundRect(r(l, t, rt, b), x(radius), x(radius), paint)

    private fun circle(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) = canvas.drawCircle(x(cx), y(cy), x(radius), paint)

    private val condensed: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    // ——— Devant : le boîtier ———

    override fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val soft = Paint(thin).apply { alpha = 120 }
        val fine = Paint(thin).apply { strokeWidth = 0.5f * density; alpha = 170 }

        // Les jointures et les vis.
        canvas.drawPath(polyline(listOf(4.4f to 34f, 20f to 34f, 24f to 30f, 76f to 30f, 80f to 34f, 95.6f to 34f)), soft)
        canvas.drawPath(polyline(listOf(4.4f to 174f, 16f to 174f, 20f to 178f, 80f to 178f, 84f to 174f, 95.6f to 174f)), soft)
        seg(canvas, 6.5f, 36f, 6.5f, 172f, fine)
        seg(canvas, 93.5f, 36f, 93.5f, 172f, fine)
        listOf(11.5f to 12f, 88.5f to 12f, 11.5f to 205f, 88.5f to 205f, 5.8f to 100f, 94.2f to 100f).forEach { (sx, sy) -> screw(canvas, p(sx, sy), thin) }

        // En haut : les deux verrous, la plaque, le bouton.
        for ((a, b) in listOf(16f to 34f, 66f to 84f)) {
            round(canvas, a, 14f, b, 22f, 1.2f, line)
            round(canvas, a + 1.2f, 15.2f, b - 1.2f, 20.8f, 0.8f, fine)
            var gx = a + 3f
            while (gx < b - 2f) {
                seg(canvas, gx, 16.5f, gx, 19.5f, fine)
                gx += 2.2f
            }
        }
        round(canvas, 38f, 23f, 62f, 29f, 1.6f, thin)
        round(canvas, 40f, 24.4f, 60f, 27.6f, 1f, fine)
        seg(canvas, 42f, 26f, 58f, 26f, fine)
        circle(canvas, 76f, 27f, 2.2f, thin)
        circle(canvas, 76f, 27f, 1.2f, fine)

        // Le cadre de l'écran, le verre, le logement du voyant.
        round(canvas, bezel[0], bezel[1], bezel[2], bezel[3], 9f, Paint(line).apply { strokeWidth = 1.5f * density })
        round(canvas, bezel[0] + 1.6f, bezel[1] + 1.6f, bezel[2] - 1.6f, bezel[3] - 1.6f, 7.6f, fine)
        canvas.drawPath(screenPath, line)
        circle(canvas, 85.5f, 44f, 0.9f, fine)

        // Les icônes des coins et le cadre de HEALTH.
        round(canvas, 15.5f, 50.5f, 19.5f, 54.5f, 0.3f, fine)
        circle(canvas, 17.5f, 52.5f, 1.1f, fine)
        for (k in 0 until 8) {
            val a = k * PI.toFloat() / 4f
            seg(canvas, 17.5f + cos(a) * 1.1f, 52.5f + sin(a) * 1.1f, 17.5f + cos(a) * 1.6f, 52.5f + sin(a) * 1.6f, fine)
        }
        round(canvas, 80.5f, 50.5f, 84.5f, 54.5f, 0.3f, fine)
        round(canvas, 81.3f, 51.6f, 83.7f, 53.4f, 0.2f, fine)
        round(canvas, 31f, 149f, 83f, 154.4f, 0.4f, thin)

        // Le compteur Geiger, ses graduations, son inscription.
        canvas.drawCircle(dial.x, dial.y, x(9f), line)
        canvas.drawCircle(dial.x, dial.y, x(7.6f), fine)
        for (k in 0..12) {
            val a = PI.toFloat() * (1.1f + 0.8f * k / 12f)
            val r0 = if (k % 3 != 0) 5.6f else 5f
            canvas.drawLine(dial.x + cos(a) * x(r0), dial.y + sin(a) * x(r0), dial.x + cos(a) * x(6.6f), dial.y + sin(a) * x(6.6f), thin)
        }
        val red = x(6.1f)
        canvas.drawArc(RectF(dial.x - red, dial.y - red, dial.x + red, dial.y + red), 309.6f, 32.4f, false, Paint(line).apply { strokeWidth = 0.9f * density })
        val label = Paint(fill).apply { typeface = condensed; textSize = x(2.6f); textAlign = Paint.Align.CENTER; alpha = 180 }
        canvas.drawText("RADS", dial.x, dial.y + x(4.5f) + label.textSize * 0.36f, label)
        // La molette crantée.
        canvas.drawCircle(knob.x, knob.y, x(8f), line)
        canvas.drawCircle(knob.x, knob.y, x(5.4f), thin)
        for (k in 0 until 24) {
            val a = k / 24f * 2f * PI.toFloat()
            canvas.drawLine(knob.x + cos(a) * x(6.2f), knob.y + sin(a) * x(6.2f), knob.x + cos(a) * x(7.4f), knob.y + sin(a) * x(7.4f), thin)
        }
        canvas.drawLine(knob.x, knob.y, knob.x, knob.y - x(4.4f), thin)
        // Les rainures.
        var rx = 38f
        while (rx <= 62f) {
            round(canvas, rx - 0.9f, 184f, rx + 0.9f, 200f, 0.9f, thin)
            rx += 3f
        }
    }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) = Unit

    override fun outlines(): List<Path> = listOf(
        Path(screenPath),
        Path().apply { addRoundRect(r(bezel[0], bezel[1], bezel[2], bezel[3]), x(9f), x(9f), Path.Direction.CW) },
        Path().apply { addCircle(dial.x, dial.y, x(9f), Path.Direction.CW) },
        Path().apply { addCircle(knob.x, knob.y, x(8f), Path.Direction.CW) },
    )

    override val origin get() = screenCenter

    /** Le réseau : de l'antenne (le bouton) au compteur Geiger ; la charge : de la molette au voyant ; le battement : le long de la plaque. */
    override fun routes() = Triple(
        listOf(
            CircuitScene.Route(listOf(p(76f, 29.2f), p(80f, 34f), p(93.5f, 34f), p(93.5f, 172f), p(84f, 174f), p(20f, 178f), p(22f, 183f))),
            CircuitScene.Route(listOf(p(76f, 29.2f), p(24f, 30f), p(20f, 34f), p(6.5f, 34f), p(6.5f, 172f), p(16f, 174f), p(22f, 183f))),
        ),
        listOf(CircuitScene.Route(listOf(p(85.5f, 45f), p(93.5f, 50f), p(93.5f, 172f), p(84f, 174f), p(78f, 184f)))),
        listOf(CircuitScene.Route(listOf(p(40f, 26f), p(60f, 26f))), CircuitScene.Route(listOf(p(58f, 26f), p(42f, 26f)))),
    )

    // ——— Ce qui vit ———

    private var glowColor = 0
    private var phosphor: RadialGradient? = null
    private var vignette: RadialGradient? = null

    /** Au fond de l'écran : la lueur du phosphore. */
    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val palette = ink.palette
        if (phosphor == null || glowColor != palette.glow) {
            glowColor = palette.glow
            val clear = palette.glow and 0x00FFFFFF
            phosphor = RadialGradient(screenCenter.x, screenCenter.y, x(70f), intArrayOf(clear or (0x30 shl 24), clear or (0x06 shl 24)), null, Shader.TileMode.CLAMP)
        }
        val flicker = 0.94f + 0.04f * sin(state.timeMillis / 1000f * 50f)
        canvas.save()
        canvas.clipPath(screenPath)
        ink.fill.shader = phosphor
        ink.fill.alpha = (255 * flicker * strength(state) * state.ignition.coerceIn(0f, 1f)).toInt()
        canvas.drawRect(screenRect, ink.fill)
        ink.fill.shader = null
        canvas.restore()
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = condensed }
    private val point = PointF()
    private val scanlines = Path().apply {
        var sy = screen[1]
        while (sy < screen[3]) {
            addRect(x(screen[0]), y(sy), x(screen[2]), y(sy + 0.35f), Path.Direction.CW)
            sy += 0.9f
        }
    }
    private val vbRect = RectF()
    private val box = RectF()

    /** L'écran : un pas de dessin, avec ce qu'il faut pour écrire. */
    private inner class Screen(val canvas: Canvas, val ink: Ink, val s: Float, val flicker: Float) {
        val palette = ink.palette

        fun text(str: String, px: Float, py: Float, size: Float, a: Float = 0.9f, align: Paint.Align = Paint.Align.LEFT, bright: Boolean = false, max: Float = 0f, dark: Boolean = false) {
            if (a <= 0f) return
            textPaint.textSize = x(size)
            if (max > 0f) {
                val w = textPaint.measureText(str)
                if (w > x(max)) textPaint.textSize *= x(max) / w
            }
            textPaint.textAlign = align
            textPaint.color = when {
                dark -> DARK
                bright -> palette.core
                else -> palette.line
            }
            textPaint.alpha = (255 * min(1f, a * if (dark) 1f else flicker) * s).toInt()
            canvas.drawText(str, x(px), y(py) + textPaint.textSize * 0.36f, textPaint)
        }

        fun width(str: String, size: Float): Float {
            textPaint.textSize = x(size)
            return textPaint.measureText(str) / u
        }

        fun fill(l: Float, t: Float, rt: Float, b: Float, a: Float, color: Int = palette.line) {
            ink.fill.color = color
            ink.fill.alpha = (255 * min(1f, a) * s).toInt()
            canvas.drawRect(x(l), y(t), x(rt), y(b), ink.fill)
        }

        fun stroke(a: Float, width: Float = 0.6f, bright: Boolean = false): Paint = ink.stroke.apply {
            color = if (bright) palette.core else palette.line
            alpha = (255 * min(1f, a) * s).toInt()
            strokeWidth = width * density
        }

        /** Une ligne sélectionnée : un bandeau plein, le texte en creux. */
        fun band(l: Float, py: Float, rt: Float, a: Float) = fill(l, py - 2.8f, rt, py + 2.8f, 0.85f * flicker * a)
    }

    private fun hash(n: Int): Float = noise(n * 2_654_435 + 17)

    override fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val flicker = 0.94f + 0.04f * sin(seconds * 50f) + 0.02f * sin(seconds * 7.3f)
        val net = state.pulses.isNotEmpty()
        val battery = state.batteryLevel.coerceIn(0f, 1f)
        val sc = Screen(canvas, ink, strength, flicker)

        canvas.save()
        canvas.clipPath(screenPath)

        // L'allumage : le tube s'allume en un trait, puis s'ouvre.
        val since = ignition * 1.4f
        val booting = ignition < 1f
        if (booting) {
            val open = ((since - 0.18f) / 0.5f).coerceIn(0f, 1f).pow(2)
            val half = max(0.35f, open * 60f)
            canvas.clipRect(x(screen[0]), y(103f - half), x(screen[2]), y(103f + half))
            if (since < 0.25f) {
                val w = 40f * min(1f, since / 0.12f)
                sc.fill(50f - w, 102.6f, 50f + w, 103.4f, 1f - since / 0.25f * 0.3f, ink.palette.core)
            }
        }

        // Les pages défilent ; chaque changement passe par un parasite.
        // Décalé pour qu'au premier coup d'œil (la miniature) STAT soit déjà affiché.
        val cycle = (seconds + 2.5f) / PAGE
        val page = (floor(cycle).toLong() % 5).toInt()
        val local = (cycle - floor(cycle)) * PAGE
        val glitch = if (local < 0.45f) 1f - local / 0.45f else 0f
        fun appear(i: Int) = ((local - 0.3f - i * 0.07f) / 0.12f).coerceIn(0f, 1f)
        val frameTick = floor(seconds * 30f).toInt()
        canvas.save()
        if (glitch > 0f) canvas.translate(x((hash(frameTick) - 0.5f) * 4f * glitch), 0f)

        // Les onglets, et les crochets qui glissent vers l'actif.
        val prev = (page + 4) % 5
        val ease = 1f - (1f - min(1f, local / 0.3f)).pow(3)
        val bx = tabs[prev].second + (tabs[page].second - tabs[prev].second) * ease
        val bw = tabHalf[prev] + (tabHalf[page] - tabHalf[prev]) * ease
        tabs.forEachIndexed { i, (name, tx) -> sc.text(name, tx, 54.2f, 4.2f, if (i == page) 1f else 0.75f, Paint.Align.CENTER, i == page, 9.4f) }
        val bright = sc.stroke(0.95f * flicker, 0.8f, true)
        canvas.drawPath(polyline(listOf(bx - bw to 57.5f, bx - bw to 52f, bx - bw + 1.2f to 52f)), bright)
        canvas.drawPath(polyline(listOf(bx + bw to 57.5f, bx + bw to 52f, bx + bw - 1.2f to 52f)), bright)
        val rule = sc.stroke(0.85f * flicker, 0.8f)
        canvas.drawPath(polyline(listOf(17f to 60f, 17f to 57.5f, bx - bw to 57.5f)), rule)
        canvas.drawPath(polyline(listOf(bx + bw to 57.5f, 83f to 57.5f, 83f to 60f)), rule)
        var sx = if (page == 1) 17.5f else 23f
        subTabs[page].forEachIndexed { i, name ->
            sc.text(name, sx, 63.5f, 3.4f, (if (i == subOn[page]) 1f else 0.55f) * appear(0), bright = i == subOn[page])
            sx += sc.width(name, 3.4f) + 2.4f
        }

        when (page) {
            0 -> statPage(sc, local, seconds, state.charging, ::appear)
            1 -> invPage(sc, local, seconds, ::appear)
            2 -> dataPage(sc, local, seconds, ::appear)
            3 -> mapPage(sc, seconds, ::appear)
            else -> radioPage(sc, local, seconds, net, ::appear)
        }

        // Le parasite : des bandes qui déchirent l'écran.
        if (glitch > 0f) for (k in 0 until 5) {
            val gy = screen[1] + hash(frameTick + k * 7) * (screen[3] - screen[1])
            sc.fill(screen[0], gy, screen[2], gy + 0.6f + hash(k + frameTick) * 2f, 0.35f * glitch, ink.palette.core)
        }
        canvas.restore()

        // HEALTH : la batterie, sur toutes les pages. En charge, elle se remplit en vague ; sous 20 %, elle clignote.
        val wave = if (state.charging) (seconds * 0.5f) % 1f else 1f
        val fillTo = 31.6f + 50.8f * battery * if (state.charging) 0.85f + 0.15f * wave else 1f
        val low = if (battery < 0.2f) (if (sin(seconds * 6f) > 0f) 1f else 0.25f) else 1f
        sc.text("HEALTH", 17.5f, 151.7f, 3.7f, low, bright = true, max = 12f)
        sc.text("HP ${(battery * 100).roundToInt()}/100", 83f, 145.6f, 3.4f, 0.9f, Paint.Align.RIGHT)
        sc.fill(31.6f, 149.6f, fillTo, 153.8f, 0.85f * flicker * low)

        // Le balayage : des lignes, une bande qui descend ; le bombé du verre.
        ink.fill.color = Color.BLACK
        ink.fill.alpha = 72
        canvas.drawPath(scanlines, ink.fill)
        val sweep = screen[1] + ((seconds * 0.18f) % 1f) * (screen[3] - screen[1] + 20f) - 10f
        ink.halo(canvas, p(30f, sweep), x(14f), (14 * strength).toInt())
        ink.halo(canvas, p(70f, sweep), x(14f), (14 * strength).toInt())
        if (vignette == null) vignette = RadialGradient(screenCenter.x, screenCenter.y, x(72f), intArrayOf(Color.TRANSPARENT, Color.argb(140, 0, 0, 0)), floatArrayOf(0.42f, 1f), Shader.TileMode.CLAMP)
        ink.fill.shader = vignette
        ink.fill.alpha = 255
        canvas.drawRect(screenRect, ink.fill)
        ink.fill.shader = null
        if (booting) sc.fill(screen[0], screen[1], screen[2], screen[3], max(0f, 0.5f - since * 0.5f), ink.palette.core)
        canvas.restore()

        // Le voyant : fixe, ou qui clignote en charge.
        val ledOn = if (state.charging) (if (sin(seconds * 5f) > 0f) 1f else 0.2f) else 0.75f
        ink.halo(canvas, led, x(3f), (128 * ledOn * strength).toInt())
        ink.fill.color = ink.palette.core
        ink.fill.alpha = (255 * ledOn * strength).toInt()
        canvas.drawCircle(led.x, led.y, x(0.8f), ink.fill)

        // Le compteur Geiger : l'aiguille tremble, des déclics s'allument ; avec du réseau, il s'affole.
        val jitter = sin(seconds * 13f) * 0.02f + sin(seconds * 29f) * 0.015f
        val level = (if (net) 0.65f else 0.25f) + 0.15f * sin(seconds * 0.4f)
        val a = PI.toFloat() * (1.1f + 0.8f * min(1f, level + jitter))
        val needle = sc.stroke(0.95f, 1f, true)
        canvas.drawLine(dial.x, dial.y, dial.x + cos(a) * x(6.6f), dial.y + sin(a) * x(6.6f), needle)
        ink.fill.color = ink.palette.core
        ink.fill.alpha = (242 * strength).toInt()
        canvas.drawCircle(dial.x, dial.y, x(0.9f), ink.fill)
        clicks.forEach { (k1, k2, k3) ->
            val ph = (seconds * (0.3f + k3) * (if (net) 3f else 1f) + k1 * 10f) % 1f
            if (ph < 0.08f) spark(canvas, ink, p(16f + k2 * 12f, 186f + k1 * 3f), (1f - ph / 0.08f) * strength, 0.3f)
        }
    }

    private fun spark(canvas: Canvas, ink: Ink, at: PointF, a: Float, radius: Float) {
        if (a <= 0.01f) return
        ink.halo(canvas, at, x(2.1f), (165 * a).toInt())
        ink.fill.color = ink.palette.core
        ink.fill.alpha = (255 * a).coerceIn(0f, 255f).toInt()
        canvas.drawCircle(at.x, at.y, x(radius), ink.fill)
    }

    // ——— Les pages ———

    /** STAT : S.P.E.C.I.A.L., le Vault Boy, le niveau, les boutons. */
    private fun statPage(sc: Screen, local: Float, seconds: Float, charging: Boolean, appear: (Int) -> Float) {
        val sel = (local / 0.9f).toInt() % special.size
        special.forEachIndexed { i, (name, value) ->
            val a = appear(i)
            if (a <= 0f) return@forEachIndexed
            val sy = 80f + i * 7.6f
            if (i == sel) sc.band(17f, sy, 48f, a)
            sc.text(name, 18.5f, sy, 3.7f, 0.85f * a, max = 22f, dark = i == sel)
            sc.text(value.toString(), 46.5f, sy, 3.7f, 0.85f * a, Paint.Align.RIGHT, dark = i == sel)
        }
        // Le Vault Boy arrive en rebondissant, puis se balance doucement.
        val pop = ((local - 0.3f) / 0.5f).coerceIn(0f, 1f)
        if (pop > 0f) {
            val bounce = if (pop < 1f) 1f - cos(pop * PI.toFloat() * 2.5f) * (1f - pop) * 0.6f else 1f
            val bob = sin(seconds * 2.4f) * 0.45f
            val grow = (0.6f + 0.4f * bounce) * (1f + sin(seconds * 4.8f) * 0.008f)
            val (cx, cy, rr) = ring
            val mask = VaultBoy.mask
            val w = rr / VaultBoy.RING_R
            val h = w * mask.height / mask.width
            vbRect.set(x(cx - VaultBoy.RING_X * w), y(cy - VaultBoy.RING_Y * h), x(cx - VaultBoy.RING_X * w + w), y(cy - VaultBoy.RING_Y * h + h))
            sc.canvas.save()
            sc.canvas.translate(0f, y(bob))
            sc.canvas.scale(grow, grow, x(cx), y(cy))
            sc.ink.fill.color = sc.palette.line
            sc.ink.fill.alpha = (235 * sc.flicker * pop * sc.s).toInt()
            sc.ink.fill.isFilterBitmap = true
            sc.canvas.drawBitmap(mask, null, vbRect, sc.ink.fill)
            sc.canvas.restore()
        }
        sc.text("LEVEL 12", 54f, 122.5f, 3.4f, 0.9f * appear(3))
        sc.canvas.drawRect(r(54f, 125.6f, 82f, 128.2f), sc.stroke(0.6f * appear(3)))
        val xp = 0.35f + 0.5f * ((seconds * 0.02f) % 1f)
        sc.fill(54.5f, 126.1f, 54.5f + 27f * xp, 127.7f, 0.8f * sc.flicker * appear(4))
        // STIMPAK s'allume en charge.
        val a = appear(6)
        val stim = if (charging) 0.5f + 0.5f * sin(seconds * 4f) else 0f
        val frame = sc.stroke(0.8f * a, 0.8f)
        sc.canvas.drawRect(r(17f, 136.5f, 33f, 142.5f), frame)
        sc.canvas.drawRect(r(35f, 136.5f, 51f, 142.5f), frame)
        sc.fill(17.5f, 137f, 32.5f, 142f, (0.7f + 0.3f * stim) * sc.flicker * a)
        if (stim > 0f) sc.ink.halo(sc.canvas, p(25f, 139.5f), x(9f), (60 * stim * sc.s).toInt())
        if (a > 0.5f) sc.text("STIMPAK", 25f, 139.6f, 3.3f, 1f, Paint.Align.CENTER, max = 14f, dark = true)
        sc.text("RADAWAY", 43f, 139.6f, 3.3f, 0.85f * a, Paint.Align.CENTER, max = 14f)
    }

    /** INV : les objets de soin, un Stimpak en fil de fer qui tourne, les caractéristiques de l'objet choisi. */
    private fun invPage(sc: Screen, local: Float, seconds: Float, appear: (Int) -> Float) {
        val sel = (local / 1.1f).toInt() % items.size
        items.forEachIndexed { i, item ->
            val a = appear(i)
            if (a <= 0f) return@forEachIndexed
            val sy = 72f + i * 7.6f
            if (i == sel) sc.band(17f, sy, 50f, a)
            sc.text(item.name, 18.5f, sy, 3.6f, 0.85f * a, max = 24f, dark = i == sel)
            sc.text("(${item.count})", 48.5f, sy, 3.6f, 0.85f * a, Paint.Align.RIGHT, dark = i == sel)
        }
        // La seringue : deux cylindres et une aiguille, en perspective.
        val a = appear(2)
        if (a > 0f) {
            val rot = seconds * 1.4f
            val c = cos(rot)
            val s = sin(rot)
            fun proj(px: Float, py: Float, pz: Float): Pair<Float, Float> {
                val xx = px * c + pz * s
                val zz = -px * s + pz * c
                return (69f + xx * (1f + zz * 0.02f)) to (84f + py * 0.95f - zz * 0.25f)
            }
            val wire = sc.stroke(0.9f * a * sc.flicker, 0.7f, true)
            val radius = 3.2f
            for ((y0, rad) in listOf(-9f to radius, 3f to radius, -11f to radius * 0.6f, -9.6f to radius * 0.6f)) {
                sc.canvas.drawPath(polyline((0..24).map { k -> val an = k / 24f * 2f * PI.toFloat(); proj(cos(an) * rad, y0, sin(an) * rad * 0.3f) }), wire)
            }
            for (k in 0 until 6) {
                val an = k / 6f * 2f * PI.toFloat()
                sc.canvas.drawPath(polyline(listOf(proj(cos(an) * radius, -9f, sin(an) * radius * 0.3f), proj(cos(an) * radius, 3f, sin(an) * radius * 0.3f))), wire)
            }
            sc.canvas.drawPath(polyline(listOf(proj(0f, 3f, 0f), proj(0f, 10f, 0f))), wire)
            sc.canvas.drawPath(polyline(listOf(proj(-1.2f, -11f, 0f), proj(1.2f, -11f, 0f))), wire)
        }
        items[sel].stats.forEachIndexed { i, (k, v) ->
            val sy = 102f + i * 6.5f
            val aa = appear(3 + i)
            sc.text(k, 57f, sy, 3.4f, 0.85f * aa)
            sc.text(v, 84f, sy, 3.4f, 0.95f * aa, Paint.Align.RIGHT, bright = true)
            seg(sc.canvas, 57f, sy + 2.4f, 84f, sy + 2.4f, sc.stroke(0.35f * aa, 0.5f))
        }
        sc.text("WG 128/250", 17.5f, 139.5f, 3.4f, 0.85f * appear(7))
        sc.text("CAPS 1337", 84f, 139.5f, 3.4f, 0.85f * appear(7), Paint.Align.RIGHT)
    }

    /** DATA : la vraie date (en 2287) et l'heure, les quêtes, les objectifs de la quête choisie. */
    private fun dataPage(sc: Screen, local: Float, seconds: Float, appear: (Int) -> Float) {
        val now = LocalDateTime.now()
        sc.text("%02d.%02d.2287".format(now.dayOfMonth, now.monthValue), 84f, 63.5f, 3.4f, 0.9f * appear(0), Paint.Align.RIGHT, bright = true)
        sc.text("%02d:%02d".format(now.hour, now.minute), 84f, 69f, 5f, appear(0), Paint.Align.RIGHT, bright = true)
        val sel = (local / 1.3f).toInt() % quests.size
        quests.forEachIndexed { i, (name, _) ->
            val a = appear(i)
            if (a <= 0f) return@forEachIndexed
            val sy = 78f + i * 7.6f
            if (i == sel) sc.band(17f, sy, 49f, a)
            sc.text(name, 18.5f, sy, 3.5f, 0.85f * a, max = 29f, dark = i == sel)
        }
        // Les objectifs s'écrivent lettre à lettre.
        val typed = local - floor(local / 1.3f) * 1.3f
        val objectives = quests[sel].second
        objectives.forEachIndexed { i, o ->
            val sy = 80f + i * 8f
            val n = max(0, ((typed - i * 0.25f) * 40f).toInt()).coerceAtMost(o.length)
            val done = i < objectives.size - 1
            sc.canvas.drawRect(r(52.5f, sy - 1.3f, 55.1f, sy + 1.3f), sc.stroke(0.8f, 0.6f))
            if (done) sc.fill(53.1f, sy - 0.7f, 54.5f, sy + 0.7f, 0.85f * sc.flicker)
            val cursor = if (n < o.length && sin(seconds * 20f) > 0f) "_" else ""
            sc.text(o.substring(0, n) + cursor, 57f, sy, 3.1f, if (done) 0.6f else 1f, bright = !done, max = 27f)
        }
        sc.text("ACTIVE QUESTS ${quests.size}", 17.5f, 139.5f, 3.4f, 0.85f * appear(6))
    }

    /** MAP : la carte défile, le joueur tourne, une onde part de lui ; la boussole suit. */
    private fun mapPage(sc: Screen, seconds: Float, appear: (Int) -> Float) {
        val canvas = sc.canvas
        val heading = seconds * 0.25f + sin(seconds * 0.3f) * 0.6f
        val panX = sin(seconds * 0.1f) * 6f
        val panY = cos(seconds * 0.13f) * 4f
        canvas.save()
        canvas.clipRect(r(16f, 68f, 84f, 128f))
        val grid = sc.stroke(0.18f * sc.flicker, 0.5f)
        var gx = -40f
        while (gx <= 140f) {
            seg(canvas, gx + panX % 6f, 68f, gx + panX % 6f, 128f, grid)
            gx += 6f
        }
        var gy = 40f
        while (gy <= 160f) {
            seg(canvas, 16f, gy + panY % 6f, 84f, gy + panY % 6f, grid)
            gy += 6f
        }
        fun at(list: List<Pair<Float, Float>>) = polyline(list.map { (a, b) -> a + panX to b + panY })
        val road = sc.stroke(0.7f * sc.flicker, 1.1f)
        canvas.drawPath(at(listOf(10f to 112f, 30f to 104f, 44f to 100f, 56f to 92f, 74f to 88f, 92f to 80f)), road)
        canvas.drawPath(at(listOf(40f to 60f, 46f to 80f, 44f to 100f, 48f to 120f, 46f to 140f)), road)
        val lane = sc.stroke(0.45f * sc.flicker, 0.6f)
        canvas.drawPath(at(listOf(56f to 92f, 64f to 108f, 80f to 116f)), lane)
        canvas.drawPath(at(listOf(30f to 104f, 26f to 84f, 20f to 70f)), lane)
        canvas.drawPath(at(listOf(74f to 88f, 70f to 70f)), lane)
        canvas.drawPath(at((0..40).map { k -> 20f + k * 1.8f to 125f + sin(k * 0.5f) * 2.5f }), sc.stroke(0.55f * sc.flicker, 0.7f))
        places.forEachIndexed { i, (px, py, name) ->
            val mx = px + panX
            val my = py + panY
            canvas.drawRect(r(mx - 1.1f, my - 1.1f, mx + 1.1f, my + 1.1f), sc.stroke(0.95f * sc.flicker, 0.7f, true))
            sc.text(name, mx + 2f, my - 2.4f, 2.8f, 0.8f * appear(i))
        }
        // L'onde qui part du joueur, et sa flèche.
        val ping = (seconds * 0.5f) % 1f
        circle(canvas, 50f, 98f, 2f + ping * 14f, sc.stroke((1f - ping) * 0.8f, 0.7f, true))
        canvas.save()
        canvas.translate(x(50f), y(98f))
        canvas.rotate(heading * 180f / PI.toFloat())
        val arrow = Path().apply {
            moveTo(0f, x(-2.4f)); lineTo(x(1.6f), x(1.8f)); lineTo(0f, x(0.9f)); lineTo(x(-1.6f), x(1.8f)); close()
        }
        sc.ink.halo(canvas, PointF(0f, 0f), x(4f), (90 * sc.s).toInt())
        sc.ink.fill.color = sc.palette.core
        sc.ink.fill.alpha = (255 * sc.flicker * sc.s).toInt()
        canvas.drawPath(arrow, sc.ink.fill)
        canvas.restore()
        canvas.restore()
        canvas.drawRect(r(16f, 68f, 84f, 128f), sc.stroke(0.7f, 0.7f))
        // La boussole.
        seg(canvas, 18f, 135f, 82f, 135f, sc.stroke(0.6f))
        val deg = heading * 180f / PI.toFloat()
        val base = (deg / 15f).roundToInt() * 15
        for (k in -12..12) {
            val d = base + k * 15
            val cx = 50f + (d - deg) * 0.4f
            if (cx < 18f || cx > 82f) continue
            val m = ((d % 360) + 360) % 360
            if (m % 45 == 0) sc.text(compass[m / 45], cx, 139.5f, 3.2f, 0.9f, Paint.Align.CENTER, bright = m == 0)
            else seg(canvas, cx, 135f, cx, 136.6f, sc.stroke(0.6f))
        }
        seg(canvas, 50f, 133.5f, 50f, 136.8f, sc.stroke(0.95f, 0.8f, true))
    }

    /** RADIO : les stations, celle qui joue, et l'oscilloscope ; plus agité avec du réseau. */
    private fun radioPage(sc: Screen, local: Float, seconds: Float, net: Boolean, appear: (Int) -> Float) {
        val playing = 1
        val sel = (local / 1.4f).toInt() % stations.size
        stations.forEachIndexed { i, name ->
            val a = appear(i)
            if (a <= 0f) return@forEachIndexed
            val sy = 72f + i * 7.6f
            if (i == sel) sc.band(17f, sy, 50f, a)
            sc.text(name, 21.5f, sy, 3.4f, 0.85f * a, bright = i == playing, max = 27f, dark = i == sel)
            if (i == playing) sc.fill(18.2f, sy - 1f, 20.2f, sy + 1f, sc.flicker, if (i == sel) DARK else sc.palette.core)
        }
        val o = floatArrayOf(53f, 70f, 84f, 112f)
        val a = appear(2)
        sc.canvas.drawRect(r(o[0], o[1], o[2], o[3]), sc.stroke(0.7f * a, 0.7f))
        val grid = sc.stroke(0.2f * a, 0.45f)
        var gx = o[0] + 3.1f
        while (gx < o[2]) {
            seg(sc.canvas, gx, o[1] + 0.4f, gx, o[3] - 0.4f, grid)
            gx += 3.1f
        }
        var gy = o[1] + 3.5f
        while (gy < o[3]) {
            seg(sc.canvas, o[0] + 0.4f, gy, o[2] - 0.4f, gy, grid)
            gy += 3.5f
        }
        val amp = if (net) 1f else 0.55f
        val wave = Path()
        var wx = o[0] + 0.5f
        while (wx <= o[2] - 0.5f) {
            val k = (wx - o[0]) / (o[2] - o[0])
            val wy = (o[1] + o[3]) / 2f + 11f * amp * sin(k * 18f + seconds * 7f) * sin(k * PI.toFloat()) * (0.55f + 0.45f * sin(seconds * 1.7f + k * 4f))
            if (wx == o[0] + 0.5f) wave.moveTo(x(wx), y(wy)) else wave.lineTo(x(wx), y(wy))
            wx += 0.3f
        }
        sc.canvas.drawPath(wave, sc.stroke(0.95f * sc.flicker * a, 1f, true))
        val ticks = sc.stroke(0.6f * a)
        for (k in 0..10) seg(sc.canvas, o[0] + k * 3.1f, 114f, o[0] + k * 3.1f, if (k % 5 != 0) 115.2f else 116f, ticks)
        sc.text("88.1", 53f, 119f, 3f, 0.7f * a)
        sc.text("108.0", 84f, 119f, 3f, 0.7f * a, Paint.Align.RIGHT)
        sc.text(if (net) "SIGNAL ▲" else "SIGNAL", 17.5f, 139.5f, 3.4f, 0.85f * appear(6), bright = net)
    }

    private fun polyline(points: List<Pair<Float, Float>>) = Path().apply {
        moveTo(x(points[0].first), y(points[0].second))
        for (i in 1 until points.size) lineTo(x(points[i].first), y(points[i].second))
    }

    private companion object {
        const val PAGE = 7f
        val DARK = Color.rgb(6, 16, 6)
    }
}

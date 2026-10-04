/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package mx.xperience.unicorn.fragments.lockscreen

import android.animation.ValueAnimator
import android.content.Context
import android.database.ContentObserver
import android.graphics.*
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.android.settingslib.Utils
import kotlin.math.*
import kotlin.random.Random

class ChargingAnimationPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val STYLE_AOSP_RIPPLE = 0
        const val STYLE_XPERIENCE = 1
        const val STYLE_WARP_PULSE = 2
        const val STYLE_XIAOMI_HYPEROS = 3
        const val STYLE_OPPO_SUPERVOOC = 4
        const val STYLE_BUBBLE_STREAM = 5
        const val STYLE_NEON = 6
        const val STYLE_BEAM = 7
        const val STYLE_PLASMA = 8
        const val STYLE_QUANTUM_SPARKS = 9
        const val STYLE_NEBULA = 10
        const val STYLE_DIGITAL_MATRIX = 11
        const val STYLE_GEOFLOW = 12

        const val COLOR_MODE_DEFAULT = 0
        const val COLOR_MODE_ACCENT = 1
        const val COLOR_MODE_RAINBOW = 2
    }

    private var isEnabled = true
    private var animationStyle = STYLE_XPERIENCE
    private var colorMode = COLOR_MODE_DEFAULT
    private var glowIntensity = 0.8f
    private var rippleOpacity = 0.6f

    // Shared paints
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glowRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val centerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val solidBlackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }
    private val textNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        color = Color.WHITE
    }
    private val textDecimalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        color = Color.WHITE
    }
    private val thinNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
        color = Color.WHITE
    }
    private val thinDecimalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        color = Color.WHITE
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        letterSpacing = 0.12f
    }
    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val matrixPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.MONOSPACE
    }
    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        color = 0xAAFFFFFF.toInt()
    }

    private val boltPath = Path()
    private val smallBoltPath = Path()
    private var animProgress = 0f
    private var rotationAngle = 0f
    private var rainbowHue = 0f
    private var loopAnimator: ValueAnimator? = null
    private var settingsObserver: ContentObserver? = null

    // Particles
    private data class PreviewParticle(
        var angle: Float = 0f, var radius: Float = 0f, var speed: Float = 0f,
        var size: Float = 0f, var alpha: Float = 0f, var x: Float = 0f,
        var y: Float = 0f, var vy: Float = 0f, var char: String = "1"
    )

    private data class StarParticle(
        var normX: Float = 0f, var normY: Float = 0f, var size: Float = 1f,
        var alpha: Float = 1f, var twinklePhase: Float = 0f, var twinkleSpeed: Float = 0.08f
    )

    private data class FiberParticle(
        var lane: Int = 0, var progress: Float = 0f, var speed: Float = 0.03f,
        var size: Float = 1.2f, var alpha: Float = 0.8f
    )

    private val particles = mutableListOf<PreviewParticle>()
    private val starParticles = mutableListOf<StarParticle>()
    private val fiberParticles = mutableListOf<FiberParticle>()
    private val matrixCharSet = listOf("0", "1", "X", "P", "E", "2", "1")

    init {
        setWillNotDraw(false)
        initBoltPaths()
        initParticles()
    }

    private fun initBoltPaths() {
        boltPath.reset()
        boltPath.moveTo(0f, -14f)
        boltPath.lineTo(-7f, 1f)
        boltPath.lineTo(-1f, 1f)
        boltPath.lineTo(-3f, 14f)
        boltPath.lineTo(7f, -1f)
        boltPath.lineTo(1f, -1f)
        boltPath.close()

        smallBoltPath.reset()
        smallBoltPath.moveTo(0f, -7f)
        smallBoltPath.lineTo(-3.5f, 0.5f)
        smallBoltPath.lineTo(-0.5f, 0.5f)
        smallBoltPath.lineTo(-1.5f, 7f)
        smallBoltPath.lineTo(3.5f, -0.5f)
        smallBoltPath.lineTo(0.5f, -0.5f)
        smallBoltPath.close()
    }

    private fun initParticles() {
        particles.clear()
        for (i in 0..24) {
            particles.add(
                PreviewParticle(
                    angle = Random.nextFloat() * 360f,
                    radius = 35f + Random.nextFloat() * 45f,
                    speed = 1.5f + Random.nextFloat() * 2f,
                    size = 1.5f + Random.nextFloat() * 2f,
                    alpha = 0.4f + Random.nextFloat() * 0.6f,
                    x = Random.nextFloat() * 200f,
                    y = Random.nextFloat() * 300f,
                    vy = -2f - Random.nextFloat() * 3f,
                    char = matrixCharSet.random()
                )
            )
        }

        starParticles.clear()
        for (i in 0..24) {
            val r = sqrt(Random.nextFloat()) * 0.85f
            val theta = Random.nextFloat() * 2f * PI.toFloat()
            starParticles.add(
                StarParticle(
                    normX = cos(theta) * r,
                    normY = sin(theta) * r,
                    size = 0.8f + Random.nextFloat() * 1.4f,
                    alpha = 0.35f + Random.nextFloat() * 0.65f,
                    twinklePhase = Random.nextFloat() * 2f * PI.toFloat(),
                    twinkleSpeed = 0.05f + Random.nextFloat() * 0.1f
                )
            )
        }

        fiberParticles.clear()
        for (i in 0..16) {
            fiberParticles.add(
                FiberParticle(
                    lane = Random.nextInt(-6, 7),
                    progress = Random.nextFloat(),
                    speed = 0.02f + Random.nextFloat() * 0.03f,
                    size = 1f + Random.nextFloat() * 1.5f,
                    alpha = 0.5f + Random.nextFloat() * 0.5f
                )
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerObserver()
        updateSettings()
        startAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterObserver()
        stopAnimator()
    }

    private fun registerObserver() {
        val resolver = context.contentResolver
        settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                updateSettings()
            }
        }
        val uri = Settings.System.getUriFor("charging_animation_enabled")
        resolver.registerContentObserver(uri, false, settingsObserver!!)
        resolver.registerContentObserver(Settings.System.getUriFor("charging_animation_style"), false, settingsObserver!!)
        resolver.registerContentObserver(Settings.System.getUriFor("charging_color_mode"), false, settingsObserver!!)
        resolver.registerContentObserver(Settings.System.getUriFor("charging_glow_intensity"), false, settingsObserver!!)
        resolver.registerContentObserver(Settings.System.getUriFor("charging_ripple_opacity"), false, settingsObserver!!)
    }

    private fun unregisterObserver() {
        settingsObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
            settingsObserver = null
        }
    }

    private fun updateSettings() {
        val resolver = context.contentResolver
        isEnabled = Settings.System.getIntForUser(resolver, "charging_animation_enabled", 1, UserHandle.USER_CURRENT) == 1
        animationStyle = Settings.System.getIntForUser(resolver, "charging_animation_style", 1, UserHandle.USER_CURRENT)
        colorMode = Settings.System.getIntForUser(resolver, "charging_color_mode", 0, UserHandle.USER_CURRENT)
        val glow = Settings.System.getIntForUser(resolver, "charging_glow_intensity", 80, UserHandle.USER_CURRENT)
        val opacity = Settings.System.getIntForUser(resolver, "charging_ripple_opacity", 60, UserHandle.USER_CURRENT)
        glowIntensity = (glow / 100f).coerceIn(0.1f, 1f)
        rippleOpacity = (opacity / 100f).coerceIn(0.1f, 1f)
        invalidate()
    }

    private fun startAnimator() {
        stopAnimator()
        loopAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1600L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                animProgress = it.animatedValue as Float
                rotationAngle = (rotationAngle + 2.5f) % 360f
                rainbowHue = (rainbowHue + 1f) % 360f

                // update particles
                particles.forEach { p ->
                    p.angle = (p.angle + p.speed) % 360f
                    p.radius -= 0.7f
                    if (p.radius < 20f) {
                        p.radius = 35f + Random.nextFloat() * 40f
                    }
                    p.y += p.vy
                    if (p.y < 20f) {
                        p.y = height.toFloat()
                        p.char = matrixCharSet.random()
                    }
                }

                starParticles.forEach { sp ->
                    sp.twinklePhase = (sp.twinklePhase + sp.twinkleSpeed) % (2f * PI.toFloat())
                }

                fiberParticles.forEach { fp ->
                    fp.progress += fp.speed
                    if (fp.progress >= 1f) {
                        fp.progress = 0f
                        fp.lane = Random.nextInt(-6, 7)
                    }
                }

                invalidate()
            }
            start()
        }
    }

    private fun stopAnimator() {
        loopAnimator?.cancel()
        loopAnimator = null
    }

    private fun getEffectColor(): Int {
        return when (colorMode) {
            COLOR_MODE_ACCENT -> Utils.getColorAccentDefaultColor(context)
            COLOR_MODE_RAINBOW -> Color.HSVToColor(floatArrayOf(rainbowHue, 0.85f, 0.95f))
            else -> when (animationStyle) {
                STYLE_XPERIENCE -> 0xFF00E5FF.toInt()
                STYLE_WARP_PULSE -> 0xFF00E676.toInt()
                STYLE_XIAOMI_HYPEROS -> 0xFF2979FF.toInt()
                STYLE_OPPO_SUPERVOOC -> 0xFF00E5FF.toInt()
                STYLE_DIGITAL_MATRIX -> 0xFF00FF66.toInt()
                else -> 0xFF00E5FF.toInt()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        // Lowered position to sit in the lower-middle half, below clock and widgets
        val cy = h * 0.55f

        // Draw ambient lockscreen mockup clock
        timePaint.textSize = 28f
        canvas.drawText("10:08", cx, h * 0.22f, timePaint)

        if (!isEnabled) {
            timePaint.textSize = 12f
            canvas.drawText("Animation Disabled", cx, cy, timePaint)
            return
        }

        val color = getEffectColor()

        when (animationStyle) {
            STYLE_AOSP_RIPPLE -> drawAospRipple(canvas, cx, cy, color)
            STYLE_XPERIENCE -> drawXPerience(canvas, cx, cy, color)
            STYLE_WARP_PULSE -> drawWarpPulse(canvas, cx, cy, color)
            STYLE_XIAOMI_HYPEROS -> drawXiaomiHyperOS(canvas, cx, cy, h, color)
            STYLE_OPPO_SUPERVOOC -> drawOppoSuperVOOC(canvas, cx, cy, color)
            STYLE_BUBBLE_STREAM -> drawBubbleStream(canvas, cx, cy, color)
            STYLE_NEON -> drawNeon(canvas, cx, h, color)
            STYLE_BEAM -> drawBeam(canvas, cx, h, color)
            STYLE_PLASMA -> drawPlasma(canvas, cx, cy, color)
            STYLE_QUANTUM_SPARKS -> drawQuantumSparks(canvas, cx, cy, color)
            STYLE_NEBULA -> drawNebula(canvas, cx, cy, color)
            STYLE_DIGITAL_MATRIX -> drawDigitalMatrix(canvas, cx, h, color)
            STYLE_GEOFLOW -> drawGeoFlow(canvas, cx, h, color)
        }
    }

    /**
     * Xiaomi HyperOS (Turbo Charge):
     * Glowing fiber optic tail from USB port up into ring, 360 radial blooming flare rays,
     * concentric side shockwave arcs, pure black core, clean thin font with live decimals,
     * and gold 67W MAX badge with lightning bolt.
     */
    private fun drawXiaomiHyperOS(canvas: Canvas, cx: Float, cy: Float, h: Float, color: Int) {
        val baseR = 34f
        val usbY = h
        val topY = cy + baseR - 1f
        val trunkHeight = usbY - topY

        // 1. Fiber tail from bottom
        val trunkShader = LinearGradient(
            cx, usbY, cx, topY,
            intArrayOf(0x002979FF, (0xCC2979FF).toInt(), 0xFF00E5FF.toInt()),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        glowRingPaint.shader = trunkShader
        glowRingPaint.strokeWidth = 2f
        for (i in -5..5) {
            val fx = cx + (i * 1.5f)
            val lineAlpha = ((1f - abs(i) / 6f) * 180).toInt().coerceIn(0, 255)
            glowRingPaint.alpha = lineAlpha
            canvas.drawLine(fx, usbY, fx, topY, glowRingPaint)
        }
        glowRingPaint.shader = null

        // Fiber particles
        sparkPaint.color = Color.WHITE
        fiberParticles.forEach { fp ->
            val fx = cx + (fp.lane * 1.4f)
            val fy = usbY - (fp.progress * trunkHeight)
            sparkPaint.alpha = (fp.alpha * 240).toInt().coerceIn(0, 255)
            canvas.drawCircle(fx, fy, fp.size, sparkPaint)
        }

        // 2. 360 Radial Blooming Flare Rays
        val numRays = 24
        for (i in 0 until numRays) {
            val angle = Math.toRadians((i * (360f / numRays) + rotationAngle * 0.4f).toDouble())
            val innerR = baseR * 0.95f
            val rayLength = (baseR * (1.18f + 0.22f * sin(animProgress * 2f * PI.toFloat() + i))).toFloat()
            val startX = cx + (cos(angle) * innerR).toFloat()
            val startY = cy + (sin(angle) * innerR).toFloat()
            val endX = cx + (cos(angle) * rayLength).toFloat()
            val endY = cy + (sin(angle) * rayLength).toFloat()

            sparkPaint.color = if (i % 2 == 0) 0xFF7C4DFF.toInt() else 0xFF2979FF.toInt()
            sparkPaint.strokeWidth = 1.6f
            sparkPaint.alpha = (90 * glowIntensity).toInt().coerceIn(0, 255)
            canvas.drawLine(startX, startY, endX, endY, sparkPaint)
        }

        // 3. Side Curved Shockwave Energy Arcs (Left & Right)
        ringPaint.strokeWidth = 1.6f
        ringPaint.color = 0xFF2979FF.toInt()
        val pulseDist = 8f + (animProgress * 12f)
        val sideRect = RectF(cx - baseR - pulseDist, cy - baseR - pulseDist,
                             cx + baseR + pulseDist, cy + baseR + pulseDist)
        ringPaint.alpha = ((1f - animProgress) * 160).toInt().coerceIn(0, 255)
        canvas.drawArc(sideRect, 130f, 100f, false, ringPaint)
        canvas.drawArc(sideRect, -50f, 100f, false, ringPaint)

        // 4. Central Solid Black Core
        canvas.drawCircle(cx, cy, baseR * 0.92f, solidBlackPaint)

        // 5. Main Intense Glowing Circular Rim
        ringPaint.strokeWidth = 2.5f
        ringPaint.color = 0xFF2979FF.toInt()
        ringPaint.alpha = 240
        canvas.drawCircle(cx, cy, baseR, ringPaint)

        glowRingPaint.strokeWidth = 1.4f
        glowRingPaint.color = Color.WHITE
        glowRingPaint.alpha = 200
        val rimRect = RectF(cx - baseR, cy - baseR, cx + baseR, cy + baseR)
        canvas.save()
        canvas.rotate(rotationAngle * 1.6f, cx, cy)
        canvas.drawArc(rimRect, 0f, 120f, false, glowRingPaint)
        canvas.drawArc(rimRect, 180f, 120f, false, glowRingPaint)
        canvas.restore()

        // 6. Battery % with clean typography & live decimals
        thinNumberPaint.textSize = 20f
        thinDecimalPaint.textSize = 9f
        val intStr = "68"
        val fastDecimal = ((SystemClock.uptimeMillis() / 25) % 100).toInt()
        val decStr = String.format(".%02d%%", fastDecimal)

        val intW = thinNumberPaint.measureText(intStr)
        val decW = thinDecimalPaint.measureText(decStr)
        val totalW = intW + decW
        val textStartX = cx - (totalW / 2f) + intW
        val textY = cy + 4f

        canvas.drawText(intStr, textStartX, textY, thinNumberPaint)
        canvas.drawText(decStr, textStartX + 1.5f, textY - 6f, thinDecimalPaint)

        // 7. Gold / Yellow "67W MAX" + Lightning Bolt
        val goldColor = 0xFFFFD600.toInt()
        badgePaint.textSize = 6.5f
        badgePaint.color = goldColor
        badgePaint.alpha = 240
        canvas.drawText("67W MAX", cx, cy + 15f, badgePaint)

        canvas.save()
        canvas.translate(cx, cy + 22f)
        canvas.scale(0.5f, 0.5f)
        boltPaint.color = goldColor
        boltPaint.alpha = 240
        canvas.drawPath(smallBoltPath, boltPaint)
        canvas.restore()
    }

    /**
     * OnePlus / Oppo SUPERVOOC:
     * Overlapping iridescent glass bubble orbs, deep cosmic night lens with glittering dust,
     * chromatic dispersion prismatic rim, clean bold white font with decimals, and SUPERVOOC™ 100W badge.
     */
    private fun drawOppoSuperVOOC(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val baseR = 35f

        // 1. Overlapping Soft Iridescent Glass Bubble Orbs behind lens
        val orbShader1 = RadialGradient(
            cx - 20f, cy - 16f, baseR * 1.2f,
            intArrayOf(0x357C4DFF.toInt(), 0x15FF6D00.toInt(), Color.TRANSPARENT),
            floatArrayOf(0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        centerGlowPaint.shader = orbShader1
        canvas.drawCircle(cx - 20f, cy - 16f, baseR * 1.15f, centerGlowPaint)

        val orbShader2 = RadialGradient(
            cx + 24f, cy + 8f, baseR * 1.1f,
            intArrayOf(0x3000E5FF.toInt(), 0x10FF4081.toInt(), Color.TRANSPARENT),
            floatArrayOf(0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        centerGlowPaint.shader = orbShader2
        canvas.drawCircle(cx + 24f, cy + 8f, baseR * 1.05f, centerGlowPaint)

        // 2. Cosmic Dark Lens Interior (#0B0B1A)
        val lensShader = RadialGradient(
            cx, cy, baseR,
            intArrayOf(0xFF14142B.toInt(), 0xFF070710.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        centerGlowPaint.shader = lensShader
        canvas.drawCircle(cx, cy, baseR, centerGlowPaint)

        // 3. Glittering Star Dust / Cosmic Particles inside lens
        sparkPaint.color = Color.WHITE
        starParticles.forEach { s ->
            val px = cx + (s.normX * baseR)
            val py = cy + (s.normY * baseR)
            val twinkle = (0.5f + 0.5f * sin(s.twinklePhase))
            sparkPaint.alpha = (s.alpha * twinkle * 240).toInt().coerceIn(0, 255)
            canvas.drawCircle(px, py, s.size, sparkPaint)
        }

        // 4. Prismatic / Chromatic Dispersion Border Rim
        val rainbowColors = intArrayOf(
            0xFF00E5FF.toInt(), 0xFF2979FF.toInt(), 0xFF7C4DFF.toInt(),
            0xFFFF4081.toInt(), 0xFFFF6D00.toInt(), 0xFFFFD600.toInt(),
            0xFF00E5FF.toInt()
        )
        ringPaint.shader = SweepGradient(cx, cy, rainbowColors, null)
        ringPaint.strokeWidth = 2.4f
        ringPaint.alpha = 245
        canvas.drawCircle(cx, cy, baseR, ringPaint)
        ringPaint.shader = null

        // Specular Reflection Arc on Top-Left
        glowRingPaint.strokeWidth = 1.5f
        glowRingPaint.color = Color.WHITE
        glowRingPaint.alpha = 210
        val rimRect = RectF(cx - baseR, cy - baseR, cx + baseR, cy + baseR)
        canvas.drawArc(rimRect, 200f, 85f, false, glowRingPaint)

        // 5. Battery % (Bold White with decimals: 86.20%)
        textNumberPaint.textSize = 18f
        textDecimalPaint.textSize = 10f
        val intStr = "86"
        val fastDecimal = ((SystemClock.uptimeMillis() / 30) % 100).toInt()
        val decStr = String.format(".%02d%%", fastDecimal)

        val intW = textNumberPaint.measureText(intStr)
        val decW = textDecimalPaint.measureText(decStr)
        val totalW = intW + decW
        val textStartX = cx - (totalW / 2f) + intW
        val textY = cy + 4f

        canvas.drawText(intStr, textStartX, textY, textNumberPaint)
        canvas.drawText(decStr, textStartX + 1.5f, textY - 4f, textDecimalPaint)

        // 6. Pill Badge with ⚡ SUPERVOOC™ 100W
        val badgeY = cy + 18f
        val pillWidth = 56f
        val pillHeight = 12f
        val pillRect = RectF(cx - pillWidth / 2f, badgeY - pillHeight / 2f,
                             cx + pillWidth / 2f, badgeY + pillHeight / 2f)

        pillPaint.color = 0x33FFFFFF.toInt()
        pillPaint.alpha = 180
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillPaint)

        badgePaint.textSize = 5.5f
        badgePaint.color = Color.WHITE
        badgePaint.alpha = 240
        canvas.drawText("⚡ SUPERVOOC™ 100W", cx, badgeY + 2f, badgePaint)
    }

    /**
     * XPerience:
     * Swirling vortex particles, glowing cyan aura, dual concentric rotating arc rings,
     * bold percentage with live decimals, and XPERIENCE badge.
     */
    private fun drawXPerience(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val baseR = 35f

        // 1. Swirling Vortex Particles
        sparkPaint.color = color
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val px = cx + (cos(rad) * p.radius * 0.75f).toFloat()
            val py = cy + (sin(rad) * p.radius * 0.75f).toFloat()
            sparkPaint.alpha = (p.alpha * 230).toInt().coerceIn(0, 255)
            canvas.drawCircle(px, py, p.size, sparkPaint)
        }

        // 2. Central Glow
        val glowR = baseR * 1.3f
        centerGlowPaint.shader = RadialGradient(
            cx, cy, glowR,
            intArrayOf(color and 0x00FFFFFF or ((90 * glowIntensity).toInt() shl 24), Color.TRANSPARENT),
            floatArrayOf(0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, glowR, centerGlowPaint)

        // 3. Rotating Outer Ring (Clockwise)
        ringPaint.strokeWidth = 2.2f
        ringPaint.color = color
        ringPaint.alpha = 230
        val outerRect = RectF(cx - baseR, cy - baseR, cx + baseR, cy + baseR)
        canvas.save()
        canvas.rotate(rotationAngle, cx, cy)
        canvas.drawArc(outerRect, 0f, 110f, false, ringPaint)
        canvas.drawArc(outerRect, 180f, 110f, false, ringPaint)
        canvas.restore()

        // 4. Rotating Inner Ring (Counter-Clockwise)
        val innerR = baseR * 0.86f
        val innerRect = RectF(cx - innerR, cy - innerR, cx + innerR, cy + innerR)
        glowRingPaint.strokeWidth = 1.4f
        glowRingPaint.color = Color.WHITE
        glowRingPaint.alpha = 180
        canvas.save()
        canvas.rotate(-rotationAngle * 1.4f, cx, cy)
        canvas.drawArc(innerRect, 45f, 60f, false, glowRingPaint)
        canvas.drawArc(innerRect, 165f, 60f, false, glowRingPaint)
        canvas.drawArc(innerRect, 285f, 60f, false, glowRingPaint)
        canvas.restore()

        // 5. Battery % with decimal counter
        textNumberPaint.textSize = 20f
        textDecimalPaint.textSize = 9f
        val intStr = "68"
        val fastDec = ((SystemClock.uptimeMillis() / 25) % 100).toInt()
        val decStr = String.format(".%02d%%", fastDec)

        val intW = textNumberPaint.measureText(intStr)
        val decW = textDecimalPaint.measureText(decStr)
        val startX = cx - (intW + decW) / 2f + intW
        canvas.drawText(intStr, startX, cy + 6f, textNumberPaint)
        canvas.drawText(decStr, startX + 1.5f, cy - 3f, textDecimalPaint)

        // 6. Badge
        badgePaint.textSize = 7f
        badgePaint.color = color
        badgePaint.alpha = 240
        canvas.drawText("XPERIENCE", cx, cy + baseR + 13f, badgePaint)
    }

    /**
     * Flash Energy / Warp Pulse:
     * Pulsing concentric energy waves, bright electric lightning bolt, sparks, and WARP CHARGE badge.
     */
    private fun drawWarpPulse(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        // Pulsing rings
        ringPaint.strokeWidth = 1.8f
        ringPaint.color = color
        for (i in 0..2) {
            val p = (animProgress + i * 0.33f) % 1f
            val r = 18f + p * 38f
            ringPaint.alpha = ((1f - p) * 220).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, r, ringPaint)
        }

        // Center orb
        centerGlowPaint.shader = null
        centerGlowPaint.color = color and 0x00FFFFFF or (0x99 shl 24)
        canvas.drawCircle(cx, cy, 18f, centerGlowPaint)

        // Lightning bolt
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(0.6f, 0.6f)
        boltPaint.color = Color.WHITE
        canvas.drawPath(boltPath, boltPaint)
        canvas.restore()

        // Battery % & badge
        textNumberPaint.textAlign = Paint.Align.CENTER
        textNumberPaint.textSize = 14f
        canvas.drawText("68%", cx, cy + 30f, textNumberPaint)
        textNumberPaint.textAlign = Paint.Align.RIGHT

        badgePaint.textSize = 7f
        badgePaint.color = color
        badgePaint.alpha = 240
        canvas.drawText("WARP CHARGE", cx, cy + 42f, badgePaint)
    }

    private fun drawAospRipple(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        ringPaint.strokeWidth = 2f
        ringPaint.color = color
        for (i in 0..2) {
            val p = (animProgress + i * 0.33f) % 1f
            val r = 12f + p * 45f
            ringPaint.alpha = ((1f - p) * 200).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, r, ringPaint)
        }
        textNumberPaint.textAlign = Paint.Align.CENTER
        textNumberPaint.textSize = 18f
        canvas.drawText("68%", cx, cy + 6f, textNumberPaint)
        textNumberPaint.textAlign = Paint.Align.RIGHT
    }

    private fun drawBubbleStream(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        sparkPaint.color = color
        particles.take(15).forEach { p ->
            sparkPaint.alpha = (p.alpha * 200 * rippleOpacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx + (p.x % 40f) - 20f, p.y % (height * 0.45f) + height * 0.45f, p.size * 1.5f, sparkPaint)
        }
        drawPreviewBadge(canvas, cx, cy, color, "CHARGING")
    }

    private fun drawNeon(canvas: Canvas, cx: Float, h: Float, color: Int) {
        val glowH = h * 0.32f
        centerGlowPaint.shader = LinearGradient(
            cx, h, cx, h - glowH,
            intArrayOf(color and 0x00FFFFFF or ((180 * glowIntensity).toInt() shl 24), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(cx - 24f, h - glowH, cx + 24f, h, centerGlowPaint)
        drawPreviewBadge(canvas, cx, h * 0.55f, color, "NEON CHARGE")
    }

    private fun drawBeam(canvas: Canvas, cx: Float, h: Float, color: Int) {
        sparkPaint.color = color
        for (i in 0..3) {
            val p = (animProgress + i * 0.25f) % 1f
            val y = h - (p * h * 0.4f)
            sparkPaint.alpha = ((1f - p) * 240).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx + (i - 1.5f) * 10f, y, 3f + i, sparkPaint)
        }
        drawPreviewBadge(canvas, cx, h * 0.55f, color, "ENERGY BEAM")
    }

    private fun drawPlasma(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        sparkPaint.color = color
        for (i in 0..2) {
            val angle = Math.toRadians((animProgress * 360f + i * 120f).toDouble())
            val px = cx + (cos(angle) * 18f).toFloat()
            val py = cy + (sin(angle) * 18f).toFloat()
            sparkPaint.alpha = 160
            canvas.drawCircle(px, py, 14f, sparkPaint)
        }
        drawPreviewBadge(canvas, cx, cy, color, "PLASMA")
    }

    private fun drawQuantumSparks(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        sparkPaint.color = color
        for (i in 0..10) {
            val angle = Math.toRadians((animProgress * 720f + i * 36f).toDouble())
            val dist = 10f + (i * 3f % 28f)
            val px = cx + (cos(angle) * dist).toFloat()
            val py = cy + (sin(angle) * dist).toFloat()
            sparkPaint.alpha = 220
            canvas.drawCircle(px, py, 1.8f, sparkPaint)
        }
        drawPreviewBadge(canvas, cx, cy, color, "QUANTUM")
    }

    private fun drawNebula(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        centerGlowPaint.shader = RadialGradient(
            cx, cy, 32f,
            intArrayOf(color and 0x00FFFFFF or (0x88 shl 24), Color.TRANSPARENT),
            floatArrayOf(0.3f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, 32f, centerGlowPaint)
        drawPreviewBadge(canvas, cx, cy, color, "COSMIC")
    }

    private fun drawDigitalMatrix(canvas: Canvas, cx: Float, h: Float, color: Int) {
        matrixPaint.color = color
        matrixPaint.textSize = 10f
        particles.take(12).forEach { p ->
            matrixPaint.alpha = (p.alpha * 240).toInt().coerceIn(0, 255)
            canvas.drawText(p.char, p.x % (width * 0.8f) + width * 0.1f, p.y, matrixPaint)
        }
        drawPreviewBadge(canvas, cx, h * 0.55f, color, "CYBER MATRIX")
    }

    private fun drawGeoFlow(canvas: Canvas, cx: Float, h: Float, color: Int) {
        ringPaint.color = color
        ringPaint.strokeWidth = 1.4f
        for (i in 0..2) {
            val p = (animProgress + i * 0.33f) % 1f
            val y = h - (p * h * 0.4f)
            val s = 10f + i * 4f
            canvas.save()
            canvas.translate(cx, y)
            canvas.rotate(p * 360f)
            canvas.drawRect(-s / 2f, -s / 2f, s / 2f, s / 2f, ringPaint)
            canvas.restore()
        }
        drawPreviewBadge(canvas, cx, h * 0.55f, color, "GEOFLOW")
    }

    private fun drawPreviewBadge(canvas: Canvas, cx: Float, cy: Float, color: Int, badge: String) {
        textNumberPaint.textAlign = Paint.Align.CENTER
        textNumberPaint.textSize = 16f
        canvas.drawText("68%", cx, cy, textNumberPaint)
        textNumberPaint.textAlign = Paint.Align.RIGHT

        badgePaint.textSize = 7f
        badgePaint.color = color
        badgePaint.alpha = 230
        canvas.drawText(badge, cx, cy + 14f, badgePaint)
    }
}

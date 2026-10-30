package com.example.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Environment
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

enum class DesignStyle(val displayName: String, val description: String) {
    GOLD_3D("3D Metallic Gold", "Luxurious metallic gold with extruded 3D bevel and reflection"),
    ELECTRIC_NEON("Electric Blue Neon", "Intense luminous cyan-blue glow with high-voltage core"),
    CYBER_HOLO("Cyber Holographic", "Vibrant cyan-magenta iridescent gradient with cyber edge"),
    CHROME_METAL("Platinum Chrome", "Sleek reflective platinum silver with high-contrast specular"),
    GLOWING_BUTTON("Glowing Button Badge", "Pill-shaped glass badge with glowing border and neon accent"),
    GAMING_STICKER("Gaming Sticker", "Thick dual-stroke sticker with pop shadow and alpha contour")
}

data class DesignOptions(
    val text: String = "CREATIVE",
    val subtext: String = "STUDIO",
    val style: DesignStyle = DesignStyle.GOLD_3D,
    val glowIntensity: Float = 1.0f, // 0.2f to 2.0f
    val depth3D: Int = 8, // 1 to 20
    val outlineWidth: Float = 4f, // 0 to 12
    val primaryColorHex: String = "#FFD700",
    val secondaryColorHex: String = "#00D2FF"
)

object GraphicDesignEngine {

    /**
     * Renders a genuinely transparent PNG with genuine Alpha channel (0x00000000 background).
     */
    fun renderTransparentDesign(options: DesignOptions, canvasSize: Int = 800): Bitmap {
        // Bitmap.Config.ARGB_8888 provides 8 bits per channel including full alpha
        val bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clear canvas with completely transparent pixels
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

        val cx = canvasSize / 2f
        val cy = canvasSize / 2f

        when (options.style) {
            DesignStyle.GOLD_3D -> render3DGoldText(canvas, cx, cy, options)
            DesignStyle.ELECTRIC_NEON -> renderNeonText(canvas, cx, cy, options)
            DesignStyle.CYBER_HOLO -> renderHolographicText(canvas, cx, cy, options)
            DesignStyle.CHROME_METAL -> renderChromeText(canvas, cx, cy, options)
            DesignStyle.GLOWING_BUTTON -> renderGlowingButton(canvas, cx, cy, options)
            DesignStyle.GAMING_STICKER -> renderGamingSticker(canvas, cx, cy, options)
        }

        return bitmap
    }

    private fun render3DGoldText(canvas: Canvas, cx: Float, cy: Float, options: DesignOptions) {
        val fontSize = 96f
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val text = options.text.uppercase()
        val subtext = options.subtext.uppercase()

        // 1. Draw 3D Extrusion Shadow layers (behind)
        val depth = options.depth3D.coerceIn(2, 16)
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#4A3700") // Deep dark gold shadow
        }

        for (i in depth downTo 1) {
            val offset = i.toFloat() * 1.8f
            canvas.drawText(text, cx + offset, cy + offset, shadowPaint)
        }

        // 2. Soft Outer Ambient Glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#FFA500")
            maskFilter = android.graphics.BlurMaskFilter(24f * options.glowIntensity, android.graphics.BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawText(text, cx, cy, glowPaint)

        // 3. Metallic Gold Gradient Fill
        val goldShader = LinearGradient(
            cx - 150f, cy - 80f, cx + 150f, cy + 30f,
            intArrayOf(
                Color.parseColor("#FFF5A6"), // Platinum highlight
                Color.parseColor("#FFD700"), // Pure gold
                Color.parseColor("#E6A100"), // Rich dark gold
                Color.parseColor("#FFF275"), // Secondary shimmer
                Color.parseColor("#B37D00")  // Deep amber gold
            ),
            floatArrayOf(0.0f, 0.25f, 0.55f, 0.8f, 1.0f),
            Shader.TileMode.CLAMP
        )
        textPaint.shader = goldShader
        canvas.drawText(text, cx, cy, textPaint)

        // 4. Crisp Metallic Stroke
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = options.outlineWidth.coerceAtLeast(1.5f)
            color = Color.parseColor("#FFFBE0")
        }
        canvas.drawText(text, cx, cy, strokePaint)

        // Subtext if present
        if (subtext.isNotBlank()) {
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                color = Color.parseColor("#F5C542")
                letterSpacing = 0.25f
            }
            canvas.drawText(subtext, cx, cy + 60f, subPaint)
        }
    }

    private fun renderNeonText(canvas: Canvas, cx: Float, cy: Float, options: DesignOptions) {
        val fontSize = 92f
        val text = options.text.uppercase()
        val subtext = options.subtext.uppercase()

        // 1. Multi-Layer Neon Glow
        val neonBlue = Color.parseColor("#00D2FF")
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 14f * options.glowIntensity
            color = neonBlue
            maskFilter = android.graphics.BlurMaskFilter(28f * options.glowIntensity, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawText(text, cx, cy, glowPaint)

        val innerGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#0066FF")
            maskFilter = android.graphics.BlurMaskFilter(12f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawText(text, cx, cy, innerGlow)

        // 2. Bright White Core
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#E0F7FF")
        }
        canvas.drawText(text, cx, cy, corePaint)

        // Subtext
        if (subtext.isNotBlank()) {
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                color = Color.parseColor("#00D2FF")
                letterSpacing = 0.3f
            }
            canvas.drawText(subtext, cx, cy + 58f, subPaint)
        }
    }

    private fun renderHolographicText(canvas: Canvas, cx: Float, cy: Float, options: DesignOptions) {
        val fontSize = 94f
        val text = options.text.uppercase()

        val holoShader = LinearGradient(
            cx - 200f, cy - 60f, cx + 200f, cy + 60f,
            intArrayOf(
                Color.parseColor("#00FFFF"), // Cyan
                Color.parseColor("#7F00FF"), // Electric Violet
                Color.parseColor("#FF007F"), // Hot Pink
                Color.parseColor("#FFD700")  // Gold flash
            ),
            floatArrayOf(0.0f, 0.35f, 0.7f, 1.0f),
            Shader.TileMode.MIRROR
        )

        // Glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#7F00FF")
            maskFilter = android.graphics.BlurMaskFilter(20f * options.glowIntensity, android.graphics.BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawText(text, cx, cy, glowPaint)

        // Main fill
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            shader = holoShader
        }
        canvas.drawText(text, cx, cy, textPaint)

        // Outline
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = options.outlineWidth.coerceAtLeast(2f)
            color = Color.WHITE
        }
        canvas.drawText(text, cx, cy, strokePaint)
    }

    private fun renderChromeText(canvas: Canvas, cx: Float, cy: Float, options: DesignOptions) {
        val fontSize = 94f
        val text = options.text.uppercase()

        val chromeShader = LinearGradient(
            cx, cy - 80f, cx, cy + 20f,
            intArrayOf(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#B0B8C4"),
                Color.parseColor("#1C2536"), // horizon dark line
                Color.parseColor("#8091A5"),
                Color.parseColor("#FFFFFF")
            ),
            floatArrayOf(0.0f, 0.45f, 0.5f, 0.75f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            shader = chromeShader
        }
        canvas.drawText(text, cx, cy, textPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.WHITE
        }
        canvas.drawText(text, cx, cy, strokePaint)
    }

    private fun renderGlowingButton(canvas: Canvas, cx: Float, cy: Float, options: DesignOptions) {
        val rectW = 480f
        val rectH = 140f
        val rectF = RectF(cx - rectW / 2f, cy - rectH / 2f, cx + rectW / 2f, cy + rectH / 2f)
        val cornerRadius = 70f

        // Outer glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00D2FF")
            style = Paint.Style.STROKE
            strokeWidth = 14f * options.glowIntensity
            maskFilter = android.graphics.BlurMaskFilter(22f * options.glowIntensity, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, glowPaint)

        // Translucent Glass Body
        val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 10, 25, 45) // Semitransparent dark blue
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, glassPaint)

        // Gloss highlight curve on top half
        val highlightRect = RectF(rectF.left + 8f, rectF.top + 6f, rectF.right - 8f, cy - 4f)
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx, highlightRect.top, cx, highlightRect.bottom,
                Color.argb(120, 255, 255, 255), Color.argb(10, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(highlightRect, cornerRadius - 4f, cornerRadius - 4f, highlightPaint)

        // Golden/Blue border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            shader = LinearGradient(
                rectF.left, rectF.top, rectF.right, rectF.bottom,
                intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#00D2FF")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            letterSpacing = 0.1f
        }
        canvas.drawText(options.text.uppercase(), cx, cy + 16f, textPaint)
    }

    private fun renderGamingSticker(canvas: Canvas, cx: Float, cy: Float, options: DesignOptions) {
        val fontSize = 90f
        val text = options.text.uppercase()

        // 1. Thick dark pop shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 24f
            color = Color.parseColor("#070B19")
        }
        canvas.drawText(text, cx + 12f, cy + 14f, shadowPaint)

        // 2. Thick White Sticker Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 18f
            color = Color.WHITE
        }
        canvas.drawText(text, cx, cy, borderPaint)

        // 3. Vibrant Gradient Fill
        val fillShader = LinearGradient(
            cx, cy - 60f, cx, cy + 20f,
            intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FF6B00")),
            null, Shader.TileMode.CLAMP
        )
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            shader = fillShader
        }
        canvas.drawText(text, cx, cy, fillPaint)
    }

    /**
     * Converts a Bitmap with alpha channel to a Base64 PNG data URI string.
     */
    fun toBase64Png(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Decodes a Base64 PNG data URI or raw base64 back into a Bitmap.
     */
    fun fromBase64(base64Str: String): Bitmap? {
        return try {
            val clean = base64Str.removePrefix("data:image/png;base64,").removePrefix("data:image/jpeg;base64,")
            val decodedBytes = Base64.decode(clean, Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves the genuine transparent PNG to local cache for export / sharing.
     */
    fun saveToCache(context: Context, bitmap: Bitmap, fileName: String = "ai_creative_${System.currentTimeMillis()}.png"): File {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}

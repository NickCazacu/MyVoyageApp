package com.nichita.myvoyage.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Палитра «тёмная марсала / перивинкл» + стиль «жидкое стекло».
 *
 * Тёмные вина (марсала/бордо/бургунди) — фон и акценты; холодные перивинкл/
 * лаванда — текст и матовые поверхности; пыльный мауве/слива — вторичные тона.
 * Карточки и панели полупрозрачны (стекло) поверх градиентного фона.
 */

// --- Базовые цвета палитры ---
val Marsala = Color(0xFF480C25)      // 1 — тёмная марсала / бордо
val WineDeep = Color(0xFF520519)     // 2 — глубокий винный
val Burgundy = Color(0xFF5F152E)     // 3 — бургунди
val Periwinkle = Color(0xFFD3DAED)   // 4 — очень светлый перивинкл
val FogBlue = Color(0xFFC0C6DC)      // 5 — туманно-голубой
val GreyLavender = Color(0xFFADB2CB) // 6 — серо-лавандовый
val DustyLilac = Color(0xFFA18EA4)   // 7 — пыльно-сиреневый
val Mauve = Color(0xFF886C85)        // 8 — мауве
val MutedPlum = Color(0xFF765E75)    // 9 — приглушённый сливовый
val WineViolet = Color(0xFF5C2B45)   // 10 — тёмный винно-фиолетовый

// --- Градиент фона приложения (глубоко затемнён ради контраста с карточками) ---
val BgTop = Color(0xFF1B040D)
val BgMid = Color(0xFF22101D)
val BgBottom = Color(0xFF1E0207)
val BgGradient = listOf(BgTop, BgMid, BgBottom)

// --- «Стеклянные» (полупрозрачные) поверхности ---
// Альфа повышена, чтобы карточки/плитки контрастно читались на затемнённом фоне.
val GlassLowest = Color(0x2CD3DAED)
val GlassLow = Color(0x34D3DAED)
val Glass = Color(0x40D3DAED)
val GlassHigh = Color(0x4CD3DAED)
val GlassHighest = Color(0x5ED3DAED)
val GlassVariant = Color(0x42C0C6DC)
val GlassOutline = Color(0x80ADB2CB)
val GlassOutlineSoft = Color(0x59ADB2CB)

// Чуть приглушённый текст — мягче для глаз, чем яркий перивинкл.
val TextMain = Color(0xFFC0C6DC)
val TextSubtle = Color(0xFF9499B2)

// --- Полупрозрачные тонированные контейнеры (цветное стекло для советов) ---
val InfoGlass = Color(0x59886C85)    // мауве — INFO
val WarningGlass = Color(0x66A18EA4) // лиловый — WARNING
val AlertGlass = Color(0x66520519)   // винный — ALERT

// Непрозрачный фон для диалогов и выпадающих меню (списков выбора),
// иначе поверх градиента они просвечивают и виден текст под ними.
val DialogSurface = Color(0xF2480C25)
val SurfaceSolid = Color(0xFF2C0915)

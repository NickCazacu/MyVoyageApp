package com.nichita.myvoyage.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Единая «жидко-стеклянная» тёмная схема.
 *
 * Фон/поверхности прозрачны — сквозь них виден градиент (рисуется в MainActivity),
 * а карточки и панели полупрозрачны (стекло). Текст — светлый перивинкл.
 */
private val AppColors = darkColorScheme(
    primary = FogBlue,
    onPrimary = Marsala,
    primaryContainer = Burgundy,
    onPrimaryContainer = TextMain,

    secondary = DustyLilac,
    onSecondary = Marsala,
    secondaryContainer = InfoGlass,
    onSecondaryContainer = TextMain,

    tertiary = GreyLavender,
    onTertiary = Marsala,
    tertiaryContainer = WarningGlass,
    onTertiaryContainer = TextMain,

    // Прозрачный фон — виден градиент. surface непрозрачен: на нём рисуются
    // выпадающие меню/списки, иначе они просвечивают.
    background = Color.Transparent,
    onBackground = TextMain,
    surface = SurfaceSolid,
    onSurface = TextMain,

    // Полупрозрачные «стеклянные» уровни (карточки — *Highest/High/Low/Lowest).
    // surfaceContainer непрозрачен — на нём фон меню (DropdownMenu).
    surfaceVariant = GlassVariant,
    onSurfaceVariant = TextSubtle,
    surfaceContainerLowest = GlassLowest,
    surfaceContainerLow = GlassLow,
    surfaceContainer = SurfaceSolid,
    surfaceContainerHigh = GlassHigh,
    surfaceContainerHighest = GlassHighest,

    outline = GlassOutline,
    outlineVariant = GlassOutlineSoft,

    error = Color(0xFFE6A9B0),
    onError = Marsala,
    errorContainer = AlertGlass,
    onErrorContainer = TextMain
)

/** Мягкие скругления в стиле «жидкого стекла». */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyVoyageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

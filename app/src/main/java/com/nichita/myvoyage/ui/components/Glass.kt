package com.nichita.myvoyage.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nichita.myvoyage.ui.theme.BgTop
import com.nichita.myvoyage.ui.theme.Periwinkle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * «Жидкое стекло»: настоящее размытие того, что нарисовано под этим элементом
 * (источник помечается [dev.chrisbanes.haze.hazeSource]). Лёгкий перивинкловый
 * тон поверх размытия даёт матовый, чуть холодный оттенок поверх винного фона.
 */
fun Modifier.frostedGlass(state: HazeState): Modifier = hazeEffect(state) {
    blurRadius = 28.dp
    backgroundColor = BgTop
    tints = listOf(HazeTint(Periwinkle.copy(alpha = 0.09f)))
}

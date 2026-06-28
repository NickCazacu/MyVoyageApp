package com.nichita.myvoyage.ui.tips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichita.myvoyage.domain.Tip
import com.nichita.myvoyage.domain.TipSeverity
import com.nichita.myvoyage.ui.components.frostedGlass
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Вкладка «Советы»: сводные rule-based рекомендации по оптимизации расходов
 * на основе всех рейсов. Сгруппированы по валюте (суммы не смешиваются).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverallTipsScreen(
    onBack: () -> Unit,
    showBack: Boolean = true,
    bottomInset: Dp = 0.dp,
    viewModel: OverallTipsViewModel = viewModel(factory = OverallTipsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val topHaze = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LargeTopAppBar(
                title = { Text("Советы") },
                modifier = Modifier.frostedGlass(topHaze),
                navigationIcon = {
                    if (showBack) TextButton(onClick = onBack) { Text("Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (state.sections.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (state.hasTrips)
                        "Добавьте расходы по рейсам — и здесь появятся советы по экономии."
                    else
                        "Пока нет рейсов.\nДобавьте рейсы и расходы — тут будут советы.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        val showCurrencyHeaders = state.sections.size > 1

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(topHaze),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp + bottomInset
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.sections.forEach { section ->
                if (showCurrencyHeaders) {
                    item {
                        Text(
                            "${section.currency.code} (${section.currency.symbol})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                items(section.tips.size) { i -> TipCard(section.tips[i]) }
            }
        }
    }
}

@Composable
private fun TipCard(tip: Tip) {
    val container = when (tip.severity) {
        TipSeverity.ALERT -> MaterialTheme.colorScheme.errorContainer
        TipSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        TipSeverity.POSITIVE -> MaterialTheme.colorScheme.primaryContainer
        TipSeverity.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(tip.title, style = MaterialTheme.typography.titleMedium)
            Text(tip.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

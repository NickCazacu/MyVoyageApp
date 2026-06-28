package com.nichita.myvoyage.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.ui.components.frostedGlass
import com.nichita.myvoyage.util.Format
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlin.math.roundToInt

/**
 * Экран сводной статистики по ВСЕМ рейсам.
 * Данные сгруппированы по валюте — суммы разных валют не складываются.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverallStatsScreen(
    onBack: () -> Unit,
    showBack: Boolean = true,
    bottomInset: Dp = 0.dp,
    viewModel: OverallStatsViewModel = viewModel(factory = OverallStatsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val topHaze = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LargeTopAppBar(
                title = { Text("Статистика") },
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
        if (state.blocks.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Пока нет данных.\nДобавьте рейсы и расходы — здесь появится сводка.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        // Заголовки валют показываем только если валют больше одной.
        val showCurrencyHeaders = state.blocks.size > 1

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
            item {
                Text(
                    "Всего рейсов: ${state.tripCount}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            state.blocks.forEach { block ->
                if (showCurrencyHeaders) {
                    item {
                        Text(
                            "${block.currency.code} (${block.currency.symbol})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                item { SummaryCard(block) }
                item {
                    Text("Расходы по категориям", style = MaterialTheme.typography.titleSmall)
                }
                item { CategoryChartCard(block.categorySums, block.totalSpent, block.currency) }
            }
        }
    }
}

@Composable
private fun SummaryCard(block: CurrencyStats) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatRow("Рейсов", block.tripCount.toString())
            StatRow("Итого потрачено", Format.money(block.totalSpent, block.currency))
            StatRow("Средний чек за рейс", Format.money(block.avgPerTrip, block.currency))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * График категорий (Vico) и легенда со значениями — по всем рейсам одной валюты.
 * Подписи вынесены в легенду (надёжнее, чем подписи на оси X).
 */
@Composable
private fun CategoryChartCard(sums: List<CategorySum>, total: Double, currency: Currency) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sums.isEmpty()) {
                Text("Нет данных для графика", style = MaterialTheme.typography.bodySmall)
                return@Column
            }

            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(sums) {
                modelProducer.runTransaction {
                    columnSeries { series(sums.map { it.total }) }
                }
            }

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            sums.forEachIndexed { index, cs ->
                val share = if (total > 0) (cs.total / total * 100).roundToInt() else 0
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${index + 1}. ${cs.category.title}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${Format.money(cs.total, currency)} ($share%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

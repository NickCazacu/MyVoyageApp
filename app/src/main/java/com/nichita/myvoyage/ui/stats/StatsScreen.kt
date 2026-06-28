package com.nichita.myvoyage.ui.stats

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.domain.FuelStats
import com.nichita.myvoyage.util.Format
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

/** Экран статистики рейса: итог, сравнение со средним, график категорий, расход топлива. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = state.trip?.currency ?: Currency.DEFAULT

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TotalCard(state.total, currency, state.avgOtherTotal, state.diffVsAvg) }
            item { FuelCard(state.fuelStats, currency) }
            item { Text("Расходы по категориям", style = MaterialTheme.typography.titleMedium) }
            item { CategoryChartCard(state.categorySums, state.total, currency) }
        }
    }
}

@Composable
private fun TotalCard(total: Double, currency: Currency, avg: Double?, diff: Double?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Итого за рейс", style = MaterialTheme.typography.labelMedium)
            Text(Format.money(total, currency), style = MaterialTheme.typography.headlineSmall)
            if (avg != null && diff != null) {
                val pct = (diff * 100).roundToInt()
                val text = when {
                    pct > 0 -> "На $pct% дороже среднего (${Format.money(avg, currency)})"
                    pct < 0 -> "На ${-pct}% дешевле среднего (${Format.money(avg, currency)})"
                    else -> "На уровне среднего (${Format.money(avg, currency)})"
                }
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (pct > 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    "Недостаточно прошлых рейсов для сравнения",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FuelCard(fuel: FuelStats, currency: Currency) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Топливо", style = MaterialTheme.typography.titleMedium)
            StatRow("Заправок", fuel.entriesCount.toString())
            StatRow("Стоимость топлива", Format.money(fuel.totalFuelCost, currency))
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
 * Карточка с графиком категорий (Vico) и легендой со значениями.
 * Подписи категорий вынесены в легенду — это надёжнее, чем подписи на оси X.
 */
@Composable
private fun CategoryChartCard(sums: List<CategorySum>, total: Double, currency: Currency) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sums.isEmpty()) {
                Text("Нет данных для графика", style = MaterialTheme.typography.bodySmall)
                return@Column
            }

            // Готовим модель данных для Vico.
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

            // Легенда: номер столбца → категория, сумма и доля.
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

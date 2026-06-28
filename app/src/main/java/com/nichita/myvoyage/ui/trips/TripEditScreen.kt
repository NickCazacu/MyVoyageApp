package com.nichita.myvoyage.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.ui.components.DateField
import com.nichita.myvoyage.ui.components.EnumDropdown
import com.nichita.myvoyage.ui.components.PlacePickerField

/** Экран создания/редактирования рейса. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TripEditViewModel = viewModel(factory = TripEditViewModel.Factory)
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditing) "Редактировать рейс" else "Новый рейс") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(onSaved) },
                        enabled = form.isValid
                    ) { Text("Сохранить") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlacePickerField(
                label = "Откуда",
                value = form.origin,
                onValueChange = viewModel::onOriginChange,
                placeholder = "напр. Кишинёв",
                modifier = Modifier.fillMaxWidth()
            )

            PlacePickerField(
                label = "Куда",
                value = form.destination,
                onValueChange = viewModel::onDestinationChange,
                placeholder = "напр. Бухарест",
                modifier = Modifier.fillMaxWidth()
            )

            DateField(
                label = "Дата начала",
                millis = form.startDate,
                onPick = viewModel::onStartDateChange
            )

            // Опциональная дата окончания
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.endDate != null,
                    onCheckedChange = { checked ->
                        viewModel.onEndDateChange(if (checked) System.currentTimeMillis() else null)
                    }
                )
                Text("Рейс завершён")
            }
            if (form.endDate != null) {
                DateField(
                    label = "Дата окончания",
                    millis = form.endDate!!,
                    onPick = viewModel::onEndDateChange
                )
            }

            EnumDropdown(
                label = "Валюта",
                options = Currency.entries,
                selected = form.currency,
                optionLabel = { "${it.code} (${it.symbol})" },
                onSelected = viewModel::onCurrencyChange
            )
        }
    }
}

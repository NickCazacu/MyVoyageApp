package com.nichita.myvoyage.ui.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.ui.components.DateField
import com.nichita.myvoyage.ui.components.EnumDropdown

/** Экран добавления/редактирования расхода. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ExpenseEditViewModel = viewModel(factory = ExpenseEditViewModel.Factory)
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditing) "Редактировать расход" else "Новый расход") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
                actions = {
                    TextButton(onClick = { viewModel.save(onSaved) }, enabled = form.isValid) {
                        Text("Сохранить")
                    }
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
            OutlinedTextField(
                value = form.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            EnumDropdown(
                label = "Категория",
                options = Category.entries,
                selected = form.category,
                optionLabel = { it.title },
                onSelected = viewModel::onCategoryChange
            )

            EnumDropdown(
                label = "Валюта",
                options = Currency.entries,
                selected = form.currency,
                optionLabel = { "${it.code} (${it.symbol})" },
                onSelected = viewModel::onCurrencyChange
            )

            DateField(
                label = "Дата",
                millis = form.date,
                onPick = viewModel::onDateChange
            )

            OutlinedTextField(
                value = form.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

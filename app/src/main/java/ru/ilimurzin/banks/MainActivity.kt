@file:OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
)

package ru.ilimurzin.banks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import ru.ilimurzin.banks.ui.theme.BanksTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BanksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BanksTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BanksScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BanksScreen(
    viewModel: BanksViewModel,
    modifier: Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val banks = uiState.banks
    val isLoading = uiState.isLoading
    val isError = uiState.isError

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else if (isError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Ошибка",
                color = MaterialTheme.colorScheme.error,
            )
        }
    } else {
        val navigator = rememberListDetailPaneScaffoldNavigator<String>()
        val scope = rememberCoroutineScope()
        val selectedBankBic = navigator.currentDestination?.contentKey

        NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    BankList(
                        banks = banks,
                        onClick = { bank ->
                            scope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    bank.bic
                                )
                            }
                        })
                }
            },
            detailPane = {
                AnimatedPane {
                    BankDetail(
                        banks.find { it.bic == selectedBankBic },
                    )
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
fun BankList(
    banks: List<Bank>,
    onClick: (Bank) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredBanks = banks.filter {
        it.bic.contains(searchQuery)
                || it.name.contains(searchQuery, ignoreCase = true)
                || it.nameInEnglish != null
                && it.nameInEnglish.contains(searchQuery, ignoreCase = true)
    }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                focusManager.clearFocus()
            }
        )
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск") },
            leadingIcon = { Icon(Icons.Filled.Search, "Иконка поиска") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, "Очистить поиск")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn {
            items(filteredBanks, key = { it.bic }) { bank ->
                ListItem(
                    headlineContent = { Text(bank.bic) },
                    supportingContent = { Text(bank.name) },
                    modifier = Modifier
                        .clickable {
                            focusManager.clearFocus()

                            onClick(bank)
                        },
                )
            }
        }
    }
}

@Composable
fun BankDetail(
    bank: Bank?,
) {
    if (bank == null) {
        return
    }

    val rows = mutableListOf<Pair<String, String>>()
    rows.add(Pair("БИК", bank.bic))
    rows.add(Pair("Наименование", bank.name))
    bank.nameInEnglish?.let {
        if (it.isNotBlank()) {
            rows.add(Pair("Наименование на английском языке", it))
        }
    }
    bank.registryNumber?.let {
        if (it.isNotBlank()) {
            rows.add(Pair("Регистрационный номер", it))
        }
    }
    bank.addressCombined?.let {
        if (it.isNotBlank()) {
            rows.add(Pair("Адрес", it))
        }
    }

    val clipboardManager = LocalClipboardManager.current

    LazyColumn {
        items(rows, key = { it.first }) { row ->
            ListItem(
                headlineContent = { Text(row.first) },
                supportingContent = { Text(row.second) },
                modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString(row.second))
                }
            )
        }
    }
}

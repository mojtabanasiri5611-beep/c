package com.example.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import com.example.presentation.ui.dialogs.AppDeleteConfirmDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.domain.model.BankAccount
import com.example.domain.model.ChequeBook
import com.example.domain.model.Counterparty
import com.example.presentation.ui.dialogs.AddBankAccountDialog
import com.example.presentation.ui.dialogs.AddChequeBookDialog
import com.example.presentation.ui.dialogs.AddCounterpartyDialog
import com.example.presentation.ui.components.CollapsibleBankAccountCard
import com.example.presentation.ui.components.CollapsibleCounterpartyCard

enum class BaseInfoTab(val title: String, val icon: ImageVector) {
    BANK_ACCOUNTS("حساب‌های بانکی", Icons.Default.AccountBalance),
    CHEQUE_BOOKS("دسته‌چک‌ها", Icons.Default.Book),
    CONTACTS("طرف‌های حساب", Icons.Default.People)
}

@Composable
fun BaseInfoTabRow(
    selectedTab: BaseInfoTab,
    onTabSelected: (BaseInfoTab) -> Unit
) {
    val tabs = BaseInfoTab.entries.toTypedArray()
    val selectedIndex = tabs.indexOf(selectedTab)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        divider = {}
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = com.example.ui.theme.VazirmatnFontFamily
                    )
                },
                icon = { Icon(tab.icon, contentDescription = tab.title) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDataScreen(
    chequeBooks: List<ChequeBook>,
    bankAccounts: List<BankAccount>,
    counterparties: List<Counterparty>,
    isToman: Boolean,
    onAddChequeBook: (ChequeBook) -> Unit = {},
    onUpdateChequeBook: (ChequeBook) -> Unit = {},
    onDeleteChequeBook: (ChequeBook) -> Unit = {},
    onAddBankAccount: (BankAccount) -> Unit = {},
    onUpdateBankAccount: (BankAccount) -> Unit = {},
    onDeleteBankAccount: (BankAccount) -> Unit = {},
    onAddCounterparty: (Counterparty) -> Unit = {},
    onUpdateCounterparty: (Counterparty) -> Unit = {},
    onDeleteCounterparty: (Counterparty) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BaseInfoTab.BANK_ACCOUNTS) }

    var isAddChequeBookDialogVisible by remember { mutableStateOf(false) }
    var isAddBankAccountDialogVisible by remember { mutableStateOf(false) }
    var isAddCounterpartyDialogVisible by remember { mutableStateOf(false) }

    var editingBankAccount by remember { mutableStateOf<BankAccount?>(null) }
    var editingChequeBook by remember { mutableStateOf<ChequeBook?>(null) }
    var editingCounterparty by remember { mutableStateOf<Counterparty?>(null) }

    var itemToDeleteDescription by remember { mutableStateOf<String?>(null) }
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (itemToDeleteDescription != null && pendingDeleteAction != null) {
        AppDeleteConfirmDialog(
            itemName = itemToDeleteDescription!!,
            onConfirm = {
                pendingDeleteAction?.invoke()
                itemToDeleteDescription = null
                pendingDeleteAction = null
            },
            onDismiss = {
                itemToDeleteDescription = null
                pendingDeleteAction = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        BaseInfoTab.BANK_ACCOUNTS -> isAddBankAccountDialogVisible = true
                        BaseInfoTab.CHEQUE_BOOKS -> isAddChequeBookDialogVisible = true
                        BaseInfoTab.CONTACTS -> isAddCounterpartyDialogVisible = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("base_data_fab_add")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = when (selectedTab) {
                        BaseInfoTab.BANK_ACCOUNTS -> "افزودن حساب بانکی"
                        BaseInfoTab.CHEQUE_BOOKS -> "افزودن دسته‌چک"
                        BaseInfoTab.CONTACTS -> "افزودن طرف‌حساب"
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("base_data_screen")
        ) {
            BaseInfoTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                BaseInfoTab.BANK_ACCOUNTS -> BankAccountsList(
                    bankAccounts = bankAccounts,
                    isToman = isToman,
                    onEditClick = { editingBankAccount = it },
                    onDeleteClick = { account ->
                        itemToDeleteDescription = "حساب ${account.bankName} (${account.accountNumber})"
                        pendingDeleteAction = { onDeleteBankAccount(account) }
                    }
                )
                BaseInfoTab.CHEQUE_BOOKS -> ChequeBooksList(
                    chequeBooks = chequeBooks,
                    onEditClick = { editingChequeBook = it },
                    onDeleteClick = { book ->
                        itemToDeleteDescription = "دسته‌چک سری ${book.sayadSeries}"
                        pendingDeleteAction = { onDeleteChequeBook(book) }
                    }
                )
                BaseInfoTab.CONTACTS -> CounterpartiesList(
                    counterparties = counterparties,
                    isToman = isToman,
                    onEditClick = { editingCounterparty = it },
                    onDeleteClick = { party ->
                        itemToDeleteDescription = "طرف‌حساب ${party.name}"
                        pendingDeleteAction = { onDeleteCounterparty(party) }
                    }
                )
            }
        }
    }

    if (isAddChequeBookDialogVisible) {
        AddChequeBookDialog(
            bankAccounts = bankAccounts,
            onDismiss = { isAddChequeBookDialogVisible = false },
            onSave = { book ->
                isAddChequeBookDialogVisible = false
                onAddChequeBook(book)
            }
        )
    }

    if (editingChequeBook != null) {
        AddChequeBookDialog(
            bankAccounts = bankAccounts,
            initialChequeBook = editingChequeBook,
            onDismiss = { editingChequeBook = null },
            onSave = { book ->
                editingChequeBook = null
                onUpdateChequeBook(book)
            }
        )
    }

    if (isAddBankAccountDialogVisible) {
        AddBankAccountDialog(
            onDismiss = { isAddBankAccountDialogVisible = false },
            onSave = { account ->
                isAddBankAccountDialogVisible = false
                onAddBankAccount(account)
            }
        )
    }

    if (editingBankAccount != null) {
        AddBankAccountDialog(
            initialAccount = editingBankAccount,
            onDismiss = { editingBankAccount = null },
            onSave = { account ->
                editingBankAccount = null
                onUpdateBankAccount(account)
            }
        )
    }

    if (isAddCounterpartyDialogVisible) {
        AddCounterpartyDialog(
            onDismiss = { isAddCounterpartyDialogVisible = false },
            onSave = { party ->
                isAddCounterpartyDialogVisible = false
                onAddCounterparty(party)
            }
        )
    }

    if (editingCounterparty != null) {
        AddCounterpartyDialog(
            initialCounterparty = editingCounterparty,
            onDismiss = { editingCounterparty = null },
            onSave = { party ->
                editingCounterparty = null
                onUpdateCounterparty(party)
            }
        )
    }
}

@Composable
private fun ChequeBooksList(
    chequeBooks: List<ChequeBook>,
    onEditClick: (ChequeBook) -> Unit,
    onDeleteClick: (ChequeBook) -> Unit
) {
    if (chequeBooks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("دسته‌چکی تعریف نشده است.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = com.example.ui.theme.VazirmatnFontFamily)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chequeBooks, key = { it.id }) { book ->
                com.example.presentation.ui.components.ChequeBookSummaryCard(
                    bankName = book.bankName,
                    accountNumber = book.accountNumber,
                    totalLeaves = book.totalLeaves,
                    voidedLeavesCount = book.voidedLeaves,
                    remainingLeaves = book.remainingLeaves,
                    sayadSeries = book.sayadSeries,
                    onEditClick = { onEditClick(book) },
                    onDeleteClick = { onDeleteClick(book) }
                )
            }
        }
    }
}

@Composable
private fun BankAccountsList(
    bankAccounts: List<BankAccount>,
    isToman: Boolean,
    onEditClick: (BankAccount) -> Unit,
    onDeleteClick: (BankAccount) -> Unit
) {
    if (bankAccounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("حسابی تعریف نشده است.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = com.example.ui.theme.VazirmatnFontFamily)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bankAccounts, key = { it.id }) { account ->
                CollapsibleBankAccountCard(
                    account = account,
                    isToman = isToman,
                    onEditClick = { onEditClick(account) },
                    onDeleteClick = { onDeleteClick(account) }
                )
            }
        }
    }
}

@Composable
private fun CounterpartiesList(
    counterparties: List<Counterparty>,
    isToman: Boolean,
    onEditClick: (Counterparty) -> Unit,
    onDeleteClick: (Counterparty) -> Unit
) {
    if (counterparties.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("طرف‌حسابی تعریف نشده است.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = com.example.ui.theme.VazirmatnFontFamily)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(counterparties, key = { it.id }) { person ->
                CollapsibleCounterpartyCard(
                    person = person,
                    isToman = isToman,
                    onEditClick = { onEditClick(person) },
                    onDeleteClick = { onDeleteClick(person) }
                )
            }
        }
    }
}

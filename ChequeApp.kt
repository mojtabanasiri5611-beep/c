package com.example.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import com.example.ui.theme.VazirmatnFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import com.example.presentation.ui.main.AppNavigationHandler
import com.example.presentation.ui.dialogs.AddChequeDialog
import com.example.presentation.ui.dialogs.EndorsementDialog
import com.example.presentation.ui.dialogs.SettingsDialog
import com.example.presentation.viewmodel.ChequeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequeApp(
    viewModel: ChequeViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val filteredCheques by viewModel.filteredCheques.collectAsState()
    val allCheques by viewModel.allCheques.collectAsState()
    val upcomingAlerts by viewModel.upcomingAlertCheques.collectAsState()
    val chequeBooks by viewModel.chequeBooks.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val counterparties by viewModel.counterparties.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }

    val activity = context as? Activity
    val isInSubPage = uiState.isAddChequeDialogVisible ||
            uiState.isEndorsementDialogVisible ||
            uiState.isSettingsDialogVisible

    AppNavigationHandler(
        currentRoute = if (uiState.selectedTab == 0) "dashboard" else "tab_${uiState.selectedTab}",
        isInSubPage = isInSubPage,
        onNavigateBack = {
            when {
                uiState.isAddChequeDialogVisible -> viewModel.hideAddChequeDialog()
                uiState.isEndorsementDialogVisible -> viewModel.hideEndorsementDialog()
                uiState.isSettingsDialogVisible -> viewModel.hideSettingsDialog()
            }
        },
        onNavigateToDashboard = {
            viewModel.setSelectedTab(0)
        },
        onExitApp = {
            activity?.finish()
        }
    )

    // Force RTL for Persian Persian/Jalali UX and lock font scale to prevent layout breaking
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides fixedFontDensity
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cheque_app_scaffold")
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "مدیریت چک",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleCurrencyUnit() }) {
                            Icon(
                                Icons.Default.CurrencyExchange,
                                contentDescription = "تغییر واحد ریال / تومان",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.showSettingsDialog() },
                            modifier = Modifier.testTag("settings_top_btn")
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "تنظیمات یادآور و هشدارها",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.navigationBarsPadding(),
                    tonalElevation = 8.dp
                ) {
                    NavigationBar(
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "داشبورد", modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                text = "داشبورد",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "چک و تاریخ", modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                text = "چک و تاریخ",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.2.sp,
                                letterSpacing = (-0.3).sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_cheques")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = "اطلاعات پایه", modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                text = "اطلاعات پایه",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.sp,
                                letterSpacing = (-0.5).sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_base_data")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.setSelectedTab(3) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "راس‌گیری", modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                text = "راس‌گیری",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = if (uiState.selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_calculators")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == 4,
                        onClick = { viewModel.setSelectedTab(4) },
                        icon = { Icon(Icons.Default.Archive, contentDescription = "آرشیو", modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                text = "آرشیو",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = if (uiState.selectedTab == 4) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_archive")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == 5,
                        onClick = { viewModel.setSelectedTab(5) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "تنظیمات", modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                text = "تنظیمات",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = if (uiState.selectedTab == 5) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState.selectedTab) {
                    0 -> DashboardScreen(
                        summary = summary,
                        upcomingAlerts = upcomingAlerts,
                        isToman = uiState.isToman,
                        onToggleCurrencyUnit = { viewModel.toggleCurrencyUnit() },
                        onAddNewCheque = { viewModel.showAddChequeDialog() },
                        onGoToCalculators = { viewModel.setSelectedTab(3) },
                        onChequeClick = { cheque ->
                            viewModel.setSelectedTab(1)
                            viewModel.setSearchQuery(cheque.sayadId)
                        }
                    )

                    1 -> ChequesScreen(
                        cheques = filteredCheques,
                        allCheques = allCheques,
                        filter = uiState.filter,
                        viewMode = uiState.viewMode,
                        calendarYear = uiState.calendarYear,
                        calendarMonth = uiState.calendarMonth,
                        selectedCalendarDate = uiState.selectedCalendarDate,
                        isToman = uiState.isToman,
                        onViewModeChange = { viewModel.setViewMode(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterDirectionChange = { viewModel.setFilterDirection(it) },
                        onFilterStatusChange = { viewModel.setFilterStatus(it) },
                        onSortOrderChange = { viewModel.setSortOrder(it) },
                        onPrevMonth = { viewModel.prevCalendarMonth() },
                        onNextMonth = { viewModel.nextCalendarMonth() },
                        onYearChange = { viewModel.setCalendarYear(it) },
                        onMonthChange = { viewModel.setCalendarMonth(it) },
                        onSelectDate = { viewModel.selectCalendarDate(it) },
                        onAddNewCheque = { viewModel.showAddChequeDialog() },
                        onUpdateStatus = { id, status -> viewModel.updateChequeStatus(id, status) },
                        onEndorseCheque = { cheque -> viewModel.showEndorsementDialog(cheque) },
                        onDeleteCheque = { id -> viewModel.deleteCheque(id) },
                        onEditCheque = { cheque -> viewModel.showEditChequeDialog(cheque) },
                        onTransferCheque = { id, name, nationalId, tracking ->
                            viewModel.transferCheque(id, name, nationalId, tracking)
                        }
                    )

                    2 -> BaseDataScreen(
                        chequeBooks = chequeBooks,
                        bankAccounts = bankAccounts,
                        counterparties = counterparties,
                        isToman = uiState.isToman,
                        onAddChequeBook = { book -> viewModel.addChequeBook(book) },
                        onUpdateChequeBook = { book -> viewModel.updateChequeBook(book) },
                        onDeleteChequeBook = { book -> viewModel.deleteChequeBook(book) },
                        onAddBankAccount = { account -> viewModel.addBankAccount(account) },
                        onUpdateBankAccount = { account -> viewModel.updateBankAccount(account) },
                        onDeleteBankAccount = { account -> viewModel.deleteBankAccount(account) },
                        onAddCounterparty = { party -> viewModel.addCounterparty(party) },
                        onUpdateCounterparty = { party -> viewModel.updateCounterparty(party) },
                        onDeleteCounterparty = { party -> viewModel.deleteCounterparty(party) }
                    )

                    3 -> CalculatorsScreen(
                        isToman = uiState.isToman
                    )

                    4 -> com.example.presentation.ui.archive.ArchiveReportScreen(
                        allCheques = allCheques,
                        isToman = uiState.isToman
                    )

                    5 -> com.example.presentation.ui.settings.SettingsScreen()
                }

                // Add Cheque Dialog
                if (uiState.isAddChequeDialogVisible) {
                    AddChequeDialog(
                        bankAccounts = bankAccounts,
                        chequeBooks = chequeBooks,
                        allCheques = allCheques,
                        counterparties = counterparties,
                        initialCheque = uiState.editingCheque,
                        onDismiss = { viewModel.hideAddChequeDialog() },
                        onSave = { cheque, updatedUsed ->
                            if (uiState.editingCheque != null) {
                                viewModel.updateCheque(cheque)
                            } else {
                                viewModel.saveCheque(cheque, updatedUsed)
                            }
                        },
                        onVoidSerials = { serials, account, book ->
                            viewModel.voidSerials(serials, account, book)
                        }
                    )
                }

                // Endorsement Dialog
                if (uiState.isEndorsementDialogVisible && uiState.activeChequeForAction != null) {
                    EndorsementDialog(
                        cheque = uiState.activeChequeForAction!!,
                        counterparties = counterparties,
                        onDismiss = { viewModel.hideEndorsementDialog() },
                        onConfirm = { endorsement ->
                            viewModel.addEndorsement(uiState.activeChequeForAction!!.id, endorsement)
                        }
                    )
                }

                // Settings Dialog
                if (uiState.isSettingsDialogVisible) {
                    SettingsDialog(
                        initialLeadDays = uiState.alertLeadDays,
                        initialSoundEnabled = uiState.isSoundAlertEnabled,
                        initialVibrationEnabled = uiState.isVibrationAlertEnabled,
                        onDismiss = { viewModel.hideSettingsDialog() },
                        onSave = { leadDays, sound, vibration ->
                            viewModel.updateAlertSettings(leadDays, sound, vibration)
                        }
                    )
                }
            }
        }
    }
}

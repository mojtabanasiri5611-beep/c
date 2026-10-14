package com.example.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.financial.SayadValidator
import com.example.ui.theme.VazirmatnFontFamily
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.presentation.ui.cheques.components.ChequeItemCard
import com.example.presentation.ui.dialogs.AppDeleteConfirmDialog
import com.example.presentation.viewmodel.ChequeFilter
import com.example.presentation.viewmodel.ChequeSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequesScreen(
    cheques: List<Cheque>,
    allCheques: List<Cheque>,
    filter: ChequeFilter,
    viewMode: Int, // 0: List, 1: Calendar
    calendarYear: Int,
    calendarMonth: Int,
    selectedCalendarDate: JalaliDate?,
    isToman: Boolean,
    onViewModeChange: (Int) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterDirectionChange: (ChequeDirection?) -> Unit,
    onFilterStatusChange: (ChequeStatus?) -> Unit,
    onSortOrderChange: (ChequeSortOrder) -> Unit = {},
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onYearChange: (Int) -> Unit = {},
    onMonthChange: (Int) -> Unit = {},
    onSelectDate: (JalaliDate) -> Unit,
    onAddNewCheque: () -> Unit,
    onUpdateStatus: (Long, ChequeStatus) -> Unit,
    onEndorseCheque: (Cheque) -> Unit,
    onDeleteCheque: (Long) -> Unit,
    onEditCheque: (Cheque) -> Unit,
    onTransferCheque: ((Long, String, String, String) -> Unit)? = null
) {
    var chequeToDelete by remember { mutableStateOf<Cheque?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    if (chequeToDelete != null) {
        val label = if (chequeToDelete!!.chequeNumber.isNotEmpty()) "چک شماره ${chequeToDelete!!.chequeNumber}" else "چک صیادی ${chequeToDelete!!.sayadId}"
        AppDeleteConfirmDialog(
            itemName = "$label (${chequeToDelete!!.counterpartyName})",
            onConfirm = {
                onDeleteCheque(chequeToDelete!!.id)
                chequeToDelete = null
            },
            onDismiss = { chequeToDelete = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize().testTag("cheques_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Tab Row: List vs Monthly Calendar
            PrimaryTabRow(selectedTabIndex = viewMode) {
                Tab(
                    selected = viewMode == 0,
                    onClick = { onViewModeChange(0) },
                    text = { Text("فهرست چک‌ها", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ViewList, contentDescription = null) }
                )
                Tab(
                    selected = viewMode == 1,
                    onClick = { onViewModeChange(1) },
                    text = { Text("تقویم ماهانه جلالی", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                )
            }

            if (viewMode == 0) {
                // Search Bar & Filter Button & Filter Chips
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = filter.searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cheque_search_input"),
                            placeholder = { Text("جستجو...", style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (filter.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "پاک کردن", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily)
                        )

                        // آیکون فیلتر و مرتب‌سازی
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (filter.sortOrder != ChequeSortOrder.DEFAULT) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, if (filter.sortOrder != ChequeSortOrder.DEFAULT) MaterialTheme.colorScheme.primary else Color.Transparent)
                            ) {
                                IconButton(
                                    onClick = { sortMenuExpanded = true },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "فیلتر و مرتب‌سازی",
                                        tint = if (filter.sortOrder != ChequeSortOrder.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                Text(
                                    text = "مرتب‌سازی بر اساس:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = VazirmatnFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                HorizontalDivider()

                                ChequeSortOrder.values().forEach { order ->
                                    val isSelected = filter.sortOrder == order
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = order.title,
                                                    fontFamily = VazirmatnFontFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onSortOrderChange(order)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direction Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = filter.direction == null && filter.status == null,
                                onClick = {
                                    onFilterDirectionChange(null)
                                    onFilterStatusChange(null)
                                },
                                label = { Text("همه") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter.direction == ChequeDirection.RECEIVABLE,
                                onClick = {
                                    onFilterDirectionChange(
                                        if (filter.direction == ChequeDirection.RECEIVABLE) null
                                        else ChequeDirection.RECEIVABLE
                                    )
                                },
                                label = { Text("دریافتی") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter.direction == ChequeDirection.PAYABLE,
                                onClick = {
                                    onFilterDirectionChange(
                                        if (filter.direction == ChequeDirection.PAYABLE) null
                                        else ChequeDirection.PAYABLE
                                    )
                                },
                                label = { Text("پرداختی") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter.status == ChequeStatus.PENDING,
                                onClick = {
                                    onFilterStatusChange(
                                        if (filter.status == ChequeStatus.PENDING) null
                                        else ChequeStatus.PENDING
                                    )
                                },
                                label = { Text("در انتظار") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter.status == ChequeStatus.CLEARED,
                                onClick = {
                                    onFilterStatusChange(
                                        if (filter.status == ChequeStatus.CLEARED) null
                                        else ChequeStatus.CLEARED
                                    )
                                },
                                label = { Text("وصول شده") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter.status == ChequeStatus.BOUNCED,
                                onClick = {
                                    onFilterStatusChange(
                                        if (filter.status == ChequeStatus.BOUNCED) null
                                        else ChequeStatus.BOUNCED
                                    )
                                },
                                label = { Text("برگشتی") }
                            )
                        }
                    }
                }

                // Cheques List
                val activeCheques = remember(cheques, filter.searchQuery) {
                    cheques.filter { cheque ->
                        cheque.status != com.example.domain.model.ChequeStatus.VOIDED
                    }
                }

                if (activeCheques.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ چکی با این مشخصات یافت نشد.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeCheques, key = { it.id }) { cheque ->
                            ChequeItemCard(
                                cheque = cheque,
                                isToman = isToman,
                                onUpdateStatus = { status -> onUpdateStatus(cheque.id, status) },
                                onEndorse = { onEndorseCheque(cheque) },
                                onTransferCheque = onTransferCheque,
                                onDeleteClick = { chequeToDelete = cheque },
                                onEditClick = { onEditCheque(cheque) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            } else {
                // Jalali Monthly Calendar View
                JalaliCalendarView(
                    year = calendarYear,
                    month = calendarMonth,
                    selectedDate = selectedCalendarDate,
                    allCheques = allCheques,
                    isToman = isToman,
                    onPrevMonth = onPrevMonth,
                    onNextMonth = onNextMonth,
                    onYearChange = onYearChange,
                    onMonthChange = onMonthChange,
                    onSelectDate = onSelectDate,
                    onUpdateStatus = onUpdateStatus,
                    onEndorse = onEndorseCheque,
                    onTransferCheque = onTransferCheque,
                    onDelete = onDeleteCheque,
                    onEdit = onEditCheque
                )
            }
        }

        // Floating Action Button to Add Cheque
        FloatingActionButton(
            onClick = onAddNewCheque,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_cheque_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "ثبت چک جدید")
        }
    }
}

@Composable
fun JalaliCalendarView(
    year: Int,
    month: Int,
    selectedDate: JalaliDate?,
    allCheques: List<Cheque>,
    isToman: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onYearChange: (Int) -> Unit = {},
    onMonthChange: (Int) -> Unit = {},
    onSelectDate: (JalaliDate) -> Unit,
    onUpdateStatus: (Long, ChequeStatus) -> Unit,
    onEndorse: (Cheque) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Cheque) -> Unit,
    onTransferCheque: ((Long, String, String, String) -> Unit)? = null
) {
    val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val monthName = months.getOrElse(month - 1) { "" }
    val daysInMonth = JalaliDate.getMonthLength(year, month)

    // Calculate start day of week for first day of month
    val firstDayJalali = JalaliDate(year, month, 1)
    val startDayOfWeek = firstDayJalali.dayOfWeek // 0 for Saturday, 6 for Friday

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var accumulatedDrag by remember { mutableStateOf(0f) }

    val monthCheques = allCheques.filter {
        val parsed = JalaliDate.parse(it.dueDateJalali)
        parsed != null && parsed.year == year && parsed.month == month
    }

    // Cheques for selected date
    val selectedDateCheques = allCheques.filter {
        if (selectedDate == null) false
        else it.dueDateJalali == selectedDate.formattedString
    }

    var chequeToDelete by remember { mutableStateOf<Cheque?>(null) }

    if (chequeToDelete != null) {
        val label = if (chequeToDelete!!.chequeNumber.isNotEmpty()) "چک شماره ${chequeToDelete!!.chequeNumber}" else "چک صیادی ${chequeToDelete!!.sayadId}"
        AppDeleteConfirmDialog(
            itemName = "$label (${chequeToDelete!!.counterpartyName})",
            onConfirm = {
                onDelete(chequeToDelete!!.id)
                chequeToDelete = null
            },
            onDismiss = { chequeToDelete = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Card (طراحی مدرن مشابه ثبت چک صیادی)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // ۱. هدر انتخاب سال: [ سال قبل ]   [ سال جاری (Bold) ]   [ سال بعد ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prevYear = year - 1
                        val nextYear = year + 1

                        Text(
                            text = JalaliDate.toPersianDigits(prevYear.toString()),
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { onYearChange(prevYear) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = JalaliDate.toPersianDigits(year.toString()),
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = JalaliDate.toPersianDigits(nextYear.toString()),
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { onYearChange(nextYear) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // ۲. هدر ناوبری ماه همراه با فلش‌های چپ و راست و دراپ‌لیست ماه
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevMonth) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "ماه قبل",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box {
                            TextButton(onClick = { monthDropdownExpanded = true }) {
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = VazirmatnFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false }
                            ) {
                                months.forEachIndexed { index, mName ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = mName,
                                                fontFamily = VazirmatnFontFamily,
                                                fontWeight = if (index + 1 == month) FontWeight.Bold else FontWeight.Normal,
                                                color = if (index + 1 == month) MaterialTheme.colorScheme.primary else Color.Unspecified
                                            )
                                        },
                                        onClick = {
                                            onMonthChange(index + 1)
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onNextMonth) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "ماه بعد",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val today = JalaliDate.now()
                                onYearChange(today.year)
                                onMonthChange(today.month)
                                onSelectDate(today)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "برو به امروز",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (selectedDate != null) {
                            Text(
                                text = JalaliDate.toPersianDigits("${selectedDate.year} / ${String.format("%02d", selectedDate.month)} / ${String.format("%02d", selectedDate.day)}"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = VazirmatnFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Weekday headers (شنبه تا جمعه)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEachIndexed { idx, dayLabel ->
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (idx == 6) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Days grid
                    val totalCells = startDayOfWeek + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (c in 0..6) {
                                val cellIndex = r * 7 + c
                                val dayNum = cellIndex - startDayOfWeek + 1

                                if (dayNum in 1..daysInMonth) {
                                    val thisDate = JalaliDate(year, month, dayNum)
                                    val isSelected = selectedDate != null && selectedDate == thisDate
                                    val dayCheques = monthCheques.filter {
                                        val p = JalaliDate.parse(it.dueDateJalali)
                                        p?.day == dayNum
                                    }
                                    val hasReceivable = dayCheques.any { it.direction == ChequeDirection.RECEIVABLE }
                                    val hasPayable = dayCheques.any { it.direction == ChequeDirection.PAYABLE }

                                    JalaliCalendarDayCell(
                                        dayNumber = dayNum,
                                        isSelected = isSelected,
                                        hasPaidCheque = hasPayable,
                                        hasReceivedCheque = hasReceivable,
                                        isFriday = (c == 6),
                                        onClick = { onSelectDate(thisDate) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cheques list for selected date
        item {
            Text(
                text = "چک‌های سررسید ${selectedDate?.formattedPersianDigits ?: ""}:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (selectedDateCheques.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "هیچ چکی در این روز سررسید ندارد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(selectedDateCheques, key = { it.id }) { cheque ->
                ChequeItemCard(
                    cheque = cheque,
                    isToman = isToman,
                    onUpdateStatus = { status -> onUpdateStatus(cheque.id, status) },
                    onEndorse = { onEndorse(cheque) },
                    onTransferCheque = onTransferCheque,
                    onDeleteClick = { chequeToDelete = cheque },
                    onEditClick = { onEdit(cheque) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun JalaliCalendarDayCell(
    dayNumber: Int,
    isSelected: Boolean,
    hasPaidCheque: Boolean,     // چک پرداختی: دایره قرمز دورش
    hasReceivedCheque: Boolean, // چک دریافتی: دایره تو پر بدون border سبز رنگ
    isFriday: Boolean,
    onClick: () -> Unit
) {
    // منطق پس‌زمینه:
    // ۱. چک دریافتی: دایره سبز تو پر (بدون border)
    // ۲. بدون چک اما کاربر کلیک کرده: دایره زرد کم‌رنگ
    val backgroundColor = when {
        hasReceivedCheque -> Color(0xFF10B981) // سبز تو پر
        isSelected && !hasPaidCheque -> Color(0xFFFEF9C3) // دایره زرد کم‌رنگ برای روز انتخابی بدون چک
        isSelected && hasPaidCheque -> Color(0xFFFEE2E2) // پس‌زمینه ملایم داخل دایره قرمز برای روز انتخابی پرداختی
        else -> Color.Transparent
    }

    // منطق border:
    // ۱. چک پرداختی: دایره قرمز رنگ دورش رسم شود
    // ۲. بدون چک اما کلیک شده: دایره زرد رنگ کم‌رنگ دورش
    // ۳. چک دریافتی بدون پرداختی: بدون border
    val borderStroke = when {
        hasPaidCheque -> BorderStroke(2.dp, Color(0xFFEF4444)) // دایره قرمز رنگ دور چک پرداختی
        isSelected && !hasReceivedCheque -> BorderStroke(2.dp, Color(0xFFFBBF24)) // دایره زرد رنگ کم‌رنگ دور روز انتخاب شده بدون چک
        else -> null
    }

    val textColor = when {
        hasReceivedCheque -> Color.White // روی دایره سبز تو پر
        isSelected && !hasPaidCheque && !hasReceivedCheque -> Color(0xFFB45309) // متن تیره روی زرد
        hasPaidCheque -> Color(0xFFDC2626) // متن قرمز
        isFriday -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .then(
                if (borderStroke != null) Modifier.border(borderStroke, CircleShape) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = JalaliDate.toPersianDigits(dayNumber.toString()),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = VazirmatnFontFamily,
                fontWeight = if (isSelected || hasPaidCheque || hasReceivedCheque) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )
    }
}

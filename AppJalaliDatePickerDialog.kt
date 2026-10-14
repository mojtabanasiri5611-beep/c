package com.example.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.common.JalaliDate
import com.example.core.utils.PersianNumberUtils
import com.example.ui.theme.VazirmatnFontFamily

/**
 * تقویم ماهانه جلالی اختصاصی:
 * طراحی شده دقیقاً مشابه و هماهنگ با تقویم تب چک و تاریخ:
 * - دارای نشانگر رنگی و عنوان برجسته در بالای تقویم (تاریخ صدور یا تاریخ سررسید)
 * - هدر انتخاب سال: [ سال قبل ] [ سال جاری (Bold) ] [ سال بعد ]
 * - هدر ناوبری ماه: فلش‌های قبلی/بعدی و منوی کشویی انتخاب مستقیم ماه‌ها
 * - نوار تاریخ: دکمه «برو به امروز» در سمت چپ و تاریخ کامل انتخاب‌شده در سمت راست
 * - ردیف روزهای هفته با رنگ قرمز برای جمعه
 * - ماتریس روزهای ماه با هایلایت روز انتخابی و تاریخ امروز
 * - بدون هیچ‌گونه سوایپ یا ژستور ناخواسته
 */
@Composable
fun AppJalaliDatePickerDialog(
    isOpen: Boolean = true,
    title: String = "انتخاب تاریخ",
    initialDateString: String? = null,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val parsedInitial = remember(initialDateString) {
        if (!initialDateString.isNullOrBlank()) {
            JalaliDate.parse(initialDateString) ?: JalaliDate.now()
        } else {
            JalaliDate.now()
        }
    }

    var displayedYear by remember { mutableIntStateOf(parsedInitial.year) }
    var displayedMonth by remember { mutableIntStateOf(parsedInitial.month) }
    var selectedDay by remember { mutableIntStateOf(parsedInitial.day) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val today = remember { JalaliDate.now() }

    // تنظیم خودکار روز انتخابی در صورت کمتر بودن روزهای ماه جدید (مثلاً ماه‌های ۳۰ یا ۲۹ روزه)
    val daysInMonth = JalaliDate.getMonthLength(displayedYear, displayedMonth)
    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }

    val startDayOfWeek = JalaliDate(displayedYear, displayedMonth, 1).dayOfWeek
    val totalCells = startDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ۱. نشانگر رنگی و عنوان برجسته در بالای تقویم (عنوان سررسید یا صدور)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // ۲. هدر انتخاب سال مشابه تب چک: [ سال قبل ]   [ سال جاری (Bold) ]   [ سال بعد ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prevYear = displayedYear - 1
                    val nextYear = displayedYear + 1

                    Text(
                        text = JalaliDate.toPersianDigits(prevYear.toString()),
                        fontFamily = VazirmatnFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .clickable { displayedYear = prevYear }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = JalaliDate.toPersianDigits(displayedYear.toString()),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .clickable { displayedYear = nextYear }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // ۳. هدر ناوبری ماه همراه با فلش‌های چپ و راست و دراپ‌لیست ماه
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (displayedMonth > 1) {
                            displayedMonth--
                        } else {
                            displayedMonth = 12
                            displayedYear--
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "ماه قبل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box {
                        TextButton(onClick = { monthDropdownExpanded = true }) {
                            Text(
                                text = monthNames.getOrElse(displayedMonth - 1) { "" },
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
                            monthNames.forEachIndexed { index, mName ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mName,
                                            fontFamily = VazirmatnFontFamily,
                                            fontWeight = if (index + 1 == displayedMonth) FontWeight.Bold else FontWeight.Normal,
                                            color = if (index + 1 == displayedMonth) MaterialTheme.colorScheme.primary else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        displayedMonth = index + 1
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = {
                        if (displayedMonth < 12) {
                            displayedMonth++
                        } else {
                            displayedMonth = 1
                            displayedYear++
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "ماه بعد",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ۴. نوار ابزار: آیکون بازگشت به تاریخ امروز در سمت چپ + نمایش فرمت کامل تاریخ انتخاب شده
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val now = JalaliDate.now()
                            displayedYear = now.year
                            displayedMonth = now.month
                            selectedDay = now.day
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

                    Text(
                        text = JalaliDate.toPersianDigits("$displayedYear / ${String.format("%02d", displayedMonth)} / ${String.format("%02d", selectedDay)}"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // ۵. عناوین روزهای هفته (شنبه تا جمعه با قرمز)
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

                Spacer(modifier = Modifier.height(2.dp))

                // ۶. شبکه روزهای ماه مشابه تب چک
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (c in 0..6) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - startDayOfWeek + 1

                            if (dayNum in 1..daysInMonth) {
                                val isSelected = (dayNum == selectedDay)
                                val isToday = (displayedYear == today.year && displayedMonth == today.month && dayNum == today.day)
                                val isFriday = (c == 6)

                                DatePickerCalendarDayCell(
                                    dayNumber = dayNum,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isFriday = isFriday,
                                    onClick = { selectedDay = dayNum }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ۷. دکمه‌های تایید و انصراف در پایین دیالوگ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "انصراف",
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            val selectedDate = JalaliDate(displayedYear, displayedMonth, selectedDay)
                            onDateSelected(selectedDate.formattedString)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "انتخاب تاریخ",
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerCalendarDayCell(
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isFriday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    val borderStroke = when {
        isSelected -> null
        isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else -> null
    }

    val textColor = when {
        isSelected -> Color.White
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
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AppJalaliDatePickerDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    AppJalaliDatePickerDialog(
        isOpen = isOpen,
        title = "انتخاب تاریخ",
        initialDateString = null,
        onDateSelected = onDateSelected,
        onDismiss = onDismiss
    )
}

@Composable
fun AppJalaliDatePickerDialog(
    datePickerTypeTitle: String,
    currentDate: JalaliDate? = null,
    onDateSelected: (JalaliDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    AppJalaliDatePickerDialog(
        isOpen = true,
        title = datePickerTypeTitle,
        initialDateString = currentDate?.formattedString,
        onDateSelected = { str ->
            val parsed = JalaliDate.parse(str) ?: JalaliDate.now()
            onDateSelected(parsed)
        },
        onDismiss = onDismissRequest
    )
}

fun convertMillisToJalaliString(millis: Long): String {
    return JalaliDate.fromTimestamp(millis).formattedString
}

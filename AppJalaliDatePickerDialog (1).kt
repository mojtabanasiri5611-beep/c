package com.mojtaba.chequemanager.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.mojtaba.chequemanager.core.ui.components.AppText
import com.mojtaba.chequemanager.core.utils.JalaliDate
import com.mojtaba.chequemanager.core.utils.PersianNumberUtils

@Composable
fun AppJalaliDatePickerDialog(
    datePickerTypeTitle: String, // "تاریخ صدور" یا "تاریخ سررسید"
    currentDate: JalaliDate,
    onDateSelected: (JalaliDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    var displayedYear by remember { mutableStateOf(currentDate.year) }
    var displayedMonth by remember { mutableStateOf(currentDate.month) }
    var selectedDay by remember { mutableStateOf(currentDate.day) }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // هدر دیالوگ: عنوان مشخص در گوشه سمت راست بالا
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AppText(
                            text = datePickerTypeTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    AppText(
                        text = "${monthNames[displayedMonth - 1]} ${PersianNumberUtils.toPersianDigits(displayedYear)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // نوار جابجایی ماه‌ها فقط با کلیک روی دکمه (سوایپ برای جلوگیری از خطای لمسی حذف شده است)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (displayedMonth < 12) displayedMonth++ else {
                            displayedMonth = 1
                            displayedYear++
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "ماه بعد")
                    }

                    AppText("جابجایی ماه‌ها", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)

                    IconButton(onClick = {
                        if (displayedMonth > 1) displayedMonth-- else {
                            displayedMonth = 12
                            displayedYear--
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "ماه قبل")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // شبکه روزهای ماه
                val daysInMonth = if (displayedMonth <= 6) 31 else if (displayedMonth <= 11) 30 else 29
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(210.dp),
                    userScrollEnabled = false // حذف هرگونه ژستور اسکرول/سوایپ ناخواسته
                ) {
                    items(daysInMonth) { index ->
                        val day = index + 1
                        val isSelected = (day == selectedDay)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            AppText(
                                text = PersianNumberUtils.toPersianDigits(day.toString()),
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // بخش پایین تقویم: دکمه امروز در پایین سمت چپ + دکمه‌های تایید
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // آیکون کوچک بازگشت به تاریخ امروز در پایین سمت چپ
                    IconButton(
                        onClick = {
                            val today = JalaliDate.now()
                            displayedYear = today.year
                            displayedMonth = today.month
                            selectedDay = today.day
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "برو به امروز",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row {
                        TextButton(onClick = onDismissRequest) {
                            AppText("انصراف", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDateSelected(JalaliDate(displayedYear, displayedMonth, selectedDay))
                                onDismissRequest()
                            }
                        ) {
                            AppText("انتخاب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

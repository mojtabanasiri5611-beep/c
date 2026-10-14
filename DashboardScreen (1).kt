package com.mojtaba.chequemanager.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mojtaba.chequemanager.core.ui.components.AppText
import com.mojtaba.chequemanager.core.utils.PersianNumberUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    totalReceivablesRial: Long = 150_000_000L,
    totalPayablesRial: Long = 85_000_000L,
    clearedAmountRial: Long = 50_000_000L,
    bouncedAmountRial: Long = 12_000_000L
) {
    // سوئیچ فراگیر تومان / ریال برای تمام ارقام داشبورد
    var isToman by remember { mutableStateOf(false) }

    val netBalanceRial = totalReceivablesRial - totalPayablesRial

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("داشبورد مدیریت نقدینگی", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    // دکمه تغییر واحد پول که روی کل صفحه اثر می‌گذارد
                    FilterChip(
                        selected = isToman,
                        onClick = { isToman = !isToman },
                        label = { AppText(if (isToman) "نمایش به تومان" else "نمایش به ریال", fontSize = 13.sp) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ۱. فیلد اول: مجموع اسناد دریافتی (تغییر عنوان به دریافتی، وسط‌نویس و چپ‌به‌راست)
            CenteredCashCard(
                title = "دریافتی",
                amountRial = totalReceivablesRial,
                isToman = isToman,
                cardColor = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ۲. فیلد دوم: پرداختی (وسط‌نویس)
            CenteredCashCard(
                title = "پرداختی",
                amountRial = totalPayablesRial,
                isToman = isToman,
                cardColor = Color(0xFFC62828)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ۳. فیلد سوم: خالص تراز نقدینگی (وسط‌نویس، منفی در سمت چپ)
            CenteredCashCard(
                title = "خالص تراز نقدینگی",
                amountRial = netBalanceRial,
                isToman = isToman,
                cardColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // ردیف مبالغ وصول‌شده و برگشت‌خورده (همگام با سوئیچ تومان/ریال)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AppText("وصول شده", fontSize = 13.sp, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.height(4.dp))
                        AppText(
                            text = PersianNumberUtils.formatCurrency(clearedAmountRial, isToman),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AppText("برگشت خورده", fontSize = 13.sp, color = Color(0xFFB71C1C))
                        Spacer(modifier = Modifier.height(4.dp))
                        AppText(
                            text = PersianNumberUtils.formatCurrency(bouncedAmountRial, isToman),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredCashCard(
    title: String,
    amountRial: Long,
    isToman: Boolean,
    cardColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor.copy(alpha = 0.1f),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardColor)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppText(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(6.dp))
            // رقم کاملاً در وسط و واحد در سمت چپ
            AppText(
                text = PersianNumberUtils.formatCurrency(amountRial, isToman),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = cardColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

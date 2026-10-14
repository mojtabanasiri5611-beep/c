package com.example.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.CurrencyFormatter
import com.example.core.common.JalaliDate
import com.example.ui.theme.VazirmatnFontFamily
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeDirection
import com.example.domain.model.FinancialSummary
import com.example.presentation.dashboard.CashFlowSummaryCard
import com.example.presentation.dashboard.CenteredCashCard

@Composable
fun DashboardScreen(
    summary: FinancialSummary,
    upcomingAlerts: List<Cheque>,
    isToman: Boolean,
    onToggleCurrencyUnit: () -> Unit,
    onAddNewCheque: () -> Unit,
    onGoToCalculators: () -> Unit,
    onChequeClick: (Cheque) -> Unit
) {
    // محلی کردن وضعیت برای نمایش دقیق در داشبورد بر اساس درخواست کاربر
    var isTomanUnit by remember(isToman) { mutableStateOf(isToman) }
    
    val totalAmount = summary.totalPendingReceivable + summary.totalPendingPayable
    val displayAmount = if (isTomanUnit) totalAmount / 10 else totalAmount
    val currencyLabel = if (isTomanUnit) "تومان" else "ریال"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Title Row with Currency Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نمای کلی وضعیت مالی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${JalaliDate.toPersianDigits(java.text.DecimalFormat("#,###").format(displayAmount))} $currencyLabel",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { 
                        isTomanUnit = !isTomanUnit
                        onToggleCurrencyUnit()
                    }) {
                        Icon(
                            Icons.Default.CurrencyExchange,
                            contentDescription = "سوییچ واحد",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Real-Time Financial Metric Cards
        item {
            FinancialMetricsGrid(
                summary = summary,
                isToman = isTomanUnit
            )
        }

        // 3. Quick Actions Row (راسگیری و تنزیل در یک خط با متن اصلاحشده)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddNewCheque,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_add_cheque_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ثبت چک جدید",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onGoToCalculators,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("quick_calculator_button"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "راسگیری و تنزیل",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 4. Critical Alerts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سررسیدهای نزدیک (۷ روز آینده)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = JalaliDate.toPersianDigits("${upcomingAlerts.size} فقره"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (upcomingAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "خوشبختانه هیچ چکی در ۷ روز آینده سررسید ندارد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(upcomingAlerts, key = { it.id }) { cheque ->
                UpcomingAlertItem(
                    cheque = cheque,
                    isToman = isToman,
                    onClick = { onChequeClick(cheque) }
                )
            }
        }

        // 5. Moved Net Cash Flow Card to the Bottom of Dashboard
        item {
            NetCashFlowCard(
                summary = summary,
                isToman = isToman
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FinancialMetricsGrid(
    summary: FinancialSummary,
    isToman: Boolean
) {
    val totalReceivablesRial = summary.totalPendingReceivable
    val totalPayablesRial = summary.totalPendingPayable
    val netBalanceRial = totalReceivablesRial - totalPayablesRial
    val clearedAmountRial = summary.totalClearedReceivable + summary.totalClearedPayable
    val bouncedAmountRial = summary.totalBounced

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ۱. فیلد اول: مجموع اسناد دریافتی (تغییر عنوان به دریافتی، وسط‌نویس و چپ‌به‌راست)
        CenteredCashCard(
            title = "دریافتی",
            amountRial = totalReceivablesRial,
            isToman = isToman,
            cardColor = Color(0xFF2E7D32)
        )

        // ۲. فیلد دوم: پرداختی (وسط‌نویس)
        CenteredCashCard(
            title = "پرداختی",
            amountRial = totalPayablesRial,
            isToman = isToman,
            cardColor = Color(0xFFC62828)
        )

        // ۳. فیلد سوم: خالص تراز نقدینگی (وسط‌نویس، منفی در سمت چپ)
        CenteredCashCard(
            title = "خالص تراز نقدینگی",
            amountRial = netBalanceRial,
            isToman = isToman,
            cardColor = MaterialTheme.colorScheme.primary
        )

        // ردیف مبالغ وصول‌شده و برگشت‌خورده (همگام با سوئیچ تومان/ریال)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("وصول شده", fontFamily = VazirmatnFontFamily, fontSize = 13.sp, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.core.utils.PersianNumberUtils.formatCurrency(clearedAmountRial, isToman),
                        fontFamily = VazirmatnFontFamily,
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
                    Text("برگشت خورده", fontFamily = VazirmatnFontFamily, fontSize = 13.sp, color = Color(0xFFB71C1C))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.core.utils.PersianNumberUtils.formatCurrency(bouncedAmountRial, isToman),
                        fontFamily = VazirmatnFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Long,
    count: Int?,
    icon: ImageVector,
    color: Color,
    isToman: Boolean
) {
    ElevatedCard(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (count != null) {
                    Text(
                        text = JalaliDate.toPersianDigits("$count فقره"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = color
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.format(amount, isToman, false),
                    style = CurrencyFormatter.LtrNumberStyle.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isToman) "تومان" else "ریال",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun NetCashFlowCard(
    summary: FinancialSummary,
    isToman: Boolean
) {
    val netCashFlow = summary.netPendingCashFlow
    val isPositive = netCashFlow >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جریان خالص نقدینگی پیشبینیشده",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isPositive) "تراز مثبت" else "کسری نقدینگی",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = CurrencyFormatter.format(netCashFlow, isToman),
                style = CurrencyFormatter.LtrNumberStyle.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPositive) Color(0xFF047857) else Color(0xFFB91C1C),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "مازاد دریافتی منهای تعهدات پرداختی چکهای معوق",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UpcomingAlertItem(
    cheque: Cheque,
    isToman: Boolean,
    onClick: () -> Unit
) {
    val isReceivable = cheque.direction == ChequeDirection.RECEIVABLE
    val daysRemaining = cheque.daysUntilDue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (daysRemaining <= 1) Color(0xFFEF4444).copy(alpha = 0.15f)
                            else Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (daysRemaining <= 1) Color(0xFFEF4444) else Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = cheque.counterpartyName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${cheque.bankName} • صیاد: ...${cheque.sayadId.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(cheque.amount, isToman),
                    style = CurrencyFormatter.LtrNumberStyle.copy(
                        fontSize = 14.sp,
                        color = if (isReceivable) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                val remainingText = when {
                    daysRemaining < 0 -> "گذشته (${Math.abs(daysRemaining)} روز پیش)"
                    daysRemaining == 0 -> "امروز سررسید"
                    daysRemaining == 1 -> "فردا سررسید"
                    else -> "$daysRemaining روز مانده"
                }
                Text(
                    text = JalaliDate.toPersianDigits(remainingText),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (daysRemaining <= 1) Color(0xFFEF4444) else Color(0xFFF59E0B)
                )
            }
        }
    }
}

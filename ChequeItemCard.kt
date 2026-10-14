package com.example.presentation.ui.cheques.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.CurrencyFormatter
import com.example.core.common.JalaliDate
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.presentation.components.TransferChequeDialog
import com.example.ui.theme.VazirmatnFontFamily

private data class ChequeCardColors(
    val containerColor: Color,
    val borderColor: Color,
    val borderWidth: androidx.compose.ui.unit.Dp,
    val amountColor: Color,
    val bankNameColor: Color
)

@Composable
fun ChequeItemCard(
    cheque: Cheque,
    isToman: Boolean,
    onEditClick: (Cheque) -> Unit,
    onDeleteClick: (Cheque) -> Unit,
    onUpdateStatus: (ChequeStatus) -> Unit,
    onEndorse: () -> Unit,
    onTransferCheque: ((chequeId: Long, name: String, nationalId: String, tracking: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    val isPayable = cheque.direction == ChequeDirection.PAYABLE
    
    // محاسبه روزهای باقی‌مانده تا سررسید برای چک‌های پرداختی
    val daysRemaining = remember(cheque.dueDateJalali) {
        val today = JalaliDate.now()
        val due = JalaliDate.parse(cheque.dueDateJalali)
        if (due != null) today.daysUntil(due) else 999
    }

    // طیف رنگ قرمز برای چک‌های پرداختی بر اساس نزدیک‌تر بودن سررسید (رنگ و ضخامت حاشیه):
    val cardStyle = remember(isPayable, daysRemaining) {
        if (!isPayable) {
            // چک دریافتی: سبز استاندارد
            ChequeCardColors(
                containerColor = Color(0xFFF0FDF4),
                borderColor = Color(0xFF86EFAC),
                borderWidth = 1.2.dp,
                amountColor = Color(0xFF16A34A),
                bankNameColor = Color(0xFF166534)
            )
        } else {
            // چک پرداختی: طیفی از قرمز هرچه سررسید نزدیک‌تر، قرمزتر و ضخیم‌تر
            when {
                daysRemaining <= 1 -> ChequeCardColors(
                    containerColor = Color(0xFFFEE2E2), // قرمز پررنگ هشدار بحرانی
                    borderColor = Color(0xFFB91C1C),
                    borderWidth = 2.0.dp,
                    amountColor = Color(0xFF991B1B),
                    bankNameColor = Color(0xFF7F1D1D)
                )
                daysRemaining in 2..4 -> ChequeCardColors(
                    containerColor = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFDC2626),
                    borderWidth = 1.6.dp,
                    amountColor = Color(0xFFB91C1C),
                    bankNameColor = Color(0xFF991B1B)
                )
                daysRemaining in 5..9 -> ChequeCardColors(
                    containerColor = Color(0xFFFFF5F5),
                    borderColor = Color(0xFFF87171),
                    borderWidth = 1.3.dp,
                    amountColor = Color(0xFFE11D48),
                    bankNameColor = Color(0xFF9F1239)
                )
                else -> ChequeCardColors(
                    containerColor = Color(0xFFFFFBFB),
                    borderColor = Color(0xFFFCA5A5),
                    borderWidth = 1.0.dp,
                    amountColor = Color(0xFFE11D48),
                    bankNameColor = Color(0xFFBE123C)
                )
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isExpanded) cardStyle.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (!isExpanded) androidx.compose.foundation.BorderStroke(cardStyle.borderWidth, cardStyle.borderColor) else null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cheque.bankName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = if (!isExpanded) cardStyle.bankNameColor else MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.format(cheque.amount, isToman),
                        style = CurrencyFormatter.LtrNumberStyle.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpanded) cardStyle.amountColor else MaterialTheme.colorScheme.primary
                        )
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (!isExpanded) cardStyle.amountColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f), thickness = 1.dp)

                    // ۱. مبلغ به حروف بدون پیشوند "مبلغ به حروف:" با رنگ مشکی
                    val amountInWords = com.example.core.util.MoneyToWordsConverter.convert(cheque.amount, isToman)
                    val unitLabel = if (isToman) "تومان" else "ریال"

                    Text(
                        text = "$amountInWords $unitLabel",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.Black
                    )

                    // ۲. شناسه صیادی ۴ رقم ۴ رقم بلوکی از چپ به راست با ارقام فارسی
                    val rawSayadClean = cheque.sayadId.replace(Regex("[^0-9]"), "")
                    val blockedSayad = JalaliDate.toPersianDigits(rawSayadClean.chunked(4).joinToString(" "))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شناسه صیادی:",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "\u200E$blockedSayad",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textDirection = TextDirection.Ltr
                            )
                        )
                    }

                    if (cheque.chequeNumber.isNotEmpty()) {
                        DetailRow(label = "شماره سریال چک", value = JalaliDate.toPersianDigits(cheque.chequeNumber))
                    }
                    DetailRow(label = "طرف حساب", value = cheque.counterpartyName)
                    DetailRow(label = "تاریخ صدور", value = JalaliDate.toPersianDigits(cheque.issueDateJalali))

                    // ۳. تاریخ سررسید دقیق همراه با روز مانده: تاریخ سر رسید : ( ۵ روز مانده)            ۱۴۰۵/۰۷/۱۸
                    val daysRemaining = cheque.daysUntilDue
                    val remainingDaysText = when {
                        daysRemaining > 0 -> "($daysRemaining روز مانده)"
                        daysRemaining == 0 -> "(سررسید امروز)"
                        else -> "(${-daysRemaining} روز گذشته)"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "تاریخ سر رسید :",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = JalaliDate.toPersianDigits(remainingDaysText),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = VazirmatnFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (daysRemaining in 0..7) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Text(
                            text = JalaliDate.toPersianDigits(cheque.dueDateJalali),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // ۴. شماره فاکتور قبل از شرح
                    if (!cheque.invoiceNumber.isNullOrEmpty()) {
                        DetailRow(label = "شماره فاکتور", value = JalaliDate.toPersianDigits(cheque.invoiceNumber))
                    }

                    // ۵. شرح بلافاصله بعد از دونقطه شروع شود
                    if (!cheque.description.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "شرح: ",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = cheque.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = VazirmatnFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // دکمه‌های عملیاتی با ارتفاع فشرده ۳۰dp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (cheque.status != ChequeStatus.CLEARED) {
                            ActionButton(
                                icon = Icons.Default.Check,
                                text = "وصول",
                                color = Color(0xFF10B981),
                                onClick = { onUpdateStatus(ChequeStatus.CLEARED) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (cheque.status != ChequeStatus.BOUNCED) {
                            ActionButton(
                                icon = Icons.Default.Close,
                                text = "برگشت",
                                color = Color(0xFFEF4444),
                                onClick = { onUpdateStatus(ChequeStatus.BOUNCED) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (cheque.direction == ChequeDirection.RECEIVABLE && (cheque.status == ChequeStatus.PENDING || cheque.status == ChequeStatus.AT_HAND)) {
                            ActionButton(
                                icon = Icons.Default.Send,
                                text = "خرج",
                                color = Color(0xFF8B5CF6),
                                onClick = { showTransferDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (cheque.direction == ChequeDirection.RECEIVABLE && (cheque.status == ChequeStatus.PENDING || cheque.status == ChequeStatus.AT_HAND)) {
                        OutlinedButton(
                            onClick = { showTransferDialog = true },
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B5CF6))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("واگذاری و خرج کردن چک", fontFamily = VazirmatnFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ActionButton(
                            icon = Icons.Default.Edit,
                            text = "ویرایش",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { onEditClick(cheque) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            text = "حذف",
                            color = MaterialTheme.colorScheme.error,
                            onClick = { onDeleteClick(cheque) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showTransferDialog) {
        TransferChequeDialog(
            chequeAmount = cheque.amountRial,
            chequeSayadId = cheque.sayadId,
            onDismissRequest = { showTransferDialog = false },
            onConfirmTransfer = { name, nationalId, tracking ->
                if (onTransferCheque != null) {
                    onTransferCheque(cheque.id, name, nationalId, tracking)
                } else {
                    onEndorse()
                }
                showTransferDialog = false
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp))
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(30.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp), color = color, fontWeight = FontWeight.Bold)
    }
}

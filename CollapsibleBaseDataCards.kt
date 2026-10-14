package com.example.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.domain.model.BankAccount
import com.example.domain.model.Counterparty
import com.example.ui.theme.VazirmatnFontFamily

/**
 * کارت تاشو و جمع‌شونده برای حساب‌های بانکی:
 * در حالت جمع شده فقط نام بانک و شماره حساب روبروی آن نمایش داده می‌شود.
 * در حالت باز، تمام اطلاعات تکمیلی حساب (شعبه، دارنده، شبا، شماره کارت ۱۶ رقمی، موجودی اولیه و...)
 * به صورت ارقام فارسی و با حذف فیلدهای خالی نمایش داده می‌شود.
 */
@Composable
fun CollapsibleBankAccountCard(
    account: BankAccount,
    isToman: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // حالت جمع‌شده: نام بانک و شماره حساب روبروی هم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.bankName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "حساب: ${JalaliDate.toPersianDigits(account.accountNumber)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // حالت باز: نمایش اطلاعات تکمیلی (فقط فیلدهای پر شده)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f), thickness = 0.8.dp)

                    if (account.accountHolder.isNotBlank()) {
                        InfoRow(label = "نام صاحب حساب", value = account.accountHolder)
                    }

                    if (!account.branchNumber.isNullOrBlank()) {
                        InfoRow(label = "شماره / کد شعبه", value = JalaliDate.toPersianDigits(account.branchNumber))
                    }

                    if (!account.shabaNumber.isNullOrBlank()) {
                        val shabaDigits = JalaliDate.toPersianDigits(account.shabaNumber)
                        InfoRow(label = "شماره شبا (IBAN)", value = "IR $shabaDigits")
                    }

                    if (!account.cardNumber.isNullOrBlank()) {
                        val rawCard = account.cardNumber.filter { it.isDigit() }
                        val blockedCard = JalaliDate.toPersianDigits(rawCard.chunked(4).joinToString(" "))
                        InfoRow(label = "شماره کارت (۱۶ رقمی)", value = blockedCard)
                    }

                    InfoRow(
                        label = "موجودی اولیه / فعلی",
                        value = JalaliDate.formatCurrency(account.currentBalance, isToman)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * کارت تاشو و جمع‌شونده برای طرف‌های حساب:
 * در حالت جمع شده فقط نام شخص یا شرکت دیده می‌شود.
 * در حالت باز، تمام اطلاعات تکمیلی (شماره تماس، کدملی، درجه اعتبار، نام و شماره ویزیتور و...)
 * به صورت ارقام فارسی و با حذف فیلدهای خالی نمایش داده می‌شود.
 */
@Composable
fun CollapsibleCounterpartyCard(
    person: Counterparty,
    isToman: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // حالت جمع‌شده: نام شخص یا شرکت
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (person.creditRating.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = JalaliDate.toPersianDigits(person.creditRating),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = VazirmatnFontFamily),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // حالت باز: تمام اطلاعات کامل
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f), thickness = 0.8.dp)

                    if (!person.phone.isNullOrBlank()) {
                        InfoRow(label = "شماره تماس", value = JalaliDate.toPersianDigits(person.phone))
                    }

                    if (!person.nationalCode.isNullOrBlank()) {
                        InfoRow(label = "کد ملی / شناسه ملی", value = JalaliDate.toPersianDigits(person.nationalCode))
                    }

                    if (person.creditRating.isNotBlank()) {
                        InfoRow(label = "درجه اعتبار", value = JalaliDate.toPersianDigits(person.creditRating))
                    }

                    if (!person.visitorName.isNullOrBlank()) {
                        InfoRow(label = "نام ویزیتور", value = person.visitorName)
                    }

                    if (!person.visitorPhone.isNullOrBlank()) {
                        InfoRow(label = "تلفن ویزیتور", value = JalaliDate.toPersianDigits(person.visitorPhone))
                    }

                    if (!person.address.isNullOrBlank()) {
                        InfoRow(label = "آدرس", value = person.address)
                    }

                    if (!person.notes.isNullOrBlank()) {
                        InfoRow(label = "یادداشت / توضیحات", value = person.notes)
                    }

                    if (person.bouncedCount > 0) {
                        InfoRow(
                            label = "چک‌های برگشتی",
                            value = "${JalaliDate.toPersianDigits(person.bouncedCount.toString())} فقره",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }

                    InfoRow(
                        label = "گردش کل چک‌ها",
                        value = JalaliDate.formatCurrency(person.totalReceivableVolume, isToman)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = VazirmatnFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            ),
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

package com.example.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.AppText
import com.example.core.utils.PersianNumberUtils
import com.example.ui.theme.VazirmatnFontFamily
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun CashFlowSummaryCard(
    title: String,
    amountRial: Long,
    currencyUnit: String = "ریال",
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor.copy(alpha = 0.12f),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardColor)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppText(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    AppText(
                        text = PersianNumberUtils.formatCurrencyOnLeft(amountRial, currencyUnit),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = cardColor
                    )
                }
            }
        }
    }
}

@Composable
fun CenteredCashCard(
    title: String,
    amountRial: Long,
    isToman: Boolean,
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    val unit = if (isToman) "تومان" else "ریال"
    val value = if (isToman) amountRial / 10 else amountRial
    val isNegative = value < 0
    val absValue = kotlin.math.abs(value)

    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
    }
    val decimalFormat = DecimalFormat("#,###", symbols)
    val formattedNumber = PersianNumberUtils.toPersianDigits(decimalFormat.format(absValue))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor.copy(alpha = 0.1f),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardColor)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppText(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(6.dp))

            // ارقام در وسط فیلد و از چپ به راست، واحد پولی و علامت منفی در سمت چپ
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isNegative) {
                        Text(
                            text = "-",
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = cardColor,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = unit,
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = cardColor,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedNumber,
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = cardColor,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

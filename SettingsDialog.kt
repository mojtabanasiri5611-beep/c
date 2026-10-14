package com.example.presentation.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate

@Composable
fun SettingsDialog(
    initialLeadDays: Int,
    initialSoundEnabled: Boolean,
    initialVibrationEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (leadDays: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) -> Unit
) {
    var leadDays by remember { mutableIntStateOf(initialLeadDays) }
    var soundEnabled by remember { mutableStateOf(initialSoundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(initialVibrationEnabled) }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides fixedFontDensity
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "تنظیمات یادآور و هشدارها",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "زمان یادآوری سررسید چک‌ها:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 5, 7).forEach { days ->
                        FilterChip(
                            selected = leadDays == days,
                            onClick = { leadDays = days },
                            label = {
                                Text(
                                    text = JalaliDate.toPersianDigits("$days روز قبل"),
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                // Sound Alert Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("هشدار صوتی", fontWeight = FontWeight.Bold)
                            Text("پخش اعلان هنگام سررسید چک", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("لرزش (ویبره)", fontWeight = FontWeight.Bold)
                            Text("فعال‌سازی لرزش برای اعلان هشدار", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }

                HorizontalDivider()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "تمام هشدارها و محاسبات درون دستگاه (آفلاین) اجرا می‌شوند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(leadDays, soundEnabled, vibrationEnabled) },
                modifier = Modifier.testTag("save_settings_btn")
            ) {
                Text("ذخیره تنظیمات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
    }
}

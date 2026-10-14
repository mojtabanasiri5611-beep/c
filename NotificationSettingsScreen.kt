package com.example.presentation.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.ui.components.AppText
import com.example.ui.theme.VazirmatnFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationReminderDropdown(
    selectedDaysBefore: Int,
    onReminderSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val reminderOptions = listOf(
        Pair("همان روز سررسید (۰ روز)", 0),
        Pair("۱ روز قبل از سررسید", 1),
        Pair("۲ روز قبل از سررسید", 2),
        Pair("۳ روز قبل از سررسید", 3),
        Pair("۵ روز قبل از سررسید", 5),
        Pair("۱ هفته (۷ روز) قبل از سررسید", 7),
        Pair("۱۰ روز قبل از سررسید", 10)
    )

    val currentText = reminderOptions.find { it.second == selectedDaysBefore }?.first ?: "انتخاب زمان یادآوری"

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        AppText("زمان ارسال یادآوری سررسید چک:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentText,
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
                label = { Text("انتخاب بازه یادآوری", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                reminderOptions.forEach { (label, days) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                fontFamily = VazirmatnFontFamily,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onReminderSelected(days)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("cheque_app_prefs", Context.MODE_PRIVATE) }
    var selectedReminderDays by remember {
        mutableIntStateOf(sharedPrefs.getInt("reminder_days", 2))
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "تنظیمات اعلان‌ها و یادآوری",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = VazirmatnFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        if (!hasNotificationPermission) {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("دریافت مجوز ارسال اعلان (Notification)", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        NotificationReminderDropdown(
            selectedDaysBefore = selectedReminderDays,
            onReminderSelected = { days ->
                selectedReminderDays = days
                sharedPrefs.edit().putInt("reminder_days", days).apply()
            }
        )
    }
}

// تابع بررسی آستانه اتمام دسته چک هنگام صدور چک پرداختی
fun checkChequeBookRemainingCapacity(remainingLeafCount: Int, bankName: String, accountNumber: String, onTriggerAlert: (String, String) -> Unit) {
    if (remainingLeafCount <= 3) { // آستانه هشدار: ۳ برگ آخر
        onTriggerAlert(bankName, accountNumber)
    }
}

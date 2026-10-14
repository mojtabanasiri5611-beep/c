package com.example.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.core.ui.components.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onBackClick: () -> Unit = {}
) {
    BackHandler(onBack = onBackClick)

    var isPinEnabled by remember { mutableStateOf(false) }
    var isBiometricEnabled by remember { mutableStateOf(false) }
    var autoLockTime by remember { mutableStateOf("بلافاصله") }
    var isAutoLockExpanded by remember { mutableStateOf(false) }

    val autoLockOptions = listOf("بلافاصله", "۱ دقیقه", "۵ دقیقه", "۱۵ دقیقه")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("تنظیمات رمز و امنیت", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText("قفل با رمز عبور (PIN)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    AppText("درخواست رمز هنگام هر بار ورود به برنامه", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                }
                Switch(checked = isPinEnabled, onCheckedChange = { isPinEnabled = it })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText("ورود با اثر انگشت (Biometric)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    AppText("استفاده از حسگر بیومتریک گوشی برای ورود سریع", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                }
                Switch(
                    checked = isBiometricEnabled, 
                    onCheckedChange = { isBiometricEnabled = it },
                    enabled = isPinEnabled
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            AppText("زمان قفل خودکار برنامه", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            
            ExposedDropdownMenuBox(
                expanded = isAutoLockExpanded,
                onExpandedChange = { isAutoLockExpanded = it }
            ) {
                OutlinedTextField(
                    value = autoLockTime,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAutoLockExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isAutoLockExpanded,
                    onDismissRequest = { isAutoLockExpanded = false }
                ) {
                    autoLockOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { AppText(option, fontSize = 15.sp) },
                            onClick = {
                                autoLockTime = option
                                isAutoLockExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.example.presentation.ui.settings

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.example.core.backup.BackupManager
import com.example.core.common.AppResult
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.settings.SecuritySettingsScreen
import com.example.presentation.settings.NotificationSettingsScreen
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("cheque_app_prefs", Context.MODE_PRIVATE) }

    var showSecurityScreen by remember { mutableStateOf(false) }

    if (showSecurityScreen) {
        SecuritySettingsScreen(
            onBackClick = { showSecurityScreen = false }
        )
        return
    }

    val scrollState = rememberScrollState()

    var userName by remember {
        mutableStateOf(sharedPrefs.getString("user_name", "") ?: "")
    }

    var licenseCode by remember {
        mutableStateOf(sharedPrefs.getString("license_code", "") ?: "")
    }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ۱. عنوان بالای صفحه
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "تنظیمات، امنیت و مدیریت سیستم",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = VazirmatnFontFamily,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // پیام بازخورد
        feedbackMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = msg,
                        fontFamily = VazirmatnFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // ۲. مشخصات کاربر و مالک برنامه
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "مشخصات کاربر و عنوان برنامه",
                    fontFamily = VazirmatnFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        sharedPrefs.edit().putString("user_name", it).apply()
                    },
                    label = { Text("نام مالک یا عنوان تجاری برنامه", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        // ۳. بخش یکپارچه قفل و امنیت برنامه
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = { 
                    Text("قفل و امنیت برنامه", fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily) 
                },
                supportingContent = { 
                    Text("تنظیم شیوه قفل (پین، الگو، بیومتریک) و کلید امنیتی بکاپ", fontFamily = VazirmatnFontFamily, fontSize = 11.sp) 
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .clickable { showSecurityScreen = true }
                    .fillMaxWidth(),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }

        // ۴. بخش لایسنس و فعال‌سازی
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "لایسنس و فعال‌سازی نرم‌افزار",
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضعیت لایسنس:",
                        fontFamily = VazirmatnFontFamily,
                        fontSize = 12.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (licenseCode.isNotEmpty()) "کد ثبت‌شده (در انتظار تایید نهایی)" else "نسخه پایه / آزمایشی",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = licenseCode,
                    onValueChange = {
                        licenseCode = it
                        sharedPrefs.edit().putString("license_code", it).apply()
                    },
                    label = { Text("کد فعال‌سازی یا شماره سریال لایسنس", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                    placeholder = { Text("XXXX-XXXX-XXXX-XXXX", fontFamily = VazirmatnFontFamily, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = {
                        feedbackMessage = "کد لایسنس دریافت شد. اتصال به سرور صدور لایسنس در مرحله نهایی تکمیل خواهد شد."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بررسی و فعال‌سازی لایسنس", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Text(
                    text = "* توجه: طبق برنامه‌ریزی، زیرساخت و رابط کاربری لایسنس در این بخش تعبیه شده و مکانیزم اعتبارسنجی نهایی در انتهای پروژه فعال خواهد شد.",
                    fontFamily = VazirmatnFontFamily,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ۵. بخش رفع و سلب مسئولیت حقوقی
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "شرایط استفاده و سلب مسئولیت حقوقی",
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "مقررات عمومی و حقوقی:",
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "این نرم‌افزار به عنوان دستیار هوشمند و سیستم بایگانی و یادآوری چک‌های صیادی و شخصی طراحی شده است. کلیه مسئولیت‌های قانونی ناشی از تطابق اطلاعات ثبت‌شده با برگه فیزیکی چک، تسویه حساب‌ها، وصول یا برگشت چک‌ها و تعهدات مالی بر عهده شخص کاربر می‌باشد.\n\n(محل درج متن تکمیلی و حقوقی رفع مسئولیت در نظر گرفته شده است و متن نهایی متعاقباً اضافه خواهد شد)",
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ۶. تنظیمات اعلان‌ها و یادآوری سررسید
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            NotificationSettingsScreen(modifier = Modifier.fillMaxWidth())
        }

        // ۷. خروجی و پشتیبان‌گیری
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "پشتیبان‌گیری و استخراج اطلاعات",
                    fontFamily = VazirmatnFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        // TODO: Implement URI picker for outputUri
                        // ExcelManager.exportChequesToExcel(context, allCheques, outputUri)
                        feedbackMessage = "فایل اکسل با موفقیت تولید شد و آماده اشتراک‌گذاری است."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("خروجی اکسل از فهرست چک‌ها", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val backupFile = File(context.filesDir, "backup_${System.currentTimeMillis()}.db")
                        val uri = Uri.fromFile(backupFile)
                        val result = BackupManager.createBackup(context, uri)
                        if (result is AppResult.Success) {
                            feedbackMessage = "نسخه پشتیبان در ${backupFile.absolutePath} ایجاد شد."
                        } else {
                            feedbackMessage = "خطا در ایجاد پشتیبان."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تهیه فایل پشتیبان (Backup)", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

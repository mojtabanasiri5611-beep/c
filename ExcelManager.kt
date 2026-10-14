package com.example.core.excel

import android.content.Context
import android.net.Uri
import com.example.core.common.AppResult
import com.example.domain.model.Cheque
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object ExcelManager {
    fun exportChequesToExcel(context: Context, cheques: List<Cheque>, outputUri: Uri): AppResult<Unit> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("چک‌ها")
            
            val headerRow = sheet.createRow(0)
            val headers = listOf("شناسه صیادی", "شماره چک", "مبلغ", "تاریخ سررسید", "وضعیت", "نوع")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            cheques.forEachIndexed { index, cheque ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(cheque.sayadId)
                row.createCell(1).setCellValue(cheque.chequeNumber)
                row.createCell(2).setCellValue(cheque.amount.toDouble())
                row.createCell(3).setCellValue(cheque.dueDateJalali)
                row.createCell(4).setCellValue(cheque.status.name)
                row.createCell(5).setCellValue(cheque.direction.name)
            }
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}

package com.example.core.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.core.utils.PersianNumberUtils

/**
 * این کلاس را به visualTransformation هر فیلد متنی اضافه کنید تا هر عددی وارد شد فوراً فارسی نمایش داده شود
 */
class PersianDigitsVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val persianText = PersianNumberUtils.toPersianDigits(text.text)
        return TransformedText(
            text = AnnotatedString(persianText),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

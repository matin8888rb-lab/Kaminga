package com.kaminga.reader.feature.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaminga.reader.core.designsystem.EmptyView

/**
 * در مرحله‌ی اول ذخیره‌سازی و حساب کاربری وجود ندارد؛ این صفحه فقط
 * حالت «کتابخانه‌ی خالی / وارد نشده» را صادقانه نشان می‌دهد.
 */
@Composable
fun LibraryScreen() {
    Column(Modifier.fillMaxSize()) {
        Text(
            "کتابخانه‌ی من",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp)
        )
        EmptyView(
            icon = Icons.Default.Favorite,
            title = "کتابخانه‌ات خالی است",
            body = "علاقه‌مندی‌ها و تاریخچه‌ی مطالعه بعد از اضافه شدن ذخیره‌سازی و حساب کاربری این‌جا نمایش داده می‌شوند. این بخش‌ها هنوز ساخته نشده‌اند."
        )
    }
}

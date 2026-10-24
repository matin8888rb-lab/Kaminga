package com.kaminga.reader.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** ساخت ساده‌ی ViewModel با وابستگی‌هایی که از بیرون پاس داده می‌شوند. */
fun <VM : ViewModel> simpleFactory(producer: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = producer() as T
    }

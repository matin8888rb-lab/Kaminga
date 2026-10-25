package com.kaminga.reader.domain.usecase

/** مقایسه‌ی «طبیعی» نام فایل‌ها: page2 قبل از page10 می‌آید. */
fun naturalCompare(a: String, b: String): Int {
    var i = 0
    var j = 0
    while (i < a.length && j < b.length) {
        val ca = a[i]
        val cb = b[j]
        if (ca.isDigit() && cb.isDigit()) {
            var endA = i
            while (endA < a.length && a[endA].isDigit()) endA++
            var endB = j
            while (endB < b.length && b[endB].isDigit()) endB++
            val numA = a.substring(i, endA).trimStart('0')
            val numB = b.substring(j, endB).trimStart('0')
            if (numA.length != numB.length) return numA.length - numB.length
            val cmp = numA.compareTo(numB)
            if (cmp != 0) return cmp
            i = endA
            j = endB
        } else {
            val cmp = ca.lowercaseChar().compareTo(cb.lowercaseChar())
            if (cmp != 0) return cmp
            i++
            j++
        }
    }
    return (a.length - i) - (b.length - j)
}

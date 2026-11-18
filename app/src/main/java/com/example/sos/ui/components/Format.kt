package com.example.sos.ui.components

import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val idrFormat = NumberFormat.getIntegerInstance(Locale.US)
private val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

/** 15000 → "IDR 15,000" */
fun formatIdr(amount: Long): String = "IDR ${idrFormat.format(amount)}"

/** → "1 September 2026" */
fun formatDate(date: LocalDate): String = date.format(dateFormat)

/** → "1 September 2026, 11:45 AM" */
fun formatDateTime(dateTime: LocalDateTime): String =
    "${dateTime.toLocalDate().format(dateFormat)}, ${dateTime.format(timeFormat)}"

/** → "today", "tomorrow", "3 days", "overdue" */
fun deadlineText(deadline: LocalDate): String {
    val days = ChronoUnit.DAYS.between(LocalDate.now(), deadline)
    return when {
        days < 0 -> "overdue"
        days == 0L -> "today"
        days == 1L -> "tomorrow"
        else -> "$days days"
    }
}

/** → "Deadline in 3 days" / "Deadline today" */
fun deadlineLabel(deadline: LocalDate): String {
    val text = deadlineText(deadline)
    return when (text) {
        "today", "tomorrow", "overdue" -> "Deadline $text"
        else -> "Deadline in $text"
    }
}

/** Untuk daftar chat: "Now", "10m", "10h", "5d", "1w". */
fun shortTimeAgo(time: LocalDateTime): String {
    val minutes = Duration.between(time, LocalDateTime.now()).toMinutes()
    return when {
        minutes < 1 -> "Now"
        minutes < 60 -> "${minutes}m"
        minutes < 60 * 24 -> "${minutes / 60}h"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d"
        else -> "${minutes / (60 * 24 * 7)}w"
    }
}

/** "12:30 PM" */
fun formatClock(time: LocalDateTime): String = time.format(timeFormat)

/** "Today", "Yesterday", atau "8 Sep 2026" untuk pemisah tanggal di chat. */
fun chatDayLabel(date: LocalDate): String {
    val days = ChronoUnit.DAYS.between(date, LocalDate.now())
    return when (days) {
        0L -> "Today"
        1L -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    }
}

/** "Sep 8, 2026" */
fun formatShortDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))

/** "11:30 PM" */
fun formatTime(time: java.time.LocalTime): String = time.format(timeFormat)

/** → "2 hours ago" */
fun timeAgo(time: LocalDateTime): String {
    val duration = Duration.between(time, LocalDateTime.now())
    val minutes = duration.toMinutes()
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 60 * 24 -> "${minutes / 60} hours ago"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)} days ago"
        else -> "${minutes / (60 * 24 * 7)} weeks ago"
    }
}

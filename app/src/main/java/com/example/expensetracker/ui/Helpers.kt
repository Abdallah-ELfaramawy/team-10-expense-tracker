package com.example.expensetracker.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatMoney(amount: Double, currency: String): String =
    String.format(Locale.getDefault(), "%.2f %s", amount, currency)

/** IMPLICIT intent: any app that can handle ACTION_SEND text may respond. */
fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startSafely(Intent.createChooser(send, "Share via"))
}

/** IMPLICIT intent: ask a maps app to search for the nearest ATM. */
fun Context.openNearbyAtm() =
    startSafely(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=ATM")))

fun Context.startSafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "No app can handle this action", Toast.LENGTH_SHORT).show()
    }
}

package com.trilhando.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trilhando.helper.NotificationHelper

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.mostrarNotificacao(context)
    }
}
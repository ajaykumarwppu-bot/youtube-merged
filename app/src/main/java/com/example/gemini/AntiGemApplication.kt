package com.example.gemini

import android.app.Application
import com.example.gemini.data.adb.AdbConnectManager
import com.example.gemini.data.updater.PluginUpdateCheckWorker
import com.example.gemini.data.updater.UpdateCheckWorker

class AntiGemApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UpdateCheckWorker.schedule(this)
        PluginUpdateCheckWorker.schedule(this)
        // Reconnects adb when Wireless debugging turns back on (it gets a new port every time)
        AdbConnectManager.init(this)
        // NOTE: TelemetryDeck analytics intentionally removed in this fork —
        // koi usage data kahin nahi bheja jata (original developer ka telemetry
        // bhi disabled). Chaaho to apna telemetry yahan add kar sakte ho.
    }
}

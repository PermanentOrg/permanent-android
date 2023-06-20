package org.permanent.permanent

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.myFiles.RelocationType


class PermanentApplication : Application(), LifecycleObserver {
    companion object {
        @JvmStatic
        lateinit var instance: PermanentApplication
            private set

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isAppInForeground = false
        Log.i(getString(R.string.app_name), "App in background")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isAppInForeground = true
        Log.i(getString(R.string.app_name), "App in foreground")
    }

    var currentActivity: Activity? = null
    var isAppInForeground: Boolean = false

    var relocateData: Pair<MutableList<Record>, RelocationType>? = null

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        instance = this
    }
}
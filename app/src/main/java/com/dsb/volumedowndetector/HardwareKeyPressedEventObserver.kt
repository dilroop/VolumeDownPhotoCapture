package com.dsb.volumedowndetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class HardwareKeyPressedEventObserver(
    private val context: Context,
    private val onKeyPressed: () -> Unit,
    private val keyToObserve: Int
) : DefaultLifecycleObserver, BroadcastReceiver() {

    override fun onResume(owner: LifecycleOwner) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, IntentFilter(EVENT_NAME))
    }

    override fun onPause(owner: LifecycleOwner) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val keyCode = parseIntent(it)
            if (keyCode != null && keyCode == keyToObserve) {
                onKeyPressed()
            }
        }
    }

    companion object {
        private const val EVENT_NAME = "com.dsb.broadcast.VOLUME_DOWN"
        private const val INTENT_KEYCODE = "keyCode"

        private fun getIntent(keyCode: Int): Intent {
            val intent = Intent(EVENT_NAME)
            intent.putExtra(INTENT_KEYCODE, keyCode)
            return intent
        }

        private fun parseIntent(intent: Intent): Int? {
            return if (intent.hasExtra(INTENT_KEYCODE)) {
                intent.getIntExtra(INTENT_KEYCODE, 0)
            } else {
                null
            }
        }

        fun sendEvent(context: Context, keyCode: Int) {
            val intent = getIntent(keyCode)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}

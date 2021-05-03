package com.well.androidApp.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import android.content.Intent

import android.content.BroadcastReceiver

import android.os.Bundle

class CustomTabActivity : AppCompatActivity() {
    private val CUSTOM_TAB_REDIRECT_REQUEST_CODE = 2
    val CUSTOM_TAB_REDIRECT_ACTION = CustomTabActivity::class.java.simpleName + ".action_customTabRedirect"
    val DESTROY_ACTION = CustomTabActivity::class.java.simpleName + ".action_destroy"

    private var closeReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("CustomTabActivity onCreate ${intent.dataString}")
//        val intent = Intent(this, CustomTabMainActivity::class.java)
//        intent.action = CUSTOM_TAB_REDIRECT_ACTION
//        intent.putExtra(CustomTabMainActivity.EXTRA_URL, getIntent().dataString)
//
//        // these flags will open CustomTabMainActivity from the back stack as well as closing this
//        // activity and the custom tab opened by CustomTabMainActivity.
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        startActivityForResult(intent, CUSTOM_TAB_REDIRECT_REQUEST_CODE)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        println("CustomTabActivity onActivityResult $requestCode $resultCode")
//        if (resultCode == RESULT_CANCELED) {
//            // We weren't able to open CustomTabMainActivity from the back stack. Send a broadcast
//            // instead.
//            val broadcast = Intent(CUSTOM_TAB_REDIRECT_ACTION)
//            broadcast.putExtra(CustomTabMainActivity.EXTRA_URL, intent.dataString)
//            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
//
//            // Wait for the custom tab to be removed from the back stack before finishing.
//            closeReceiver = object : BroadcastReceiver() {
//                override fun onReceive(
//                    context: Context?,
//                    intent: Intent?
//                ) {
//                    finish()
//                }
//            }
//            LocalBroadcastManager.getInstance(this)
//                .registerReceiver(closeReceiver, IntentFilter(CustomTabActivity.DESTROY_ACTION))
//        }
    }
}
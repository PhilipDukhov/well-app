package com.well.modules.features.notifications

import com.well.modules.atomic.CloseableContainerImpl
import com.well.modules.models.CallInfo
import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope

internal actual class CallService actual constructor(
    services: CallServiceServices,
    parentCoroutineScope: CoroutineScope,
): CloseableContainerImpl() {
    class CallTest(private val context: Context) {

        fun place() {
            Napier.d("start placeCall")
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            val handle = createHandle()
//        val extras = Bundle()
//        extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
//        telecomManager.placeCall(Uri.fromParts("tel", "+99084", null), extras)

            telecomManager.addNewIncomingCall(handle, Bundle.EMPTY)
        }

        private fun createHandle(): PhoneAccountHandle {
            TODO()
//            val tm = context.getSystemService(Application.TELECOM_SERVICE) as TelecomManager
//            val handle = PhoneAccountHandle(ComponentName(context, com.well.androidApp.call.CallConnectionService::class.java), "uid1")
//            val account = PhoneAccount.Builder(handle, "ContactsController.formatName(self.first_name, self.last_name)")
//                .apply {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
//                    }
//                }
//                .setIcon(android.graphics.drawable.Icon.createWithContentUri("https://i.stack.imgur.com/nZvAi.jpg"))
//                .setHighlightColor(-0xd35a20)
//                .addSupportedUriScheme("well")
//                .build()
//            tm.registerPhoneAccount(account)
//            return handle
        }
    }

    actual suspend fun reportNewIncomingCall(callInfo: CallInfo) {
    }

    actual fun updateCallHasVideo(hasVideo: Boolean) {
    }

    actual suspend fun reportNewOutgoingCall(callInfo: CallInfo) {
    }

    actual fun reportOutgoingCallConnected() {
    }

}
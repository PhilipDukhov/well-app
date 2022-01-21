package com.well.androidApp.notifications

import com.well.androidApp.featureProvider
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.models.NotificationToken
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        featureProvider.accept(TopLevelFeature.Msg.UpdateNotificationToken(NotificationToken.Fcm(token)))
        println("onNewToken $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        featureProvider.accept(TopLevelFeature.Msg.RawNotificationReceived(message))

//        messagesStateFlow.update {
//            it + message
//        }

//        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
//        val extras = Bundle()
////        extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, addAccountToTelecomManager())
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            telecomManager.addNewIncomingCall(createHandle(), Bundle.EMPTY)
//        }
//        telecomManager.placeCall(Uri.fromParts("tel", "+99084", null), extras)
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun createHandle(): PhoneAccountHandle {
//        val tm = getSystemService(TELECOM_SERVICE) as TelecomManager
//        val handle = PhoneAccountHandle(ComponentName(this, CallConnectionService::class.java), "uid")
//        val account = PhoneAccount.Builder(handle, "ContactsController.formatName(self.first_name, self.last_name)")
//            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
//            .setIcon(android.graphics.drawable.Icon.createWithContentUri("https://i.stack.imgur.com/nZvAi.jpg"))
//            .setHighlightColor(-0xd35a20)
//            .addSupportedUriScheme("sip")
//            .build()
//        tm.registerPhoneAccount(account)
//        return handle
//    }
}
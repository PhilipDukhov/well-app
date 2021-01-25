package com.well.sharedMobile.networking

import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.freeze
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

class PickerTest(val controller: UIViewController) {
    fun showPicker() {
        val imagePicker = UIImagePickerController()
        controller.presentViewController(imagePicker, true) {
            NSLog("presentViewController finished")
        }
    }

    fun showPicker2() {
        GlobalScope.launch {
            NSLog("${showPikerSuspendable()}")
        }
    }

    private suspend fun showPikerSuspendable(): Int =
        suspendCancellableCoroutine { continuation ->
            NSLog("pickSystemImage suspendCancellableCoroutine")
//            GlobalScope.launch(NsQueueDispatcher(dispatch_get_main_queue())) {
            MainScope().launch {
                NSLog("pickSystemImage MainScope().launch")
                val imagePicker = UIImagePickerController()
                NSLog("pickSystemImage 1")
                @Suppress("NOTHING_TO_OVERRIDE")

                NSLog("pickSystemImage 3")
                imagePicker.delegate!!.freeze()
                NSLog("presentViewController")
                controller
                    .presentViewController(imagePicker, true) {
                        NSLog("presentViewController finished")
                        continuation.resume(10)
                    }
            }
        }
}
package com.well.androidApp.ui.fragments
//
//import android.annotation.SuppressLint
//import android.app.Activity.RESULT_OK
//import android.content.Intent
//import android.content.Intent.*
//import android.graphics.Bitmap
//import android.graphics.Bitmap.CompressFormat.JPEG
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//import android.view.*
//import android.view.View.GONE
//import android.view.View.VISIBLE
//import android.webkit.MimeTypeMap
//import androidx.appcompat.app.AlertDialog
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.GlideException
//import com.bumptech.glide.request.RequestListener
//import com.bumptech.glide.request.target.Target
//import com.well.androidApp.R
//import com.well.androidApp.databinding.FragmentShareScreenBinding
//import com.well.androidApp.ui.MainActivity
//import com.well.androidApp.ui.customViews.BaseFragment
//import com.well.serverModels.Color
//import com.well.serverModels.Point
//import com.well.sharedMobile.leafs.SharingScreen
//import com.well.sharedMobile.leafs.SharingScreen.Msg.*
//import com.well.sharedMobile.leafs.SharingScreen.State.*
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.MainScope
//import kotlinx.coroutines.launch
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import java.io.ByteArrayOutputStream
//
//class ShareScreenFragment : BaseFragment(R.layout.fragment_share_screen) {
//    private lateinit var viewBinding: FragmentShareScreenBinding  // by viewBinding(createMethod = INFLATE)
//    private lateinit var job: Job
////    private lateinit var renderDispatch: Dispatch<SharingScreen.Msg>
//
//    private lateinit var onSignOut: () -> Unit  // TODO: move to oolong
//
////    private val onlineNotifier = OnlineNotifier(MainActivity.Singleton.token!!)
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        viewBinding = FragmentShareScreenBinding.inflate(inflater, container, false)
//        return viewBinding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewBinding.apply {
//            signOut.setOnClickListener {
//                onSignOut()
//            }
////            chooseImage.setOnClickListener { onChooseImageClick() }
//            GlobalScope.launch {
//                while (true) {
//                    try {
////                        onlineNotifier.subscribeOnOnlineUsers {
////                            println("websocket $it")
////                        }
//                    } catch (e: Throwable) {
//                        println("websocket error $e")
//                    }
//                }
//            }
////            startSharing.setOnClickListener {
////                renderDispatch(BecomeHost)
////            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//// job = runtime(
//// SharingScreen.init(FirebaseAuth.getInstance().currentUser!!.uid),
//// SharingScreen.update,
//// SharingScreen.view,
//// render
//// )
////
//// sessionRef.addValueEventListener(object : ValueEventListener {
//// override fun onDataChange(snapshot: DataSnapshot) {
//// renderDispatch(
//// ServerScreenUpdated(
//// (snapshot.value as? String)?.let { Json.decodeFromString(it) }
//// )
//// )
//// }
////
//// override fun onCancelled(error: DatabaseError) {
//// println(error)
//// }
//// })
//    }
//
//    override fun onPause() {
//        super.onPause()
//        job.cancel()
//    }
//
//    override fun onActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?
//    ) {
//        super.onActivityResult(requestCode, resultCode, data)
////        when (resultCode) {
////            pickImageRequestCode, RESULT_OK -> data?.data?.let(::updateImage)
////
////            else -> return
////        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
////    private val render = { props: SharingScreen.Props, dispatch: Dispatch<SharingScreen.Msg> ->
////        viewBinding.drawingView.apply {
////            paths = props.screen?.paths
////            setOnTouchListener(
////                if (props.state == Host) {
////                    listener@ { _, event ->
////                        when (event?.action) {
////                            KeyEvent.ACTION_DOWN -> dispatch(StartPath(Color(0xFF0_000)))
////                            MotionEvent.ACTION_MOVE -> dispatch(AddPoint(Point(event.x, event.y)))
////                            KeyEvent.ACTION_UP -> return@listener false
////                        }
////                        return@listener true
////                    }
////                } else {
////                    null
////                }
////            )
////        }
////
//////        renderDispatch = dispatch
////
////        onSignOut = {
////// if (props.state == Host) {
////// sessionRef.setValue(null) { _, _ ->
////// FirebaseAuth.getInstance().signOut()
////// }
////// } else {
////// FirebaseAuth.getInstance().signOut()
////// }
////        }
////
////        when (props.state) {
////            Idle, Guest ->
////                Glide
////                    .with(requireContext())
////                    .load(props.screen?.imageURL)
////                    .into(viewBinding.imageView)
////            Host -> {
////// sessionRef.setValue(props.screen?.let { Json.encodeToJsonElement(it).toString() })
////            }
////        }
//////        updateState(props.state)
////    }
////    private val pickImageRequestCode = 7230
////    private fun onChooseImageClick() {
////        val intents = listOf(
////            Intent(ACTION_GET_CONTENT),
////            Intent(ACTION_PICK, EXTERNAL_CONTENT_URI),
////        ).apply {
////            forEach {
////                it.type = "image/*"
////            }
////        }
//////        startActivityForResult(createChooser(intents), pickImageRequestCode)
////    }
//
//    private fun updateImage(uri: Uri) {
//        Glide.with(requireContext()).apply {
//            load(uri)
//                .into(viewBinding.imageView)
//            asBitmap()
//                .load(uri)
//                .override(1125, 2436)
//                .fitCenter()
//                .listener(object : RequestListener<Bitmap> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Bitmap>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        MainScope().launch {
//                            AlertDialog
//                                .Builder(requireContext())
//                                .setTitle("Cannot open image file")
//                                .setMessage("If you think it should be supported, please contact us")
//                                .setNeutralButton("OK", null)
//                                .show()
//                        }
//                        return true
//                    }
//
//                    override fun onResourceReady(
//                        resource: Bitmap?,
//                        model: Any?,
//                        target: Target<Bitmap>?,
//                        dataSource: com.bumptech.glide.load.DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        resource?.let {
//                            GlobalScope.launch {
//                                val fileExt: String =
//                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString())
////                                upload(fileExt, it)
//                            }
//                        }
//                        return true
//                    }
//                })
//                .submit()
//        }
//    }
//
////    private fun upload(fileExt: String, bitmap: Bitmap) {
////        val stream = ByteArrayOutputStream()
////        bitmap.compress(JPEG, 100, stream)
////        renderDispatch(UploadImageData(stream.toByteArray(), fileExt))
////    }
////
////    private fun createChooser(intents: List<Intent>): Intent {
////        val chooserIntent = createChooser(intents.first(), "Select Image")
////        chooserIntent.putExtra(EXTRA_INITIAL_INTENTS, intents.drop(1).toTypedArray())
////        return chooserIntent
////    }
////
////    private fun updateState(state: SharingScreen.State) {
////        viewBinding.apply {
////            val visibleView = when (state) {
////                Host -> editingLayout
////                Guest -> null
////                Idle -> idleLayout
////            }
////            listOf(
////                editingLayout,
////                idleLayout
////            ).forEach {
////                it.visibility = if (it == visibleView) VISIBLE else GONE
////            }
////        }
////    }
//}

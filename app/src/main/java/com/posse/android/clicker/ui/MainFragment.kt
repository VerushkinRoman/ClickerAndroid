package com.posse.android.clicker.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.getSystemService
import androidx.core.graphics.get
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.posse.android.clicker.R
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.SCRIPT
import com.posse.android.clicker.databinding.FragmentMainBinding
import com.posse.android.clicker.databinding.LogItemBinding
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.utils.showToast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.abs
import kotlin.system.exitProcess

class MainFragment : Service() {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field = getSystemService()
            return field
        }

    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var backgroundLayoutParams: WindowManager.LayoutParams
    private lateinit var backgroundView: BackgroundView
    private lateinit var coordinates: Observable<BackgroundView.Point>
    private var disposable: Disposable? = null
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val clicker by lazy { Clicker(binding.root) }

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    private var touchConsumedByMove = false

    private var script = SCRIPT.values()[0]

    private val menuItems: MutableList<SCRIPT> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        _binding = FragmentMainBinding.inflate(LayoutInflater.from(this), null, false)
        initMenu()
        initBackground()
        initButtons()
        initFloatingWindow()
    }

    private fun initBackground() {
        backgroundLayoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        backgroundView = BackgroundView(this)

        windowManager?.addView(backgroundView, backgroundLayoutParams)
    }

    private fun initButtons() {
        initCloseButton()
        initStartButton()
        initStopButton()
        initEditorButton()
        initLogButton()
        initLog()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingWindow() {
        binding.root.setOnTouchListener { view, event ->
            val totalDeltaX = lastX - firstX
            val totalDeltaY = lastY - firstY

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    firstX = lastX
                    firstY = lastY
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX.toInt() - lastX
                    val deltaY = event.rawY.toInt() - lastY
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    if (abs(totalDeltaX) >= 5 || abs(totalDeltaY) >= 5) {
                        if (event.pointerCount == 1) {
                            layoutParams.x += deltaX
                            layoutParams.y += deltaY
                            backgroundLayoutParams.x += deltaX
                            backgroundLayoutParams.y += deltaY
                            touchConsumedByMove = true
                            windowManager?.apply {
                                updateViewLayout(binding.root, layoutParams)
                                updateViewLayout(backgroundView, backgroundLayoutParams)
                            }
                        } else {
                            touchConsumedByMove = false
                        }
                    } else {
                        touchConsumedByMove = false
                    }
                }
                else -> {
                }
            }
            touchConsumedByMove
        }
        initWindowParams()
        windowManager?.addView(binding.root, layoutParams)
    }

    private fun initWindowParams() {
        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    private fun initStopButton() {
        binding.stopButton.setOnClickListener {
            clicker.stop()
        }
    }

    private fun initStartButton() {
        binding.startButton.setOnClickListener {
            clicker.start(script)
        }
    }

    private fun initCloseButton() {
        binding.closeButton.setOnClickListener { dismiss() }
    }

    private fun initEditorButton() {
        coordinates = backgroundView.getData()
        binding.editorButton.setOnClickListener {
            binding.editorLayout.isVisible = !binding.editorLayout.isVisible
            binding.logScrollView.isVisible = false
            changeBackgroundView()
        }
    }

    private fun changeBackgroundView() {
        if (binding.editorLayout.isVisible) {
            backgroundLayoutParams.apply {
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            disposable = coordinates
                .toFlowable(BackpressureStrategy.MISSING)
                .onBackpressureDrop {
                    showToast("Dropped click x:${it.x} y:${it.y}")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation(), false, 1)
                .subscribe { point ->
                    val picture = Screenshot.get()
                    picture?.let {
                        binding.root.post {
                            binding.savedColor.text = it[point.x, point.y].toString()
                            binding.savedX.text = point.x.toString()
                            binding.savedY.text = point.y.toString()
                            binding.colorLabel.setBackgroundColor(it[point.x, point.y])
                        }
                    }
                }
        } else {
            backgroundLayoutParams.apply {
                flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            disposable?.dispose()
        }

        windowManager?.apply {
            updateViewLayout(backgroundView, backgroundLayoutParams)
        }
    }

    private fun initLogButton() {
        binding.logButton.setOnClickListener {
            binding.logScrollView.isVisible = !binding.logScrollView.isVisible
            binding.editorLayout.isVisible = false
            changeBackgroundView()
            binding.logScrollView.post { binding.logScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun initLog() {
        val log = MyLog.get()
        log
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val textView = LogItemBinding.inflate(LayoutInflater.from(this)).root
                textView.text = it
                binding.logView.addView(textView)
                binding.logScrollView.post { binding.logScrollView.fullScroll(View.FOCUS_DOWN) }
            }
    }

    private fun initMenu() {
        initMenuItems()
        val adapter = ArrayAdapter(this, R.layout.list_item, menuItems)
        (binding.chooseLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        binding.chooseLayout.editText?.setText(menuItems[0].text)

        binding.chooseLayout.editText?.doOnTextChanged { text, _, _, _ ->
            SCRIPT.values().forEach {
                if (it.text == text) script = it
            }
        }
    }

    private fun initMenuItems() {
        SCRIPT.values().forEach {
            menuItems.add(it)
        }
    }

    private fun dismiss() {
        exitProcess(0)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}
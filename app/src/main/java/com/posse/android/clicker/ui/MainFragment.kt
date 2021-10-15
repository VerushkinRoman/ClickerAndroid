package com.posse.android.clicker.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.posse.android.clicker.R
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.SCRIPT
import com.posse.android.clicker.databinding.FragmentMainBinding
import com.posse.android.clicker.databinding.LogItemBinding
import com.posse.android.clicker.model.MyLog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.system.exitProcess

class MainFragment : Service() {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field = getSystemService()
            return field
        }

    private lateinit var layoutParams: WindowManager.LayoutParams

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
        initButtons()
        initFloatingWindow()

        readInput()

    }

    private fun readInput() {
        Thread {
            try {
                val process =
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -t /dev/input/event2"))
                val input = InputStreamReader(process.inputStream)
                var s: String? = ""
                val br = BufferedReader(input)
                val xPrefix = "0003 0035 "
                val yPrefix = "0003 0036 "
                val end = "0003 0039 ffffffff"
                var lastX = ""
                var lastY = ""
                while ((s != null) && Thread.currentThread().isAlive) {
                    s = br.readLine()
                    if (s.contains(xPrefix)) lastX = s
                    if (s.contains(yPrefix)) lastY = s
                    if (s.contains(end)){
                        Log.d("touch", "X: ${lastX.substringAfterLast(xPrefix).toLong(16)}")
                        Log.d("touch", "Y: ${lastY.substringAfterLast(yPrefix).toLong(16)}")
                        Log.d("touch", "========================")
                    }
//                    Log.d("touch", s)
                }
                input.close()
                process.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
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
                            touchConsumedByMove = true
                            windowManager?.apply {
                                updateViewLayout(binding.root, layoutParams)
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
        binding.editorButton.setOnClickListener {
            binding.editorLayout.isVisible = !binding.editorLayout.isVisible
            binding.logScrollView.isVisible = false
        }
    }

    private fun initLogButton() {
        binding.logButton.setOnClickListener {
            binding.logScrollView.isVisible = !binding.logScrollView.isVisible
            binding.editorLayout.isVisible = false
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
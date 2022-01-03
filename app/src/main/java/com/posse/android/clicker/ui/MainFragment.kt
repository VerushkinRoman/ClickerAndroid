package com.posse.android.clicker.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.getSystemService
import androidx.core.graphics.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.posse.android.clicker.R
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.Game
import com.posse.android.clicker.core.Script
import com.posse.android.clicker.databinding.FragmentMainBinding
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.ui.adapter.MainAdapter
import com.posse.android.clicker.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import kotlin.math.abs
import kotlin.system.exitProcess

class MainFragment : Service() {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field = getSystemService()
            return field
        }

    private val preferences: SharedPreferences by inject()
    private val log: MyLog by inject()
    private val screenshot: Screenshot by inject()

    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var backgroundLayoutParams: WindowManager.LayoutParams
    private lateinit var backgroundView: BackgroundView
    private lateinit var coordinates: StateFlow<BackgroundView.Point>
    private val screenShotScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var clicker: Clicker? = null

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    private var touchConsumedByMove = false
    private var lasTimeExpandTouched = System.currentTimeMillis()

    private var script: Script = Game.Market
    private val adapter: MainAdapter = MainAdapter()

    private var defaultButton: ButtonDimens? = null

    override fun onCreate() {
        super.onCreate()
        _binding = FragmentMainBinding.inflate(LayoutInflater.from(this), null, false)
        initMenu()
        initBackground()
        initButtons()
        initAdapter()
        initLog()
        initFloatingWindow()
        openLastError()
        initClicker()
    }

    private fun initClicker() {

        val animator: Animator? = if (preferences.animator) Animator(binding.root) else null
        var msg = preferences.telegramMsg
        if (msg.isNullOrEmpty()) msg = " "
        var loginMsg = preferences.loginText
        if (loginMsg.isNullOrEmpty()) loginMsg = " "

        clicker = Clicker(msg, loginMsg, animator, log, screenshot) { isRunning ->
            val color = if (isRunning) android.R.color.holo_green_light
            else android.R.color.darker_gray
            binding.startButton.post {
                binding.startButton.setBackgroundColor(binding.root.context.getColor(color))
            }
        }
    }

    private fun openLastError() {
        val error = preferences.lastError
        if (!error.isNullOrEmpty()) {
            clicker?.stop()
            binding.logRecyclerView.visibility = View.VISIBLE
            adapter.add(error)
            preferences.lastError = null
        }
    }

    private fun initBackground() {
        backgroundLayoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE
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
        initExpandButton()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initExpandButton() {
        binding.expandButton.setOnTouchListener { _, event ->

            val totalDeltaX = lastX - firstX
            val totalDeltaY = lastY - firstY

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    lasTimeExpandTouched = System.currentTimeMillis()
                    touchConsumedByMove = false
                }

                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - lasTimeExpandTouched < 500) {
                        preferences.expanded = !binding.logButton.isVisible
                        if (binding.logButton.isVisible) {
                            collapseWindow()
                        } else {
                            expandWindow()
                        }
                        touchConsumedByMove = true
                    } else {
                        preferences.windowXPosition = layoutParams.x
                        preferences.windowYPosition = layoutParams.y
                        touchConsumedByMove = false
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    changeRootPosition(event, totalDeltaX, totalDeltaY)
                }
            }
            touchConsumedByMove
        }
    }

    private fun expandWindow() {
        expandButton(binding.startButton, getText(R.string.start))
        expandButton(binding.stopButton, getText(R.string.stop))
        binding.logButton.visibility = View.VISIBLE
        binding.editorButton.visibility = View.VISIBLE
        binding.chooseLayout.visibility = View.VISIBLE
    }

    private fun collapseWindow() {
        collapseButton(binding.startButton, ">")
        collapseButton(binding.stopButton, "O")
        binding.logButton.visibility = View.GONE
        binding.editorButton.visibility = View.GONE
        binding.chooseLayout.visibility = View.GONE
        binding.logRecyclerView.visibility = View.GONE
        binding.editorLayout.visibility = View.GONE
        changeBackgroundView()
    }

    private fun collapseButton(button: MaterialButton, text: CharSequence) {
        if (defaultButton == null) {
            defaultButton = ButtonDimens(
                button.minWidth,
                button.minimumWidth,
                button.minHeight,
                button.minimumHeight
            )
        }
        button.minimumHeight = 0
        button.minimumWidth = 0
        button.minHeight = 0
        button.minWidth = 0
        button.text = text
    }

    private fun expandButton(button: MaterialButton, text: CharSequence) {
        button.minimumWidth = defaultButton?.minimumWidth ?: 132
        button.minimumHeight = defaultButton?.minimumHeight ?: 72
        button.minWidth = defaultButton?.minWidth ?: 132
        button.minHeight = defaultButton?.minHeight ?: 72
        button.text = text
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
                    preferences.windowXPosition = layoutParams.x
                    preferences.windowYPosition = layoutParams.y
                    view.performClick()
                }
                MotionEvent.ACTION_MOVE -> {
                    changeRootPosition(event, totalDeltaX, totalDeltaY)
                }
                else -> {
                    touchConsumedByMove = false
                }
            }
            touchConsumedByMove
        }
        initWindowParams()
        windowManager?.addView(binding.root, layoutParams)
        if (preferences.expanded) expandWindow() else collapseWindow()
    }

    private fun changeRootPosition(
        event: MotionEvent,
        totalDeltaX: Int,
        totalDeltaY: Int
    ) {
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

    private fun initWindowParams() {
        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = preferences.windowXPosition
            y = preferences.windowYPosition
        }
    }

    private fun initStopButton() {
        binding.stopButton.setOnClickListener {
            clicker?.stop()
        }
    }

    private fun initStartButton() {
        binding.startButton.setOnClickListener {
            clicker?.start(script)
        }
    }

    private fun initCloseButton() {
        binding.closeButton.setOnClickListener { dismiss() }
    }

    private fun initEditorButton() {
        coordinates = backgroundView.getData()
        binding.editorButton.setOnClickListener {
            binding.editorLayout.isVisible = !binding.editorLayout.isVisible
            binding.logRecyclerView.isVisible = false
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
            screenShotScope.launch {
                coordinates
                    .collectLatest { point ->
                        val picture = screenshot.get()
                        binding.root.post {
                            binding.savedColor.text = picture[point.x, point.y].toString()
                            binding.savedX.text = point.x.toString()
                            binding.savedY.text = point.y.toString()
                            binding.colorLabel.setBackgroundColor(picture[point.x, point.y])
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
            screenShotScope.coroutineContext.cancelChildren()
        }

        windowManager?.apply {
            updateViewLayout(backgroundView, backgroundLayoutParams)
        }
    }

    private fun initLogButton() {
        binding.logButton.setOnClickListener {
            binding.logRecyclerView.isVisible = !binding.logRecyclerView.isVisible
            binding.editorLayout.isVisible = false
            changeBackgroundView()
            if (binding.logRecyclerView.isVisible) binding.logRecyclerView.post {
                binding.logRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun initAdapter() {
        binding.logRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.logRecyclerView.adapter = adapter
    }

    private fun initLog() {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            log.get().collect {
                adapter.add(it)
                binding.logRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun initMenu() {
        val menuItems: MutableList<String> = mutableListOf()
        Game.values().forEach { game ->
            if (game.game.naming == preferences.lastSelectedGame) menuItems.add(game.script)
        }
        val adapter = ArrayAdapter(this, R.layout.list_item, menuItems)
        (binding.chooseLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        if (!menuItems.contains(preferences.lastScript)) preferences.lastScript = menuItems.first()
        binding.script.setText(preferences.lastScript, false)
        binding.script.setOnItemClickListener { _, _, position, _ ->
            val element = adapter.getItem(position)
            Game.values().forEach { game ->
                if (game.script == element) script = game
            }
        }
    }

    private fun dismiss() {
        screenShotScope.coroutineContext.cancelChildren()
        exitProcess(0)
    }

    override fun onDestroy() {
        screenShotScope.coroutineContext.cancelChildren()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    data class ButtonDimens(
        val minWidth: Int,
        val minimumWidth: Int,
        val minHeight: Int,
        val minimumHeight: Int
    )

    fun interface StartButtonChanger {
        fun changeColor(isRunning: Boolean)
    }
}
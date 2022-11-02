package com.posse.android.clicker.core

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.ScreenShotType
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.FifaMobile
import com.posse.android.clicker.scripts.LooneyTunes
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.ui.Animator
import com.posse.android.clicker.ui.Animator.Companion.ANIMATION_DURATION
import com.posse.android.clicker.ui.MainFragment
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.io.OutputStreamWriter

class Clicker(
    private val msg: String,
    private val loginMsg: String,
    private val animator: Animator?,
    private val log: MyLog,
    private val screenshot: Screenshot,
    private val startButtonChanger: MainFragment.StartButtonChanger
) : KoinComponent {

    private val telegram: Telegram by inject()
    private val outputStream: OutputStreamWriter by inject()
    private val textRecognizer: TextRecognizer by inject()
    private var clickerJob: Job? = null
    private var telegramJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var x: Int = 0
    private var y: Int = 0

    fun start(script: ScriptProps) {
        if (clickerJob == null) {
            startButtonChanger.changeColor(true)
            clickerJob = coroutineScope.launch {
                when (Games.values().find { it.gameScripts.contains(script) }) {
                    Games.FifaMobile -> FifaMobile(
                        clicker = this@Clicker,
                        script = script as FifaGameScript,
                        msg = msg,
                        loginMsg = loginMsg
                    ).run()
                    Games.LooneyTunes -> LooneyTunes(
                        clicker = this@Clicker,
                        script = script as LooneyGameScript,
                        msg = msg,
                        loginMsg = loginMsg
                    ).run()
                    else -> throw RuntimeException("Unknown Script: ${script.scriptName}")
                }
            }
        }
    }

    fun stop() {
        coroutineScope.coroutineContext.cancelChildren()
        animator?.stop()
        startButtonChanger.changeColor(false)
        clickerJob?.cancel()
        clickerJob = null
        stopTelegram()
    }

    suspend fun click(x: Int, y: Int) {
        this.x = x
        this.y = y
        animator?.animateClick(x, y)
        sendTouch(x, y)
        delay(ANIMATION_DURATION * 2)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun sendTouch(x: Int, y: Int) {
        coroutineScope {
            launch {
                outputStream.write("$EVENT 1 330 1\n")
                outputStream.write("$EVENT 3 53 $x\n")
                outputStream.write("$EVENT 3 54 $y\n")
                outputStream.write("$EVENT 0 2 0\n")
                outputStream.write("$EVENT 0 0 0\n")
                outputStream.write("$EVENT 0 2 0\n")
                outputStream.write("$EVENT 0 0 0\n")
                outputStream.flush()
            }
        }
    }

    fun recentButton() = sendCommand("input keyevent KEYCODE_APP_SWITCH\n")

    fun backButton() = sendCommand("input keyevent KEYCODE_BACK\n")

    private fun sendCommand(command: String) {
        try {
            outputStream.write(command)
            outputStream.flush()
        } catch (e: IOException) {
            putLog(e.toString())
        }
    }

    suspend fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long
    ) {
        coroutineScope {
            launch {
                sendCommand("input swipe $startX $startY $endX $endY $duration\n")
                delay(400)
                animator?.animateDrag(startX, startY, endX, endY, duration)
            }
        }
    }

    fun startTelegram(msg: String, delay: Int, repeat: Boolean) {
        telegram.apply {
            this.msg = msg
            this.delay = delay
            this.repeat = repeat
        }
        if (telegramJob == null) {
            telegramJob = coroutineScope.launch { telegram.run() }
        } else telegram.countdown = 0
    }

    fun stopTelegram() {
        telegramJob?.cancel()
        telegramJob = null
    }

    fun getScreen(screenShotType: ScreenShotType): Bitmap? {
        return when (screenShotType) {
            ScreenShotType.Full -> screenshot.get()
            ScreenShotType.WithHole -> screenshot.getWithHole(
                cx = x.toFloat(),
                cy = y.toFloat(),
                radius = Animator.SIZE * Animator.MAX_SCALE
            )
        }
    }

    fun getPixelColor(screen: Bitmap?, x: Int, y: Int) = screen?.getPixel(x, y)

    suspend fun getPrice(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int
    ): Int = withContext(Dispatchers.Default) {
        var result = -1
        val bitmap = getScreen(ScreenShotType.Full) ?: return@withContext result
        val cropImage = Bitmap.createBitmap(
            bitmap,
            startX,
            startY,
            endX - startX,
            endY - startY
        )
        val image = InputImage.fromBitmap(cropImage, 0)
        var readyResult = false
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                putLog("recognized text: ${visionText.text}")
                try {
                    result = visionText
                        .textBlocks
                        .first()
                        .lines
                        .first()
                        .text
                        .replace(
                            regex = "\\s".toRegex(),
                            replacement = ""
                        )
                        .toInt()
                } catch (e: Exception) {
                    e.message?.let { putLog(it) }
                }
                readyResult = true
            }
            .addOnFailureListener { e ->
                putLog(e.message.toString())
                readyResult = true
            }

        while (!readyResult) {
            delay(100)
        }

        result
    }

    fun getPixelCount(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        color: Int
    ): Int {
        var result = 0
        val screen = getScreen(ScreenShotType.Full) ?: return 0
        for (x in startX..endX) {
            for (y in startY..endY) {
                if (screen.getPixel(x, y) == color) result++
            }
        }
        return result
    }

    fun putLog(message: String) = coroutineScope.launch { log.add(message) }

    companion object {
        const val CLICK_DURATION = 1
        private const val EVENT = "sendevent /dev/input/event4"
    }
}
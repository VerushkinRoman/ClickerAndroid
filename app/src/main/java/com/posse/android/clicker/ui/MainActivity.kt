package com.posse.android.clicker.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.posse.android.clicker.R
import com.posse.android.clicker.core.SCRIPT
import com.posse.android.clicker.databinding.ActivityMainBinding
import com.posse.android.clicker.service.MyAccessibilityService
import com.posse.android.clicker.utils.showToast

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val clicker by lazy { Intent(this, MainFragment::class.java) }

    private val permissions = ActivePermissions(
        service = false,
        overlay = false,
        screenResolution = false
    )

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (!Settings.canDrawOverlays(this)) {
                    showToast(getString(R.string.overlay_not_granted))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkDirectRun()
    }

    private fun checkDirectRun() {
        if (Settings.canDrawOverlays(this)
            && isAccessibilitySettingsOn()
            && checkScreenResolution()
        ) {
            showMainFragment()
            finish()
        }
    }

    private fun initButtons() {
        initOverlayButton()
        initServiceButton()
        initStartButton()
    }

    private fun initStartButton() {
        if (permissions.service && permissions.overlay && permissions.screenResolution) {
            binding.runButton.isEnabled = true
            binding.runButton.setOnClickListener {
                showMainFragment()
                finish()
            }
        } else binding.runButton.isEnabled = false
    }

    private fun initOverlayButton() {
        if (Settings.canDrawOverlays(this)) {
            binding.overlayButton.isEnabled = false
            binding.overlayText.text = getString(R.string.overlay_granted)
            permissions.overlay = true
        } else {
            binding.overlayButton.isEnabled = true
            binding.overlayText.text = getString(R.string.overlay_not_granted)
            permissions.overlay = false
            binding.overlayButton.setOnClickListener {
                startManageDrawOverlaysPermission()
            }
        }
    }

    private fun initServiceButton() {
        if (isAccessibilitySettingsOn()) {
            binding.serviceButton.isEnabled = false
            binding.serviceText.text = getString(R.string.service_running)
            permissions.service = true
        } else {
            binding.serviceButton.isEnabled = true
            binding.serviceText.text = getString(R.string.service_not_running)
            permissions.service = false
            binding.serviceButton.setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkScreenResolution()
        initButtons()
    }

    private fun checkScreenResolution(): Boolean {
        binding.runText.visibility = View.VISIBLE
        val size = Point()
        val display = windowManager.defaultDisplay
        display.getSize(size)
        val width: Int = size.x
        val height: Int = size.y
        SCRIPT.values().forEach {
            if ((it.height == height || (it.height == width))
                && (it.width == width || it.width == height)
            ) {
                binding.runText.visibility = View.GONE
                permissions.screenResolution = true
                return true
            }
        }
        return false
    }

    private fun showMainFragment() {
        startService(clicker)
    }

    private fun startManageDrawOverlaysPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${applicationContext.packageName}")
        )
        resultLauncher.launch(intent)
    }

    private fun isAccessibilitySettingsOn(): Boolean {
        var accessibilityEnabled = 0
        val service = packageName + "/" + MyAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
        }
        val stringColonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                stringColonSplitter.setString(settingValue)
                while (stringColonSplitter.hasNext()) {
                    val accessibilityService = stringColonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        } else {
            return false
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private class ActivePermissions(
        var service: Boolean,
        var overlay: Boolean,
        var screenResolution: Boolean
    )
}
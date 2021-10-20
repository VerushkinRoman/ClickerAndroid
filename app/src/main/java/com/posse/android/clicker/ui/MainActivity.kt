package com.posse.android.clicker.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.posse.android.clicker.databinding.ActivityMainBinding
import com.posse.android.clicker.service.MyAccessibilityService
import com.posse.android.clicker.utils.showToast

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val clicker by lazy { Intent(this, MainFragment::class.java) }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (Settings.canDrawOverlays(this)) {
                    showMainFragment()
                } else {
                    showToast("Permission is not granted!")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButtons()

        if (Settings.canDrawOverlays(this)) {

        } else {
            startManageDrawOverlaysPermission()
        }

        if (!isAccessibilitySettingsOn()) startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun initButtons() {
        initStartButton()
    }

    private fun initStartButton() {
        binding.runButton.setOnClickListener {
            showMainFragment()
            finish()
        }
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
}
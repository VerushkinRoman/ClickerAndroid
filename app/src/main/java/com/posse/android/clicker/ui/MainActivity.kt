package com.posse.android.clicker.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.posse.android.clicker.R
import com.posse.android.clicker.core.Games
import com.posse.android.clicker.databinding.ActivityMainBinding
import com.posse.android.clicker.utils.lastSelectedGame
import com.posse.android.clicker.utils.showToast
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val preferences: SharedPreferences by inject()

    private val clicker by lazy { Intent(this, MainFragment::class.java) }

    private val permissions = ActivePermissions(
        overlay = false,
        screenResolution = false,
        root = false
    )

    private val menuItems: MutableList<String> = mutableListOf()

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGameSelector()
    }

    private fun initGameSelector() {
        menuItems.addAll(Games.values().map { it.gameName })
        val adapter = ArrayAdapter(this, R.layout.list_item, menuItems)
        (binding.gameLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        val currentGame = Games.values().find { it.gameName == preferences.lastSelectedGame }
        val adapterText = currentGame?.gameName ?: getString(R.string.choose_game)
        binding.game.setText(adapterText, false)
        binding.game.setOnItemClickListener { _, _, position, _ ->
            preferences.lastSelectedGame = adapter.getItem(position)
            checkScreenResolution()
        }
    }

    private fun initButtons() {
        initOverlayButton()
        initSettingsButton()
        initRootButton()
        initStartButton()
    }

    private fun initSettingsButton() {
        binding.settingsButton.setOnClickListener {
            SettingsFragment.newInstance().show(supportFragmentManager, null)
        }
    }

    private fun initRootButton() {
        if (checkRoot()) {
            permissions.root = true
            binding.superUserText.visibility = View.GONE
            binding.superUserButton.isEnabled = false
        } else {
            permissions.root = false
            binding.superUserText.visibility = View.VISIBLE
            binding.superUserButton.isEnabled = true
            binding.superUserButton.setOnClickListener {
                initRootButton()
            }
        }
    }

    private fun checkRoot(): Boolean {
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su")
                || canExecuteCommand("which su")
    }

    private fun canExecuteCommand(command: String): Boolean {
        var executedSuccessfully = false
        try {
            Runtime.getRuntime().exec(command)
            executedSuccessfully = true
        } catch (e: Exception) {
        }
        return executedSuccessfully
    }

    private fun initStartButton() {
        if (permissions.overlay
            && permissions.screenResolution
            && permissions.root
        ) {
            binding.runButton.setOnClickListener {
                showMainFragment()
                finish()
            }
        } else binding.runButton.isEnabled = false
    }

    private fun initOverlayButton() = with(binding) {
        if (Settings.canDrawOverlays(this@MainActivity)) {
            overlayButton.isEnabled = false
            overlayText.text = getString(R.string.overlay_granted)
            permissions.overlay = true
        } else {
            overlayButton.isEnabled = true
            overlayText.text = getString(R.string.overlay_not_granted)
            permissions.overlay = false
            overlayButton.setOnClickListener {
                startManageDrawOverlaysPermission()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkScreenResolution()
        initButtons()
    }

    @Suppress("DEPRECATION")
    private fun checkScreenResolution(): Boolean {
        binding.runText.visibility = View.VISIBLE
        binding.runButton.isEnabled = false
        permissions.screenResolution = false
        val size = Point()
        val display = windowManager.defaultDisplay
        display.getSize(size)
        val width: Int = size.x
        val height: Int = size.y
        val resolutions = listOf(width, height)

        val currentSelectedGame =
            Games.values().find { it.gameName == preferences.lastSelectedGame } ?: return false

        currentSelectedGame.gameScripts.forEach { gameScript ->
            if (resolutions.containsAll(listOf(gameScript.width, gameScript.height))) {
                binding.runText.visibility = View.INVISIBLE
                binding.runButton.isEnabled = true
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

    private class ActivePermissions(
        var overlay: Boolean,
        var screenResolution: Boolean,
        var root: Boolean
    )
}
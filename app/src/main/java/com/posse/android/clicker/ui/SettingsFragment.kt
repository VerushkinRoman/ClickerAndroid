package com.posse.android.clicker.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.posse.android.clicker.databinding.SettingsLayoutBinding
import com.posse.android.clicker.utils.*
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class SettingsFragment : DialogFragment(), KoinComponent {

    private var _binding: SettingsLayoutBinding? = null
    private val binding get() = _binding!!

    private val preferences: SharedPreferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWindowSize()
        initErrorText()
        initLoginText()
        initChatID()
        initBotToken()
        initAnimator()
        initCloseButton()
    }

    private fun initCloseButton() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setWindowSize() {
        dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    private fun initBotToken() = initTextLayout(binding.botToken, preferences.botToken.orEmpty()) {
        preferences.botToken = it
    }

    private fun initChatID() = initTextLayout(binding.chatId, preferences.chatID.toString()) {
        preferences.chatID = it.toLong()
    }

    private fun initLoginText() =
        initTextLayout(binding.loginMessage, preferences.loginText.orEmpty()) {
            preferences.loginText = it
        }

    private fun initErrorText() =
        initTextLayout(binding.errorMessage, preferences.telegramMsg.orEmpty()) {
            preferences.telegramMsg = it
        }

    private fun initTextLayout(
        textInputLayout: TextInputLayout,
        textFieldString: String,
        callback: (String) -> Unit
    ) {
        textInputLayout.editText?.setText(textFieldString)
        textInputLayout.setEndIconOnClickListener {
            textInputLayout.editText?.setText("")
            textInputLayout.isEndIconVisible = false
        }
        textInputLayout.editText?.doOnTextChanged { text, _, _, count ->
            textInputLayout.isEndIconVisible = count > 0
            callback(text.toString())
        }
    }

    private fun initAnimator() {
        binding.animationBox.isChecked = preferences.animator
        binding.animationBox.setOnCheckedChangeListener { _, isChecked ->
            preferences.animator = isChecked
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
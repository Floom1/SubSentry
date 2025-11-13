package com.example.subsentry.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.subsentry.R
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.databinding.FragmentNotificationSettingsBinding
import com.example.subsentry.utils.NotificationManager
import com.example.subsentry.utils.NotificationPreferences
import com.example.subsentry.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationPreferences: NotificationPreferences
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация sessionManager и notificationPreferences
        sessionManager = SessionManager(requireContext())
        notificationPreferences = NotificationPreferences(requireContext())

        // Настройка Toolbar
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // Обработчик кнопки "назад"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        // Обработчик для переключателя уведомлений
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            notificationPreferences.setNotificationsEnabled(isChecked)
            updateCheckIntervalEnabled(isChecked)
        }

        // Обработчик для кнопки "Проверить сейчас"
        binding.checkNowButton.setOnClickListener {
            checkNotificationsNow()
        }
    }

    private fun loadSettings() {
        // Загрузка сохраненных настроек
        val isNotificationsEnabled = notificationPreferences.isNotificationsEnabled()
        binding.notificationsSwitch.isChecked = isNotificationsEnabled
        updateCheckIntervalEnabled(isNotificationsEnabled)

        val checkInterval = notificationPreferences.getCheckInterval()
        binding.checkIntervalInput.setText(checkInterval.toString())
    }

    private fun updateCheckIntervalEnabled(isEnabled: Boolean) {
        binding.checkIntervalLayout.isEnabled = isEnabled
        binding.checkIntervalInput.isEnabled = isEnabled
        binding.demoModeText.isEnabled = isEnabled
    }

    private fun validateCheckInterval(): Boolean {
        val intervalText = binding.checkIntervalInput.text?.toString()?.trim() ?: ""

        if (intervalText.isEmpty()) {
            binding.checkIntervalLayout.error = "Введите значение"
            return false
        }

        val interval = try {
            intervalText.toInt()
        } catch (e: NumberFormatException) {
            binding.checkIntervalLayout.error = "Введите число"
            return false
        }

        if (interval < 1) {
            binding.checkIntervalLayout.error = "Минимальное значение: 1 минута"
            return false
        }

        binding.checkIntervalLayout.error = null
        return true
    }

    private fun saveSettings() {
        if (!validateCheckInterval()) {
            return
        }

        val interval = binding.checkIntervalInput.text?.toString()?.toInt() ?: 60
        notificationPreferences.setCheckInterval(interval)

        Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
    }

    private fun checkNotificationsNow() {
        binding.checkNowButton.isEnabled = false
        binding.checkNowButton.text = "Проверка..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.checkNotifications()

                if (response.isSuccessful) {
                    val notificationsCreated = response.body()?.notificationsCreated ?: 0

                    withContext(Dispatchers.Main) {
                        if (notificationsCreated > 0) {
                            Toast.makeText(
                                requireContext(),
                                "Обнаружено $notificationsCreated уведомлений",
                                Toast.LENGTH_LONG
                            ).show()

                            val manager = NotificationManager(requireContext())
                            manager.showNotification(
                                id = System.currentTimeMillis().toInt(),
                                title = "Новые уведомления",
                                message = "Найдены подписки для оплаты"
                            )
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Нет новых уведомлений",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка проверки: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.checkNowButton.isEnabled = true
                    binding.checkNowButton.text = "Проверить сейчас"
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        // Сохраняем настройки при уходе с экрана
        if (isAdded) {
            saveSettings()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
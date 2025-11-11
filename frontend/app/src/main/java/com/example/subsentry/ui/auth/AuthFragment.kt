package com.example.subsentry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.subsentry.R
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.databinding.FragmentAuthBinding
import com.example.subsentry.models.LoginRequest
import com.example.subsentry.models.User
import com.example.subsentry.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Если пользователь уже вошел - переходим на главную
        if (sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.mainFragment)
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // Переключение на регистрацию
        binding.switchAuthText.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        // Кнопка входа
        binding.loginButton.setOnClickListener {
            validateAndLogin()
        }
    }

    private fun isFragmentActive(): Boolean {
        return viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    private fun validateAndLogin() {
        if (!isFragmentActive()) {
            return
        }
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (username.isEmpty()) {
            binding.usernameLayout.error = "Введите никнейм"
            return
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = "Пароль должен быть не менее 6 символов"
            return
        }

        binding.usernameLayout.error = null
        binding.passwordLayout.error = null
        binding.loginButton.isEnabled = false

        login(username, password)
    }

    private fun login(username: String, password: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.loginUser(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful) {
                    val user = response.body()
                    val cookies = response.headers()["Set-Cookie"] ?: ""

                    if (user != null) {
                        withContext(Dispatchers.Main) {
                            if (isFragmentActive()) {
                                saveSession(user, cookies)
                                navigateToMain()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            if (isFragmentActive()) {
                                showError("Ошибка при получении данных пользователя")
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (isFragmentActive()) {
                            showError("Неверный никнейм или пароль")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFragmentActive()) {
                        showError("Ошибка сети: ${e.message}")
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    if (isFragmentActive()) {
                        binding.loginButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun saveSession(user: User, cookies: String) {
        // Извлекаем sessionid из cookies
        val sessionCookie = cookies.split(";").firstOrNull { it.contains("sessionid=") } ?: ""

        sessionManager.login(
            userId = user.id,
            username = user.username,
            email = user.email,
            sessionCookie = sessionCookie
        )
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.mainFragment)
        Toast.makeText(requireContext(), "Добро пожаловать!", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
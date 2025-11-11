package com.example.subsentry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.subsentry.R
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.databinding.FragmentRegisterBinding
import com.example.subsentry.models.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Переключение на вход
        binding.switchToLoginText.setOnClickListener {
            findNavController().navigate(R.id.authFragment)
        }

        // Кнопка регистрации
        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun validateAndRegister() {
        val email = binding.emailInput.text.toString().trim()
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Введите корректный email"
            isValid = false
        }

        if (username.isEmpty() || username.length < 3) {
            binding.usernameLayout.error = "Никнейм должен быть не менее 3 символов"
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = "Пароль должен быть не менее 6 символов"
            isValid = false
        }

        if (confirmPassword != password) {
            binding.confirmPasswordLayout.error = "Пароли не совпадают"
            isValid = false
        }

        if (!isValid) return

        binding.emailLayout.error = null
        binding.usernameLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null
        binding.registerButton.isEnabled = false

        register(email, username, password)
    }

    private fun register(email: String, username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.registerUser(
                    RegisterRequest(username, email, password)
                )

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Регистрация успешна! Войдите в аккаунт", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.authFragment)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка регистрации: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.registerButton.isEnabled = true
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
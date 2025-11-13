package com.example.subsentry.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.subsentry.MainActivity
import com.example.subsentry.R
import com.example.subsentry.databinding.FragmentProfileBinding
import com.example.subsentry.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupUI()
        loadUserProfile()
    }

    private fun setupUI() {
        // Обработчик для кнопки настроек уведомлений
        binding.notificationsSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationSettingsFragment)
        }

        // Обработчик для кнопки выхода
        binding.logoutButton.setOnClickListener {
            (requireActivity() as MainActivity).logout()
        }
    }

    private fun loadUserProfile() {
        binding.usernameValue.text = sessionManager.getUsername()
        binding.emailValue.text = sessionManager.getEmail()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

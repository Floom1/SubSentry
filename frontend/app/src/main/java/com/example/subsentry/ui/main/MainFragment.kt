package com.example.subsentry.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.subsentry.R
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.databinding.FragmentMainBinding
import com.example.subsentry.models.Subscription
import com.example.subsentry.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var subscriptionAdapter: SubscriptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val swipeRefreshLayout = binding.swipeRefresh
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    private fun setupUI() {
        // Настройка RecyclerView
        subscriptionAdapter = SubscriptionAdapter(
            onEditClick = { subscription ->
                // Перейти к экрану редактирования
                findNavController().navigate(
                    R.id.addFragment,
                    Bundle().apply {
                        putInt("subscriptionId", subscription.id)
                        putBoolean("isEditing", true)
                    }
                )
            },
            onDeleteClick = { subscription ->
                // Удалить подписку
                deleteSubscription(subscription)
            }
        )

        binding.subscriptionsRecyclerView.apply {
            adapter = subscriptionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Исправленная настройка pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun deleteSubscription(subscription: Subscription) {
        // Добавляем диалог подтверждения
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить подписку \"${subscription.name}\"?")
            .setPositiveButton("Да") { _, _ ->
                // Запускаем удаление только после подтверждения
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.apiService.deleteSubscription(subscription.id)
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Подписка удалена", Toast.LENGTH_SHORT).show()
                                viewModel.refreshData() // Обновляем данные
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Ошибка удаления подписки: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private fun setupObservers() {
        viewModel.subscriptions.observe(viewLifecycleOwner) { subscriptions ->
            subscriptionAdapter.submitList(subscriptions)
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            binding.totalAmount.text = String.format("%.2f ₽", total)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Исправлено: устанавливаем isRefreshing только если SwipeRefreshLayout инициализирован
            binding.swipeRefresh?.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                showError(errorMessage)
            }
        }
    }

    private fun showError(message: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorMessage.text = message
        binding.retryButton.setOnClickListener {
            viewModel.refreshData()
            binding.errorLayout.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
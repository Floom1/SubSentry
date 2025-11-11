package com.example.subsentry.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.subsentry.R
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.databinding.FragmentAddBinding
import com.example.subsentry.models.Category
import com.example.subsentry.models.Subscription
import com.example.subsentry.models.SubscriptionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private var categories = listOf<Category>()
    private var selectedCategoryId: Int? = null
    private var isCategoriesLoaded = false
    private var isEditing = false
    private var subscriptionId: Int? = null
    private var currentSubscription: Subscription? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем режим редактирования
        isEditing = arguments?.getBoolean("isEditing") ?: false
        subscriptionId = arguments?.getInt("subscriptionId", -1)
        if (subscriptionId == -1) subscriptionId = null

        setupUI()
        setupCategorySpinner()

        if (isEditing && subscriptionId != null) {
            loadSubscriptionData(subscriptionId!!)
        } else {
            loadCategories()
        }
    }

    private fun setupUI() {
        // Настройка выбора даты
        binding.dateInput.setOnClickListener {
            showDatePicker()
        }

        // Настройка кнопки сохранения
        binding.saveButton.setOnClickListener {
            validateAndSave()
        }
    }


    private fun loadSubscriptionData(subscriptionId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getSubscription(subscriptionId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        currentSubscription = response.body()
                        if (currentSubscription != null) {
                            populateForm(currentSubscription!!)
                            loadCategories()
                        } else {
                            showError("Подписка не найдена")
                            findNavController().navigateUp()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка загрузки подписки: ${response.message()}")
                        findNavController().navigateUp()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun populateForm(subscription: Subscription) {
        binding.nameInput.setText(subscription.name)
        binding.priceInput.setText(subscription.price.toString())
        binding.dateInput.setText(subscription.nextPaymentDate)
        binding.notificationDaysInput.setText(subscription.notificationDaysBefore.toString())

        // Устанавливаем выбранную категорию
        selectedCategoryId = subscription.categoryId
        updateSpinnerAdapter()

        // Если есть категория, устанавливаем её в спиннер
        if (selectedCategoryId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                // Даем время для обновления адаптера
                delay(100)
                val position = categories.indexOfFirst { it.id == selectedCategoryId }
                if (position >= 0) {
                    binding.categorySpinner.setSelection(position)
                }
            }
        }
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getCategories()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        categories = response.body() ?: emptyList()
                        updateSpinnerAdapter()

                        // Если была выбрана новая категория, но список обновился
                        if (selectedCategoryId == null && isCategoriesLoaded) {
                            // Выбираем первую категорию, если она есть
                            if (categories.isNotEmpty()) {
                                binding.categorySpinner.setSelection(0)
                                selectedCategoryId = categories[0].id
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка загрузки категорий: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun setupCategorySpinner() {
        // Инициализируем адаптер даже если категорий еще нет
        updateSpinnerAdapter()

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (categories.isNotEmpty() && position < categories.size) {
                    selectedCategoryId = categories[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = null
            }
        }

        // Обработчик для кнопки добавления категории
        binding.addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }

        isCategoriesLoaded = true
    }

    private fun showAddCategoryDialog() {
        val dialogView = android.widget.EditText(requireContext()).apply {
            hint = "Название категории"
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Новая категория")
            .setMessage("Введите название категории:")
            .setView(dialogView)
            .setPositiveButton("ОК") { _, _ ->
                val categoryName = dialogView.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    createCategory(categoryName)
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private fun createCategory(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.createCategory(
                    com.example.subsentry.models.CategoryRequest(name)
                )
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Категория создана", Toast.LENGTH_SHORT).show()
                        // Просто перезагружаем категории - новая автоматически появится в списке
                        loadCategories()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка создания категории: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun updateSpinnerAdapter() {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.dateInput.setText(formattedDate)
            },
            year,
            month,
            day
        ).show()
    }

    private fun validateAndSave() {
        val name = binding.nameInput.text.toString().trim()
        val priceStr = binding.priceInput.text.toString().trim()
        val date = binding.dateInput.text.toString().trim()
        val notificationDaysStr = binding.notificationDaysInput.text.toString().trim()

        // Валидация названия
        if (name.isEmpty()) {
            binding.nameLayout.error = "Введите название подписки"
            return
        } else {
            binding.nameLayout.error = null
        }

        // Валидация цены
        val price = try {
            priceStr.toDouble()
        } catch (e: NumberFormatException) {
            binding.priceLayout.error = "Введите корректную цену"
            return
        }
        if (price <= 0) {
            binding.priceLayout.error = "Цена должна быть больше 0"
            return
        } else {
            binding.priceLayout.error = null
        }

        // Валидация даты
        if (date.isEmpty()) {
            binding.dateLayout.error = "Выберите дату"
            return
        } else {
            binding.dateLayout.error = null

            // Проверка, что дата не раньше сегодняшней
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val selectedDate = dateFormat.parse(date)
                val today = Calendar.getInstance().time

                if (selectedDate.before(today)) {
                    binding.dateLayout.error = "Дата должна быть не раньше сегодняшней"
                    return
                }
            } catch (e: Exception) {
                binding.dateLayout.error = "Некорректный формат даты"
                return
            }
        }

        // Валидация дней уведомления
        val notificationDays = try {
            notificationDaysStr.toInt()
        } catch (e: NumberFormatException) {
            binding.notificationDaysLayout.error = "Введите число"
            return
        }
        if (notificationDays < 1 || notificationDays > 30) {
            binding.notificationDaysLayout.error = "Дней должно быть от 1 до 30"
            return
        } else {
            binding.notificationDaysLayout.error = null
        }

        // Проверка, выбрана ли категория
        if (selectedCategoryId == null) {
            Toast.makeText(requireContext(), "Выберите категорию", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditing && subscriptionId != null) {
            updateSubscription(
                subscriptionId!!,
                name,
                price,
                date,
                notificationDays
            )
        } else {
            saveSubscription(name, price, date, notificationDays)
        }
    }

    private fun saveSubscription(
        name: String,
        price: Double,
        date: String,
        notificationDays: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.createSubscription(
                    SubscriptionRequest(
                        name = name,
                        price = price,
                        next_payment_date = date,
                        category = selectedCategoryId,
                        notification_days_before = notificationDays
                    )
                )

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Подписка добавлена", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка добавления подписки: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun updateSubscription(
        id: Int,
        name: String,
        price: Double,
        date: String,
        notificationDays: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.updateSubscription(
                    id,
                    SubscriptionRequest(
                        name = name,
                        price = price,
                        next_payment_date = date,
                        category = selectedCategoryId,
                        notification_days_before = notificationDays
                    )
                )

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Подписка обновлена", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка обновления подписки: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка сети: ${e.message}")
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
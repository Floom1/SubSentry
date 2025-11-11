package com.example.subsentry.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.subsentry.databinding.ItemSubscriptionBinding
import com.example.subsentry.models.Subscription

class SubscriptionAdapter(
    private val onEditClick: (Subscription) -> Unit,
    private val onDeleteClick: (Subscription) -> Unit
) : ListAdapter<Subscription, SubscriptionAdapter.SubscriptionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SubscriptionViewHolder(private val binding: ItemSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription) {
            binding.subscriptionName.text = subscription.name
            binding.subscriptionPrice.text = String.format("%.2f ₽", subscription.price)
            binding.subscriptionCategory.text = subscription.categoryName ?: "Без категории"
            binding.nextPaymentDate.text = "Следующий платеж: ${formatDate(subscription.nextPaymentDate)}"

            binding.editButton.setOnClickListener {
                onEditClick(getItem(adapterPosition))
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(getItem(adapterPosition))
            }
        }
    }

    private fun formatDate(dateString: String): String {
        // Простой форматтер для примера, в реальном приложении используйте SimpleDateFormat
        return dateString.replace("-", ".")
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Subscription>() {
            override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
                return oldItem == newItem
            }
        }
    }
}
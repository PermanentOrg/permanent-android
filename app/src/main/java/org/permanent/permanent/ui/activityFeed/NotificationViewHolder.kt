package org.permanent.permanent.ui.activityFeed

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemNotificationBinding
import org.permanent.permanent.models.Notification

class NotificationViewHolder(
    private val binding: ItemNotificationBinding, private val listener: NotificationListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(notification: Notification) {
        binding.notification = notification
        binding.executePendingBindings()
        binding.root.setOnClickListener { listener.onNotificationClick(notification) }
    }
}
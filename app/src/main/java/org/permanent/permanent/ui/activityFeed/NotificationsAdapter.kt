package org.permanent.permanent.ui.activityFeed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemNotificationBinding
import org.permanent.permanent.models.Notification

class NotificationsAdapter(
    private val listener: NotificationListener) : RecyclerView.Adapter<NotificationViewHolder>() {
    private var notifications: MutableList<Notification> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding, listener)
    }

    fun set(notifications: MutableList<Notification>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }

    override fun getItemCount() = notifications.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }
}
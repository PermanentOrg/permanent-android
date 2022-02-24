package org.permanent.permanent.ui.publicWorkspace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemOnlinePresenceBinding
import org.permanent.permanent.models.ProfileItem

class OnlinePresenceListAdapter(
    private val onlinePresenceListener: OnlinePresenceListener
    ) : RecyclerView.Adapter<OnlinePresenceListViewHolder>() {
    private var onlinePresences: MutableList<ProfileItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlinePresenceListViewHolder {
        val binding = ItemOnlinePresenceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnlinePresenceListViewHolder(binding, onlinePresenceListener)
    }

    fun set(onlinePresences: MutableList<ProfileItem>) {
        this.onlinePresences = onlinePresences.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = onlinePresences.size

    override fun onBindViewHolder(holder: OnlinePresenceListViewHolder, position: Int) {
        holder.bind(onlinePresences[position])
    }
}

package org.permanent.permanent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMenuShareBinding
import org.permanent.permanent.models.Share

class MenuSharesAdapter : RecyclerView.Adapter<MenuShareViewHolder>() {
    private var shares: MutableList<Share> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuShareViewHolder {
        val binding = ItemMenuShareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuShareViewHolder(binding)
    }

    fun set(shares: List<Share>) {
        this.shares = shares.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = shares.size

    override fun onBindViewHolder(holder: MenuShareViewHolder, position: Int) {
        holder.bind(shares[position])
    }
}
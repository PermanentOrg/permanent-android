package org.permanent.permanent.ui.members

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMemberBinding
import org.permanent.permanent.models.Account

class MembersAdapter(
    private val memberListener: MemberListener
) : RecyclerView.Adapter<MemberViewHolder>() {
    private var members: MutableList<Account> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding, memberListener)
    }

    fun set(accounts: List<Account>) {
        members = accounts.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = members.size

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }
}
package org.permanent.permanent.ui.members

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMemberBinding
import org.permanent.permanent.models.Member

class MembersAdapter : RecyclerView.Adapter<MemberViewHolder>() {
    private var members: MutableList<Member> = ArrayList()

    init {
        val member1 = Member()
        member1.displayName = "Bryson Williams"
        member1.email = "bryson@permanent.org"
        members.add(member1)

        val member2 = Member()
        member2.displayName = "Flavia Handrea"
        member2.email = "flavia.handrea@vspartners.us"
        members.add(member2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding)
    }

    override fun getItemCount() = members.size

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        members[position]?.let { holder.bind(it) }
    }
}
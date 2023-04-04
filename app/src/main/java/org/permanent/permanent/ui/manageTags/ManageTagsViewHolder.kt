package org.permanent.permanent.ui.manageTags

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.models.Tag

class ManageTagsViewHolder(inflater: LayoutInflater, parent: ViewGroup, val listener: ManageTagListener) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_manage_tag, parent, false)) {

    private var tagName: TextView? = null
    private var editButton: ImageButton? = null
    private var deleteButton: ImageButton? = null

    init {
        tagName = itemView.findViewById(R.id.tagChip)
        editButton = itemView.findViewById(R.id.editButton)
        deleteButton = itemView.findViewById(R.id.deleteButton)
    }

    fun bind(tag: Tag) {
        tagName?.text = tag.name
        editButton?.setOnClickListener { listener.onTagEditClicked(tag) }
        deleteButton?.setOnClickListener { listener.onTagDeleteClicked(tag) }
    }
}
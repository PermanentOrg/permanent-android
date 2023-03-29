package org.permanent.permanent.ui.manageTags

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.permanent.permanent.R
import org.permanent.permanent.models.Tag

class ManageTagsViewHolder(inflater: LayoutInflater, parent: ViewGroup, val listener: ManageTagListener) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_manage_tag, parent, false)) {

    private var tagName: Chip? = null
    private var editButton: ImageButton? = null

    init {
        tagName = itemView.findViewById(R.id.tagChip)
        editButton = itemView.findViewById(R.id.editButton)
    }

    fun bind(tag: Tag) {
        tagName?.text = tag.name
        editButton?.setOnClickListener { listener.onTagEditClicked(tag) }
    }
}
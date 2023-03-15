package org.permanent.permanent.ui.manageTags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.permanent.permanent.R
import org.permanent.permanent.models.Tag

class ManageTagsViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_manage_tag, parent, false)) {

    private var tagName: Chip? = null

    init {
        tagName = itemView.findViewById(R.id.tagChip)
    }

    fun bind(tag: Tag) {
        tagName?.text = tag.name
    }
}
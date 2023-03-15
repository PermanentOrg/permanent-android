package org.permanent.permanent.ui.manageTags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.models.Tag

class ManageTagsAdapter (private val mTags: List<Tag>) : RecyclerView.Adapter<ManageTagsViewHolder>()
{
    // ... constructor and member variables
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageTagsViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        return ManageTagsViewHolder(inflater, parent)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: ManageTagsViewHolder, position: Int) {
        // Get the data model based on position
        val tag: Tag = mTags.get(position)

        viewHolder.bind(tag)
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return mTags.size
    }
}
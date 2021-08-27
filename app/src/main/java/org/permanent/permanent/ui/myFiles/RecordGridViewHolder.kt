package org.permanent.permanent.ui.myFiles

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.shares.PreviewState

class RecordGridViewHolder(
    val context: Context, val binding: ItemGridRecordBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        record: Record,
        lifecycleOwner: LifecycleOwner,
        previewState: MutableLiveData<PreviewState>
    ) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner

        if (record.isThumbBlurred != null
            && record.isThumbBlurred!!
            && previewState.value != PreviewState.ACCESS_GRANTED
        ) {
            Picasso.get()
                .load(record.thumbURL500)
                .placeholder(R.drawable.ic_stop_light_grey)
                .fit()
                .transform(BlurTransformation(context, 25, 5))
                .into(binding.ivRecordThumb)
        }
    }
}
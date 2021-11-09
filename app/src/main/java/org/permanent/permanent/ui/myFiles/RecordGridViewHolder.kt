package org.permanent.permanent.ui.myFiles

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.shares.PreviewState

class RecordGridViewHolder(
    private val context: Context,
    private val binding: ItemGridRecordBinding,
    private val isForSharePreviewScreen: Boolean,
    private val isForSharesScreen: Boolean,
    private val recordListener: RecordListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        record: Record,
        lifecycleOwner: LifecycleOwner,
        previewState: MutableLiveData<PreviewState>
    ) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.root.setOnClickListener { recordListener.onRecordClick(record) }
        binding.btnOptions.setOnClickListener { recordListener.onRecordOptionsClick(record) }
        binding.btnOptions.visibility =
            if ((CurrentArchivePermissionsManager.instance.getAccessRole() == AccessRole.VIEWER ||
                        isForSharesScreen) && record.type == RecordType.FOLDER ||
                isForSharePreviewScreen) View.GONE else View.VISIBLE

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
            binding.tvRecordName.visibility = View.INVISIBLE
        } else {
            binding.tvRecordName.visibility = View.VISIBLE
        }
    }
}
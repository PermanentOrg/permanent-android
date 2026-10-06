package org.permanent.permanent.ui.myFiles

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import org.permanent.permanent.databinding.FragmentSortOptionsBinding
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.drawBehindSystemBars
import org.permanent.permanent.ui.fileView.compose.isLiveBlurSupported
import org.permanent.permanent.viewmodels.SortOptionsViewModel
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val PARCELABLE_SORT_OPTION_KEY = "parcelable_sort_option_key"
const val SORT_OPTIONS_ANCHOR_KEY = "sort_options_anchor_key"

class SortOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentSortOptionsBinding
    private lateinit var viewModel: SortOptionsViewModel
    private val onSortRequest = MutableLiveData<SortType>()

    private val anchor: Rect? by lazy { arguments?.getParcelable(SORT_OPTIONS_ANCHOR_KEY) }
    private val backdrop = mutableStateOf<PopupBackdrop?>(null)

    // With an anchor, the options open as a popup over the sort row instead of a sheet.
    fun setBundleArguments(sortOption: SortType, anchor: Rect? = null) {
        val bundle = Bundle()
        bundle.putString(PARCELABLE_SORT_OPTION_KEY, sortOption.toBackendString())
        bundle.putParcelable(SORT_OPTIONS_ANCHOR_KEY, anchor)
        this.arguments = bundle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (anchor == null) return super.onCreateDialog(savedInstanceState)
        return Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar).apply {
            window?.drawBehindSystemBars(activity?.window)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val popupAnchor = anchor
        val currentSort = SortType.fromServerValue(arguments?.getString(PARCELABLE_SORT_OPTION_KEY))
        if (popupAnchor != null && currentSort != null) {
            captureBackdrop()
            return ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    MaterialTheme {
                        SortOptionsPopup(
                            anchor = popupAnchor,
                            currentSort = currentSort,
                            backdrop = backdrop.value,
                            onSortClick = { sort ->
                                dismiss()
                                if (sort != currentSort) onSortRequest.value = sort
                            },
                            onDismiss = { dismiss() }
                        )
                    }
                }
            }
        }
        binding = FragmentSortOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(SortOptionsViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.setCurrentSortOption(arguments?.getString(PARCELABLE_SORT_OPTION_KEY))

        return binding.root
    }

    // Copies the screen behind the popup; PixelCopy also reads hardware-backed images.
    private fun captureBackdrop() {
        val screenWindow = activity?.window ?: return
        val screen = screenWindow.decorView
        if (screen.width == 0 || screen.height == 0) return
        val bitmap = Bitmap.createBitmap(screen.width, screen.height, Bitmap.Config.ARGB_8888)
        val origin = IntArray(2).also { screen.getLocationOnScreen(it) }
        PixelCopy.request(screenWindow, bitmap, { result ->
            if (result != PixelCopy.SUCCESS || !isAdded) return@request
            val isLiveBlur = isLiveBlurSupported
            viewLifecycleOwnerLiveData.value?.lifecycleScope?.launch {
                val shown = if (isLiveBlur) bitmap else withContext(Dispatchers.Default) {
                    BlurTransformation(requireContext(), LEGACY_BLUR_RADIUS, LEGACY_BLUR_SAMPLING)
                        .transform(bitmap)
                }
                backdrop.value = PopupBackdrop(
                    shown,
                    IntOffset(origin[0], origin[1]),
                    IntSize(screen.width, screen.height),
                    isBlurred = !isLiveBlur
                )
            }
        }, Handler(Looper.getMainLooper()))
    }

    // The screen copy is large; let it go with the popup.
    override fun onDestroyView() {
        backdrop.value = null
        super.onDestroyView()
    }

    private val onSortRequestObserver = Observer<SortType> {
        dismiss()
        onSortRequest.value = it
    }

    fun getOnSortRequest(): MutableLiveData<SortType> {
        return onSortRequest
    }

    override fun connectViewModelEvents() {
        if (::viewModel.isInitialized) viewModel.getOnSortRequest().observe(this, onSortRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        if (::viewModel.isInitialized) viewModel.getOnSortRequest().removeObserver(onSortRequestObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        // Bitmap blur below API 31, sized to look like the 20dp live blur at screen scale.
        private const val LEGACY_BLUR_RADIUS = 20
        private const val LEGACY_BLUR_SAMPLING = 4
    }
}

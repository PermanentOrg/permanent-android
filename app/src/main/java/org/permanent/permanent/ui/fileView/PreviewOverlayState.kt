package org.permanent.permanent.ui.fileView

/**
 * Status overlay of the non-image previews (video/PDF/docs) — the branded counterpart
 * of the image viewer's loader and S6/S7 states (VSP-1754). [LOADING] shows the
 * translucent spinner over the blurred thumbnail (or skeleton) until the renderer
 * reports ready; [OFFLINE] auto-retries when connectivity returns; [FAILED] retries
 * on tap.
 */
enum class PreviewOverlayState {
    NONE,
    LOADING,
    FAILED,
    OFFLINE
}

package org.permanent.permanent.ui.fileView

/**
 * Failure/offline card of the non-image previews (video/PDF/docs) — the branded
 * counterpart of the image viewer's S6/S7 states (VSP-1754). [OFFLINE] auto-retries
 * when connectivity returns; [FAILED] retries on tap.
 */
enum class PreviewErrorState {
    NONE,
    FAILED,
    OFFLINE
}

package org.permanent.permanent.ui.dashboard

import org.permanent.permanent.models.Archive

/**
 * The widget framework's backbone.
 *
 * The Dashboard renders an ordered list of [DashboardWidgetType]. To add, remove, or
 * reorder widgets, edit [defaultDashboardWidgets] — the screen dispatches each type to a
 * self-contained composable in `ui/dashboard/widgets/`. Keeping the inventory as a plain
 * ordered list (rather than hard-coding widgets into the screen) is what makes the
 * dashboard easy to experiment with.
 */
enum class DashboardWidgetType {
    GREETING,
    CREATE_ARCHIVE,
    IMPORTANT_TO_YOU,
    CHART_PATH,
}

/** Default widget inventory + order, mirroring the Figma Dashboard frame. */
val defaultDashboardWidgets: List<DashboardWidgetType> = listOf(
    DashboardWidgetType.GREETING,
    DashboardWidgetType.CREATE_ARCHIVE,
    DashboardWidgetType.IMPORTANT_TO_YOU,
    DashboardWidgetType.CHART_PATH,
)

/**
 * UI state shared by the tag-saving widgets ("What's important to you?" and "Chart your path
 * to success"). Both persist selected chips via the account-tags endpoint, then dismiss.
 */
enum class WidgetActionState {
    /** Showing, accepting selection. */
    Idle,

    /** Save in flight — footer actions disabled. */
    Saving,

    /** Saved or "remind me later" — the screen stops rendering the widget. */
    Done,
}

/** UI state for the (fully-wired) Create Archive widget. */
sealed interface CreateArchiveState {
    data object Idle : CreateArchiveState
    data object Creating : CreateArchiveState
    data class Success(val archive: Archive) : CreateArchiveState
}

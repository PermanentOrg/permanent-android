package org.permanent.permanent.models

import org.permanent.permanent.ui.Workspace

object FileSessionData {
    var records: List<Record>? = null
    // Set by the screen that opens the viewer; decides the viewer's menu and "Open with…"
    var workspace = Workspace.FILE_VIEW_PRIVATE_FILES

    // Public gallery records live in a foreign archive but are public, so the V2 detail
    // read may serve them.
    val allowsForeignStelaDetail get() = workspace == Workspace.PUBLIC_ARCHIVES
}

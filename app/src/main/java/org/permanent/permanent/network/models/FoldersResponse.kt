package org.permanent.permanent.network.models

// GET api/v2/folders (plural, paginated): items reuse the children item shape.
data class FoldersResponse(val items: List<ItemDTO>?)

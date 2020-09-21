package org.permanent.permanent.network.models

import java.util.ArrayList

class RequestVO {
    var apiKey: String? = null
    var csrf: String? = null
    var data: List<Datum> = ArrayList()
}
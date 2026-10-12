package org.permanent.permanent.models

object FileSessionData {
    var records: List<Record>? = null
    // Set by the public gallery: its records live in a foreign archive but are public,
    // so the V2 detail read may serve them; every other producer leaves it false.
    var allowsForeignStelaDetail = false
}

package org.permanent.permanent.network

import org.permanent.permanent.FeatureFlags

// While set, optional-auth V2 reads answer an anonymous empty list — gate V2 reads on it.
object StelaAuthState {
    @Volatile
    var isBearerRejected: Boolean = false

    val isV2ReadEnabled: Boolean
        get() = FeatureFlags.useStelaMigration && !isBearerRejected

    // Writes carry a V1 failsafe; this only spares the doomed round trip after a rejected bearer.
    val isV2WriteEnabled: Boolean
        get() = isV2ReadEnabled
}

package org.permanent.permanent.network

import org.permanent.permanent.FeatureFlags

// While set, optional-auth V2 reads answer an anonymous empty list — gate V2 reads on it.
object StelaAuthState {
    @Volatile
    var isBearerRejected: Boolean = false

    val isV2ReadEnabled: Boolean
        get() = FeatureFlags.useStelaMigration && !isBearerRejected
}

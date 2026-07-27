package org.permanent.permanent

/**
 * In-app feature flags — compile-time switches with no server-side remote config
 * (mirrors iOS's FeatureFlags in Constants.swift). Flip the value and ship a build
 * to change behavior.
 */
object FeatureFlags {
    /**
     * Master switch for the Stela V2 migration: Private Files navigation now
     * (VSP-1778), records, folder creation etc. in future tickets — one flag for
     * the whole migration. ON in debug builds so the V2 path gets exercised;
     * OFF in every release build until flipped for rollout. V1 remains an
     * automatic failsafe on every gated path, so OFF is always safe.
     * `var` so tests or a future debug toggle can pin it; nothing mutates it
     * in production code.
     */
    var useStelaMigration: Boolean = BuildConfig.DEBUG
}

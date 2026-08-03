package org.permanent.permanent

/**
 * In-app feature flags — compile-time switches with no server-side remote config
 * (mirrors iOS's FeatureFlags in Constants.swift). Flip the value and ship a build
 * to change behavior.
 */
object FeatureFlags {
    /**
     * Master switch for the Stela V2 migration — one flag for the whole migration
     * (Private Files VSP-1778, Public Files VSP-1808, more tickets to come). The
     * default is declared per flavor (STELA_MIGRATION_DEFAULT in app/build.gradle)
     * so the environment fact lives with the flavor that owns it: a productionDebug
     * build must never send V2 calls to the production API (the leak iOS fixed in
     * their PRs #575/#580). V1 remains an automatic failsafe on every gated path,
     * so OFF is always safe. `var` so tests or a future debug toggle can pin it;
     * nothing mutates it in production code.
     */
    var useStelaMigration: Boolean = BuildConfig.STELA_MIGRATION_DEFAULT
}

package org.permanent.permanent.models

interface EventAction {
    val entity: String
    val event: String
    val action: String

    fun rawValue(): String
}

enum class AccountEventAction(private val actionValue: String) : EventAction {
    CREATE("create"),
    LOGIN("login"),
    START_ONBOARDING("start_onboarding"),
    SUBMIT_GOALS("submit_goals"),
    SUBMIT_REASONS("submit_reasons"),
    OPEN_ACCOUNT_MENU("open_account_menu"),
    OPEN_ARCHIVE_MENU("open_archive_menu"),
    OPEN_ARCHIVE_PROFILE("open_archive_profile"),
    OPEN_STORAGE_MODAL("open_storage_modal"),
    PURCHASE_STORAGE("purchase_storage"),
    OPEN_PROMO_ENTRY("open_promo_entry"),
    SUBMIT_PROMO("submit_promo"),
    SKIP_CREATE_ARCHIVE("skip_create_archive"),
    SKIP_GOALS("skip_goals"),
    SKIP_WHY_PERMANENT("skip_why_permanent"),
    OPEN_LOGIN_INFO("open_login_info"),
    OPEN_VERIFY_EMAIL("open_verify_email"),
    OPEN_BILLING_INFO("open_billing_info"),
    UPDATE("update"),
    INITIATE_UPLOAD("initiate_upload"),
    OPEN_LEGACY_CONTACT("open_legacy_contact"),
    OPEN_ARCHIVE_STEWARD("open_archive_steward"),
    OPEN_PRIVATE_WORKSPACE("open_private_workspace"),
    OPEN_PUBLIC_WORKSPACE("open_public_workspace"),
    OPEN_SHARED_WORKSPACE("open_shared_workspace"),
    OPEN_PUBLIC_GALLERY("open_public_gallery"),
    OPEN_REDEEM_GIFT("open_redeem_gift"),
    OPEN_SHARE_MODAL("open_share_modal"),
    COPY_SHARE_LINK("copy_share_link");

    override val entity: String = "account"

    override val event: String
        get() = when (this) {
            CREATE -> "Sign up"
            LOGIN -> "Sign in"
            START_ONBOARDING -> "Onboarding: start"
            SUBMIT_GOALS -> "Onboarding: goals"
            SUBMIT_REASONS -> "Onboarding: reason"
            OPEN_ACCOUNT_MENU, OPEN_ARCHIVE_MENU, OPEN_STORAGE_MODAL, OPEN_PROMO_ENTRY, OPEN_ARCHIVE_PROFILE -> "Screen View"
            PURCHASE_STORAGE -> "Purchase Storage"
            SUBMIT_PROMO -> "Redeem Gift"
            SKIP_CREATE_ARCHIVE -> "Skip create archive"
            SKIP_GOALS -> "Skip goals"
            SKIP_WHY_PERMANENT -> "Skip why permanent"
            OPEN_LOGIN_INFO -> "View Login Info"
            OPEN_VERIFY_EMAIL -> "Verify Email"
            OPEN_BILLING_INFO -> "View Billing Info"
            UPDATE -> "Edit Address"
            INITIATE_UPLOAD -> "Initiate Upload"
            OPEN_LEGACY_CONTACT -> "View Legacy Contact"
            OPEN_ARCHIVE_STEWARD -> "View Archive Steward"
            OPEN_PRIVATE_WORKSPACE -> "View Private Workspace"
            OPEN_PUBLIC_WORKSPACE -> "View Public Workspace"
            OPEN_SHARED_WORKSPACE -> "View Shared Workspace"
            OPEN_PUBLIC_GALLERY -> "View Public Gallery"
            OPEN_REDEEM_GIFT -> "View Redeem Gift"
            OPEN_SHARE_MODAL -> "Share"
            COPY_SHARE_LINK -> "Copy Share Link"
        }

    override val action: String
        get() = actionValue

    override fun rawValue(): String = actionValue
}

enum class RecordEventAction(private val actionValue: String) : EventAction {
    SUBMIT("submit"),
    MOVE("move"),
    COPY("copy");

    override val entity: String = "record"

    override val event: String
        get() = when (this) {
            SUBMIT -> "Finalize Upload"
            MOVE -> "Move Record"
            COPY -> "Copy Record"
        }

    override val action: String
        get() = actionValue

    override fun rawValue(): String = actionValue
}

enum class ProfileItemEventAction(private val actionValue: String) : EventAction {
    UPDATE("update");

    override val entity: String = "profile_item"

    override val event: String
        get() = when (this) {
            UPDATE -> "Edit Archive Profile"
        }

    override val action: String
        get() = actionValue

    override fun rawValue(): String = actionValue
}

enum class LegacyContactEventAction(private val actionValue: String) : EventAction {
    CREATE("create"),
    UPDATE("update");

    override val entity: String = "legacy_contact"

    override val event: String
        get() = when (this) {
            CREATE, UPDATE -> "Edit Legacy Contact"
        }

    override val action: String
        get() = actionValue

    override fun rawValue(): String = actionValue
}

enum class DirectiveEventAction(private val actionValue: String) : EventAction {
    CREATE("create"),
    UPDATE("update");

    override val entity: String = "directive"

    override val event: String
        get() = when (this) {
            CREATE, UPDATE -> "Edit Archive Steward"
        }

    override val action: String
        get() = actionValue

    override fun rawValue(): String = actionValue
}
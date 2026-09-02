# Android · Stela V2 — verified contracts (folders children · record copy)

Verified against the merged iOS implementation (permanent-ios PR #573, commit `38d9622`;
updated 2026-07-30 against PRs #574/#575/#576/#580, and 2026-08-14 against iOS Development
`808aea6` + the stela source itself for VSP-1802), live production captures (2026-07-22),
and the published stela docs — in that order of authority. Written for VSP-1778 (Private
Files navigation), extended for VSP-1808 (Public Files), VSP-1810 (Public Gallery),
VSP-1806 (Search drill-in), VSP-1803 (Shared By Me drill-in), VSP-1802 (Shared With Me
drill-in), VSP-1788 (root resolution) and VSP-1839 (Public Files root resolution); reuse
for future Stela tickets instead of re-deriving.

## Root resolution — My Files (VSP-1788)

Replaces the V1 `folder/getroot` bootstrap: with the flag on, the private root is
discovered from Stela reads only. Verified against iOS Development (`ArchiveV2Endpoint`,
`MyFilesViewModel.resolveSectionRootTargetV2`, wire shape pinned in
`FilesViewModelTests.swift:2224`) and a live staging capture (2026-08-19).

- **The chain (2 reads, then the existing listing):**
  `GET api/v2/archives?callerMembershipRole=owner&…&callerMembershipRole=viewer&pageSize=100`
  (header `Request-Version: 2`, bearer auth) → match `items[].archiveNbr` string-to-string
  against the session's current archive (`PreferencesHelper.getCurrentArchiveNr()`) →
  `items[].rootFolderId` (numeric string, a **V2 folderId**, directly usable) →
  `GET api/v2/folders/{rootFolderId}/children` → the children are the **section roots**,
  short types `app-root` / `private-root` / `public-root` (live capture 2026-08-20 —
  the iOS spellings, NOT the `root.private` form previously assumed from the field-notes
  table); pick the private root (`type.folder.private-root` after mapper normalization,
  underscore spelling tolerated like iOS, display-name `"My Files"` as the safety net,
  folders only) → that child seeds the existing V2 navigation.
- **Query form:** repeated unbracketed `callerMembershipRole=` params (the form the
  server's own `nextPage` emits — NOT `[]`-bracketed, NOT comma-joined). All 6 roles are
  passed because the endpoint requires a query or a role. Both pinned by iOS unit tests.
- **Live archives payload (staging, 2026-08-20):** items carry, beyond the 7 fields
  Android decodes, `description`, `public`, `publicAt`, `allowPublicDownload`,
  `thumbnailUrls`, `owner`, `payerAccountId`, `milestoneSortOrder`, `createdAt`,
  `updatedAt`, `totalPages` — ignored by Moshi. Quirk: `pagination.nextPage` points at
  `/api/v2/archive` (singular) — irrelevant while pagination stays unused, but don't
  trust that URL if cursor paging is ever adopted.
- **`archive/change` does NOT carry a root id — live-verified 2026-08-19.** The ticket
  hypothesized it would; the raw staging response has `ChildFolderVOs`/`FolderSizeVOs`/
  `RecordVOs`/`ItemVOs` all `[]` and no root-folder scalar (fields beyond our Kotlin
  model: `milestoneSortOrder`, `vaultKey`, `allowPublicDownload`, `payerAccountId`,
  `view`, `viewProperty`, `imageRatio`, `publicDT`, `thumbDT`). The web-app likewise calls
  `/folder/getRoot` right after `/archive/change`. The archives search is the only source.
- **Refresh semantics: nothing is cached** (iOS parity). The 2-call discovery re-runs on
  every root load — My Files screen open and the archive-changed observer. Only the newest
  root load may commit (`rootLoadGeneration` in `MyFilesViewModel`), and a commit resets
  `folderPathStack` so a switch on a live ViewModel can't leak the previous archive's root
  into back navigation.
- **V1 failsafe unchanged:** any anomaly (archives fetch fails, no archiveNbr match — e.g.
  >100 memberships, missing/non-positive `rootFolderId`, children contract failure, no
  section-root child) falls back to V1 `getRoot`, whose record still carries `folderId` so
  drill-in stays on V2. Flag off runs the V1 path byte-for-byte. A discovery 401 can never
  logout (`treatStelaUnauthorizedAsSessionExpiry` stays off).
- **Hidden second job preserved:** V1 `getRoot` was the sole writer of the
  `PREFS_PUBLIC_RECORD_*` keys (publish-to-Public + profile banner). The V2 path finds the
  public section root (`public-root`, fallback name `"Public"`) among the same archive-root
  children and writes the same prefs through the same helper, which skips null fields — so
  a missing child leaves working prefs untouched on both paths. A superseded root load
  (rapid archive switch) is short-circuited in the repository before the prefs write, so a
  stale response can't stamp the outgoing archive's public root.
- **`getPublicRoot`:** own-archive Public Files now rides this same resolver (VSP-1839,
  next section); foreign archives can never use it (`/archives` is membership-scoped).

## Root resolution — Public Files, own archive (VSP-1839)

Rides the VSP-1788 resolver: the same archives→children chain now also hands the public
section root to navigation. **Android leads here** — iOS intentionally keeps
`folder/getPublicRoot` for public root discovery, so the mechanism proof is our own
shipped VSP-1788 code and its live captures, not an iOS PR; any backend quirk found on
this path is new information worth sharing with backend.

- **Chain and selection:** identical two reads (`GET api/v2/archives` → `rootFolderId` →
  `GET api/v2/folders/{rootFolderId}/children`); pick the public root
  (`type.folder.public-root` after mapper normalization, display-name `"Public"` as the
  safety net, folders only — the same live-verified constants the VSP-1788 prefs side
  effect already used). Implemented as `FileRepositoryImpl.getPublicRootV2`, a
  parameterization of the same private helper `getMyFilesRecordV2` delegates to
  (`getSectionRootRecordV2`).
- **Own archive only.** `/v2/archives` is membership-scoped, so foreign archives can
  never resolve through it: `PublicArchiveViewModel` (gallery/foreign browsing) and
  `PublicViewModel` (profile-banner thumb) stay on V1 `getPublicRoot` by design, and
  public folder deep links keep the V1 synthesized-record path (blocked on the
  `folder_linkId` resolver — see the link-migration doc).
- **No caching (decided at planning):** exact VSP-1788 parity — the 2-call discovery
  re-runs on every Public Files entry (2 reads vs V1's single call, accepted). The whole
  V2→V1 chain lives once in `MyFilesViewModel` (generation supersede guard, plus an
  in-flight flag that suppresses the onResume `refreshCurrentFolder` double-fetch);
  `PublicFilesViewModel` overrides only the two resolver legs
  (`resolveRootV2`/`resolveRootFailsafe`), its V1 body, and `commitRootRecord` — the
  latter solely to re-fire its root-ready replay event (pending deep-link navigation and
  uploads) after both a V2 success and a V1-failsafe success.
- **V1 failsafe unchanged:** any anomaly falls back to V1 `getPublicRoot` under the same
  generation guard; a discovery 401 can never log out. Flag off runs the previous V1 body
  byte-for-byte (push-only stack, no supersede guard — preserved deliberately). A missing
  `public-root` child among a resolvable root's children fails with a distinct
  DEBUG-only message (`section root … missing from the archive root's children`) — that
  would be backend data damage, not a transport error.
- **Prefs side effect shared:** both entry points keep writing `PREFS_PUBLIC_RECORD_*`
  from the same children, stale-guarded before the write; values are identical whichever
  screen resolves first. (V1 `getPublicRoot` writes nothing — the write is the bootstrap
  parity job, not new behavior.)

## Shared With Me → folder drill-in (VSP-1802)

- **Same endpoint, same bearer auth, one gate for both tabs.** Both tabs go through the
  same single call site (`SharedXMeViewModel.loadFilesOf`, same supersede machinery, same
  V1 failsafe) with the gate `useStelaMigration && folderId > 0` and **no session-archive
  condition** — foreign archives are the normal case on with-me, and the server resolves
  share membership per caller and item. This matches the merged iOS nav reads, which have
  no ownership condition either (iOS #576's session-archive gate applies only to the
  rename PATCH — a write). *(The by-me tab briefly kept VSP-1803's per-fetch
  `archiveNr == session archiveNr` re-check; removed 2026-08-14 — see the defect note in
  the VSP-1803 section.)*
- **The feasibility verdict that unblocked this ticket** (2026-08-14, verified in the
  stela source + tests, cross-checked against iOS Development): bearer-only V2 READS of
  share-membership foreign content **work**. The children route uses optional auth
  (`extractUserEmailFromAuthToken` — never throws), and the hydration SQL has an explicit
  share-membership leg (caller account → `access` table on the item's own
  `folder_linkid`); access rows **propagate to descendants** of shared folders. iOS
  shipped the same drill-in in PR #574 and never reverted it. The old "membership-foreign
  ✗" access-map entry was inferred from #576's PATCH failure — a write; reads and writes
  authorize differently (reads filter, writes reject).
- **Per-item permissions come from the payload, not a stamp.** V2 children DO carry a
  top-level caller-resolved `accessRole` (share-membership-aware — stela computes
  least-permissive of caller-archive role and share role per pair, most permissive
  overall). The field is new: delivered by **PER-10716** (stela PR #835, merged
  2026-08-13 — the ★P1 mobile ask) and live on staging (capture 2026-08-14); the earlier
  gap-1 "missing" reports were accurate until then. On the wire the children route sends
  the **dotted** form (`access.role.viewer` — live capture), while the backend's own
  folders-page test expects short `"viewer"`; the mapper's dot-tolerance covers both and
  is therefore load-bearing. Platform difference to remember for cross-platform QA: iOS derives
  child roles by inheriting the entered folder's role (fail-closed to viewer); Android
  decodes the payload field in
  `ItemMapper.toRecordV2` (`AccessRole.fromStelaBackendValue`, short form, dotted-form
  tolerant), which fits our V1 parity goal (V1's
  `getLeanItems` returned true per-item roles). An absent role clamps to VIEWER in the
  mapper — V1 parity: a listed Record always carries a non-null role, and
  `isCreateAvailable`/the record menu rely on that. The by-me tab overrides with the
  session archive's role — retained VSP-1803 behavior, equivalent for own-archive
  content, not a payload gap.
- **The shares ROOT listing stays V1** (`POST share/getShares`) — at VSP-1802 time no V2
  aggregate route existed; `/folders/{id}/children` cannot serve it because the root
  spans many foreign archives with no single parent folder. *(Update 2026-08-17: the
  backend shipped the with-me half as PER-10715 — `GET /v2/archives/{id}/received-shares`,
  cursor-paginated, per-item caller `accessRole`, sharing-archive summary. Not adoptable
  yet: its `ItemSummary` carries no `folderLinkId`/`archiveNumber` — our
  `loadFilesOf` precondition and the retained V1 writes key on both — and there is no
  given-direction counterpart for by-me. Root migration = its own ticket once those land.)*
  Back-to-root and root
  pull-to-refresh re-enter V1 by construction (`onRootSharesNeeded` →
  `SharesFragment.requestShares`). This is the last V1 dependency of the Shares
  navigation surface — V1-sunset bucket, alongside gaps 5–6.
- **Read failure mode is silent omission, never 401.** Children the caller isn't
  authorized for are dropped from a 200 listing; a fully inaccessible folder returns
  `items: []` (verified in stela tests) — indistinguishable from a genuinely empty or
  just-revoked share, and no V1 failsafe triggers. 401 on this read can only be
  token-level, and the 401-never-logs-out guarantee is unchanged (host-scoped
  `UnauthorizedInterceptor`, `treatStelaUnauthorizedAsSessionExpiry` OFF — see gap 7).

## Shared By Me → folder drill-in (VSP-1803)

- **Same endpoint, same bearer auth.** The single drill-in call site
  (`SharedXMeViewModel.loadFilesOf` — folder tap, deeper drill-in, back navigation, sort
  change, pull-to-refresh and the delayed post-upload/post-delete refreshes all funnel
  through it) branches to `getChildRecordsOfV2` behind
  `useStelaMigration && folderId > 0` (as shipped, VSP-1803 also required
  `isSharedByMe && archiveNr == session archiveNr` — see the defect note below for why
  both extra conditions are gone),
  with the V1 two-step (`navigateMin` + `getLeanItems`) as automatic failsafe. The
  **shares root listing stays V1** (`POST share/getShares` — no V2 route exists on either
  platform; iOS's proposed `GET /v2/shares?direction=…` was never built).
- **Shares-territory access map on V2** *(corrected 2026-08-14, VSP-1802 — the ✗ was
  inferred from iOS #576's PATCH, a write; reads authorize differently)*: own-archive
  content ✓ bearer-only V2 · public foreign content ✓ bearer-only V2 · share-membership
  foreign content **✓ for READS** (stela's hydration SQL checks the caller's `access`
  rows, descendants included; see the VSP-1802 section) · foreign **writes** ✗ today
  (strict auth, rejected — keep them V1/own-archive-gated; the backend confirmed
  2026-08-17 this is a bug and share-permitted writes are intended to succeed, gap 7).
  Shared By Me is the first bucket
  **by construction**: `SharesViewModel.requestShares` splits by-me/with-me on
  `currentArchiveId == archive.archiveId` against the SESSION archive, so by-me items —
  and transitively their children — always belong to the session archive.
- **Defect found in QA (Sergiu, fixed 2026-08-14): the per-fetch
  `archiveNr == getCurrentArchiveNr()` re-check made the by-me V2 path dead at the root
  hop.** On by-me root rows `Record.archiveNr` comes from `itemVO.archiveNbr` in the
  `getShares` payload — which carries the **share counterpart's** archive, not the owning
  session archive (the ownership fact lives on the *wrapping* `ArchiveVO`, which is what
  the by-me split checks). So the re-check compared the counterpart's number to the
  session's and was **always false**: every root-level by-me drill-in silently ran V1.
  Levels below the first hop DID pass (their records come from V1 `getLeanItems`, whose
  `archiveNbr` is the owning archive) — which is why V2 logs still appeared in VSP-1803
  QA. Fix: the re-check is deleted; `isSharedByMe` already encodes session-archive
  ownership at listing time, and VSP-1802 established that reads need no ownership
  condition at all. Lesson recorded: **`itemVO.archiveNbr` in the `getShares` payload
  identifies the counterpart archive** — never use it as an ownership signal.
- ~~**Shared With Me is hard-locked to V1**~~ *(superseded by VSP-1802, 2026-08-14: the
  with-me drill-in now opens the gate without the ownership condition and decodes the
  payload `accessRole` — see its section above; the "blocked on gap 1" premise no longer
  holds, the field is on the wire)*. The `isSharedByMe` bundle discriminator
  (the with-me page always carries `SHARED_WITH_ME_ITEM_LIST_KEY`, the by-me page never
  does) now selects the gate variant and role source instead of locking with-me out.
- **401 semantics, verified structurally:** Android cannot repeat iOS's #576 logout bug.
  `UnauthorizedInterceptor` fires only on URLs containing `BASE_API_URL` (the V1 host);
  the Stela base is a different host, so a V2 401 surfaces as `onFailed` → V1 failsafe,
  never a session-expiry logout. A genuinely expired session then fails on the V1
  fallback call, which correctly triggers the normal logout.
- **Full supersede machinery — the first non-MyFiles screen to need it.** Unlike the
  gallery/search screens (`isBusy` guard, no refresh sources), Shared By Me has
  pull-to-refresh, a sort picker and two delayed background refreshes that can race a
  folder tap — so `SharedXMeViewModel` mirrors `MyFilesViewModel`'s
  `childrenFetchGeneration` policy verbatim (newest generation commits or falls back;
  superseded failures never run the failsafe; forward-navigation taps retry once).
- **Shares-local derivations on the V2 path** (V1 parity): every mapped record gets
  `displayInShares = true` (share badges; what the V1 `getRecords` stamps) and, on the
  by-me tab, `accessRole =` the **session archive's role** from
  `CurrentArchivePermissionsManager` — retained as shipped behavior after VSP-1802
  showed the payload does carry a per-item role (own-archive items make both equivalent
  after the menu's inferior-clamp). The session role reproduces
  V1 because `RecordMenuViewModel` clamps with `getInferior(archive role)` and own-archive
  item roles are never below the archive role; stamping OWNER would overstate permissions
  for Editor/Curator members. Header title uses the tapped folder's own `displayName`
  (same as V1); the active sort is applied locally via `SortType.toComparator()`. No
  `parentFolderArchiveNr` stamp — its only consumer is the public-gallery copy-link
  button (`Workspace.PUBLIC_ARCHIVES`); Shares opens menus with `Workspace.SHARES`.
- **Pre-existing quirks found during VSP-1803 (all flag-independent, not fixed here):**
  the shares root listing only delivers **non-empty** lists (`SharesViewModel`), so a
  refresh returning zero by-me shares leaves the stale list on screen (the inverse of
  iOS #583's clear-early/fill-late race, which Android does not have); the ⋮ → Share
  sheet's pending-invites call (`ShareManagementViewModel.refreshPendingInvites`) already
  hits V2 **ungated**; root share rows show an empty size because V1 `ItemVO` carries no
  `size` field.

## Search → folder drill-in (VSP-1806)

- **Same endpoint, same bearer auth, same gate.** The single drill-in call site
  (`RecordSearchViewModel.loadChildRecordsOf` — folder tap, deeper drill-in and back
  navigation all funnel through it) branches to `getChildRecordsOfV2` behind
  `useStelaMigration && folderId > 0`, with the V1 two-step as automatic failsafe —
  the `PublicFolderViewModel` pattern verbatim. The **search query itself stays V1**
  (`POST search/folderAndRecord`, 10-result cap): no V2 search route exists on either
  platform.
- **The identity question — search results DO carry `folderId`.** The V1 search payload's
  `RecordVO` populates `folderId` on folder hits; proof is structural: the app classifies
  a result as a folder *only* when `folderId != null` (`Record.kt`), and only folders are
  drill-in tappable — a payload without it would render as a file and never navigate. So
  no `folder_linkId → folderId` resolver (gap 5) is needed here; the `folderId > 0` gate
  plus V1 failsafe covers any edge payload regardless.
- **iOS reference — search IS wired to V2, as a soft bridge** (`SearchFilesViewModel`
  opts into the flag; the tap seeds `v2NavigationTarget` with the tapped `FileModel` and
  still issues V1-shaped `NavigateMinParams`; `navigateMin` takes V2 only when
  `flag && target.folderId > 0`). iOS never asserts the payload carries `folderId` — it
  silently degrades to V1. Android expresses the same decision directly at the tap-site
  branch (the `Record` is already the parameter; no out-of-band target needed).
- **No supersede machinery** — the screen's `isBusy` re-entrancy guard allows at most one
  fetch in flight (no pull-to-refresh, no sort picker), same rationale as the gallery.
- **Search-local derivations on the V2 path** (V1 parity): the header title uses the
  tapped folder's own `displayName` — which is what the *existing V1 search listener
  already uses* (it ignores the `getLeanItems` envelope name), so both paths are
  identical by construction; fixed `NAME_ASCENDING` applied locally via
  `SortType.toComparator()` (V1 sends the same sort as a backend param). No
  `parentFolderArchiveNr` stamp — the V1 search path never sets it and nothing on this
  screen consumes it (no options/toolbar menu: `isForSearchScreen` suppresses it).
- **Section-agnostic by construction:** search has no section concept (no section in
  request, response, or nav args; results are scoped server-side to the session archive)
  and V1 addresses folders by `archiveNbr + folder_linkId` regardless of tree — the
  bearer-auth `/children` call is equally tree-agnostic (public trees verified in
  VSP-1810). The Shares surfaces have no search entry point, and the shared-tree gaps
  (1–2 below) can't bite because the search screen renders no permissions UI.
- **Operational note (iOS, 2026-08):** upstream iOS currently ships with
  `useStelaNavigation = false` in **every** build ("Ship with Stela V2 navigation
  disabled" — the 1.16.0 release deferred the epic; re-enablable at launch via
  `--forceStelaNavigation` on debug/staging builds). Android's gating is unchanged
  (staging flavor = on), but cross-platform QA comparisons must force the iOS flag on.

## Public Gallery (VSP-1810)

- **Same endpoint, same bearer auth.** The gallery's two children call sites
  (`PublicArchiveViewModel.loadFilesOf` — root listing of a public archive — and
  `PublicFolderViewModel.loadFilesOf` — sub-folder browsing) branch to
  `getChildRecordsOfV2` behind the same flag, mirroring `MyFilesViewModel`'s gate
  (`useStelaMigration && folderId > 0`), with the V1 two-step as automatic failsafe.
- **The iOS gallery reference is PR #576 (VSP-1811)**, merged 2026-07-29 — a dedicated
  gallery navigation PR (the migration artifacts still list foreign-public browsing as
  "pending"; they lag the merged code). Follow-up PR #582 moved gallery record *detail*
  to the V2 record endpoint — Android's `record/get` deep-link read stays V1 (candidate
  follow-up ticket).
- **Root discovery stays V1 `folder/getPublicRoot`** (V2 `/archives` lists only the
  caller's own memberships, so it cannot resolve a foreign archive — same on iOS). The
  V1 root response carries `folderId`, so the V2 fork engages from the root listing down.
- **Foreign archives on bearer: verified from Android** (staging QA session, 2026-08-05,
  OkHttp capture): `/children` serves another archive's public tree on plain bearer auth —
  matching iOS's 2026-07-28 verification (PR #576). The same session verified empty
  listings commit as verified-empty (42-byte `items: []`, no failsafe misfire), large
  (100KB+) listings, and zero V1 fallbacks across the full manual checklist.
- **No supersede machinery needed:** both gallery ViewModels keep their `isBusy`
  re-entrancy guard — at most one fetch in flight (no pull-to-refresh, no sort picker,
  no background refresh), so a fetch either commits or falls back to V1.
- **Gallery-local derivations on the V2 path** (V1 parity): each mapped record gets
  `parentFolderArchiveNr` stamped from the listed folder (only consumer:
  `FileViewOptionsViewModel`'s copy-link button); the sub-folder ActionBar title uses the
  listed folder's own `displayName` (V2 has no parent-name envelope; same value V1 sent);
  fixed `NAME_ASCENDING` applied locally via `SortType.toComparator()`.
- **Deep-linked folders fall through to V1 by design:** the deep-link path synthesizes
  `Record(archiveNr, folderLinkId)` with no `folderId`, so the gate rejects it; its
  V1-listed children carry `folderId` and upgrade to V2 below that point (same graceful
  degradation as iOS's `navigateMin` folderId gate). The permanent fix needs the backend
  (gap 5 below — resolver ask raised 2026-08-06, comment on VSP-1810). Scope note: this
  is the ONLY deep-link family affected — private share links (`/share/{token}`) resolve
  by token and are already live on V2; record deep links have the analogous
  `file_archive_nr` vs `recordId` mismatch, but that belongs to the record-detail
  follow-up (iOS PR #582).
- **Copy-link caveats found during QA (both pre-existing, flag-independent):** the ⋮
  menu that reaches "Copy Link" on public folder screens is unreachable — a toolbar
  regression from the 2026-04 Share Preview redesign wipes per-screen menu state
  (**VSP-1833**, filed 2026-08-06); and `onCopyLinkBtnClick` builds the URL's first
  segment from the *logged-in* archive number, not the browsed one
  (`PublicArchiveViewModel.kt:107`, `FileViewOptionsViewModel` — unticketed). Neither
  affects V1/V2 parity; the V2 `parentFolderArchiveNr` stamp is what will gate the
  button correctly once VSP-1833 lands.
- Read-only surface: `Workspace.PUBLIC_ARCHIVES` menu branch hides all write actions
  regardless of record fields, so no permission derivation is needed. (Pre-existing,
  flag-independent gap: the file viewer derives its menu from the persisted workspace,
  which the gallery never sets — separate ticket; iOS pinned gallery permissions
  read-only in PR #576.)

## Public folders (VSP-1808)

- **Same endpoint, same bearer auth, no public variant.** The children call for the owner's
  Public Files tree is byte-identical to the private one — only root resolution differs.
  *(As shipped in VSP-1808, the root stayed V1 `folder/getPublicRoot`, whose response
  carries `folderId` so the V2 fork engaged from the root down; own-archive root discovery
  has since moved to the archives chain — My Files in VSP-1788, Public Files in VSP-1839,
  see the root-resolution sections above.)* iOS does the same drill-in (their PR #573
  already covered Public Files via ViewModel inheritance) and moved My Files root
  discovery to the archives chain in VSP-1787 (PR #574).
- **Foreign public archives** (another archive's public tree): iOS verified on staging
  (2026-07-28, their PR #576) that `/children` serves it on plain bearer auth — but root
  discovery must stay V1 `getPublicRoot`, because `/v2/archives` only lists the caller's
  own memberships. Android's `PublicArchiveViewModel`/`PublicFolderViewModel` were
  migrated in VSP-1810 (see the Public Gallery section above).
- Backend gaps 1–2 don't bite Public Files: item permissions are archive-derived there, and
  the pending-invitation badge is not rendered on that screen.

## Feature flag environment gating (fixed in VSP-1808)

`FeatureFlags.useStelaMigration = BuildConfig.STELA_MIGRATION_DEFAULT` — a per-flavor
boolean `buildConfigField` in `app/build.gradle` (`true` in staging, `false` in
production), so the flavor block owns the environment fact and the rollout flip is a
one-word gradle edit. Before the fix the flag was `BuildConfig.DEBUG`, which let a
productionDebug build send V2 calls to the production API — the exact leak iOS closed in
PR #575, refined in PR #580 after their QA's release-type staging build silently pinned
the flag OFF. Net rule on both platforms: flag on ⇔ environment is staging, in every
build type.

## Request

```
GET {BASE_API_URL_STELA}api/v2/folders/{folderId}/children?pageSize=99999999
Headers: Request-Version: 2
         Authorization: Bearer <token>          (attached automatically by our OkHttp interceptor)
         X-Permanent-Share-Token: <token>       (share-preview flavor only — mutually exclusive use case)
```

- **Path**: use the **plural** `/folders/` form. The singular `/folder/` (used by the older
  share-preview call) is a deprecated backend alias — verified byte-identical responses on
  production, but new code uses the documented plural route.
- **`pageSize`** (required): we send `99_999_999` (`StelaAccountService.MAX_CHILDREN_PAGE_SIZE`)
  to fetch the whole folder in one page, same as iOS. Cursor pagination is deliberately
  deferred (see Pagination below).
- **`cursor`** (optional, unused): the `folderLinkId` of the item *before* the first item you
  want — in practice the last item of the previous page.
- **No sort parameter — by design.** Sort is a folder attribute; `/children` returns the
  folder's stored order. Clients apply the user's sort locally (Android:
  `SortType.toComparator()`; iOS: `FilesViewModel.sorted(_:by:)`).
- **401s must not force logout** — the V1 failsafe follows. On Android this is already true:
  `UnauthorizedInterceptor` only matches `BASE_API_URL`, not the Stela base. iOS sets
  `ignoreErrors = true` for the same reason. **This protection is on borrowed time**: it
  works only while V1 exists to detect real session expiry — see gap 7 for the V1-sunset
  plan and the `treatStelaUnauthorizedAsSessionExpiry` switch (added 2026-08-13, OFF).

## Response

```json
{
  "items": [ { ...folder or record... } ],
  "pagination": { "nextCursor": "...", "totalPages": 0, "nextPage": "..." }
}
```

### Contract-failure rules (drive the V1 failsafe)

- A 2xx body **without an `items` key is a contract failure, not an empty folder**. Only a
  present-but-empty `"items": []` means "verified empty".
- Every item must carry a positive numeric id for its kind (`folderId` for folders,
  `recordId` for records), a positive `folderLinkId`, and a non-empty `archiveNumber` —
  the retained V1 writes (delete/move/rename/share) key on `folder_linkId` + `archiveNbr`.
  One bad item fails the whole fetch → V1 failsafe.

### Items — field notes

Discrimination: **`isFolder = folderId != null && recordId == null`**. (An `itemType`
field ("folder"/"record") does appear on the wire — verified staging 2026-07-23 — but the
merged iOS code ignores it and discriminates by id presence; Android does the same.)

| Field | Notes |
|---|---|
| `recordId`, `folderId`, `archiveId`, `parentFolderId`, `parentFolderLinkId`, `folderLinkId` | **Numeric strings** (e.g. `"12345"`). Convert to Int at the mapper boundary only, with a `-1` sentinel for missing/non-numeric (`ItemMapper.toRecordV2`). The contract sheet's claim that `folderLinkId` is an integer is wrong — the wire sends strings. |
| `archiveNumber` | Non-numeric string (e.g. `"0001-test"`) — **never** int-convert. Also available nested as `archive.archiveNumber`. |
| `displayName` | Same as V1. |
| `displayDate` / `displayTimestamp` | Split by kind: **records → `displayDate`**, **folders → `displayTimestamp`** (ISO-8601). Normalize with the existing `replace("T", " ")`. `fileCreatedAt`, `createdAt`, `updatedAt` also present. |
| `type` | **Records keep dotted legacy forms** (`type.record.image`); **folders answer new short forms** (`private`; section roots are `private-root`/`public-root`/`app-root` — live-verified 2026-08-20, the earlier `root.private` example here was wrong). Android normalizes folders back to `type.folder.<short>` in the mapper so downstream consumers see V1-shaped values. Confirmed intentional server-side (`prettifyFolderType` in stela). |
| `status` | Same split: records dotted (`status.generic.ok`), folders short (`ok`, `copying`, `moving`). `copying`/`moving` → item non-tappable (`isProcessing`). There is **no `thumbStatus`** — a file with no thumbnails yet is treated as still processing (same derivation as the shares screen's `Record(ItemVO)`). |
| `parentFolder { id, folderLinkId }` | **Folders nest** parent info here; **records send it flat** (`parentFolderId`/`parentFolderLinkId`). Resolve flat-then-nested. Added in stela PR #773. |
| `paths { names, folderLinkIds, archiveNumbers }` | Full breadcrumb trail (stela PR #773). Android doesn't consume it (breadcrumbs are the client-side `folderPathStack`). |
| `size` | Bytes; present on folders too. |
| `accessRole` | **Top-level, caller-resolved, share-membership-aware** — stela computes least-permissive of caller-archive role and share role per pair, most permissive overall. New field: delivered by PER-10716 (2026-08-13). **Wire form on children is DOTTED** (`access.role.viewer`, live capture 2026-08-14) though the backend test uses short `"viewer"` — the mapper's dot-tolerance handles both. Decoded since VSP-1802 (`ItemMapper.toRecordV2` → `AccessRole.fromStelaBackendValue`, absent clamps to VIEWER). Platform difference for cross-platform QA: iOS derives child roles by inheritance instead of this field. |
| `shares[]` | `{ id, accessRole, status, archive { id, archiveNumber, name, thumbs } }`. **Populated for owner bearer requests** (verified live on staging 2026-07-23) — the earlier share-token capture's `null` was flavor-specific. Status filtering **differs by item kind** (2026-07-31): **record items** keep everything but deleted (`get_records.sql`), so `status.generic.pending` entries appear; **folder items** are filtered to `status.generic.ok` (`get_folders.sql`) — see gap 2. Feeds the shared/pending badge only — **item permissions stay archive-derived** (see gaps). |
| `pendingShares[]` | `{ id, email, name, accessRole }` — pending **email invitations** only (`invite_share` table), and only for owner/manager callers. Pending **shares to existing archives** (`share` rows with `status.generic.pending`) ride in `shares[]` on **record items** but are absent from **both** arrays on **folder items** — see gap 2. |

### Thumbnails — the rules that matter

- **Settled contract (backend confirmed 2026-07-27: no further changes planned; verified
  against a fresh JPG upload, record 89646, 2026-07-24):** records send the `.thumb.wNNN`
  renditions both flat (`thumbUrl200/500/1000/2000`) and nested under `thumbnailUrls`
  (`"200"…"2000"`, duplicates); **folders send only the nested object**. A fresh upload has
  no flat `thumbnail256` at first — the 256 access copy appears once processing finishes.
  Android reads the **nested** object only (covers records and folders alike; the flat
  `thumbUrl*` duplicates are not read on V2), with flat `thumbnail256` preferred when
  present.
- **Nested `thumbnailUrls."256"` is a LAST resort only, never for HEIC** *(refined 2026-07-30
  per iOS PR #575, replacing the earlier "never use" rule)*: it is the Archivematica
  access-copy thumbnail, blank for HEIC originals (white square). The `.thumb.wNNN` renditions
  always win — but a record created via the Stela V2 copies endpoint gets **no** renditions
  (backend gap), so the access copy is the only thumbnail it has — **and live QA
  (2026-08-24, records 90925/90926) showed even that is missing: see the copy section's
  thumbnail paragraph; V2 copies currently get no thumbnail at all, on any client**. Android
  (`ItemMapper.accessCopyThumb256`) uses it as the final fallback in the 200 slot, guarded by
  HEIC detection (`files[]` original format/type first, `uploadFileName`/`downloadName`
  suffix fallback). The 256/blur slot stays flat-`thumbnail256`-only, matching iOS
  `resolvedThumb256`.
- A record with no `thumbnail256` and no nested 200 is treated as still processing
  (matches V1's spinner behavior for fresh uploads).
- Thumbnail URLs may arrive as **empty strings instead of null** — treat empty as absent.

### Pagination — decoded but not honored (deliberately)

- `totalPages` is unreliable (live capture: `0` with 9 items).
- `nextCursor` is **non-null even when the whole folder fit in one page**, so cursor-loop
  termination cannot rely on it — the reason both platforms ship the huge single `pageSize`
  until real pagination is specced.
- An undocumented `pagination.nextPage` convenience URL exists (itself on the plural path).
- Residual risk of the huge-pageSize workaround: if the server ever clamps `pageSize` below a
  folder's real size, listings would **silently truncate** (iOS carries the same known risk).

## Supporting endpoint

`GET api/v2/folders?folderIds[]={id}` — batch folder metadata (`folderId`, `folderLinkId`,
`displayName`, `shares[]`, `pendingShares[]`). Same headers. The singular `/folder` form is
the deprecated alias (Android's existing `StelaAccountService.getFolder` still uses it for
share management — migrate opportunistically).

## Record copy — POST /v2/records/{id}/copies (VSP-1790, verified 2026-08-24)

The first V2 **write** on the file surface. Verified against iOS `Development` (#575's copy
work, intact through the #587 merge — later PRs only changed the flag default and V1 HEIC
guards) and the stela `main` source (`packages/api/src/record/service.ts`); the published
stela docs page omits the request/response schema entirely.

**Request** — `POST api/v2/records/{recordId}/copies`, headers `Content-Type:
application/json` + `Request-Version: 2` + bearer, no share token. Body is exactly one key:

```json
{ "destinationFolderId": "42" }
```

The destination **folderId** (not `folder_linkId`, not `archiveNbr`) as a numeric string —
iOS pins the one-key body in a unit test. One request per record, **serial**; there is no
batch form and no folder-copy route (folders stay V1). Android: `StelaAccountService.copyRecord`
via `NetworkClient.copyRecordV2`, body model `CopyRecordV2Request`.

**Response** — `200` with `{ "data": <full V2 record> }`, but neither platform decodes it:
success is the 2xx alone, and the pasted item surfaces through the normal destination-folder
refetch. Errors use the standard V2 envelope: `400` (incl. "Not enough storage to make a
copy"), `403`, `404`, `500`.

**Why V2**: the copy runs in **one DB transaction** server-side (storage check + copy), so a
failed copy leaves nothing behind — the fix for V1's orphaned invisible files (PER-10599).

**Source eligibility (client gate, both platforms)** — records only, `recordId > 0`, and the
record's archive must be the **session archive**. The server requires Owner on the *origin*
archive for cross-archive copies, so shared-with-me / shared-by-me / public-gallery foreign
sources 403 on V2 and stay V1 (the #582 foreign relaxation covered reads only). Android gates
in `FileRepositoryImpl.isEligibleForStelaCopy` against `prefsHelper.getCurrentArchiveId()` —
deliberately *not* iOS's `currentArchive` predicate, which reads the *viewed* archive and is
a known latent bug (#576 fixed the same class for rename via `isInSessionArchive`; copy was
never migrated). Destination side, the server wants Curator+ — **Manager+ when the
destination is the public or app workspace** (affects publish; QA with a Curator).

**Failure semantics — no failsafe, ever.** This write inverts the navigation tickets' V1
failsafe rule: an ambiguous V2 failure (timeout after the server committed) retried on V1
would duplicate the copy. Both platforms: no V1 fallback, no retry, all failures collapse to
the generic error, refresh shows server truth. Multi-select is best-effort serial (a failed
item doesn't abort the rest); the aggregate reports success only if every item succeeded.
The POST carries **no idempotency key**, so a blind client retry after a timeout can
duplicate — open backend question. Mixed selections split: eligible records → V2 serial,
folders + foreign items → one V1 batch after them. PUBLISH rides the same routing (it is a
copy into the public workspace; destination folderId from `getPublicRecordFolderId()`,
0/missing → pure V1). MOVE never touches V2.

**Fresh-copy thumbnails — worse than documented (live-verified 2026-08-24, staging,
records 90925/90926).** A V2-copied record gets **no thumbnail at all, permanently** —
not "no renditions until processing finishes". The copy POST's own 200 response and every
later children refetch (+1 min, +4 min) return `status.generic.ok` with all five
`thumbnailUrls` slots, all flat `thumbUrl*` fields and `thumbnail256` **empty**, and
`files[]` holding only the copied original. The web app shows no thumbnail for the copy
either — this is server-side, source-verified in stela `main`: `copy_record.sql`'s record
INSERT **omits every `thumburl*`/`thumbnail256` column**, its file INSERT copies only the
`file.format.original` row (no access copy), the `file` `copy` event row is audit-only,
and the `access_copy_attacher` is an S3-upload-triggered Lambda that never fires for
copies. Nothing regenerates the missing thumbnails. Consequences: Android's
"file without thumbnail = still processing" rule leaves the pasted copy a **permanent
spinner and non-tappable** (worse than web's blank slot) — QA-blocking for VSP-1790;
backend ask raised (copy the thumb columns from the source record — same bytes, same
renditions — or enqueue regeneration for copies). Re-verified 2026-08-25: record 90925
still had zero thumbnails 26+ hours after creation. **Client mitigation designed,
deferred (2026-08-26)**: the copy's original file *is* viewable (`FileData` falls back
to the original when no access copy exists; the progressive viewer's S5 state handles
the missing thumbnail), so tap can be re-enabled by splitting `isProcessing` — spinner
stays, a new `Record.isTapBlocked` (true on V2 only for `copying`/`moving` status,
mirrors `isProcessing` on V1) feeds the three `onRecordClick` guards. Trade-off: also
un-blocks mid-processing fresh uploads on flag-on listings. Android otherwise keeps its V1 paste
surfacing (optimistic insert + one 3 s refresh, no iOS-style polling — decision
2026-08-24).

**401 divergence (deliberate)** — iOS treats a copy 401 as real session expiry
(`ignoreErrors=false` on writes; safety comes from their gate). Android's
`UnauthorizedInterceptor` never treats Stela-host 401s as expiry while
`treatStelaUnauthorizedAsSessionExpiry` is false, so a V2 copy 401 surfaces as an ordinary
error and can never log the user out — strictly safer, revisit with gap 7's switch.

## Impact summary (updated 2026-07-31)

With the flag ON, only one thing visibly breaks on Private Files: the **pending badge
undercounts on FOLDER rows** (gap 2 below — record rows are fine, live-verified
2026-07-31). Everything else falls back to V1 or is handled in the app. What remains of
gaps 1 and 2 is one backend theme — *send complete share/badge data on children* — with
gap 2's concrete one-line filter ask already raised (2026-07-31); gap 1's `accessRole`
half was resolved by decoding the existing payload field (VSP-1802). Gaps 3–6 break
nothing today; they only block future migration tickets, and those surfaces simply stay
on V1.

## Known backend gaps (as of 2026-07-23)

1. **~~No per-item caller `accessRole`~~ — the accessRole half is DELIVERED:** shipped
   by the backend as **PER-10716** (stela PR #835, merged 2026-08-13; Jira Done), live
   on staging (capture 2026-08-14), decoded by Android since VSP-1802 — see its section.
   The earlier "missing" reports were accurate until that delivery. **What remains:**
   `shares[]` on descendants inside a shared tree is still incomplete (the hydration SQL
   only aggregates direct share rows), so the pending/share **badge** data on shared-tree
   children is thin — display-only impact, same theme as gap 2.
2. **V2 drops pending shares-to-archives on FOLDER items only — so the pending badge
   undercounts on folders.** *(Refined 2026-07-31 after backend follow-up + stela source
   verification; supersedes the earlier "in neither list for all items" wording.)*
   There are two kinds of "pending" share: an **email invite** (the person has no account
   yet) and a **share to an existing archive** (not accepted yet, e.g. via a restricted
   share link with auto-approve off + a request-access click). V1 sends both in one list
   (`ShareVOs`). On V2 the children route (`folder/service.ts:getFolderChildren`) hydrates
   the two item kinds through **different queries with different share filters**:
   - **record items** → `get_records.sql`: `share.status != 'status.generic.deleted'` —
     pending shares to archives **ARE included** in `shares[]` (with
     `status.generic.pending`). Confirmed by the backend on a live `/records` response
     and **verified live through the children call on staging (2026-07-31)**: record
     `faux-potted-cactus` (`folder_linkId 115428`, inside the Vacation folder) returns
     `{ id: 2187, status: status.generic.pending, accessRole: access.role.viewer }`;
   - **folder items** → `get_folders.sql`: `share.status = 'status.generic.ok'` —
     pending shares to archives are **EXCLUDED**;
   - `pendingShares[]` contains only **email invites** (`invite_share`) on both, and only
     for owner/manager callers.
   Our staging capture (2026-07-23, Vacation **folder**, `folder_linkId 115425`, pending
   `shareId 2010` present on V1 and absent on V2) and the backend's record example are
   both consistent with this split — and the folder side was **re-confirmed live in the
   same 2026-07-31 children capture** (the Vacation folder item still returns only its OK
   owner share; 2010 remains absent).
   **Android impact:** `ItemMapper.buildShares` already normalizes
   `status.generic.pending` inside `shares[]`, so record rows badge correctly today and
   folder rows will start working with **no app change** once the backend aligns the
   filter. **Ask (one line):** make `get_folders.sql` use the same
   `!= 'status.generic.deleted'` filter as `get_records.sql`. Fix needed before the
   production flag flip. Both sides are live-verified on staging (2026-07-31, one
   children capture session): pending present on the record item, absent on the folder
   item.
3. **No V2 folder-creation endpoint** (`POST /v2/folders` does not exist) — folder creation
   stays on V1 `folder/post`. Blocks the folder-creation migration follow-up ticket.
4. **Folder PATCH** (`PATCH /v2/folders/{id}`) now appears in the stela docs; the
   sort-persistence re-sort flow (PATCH then re-fetch children) is a future ticket —
   re-verify the endpoint when that is specced.
5. **No V2 route from public-link ids to V2 ids** *(found in VSP-1810, raised with the
   backend 2026-08-06 — comment on VSP-1810)*. Public deep links carry the V1 address
   (`archiveNbr` + `folder_linkId`; record links carry `file_archive_nr`), while V2 is
   addressed by `folderId`/`recordId` — and the mapping lives only in the backend's
   database (the apps open these links today only because V1 `navigateMin` accepts the
   link's ids directly). Ask, preferred first: (a) a resolver
   (`folder_linkId → folderId`, ideally covering records too), or (b) `/children`
   accepting a `folderLinkId` address. Changing the URL format alone doesn't fix
   already-shared links. Blocks nothing today (V1 fallback); prerequisite for the V1
   navigation sunset, same bucket as the remaining `getPublicRoot` root discovery
   (foreign archives and public links; own-archive Public Files migrated in VSP-1839).
   **Fuller inventory (all workspaces swept, 2026-08-11):** this is one of FOUR V1
   link-address dependencies — see `docs/stela-v2-link-migration.md` for the complete
   picture (also record `archiveNbr → recordId`, share `token → shareLinkId`, and
   `archiveNbr →` public root `folderId`) and the ranked backend asks covering all four.
6. **No V2 search endpoint** *(confirmed during VSP-1806, 2026-08-11)*. The search query
   itself stays on V1 `POST search/folderAndRecord` (hard-capped at 10 results, no
   pagination) — neither the stela docs nor the merged iOS code have any V2 search
   route; iOS ships the identical V1 call. VSP-1806 migrated only the *drill-in from* a
   search result (possible because results already carry `folderId`); the query is the
   remaining V1 dependency on the search screen. Blocks nothing today; prerequisite for
   the V1 sunset, same bucket as `getShares` and root discovery. Future ticket once the
   backend exposes a search route — a chance to also lift the 10-result cap.
7. **401 is not reserved for session expiry — blocks moving logout detection to the
   Stela host** *(raised 2026-08-13, VSP-1803 follow-up discussion)*. Today Android
   detects session expiry only on the V1 host (`UnauthorizedInterceptor` matches
   `BASE_API_URL` alone) — which is exactly what makes V2 401s degrade safely to the
   V1 failsafe instead of logging the user out. When V1 is sunset that mechanism
   detects nothing: an expired session would never log out — every screen would just
   silently fail. The fix (option A) is to extend the interceptor to the Stela host;
   the code is already in place behind
   `FeatureFlags.treatStelaUnauthorizedAsSessionExpiry` (**default OFF in every
   build**, added 2026-08-13). **It must stay OFF until the backend reserves 401 for
   invalid/expired tokens and returns 403 for permission denials.** Known
   counterexample under today's semantics — iOS PR #576's reproducer: a bearer-only
   `PATCH /api/v2/records/{id}` on a shared-with-me (foreign-archive) record answers
   **401**, not 403, for what is a permission/credential-shape problem; enabling the
   switch now would log users out on exactly the failures the failsafe exists to
   absorb (iOS's #576 bug, reintroduced on Android). **Ask:** confirm or change the
   401/403 split across `/api/v2/*`; then the rollout is one boolean flip.
   **Backend answer (Slack, 2026-08-17, Liam): confirmed.** The intent is 401
   strictly for invalid tokens; the shared-record PATCH answering 401 is acknowledged
   as a bug — it should be **403**, or **no error at all when the share grants
   sufficient permissions** (which also means shared-record writes over bearer V2 are
   *intended* to work; revisit the write gates when the fix ships). The switch stays
   OFF until the fix lands and is re-verified on staging (re-run the #576 reproducer);
   then the rollout is the one boolean flip. Fallback options below are moot unless
   the fix stalls: a token-refresh `Authenticator`
   (needs `/v2/idpuser/*` refresh support — unverified) or per-call exemption tags
   (iOS's `ignoreErrors` shape). Not urgent while any regularly-hit call is still V1
   (login, `getShares`, root discovery all are) — but a hard prerequisite for the V1
   sunset, same bucket as gaps 5–6. Enable-time note: with the switch ON the
   interceptor buffers every Stela response body to a `String` to inspect it (same
   as V1 today) — that includes the large single-page children listings, so keep it
   in mind for any future response-size profiling.

## Spec-vs-reality discrepancies found (do not trust these in the docs/tickets)

| Doc/ticket claim | Reality |
|---|---|
| `itemType` discriminator | Present on staging (2026-07-23), but unused — both platforms discriminate by `recordId`/`folderId` presence (iOS models predate it) |
| `folderLinkId: integer` | Numeric **string** |
| `/folder/<id>/children` (singular, Jira) | Alias; use plural |
| Reliable `nextCursor`/`totalPages` | Both unreliable (above) |
| Uniform dotted enum values | Folders use short forms, records dotted |
| iOS gated by a "remote flag" | iOS's merged flag is a compile-time constant (Android mirrors: `FeatureFlags.useStelaMigration`) |
| iOS status board: Public Files nav = "VSP-1809, shipped in PR #574" | Merged code: drill-in shipped in **PR #573** (inheritance); PR #574 is **VSP-1787** (V2 root discovery). Board lags/mislabels |
| iOS artifacts: foreign public browsing "confirmed solvable, not yet wired" | Superseded — **PR #576** (merged 2026-07-29) wired it (drill-in V2, root stays V1 `getPublicRoot`) |
| iOS artifacts + old gap 1: "V2 children carry no per-child caller `accessRole`" | Was true when written — the field was delivered 2026-08-13 by PER-10716 (stela PR #835). Android decodes it since VSP-1802; iOS derives child roles by inheritance |
| PER-10716 folders-page test: `accessRole` = short `"viewer"` | Resolved 2026-08-20: stela **main** now emits the short form for the caller-level `accessRole` on folder/record/children responses (`resolveAccessRole` returns `ArchiveMembershipRole` — source-verified), but **staging still sends dotted** (live capture 2026-08-20) — the deploy is pending. Web adapted ahead of it (web-app PR #1139). Android is safe both ways: `fromStelaBackendValue`'s dot-tolerance parses both forms, and **released** Android builds don't decode the field at all (master's ItemDTO/RecordResponse/FolderResponse have no caller accessRole; Moshi drops unknown keys). `pendingShares[].accessRole` and `shares[].accessRole` are raw DB passthrough (dotted) — NOT routed through `resolveAccessRole`, so they don't change with this deploy; their parsers clamp unknowns to VIEWER anyway |
| Access map: "share-membership foreign content ✗ on V2" | Wrong for READS — inferred from #576's PATCH (a write). Reads authorize via the `access` table, descendants included; corrected 2026-08-14 |
| VSP-1788 ticket: "the call to change archives should return the rootFolderId" | It does not — live staging capture 2026-08-19 shows no root identity anywhere in the `archive/change` response; the source is `GET /v2/archives` `items[].rootFolderId` |
| Field-notes short-form example `root.private` for section roots | Wrong — live staging (2026-08-20) sends `private-root`/`public-root`/`app-root`, matching iOS's `FileType.fromV2` spellings. Android matches `type.folder.private-root` post-normalization (underscore tolerated), display name as safety net |

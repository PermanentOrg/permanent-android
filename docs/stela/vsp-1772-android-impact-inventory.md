# VSP-1772 — Android Impact Assessment: `navigateMin` + `getLeanItems` → `GET /v2/folders/{id}/children`

**Ticket:** [VSP-1772](https://permanent.atlassian.net/browse/VSP-1772) · **iOS counterpart:** VSP-1684 · **Backend blocker:** [PER-10476](https://permanent.atlassian.net/browse/PER-10476) (stela [PR #773](https://github.com/PermanentOrg/stela/pull/773))
**Scope:** Investigation only — call-site inventory and impact analysis for the Permanent Android app. No code changes.
**Sources:** Android codebase (branch `feature/VSP-1778`, July 2026), Stela OpenAPI spec extracted from https://permanentorg.github.io/stela/ (treated as *indicative, not authoritative* — see PER-10476), Jira VSP-1772 / VSP-1684 / PER-10476. iOS findings taken from the VSP-1684 analysis (Lucian Cerbu, Feb 2026).

---

## 1. Executive summary

Every usage of the legacy two-step navigation pattern on Android funnels through a single repository method — `FileRepositoryImpl.getChildRecordsOf()` — consumed by **5 ViewModels across 6 screens** (My Files, Public Files, Shared-with/by-me, Public Archive, Public Folder/Gallery, Search drill-in). Upload flows and file operations never call the pattern directly; they only trigger a folder refresh afterwards. No tests exercise it.

Crucially, **Android already calls the new children endpoint in production**: `StelaAccountService.getFolderChildren()` (`GET api/v2/folder/{folderId}/children`, share-token auth, `pageSize=99999999`) powers the share-link preview via `FolderChildrenResponse`/`ItemDTO`/`ItemMapper.toRecord()`. The migration is therefore an *extension* of an existing, working stack (add bearer-token flavor, cursor, and the missing item fields), not greenfield scaffolding.

**Biggest risks / complexity drivers, in order:**

1. **Permission derivation — backend-blocked for the Shares surface** — the new contract has no item-level `accessRole`; roles gating the Shares item menu must be derived from `shares[]`. But the live capture (July 22) returned `shares: null`, and the **iOS contract sheet (July 21) confirms this is a V2 gap**: incomplete `shares[]` / no caller `accessRole` blocks their Shared drill-in too, and "caller accessRole + complete shares[]" sits on their P2 backend-asks list. Android's Shares browsing has the same dependency.
2. **Sorting model changes** — the new endpoint has no `sort` parameter *by design*: sort is a **folder attribute**, and `/children` returns items in the folder's stored order. Per the backend team (July 22): that attribute can't be edited in the new API yet; `sort` will be added to the **folder PATCH** endpoint. Until then, sort client-side over a full fetch (iOS's current approach). Once PATCH ships, re-sorting becomes **two calls** (PATCH the folder, then re-fetch children) and the order **persists server-side on the folder** — a semantic change from today's per-request sort. Backend discussion (July 22–23): returning children from PATCH would break their pagination conventions, so the two-call flow is considered **inevitable** — plan for PATCH + re-fetch (a `PATCH /folders/{id}/children` variant was floated but is unresolved).
3. **Cursor pagination vs. full-list assumptions** — file-viewer swiping, select-all, empty-state detection, and the adapter's replace-everything update model all assume the complete folder is in memory. A phase-1 "huge pageSize" approach defers this — this is exactly what iOS ships today (`pageSize=99999999`, pagination decoded but not honored), and the app's own share preview already does it (`StelaAccountService.kt:49`).
4. **Int → String IDs** — the contract change is real, but conversion belongs at the mapper boundary: **iOS did exactly this** (numeric strings → Int at the boundary with a −1 sentinel; the Feb 2026 "200+ file Int→String migration" claim did not survive their implementation), and Android's `ItemMapper.toRecord()` already uses `toIntOrNull()`.

**PER-10476 exposure: RESOLVED — verified live on production** *(July 22, 2026)*. Android consumes `folder_linkId` (navigation-critical), `archiveNbr`, and `parentFolder_linkId` on every browsed item, and the folder-level equivalents were originally missing from the deployed endpoint. Stela **PR #773 merged June 15, 2026**, shipped in **v0.87.0 (deployed to prod June 23** per the repo's GitHub deployment records; prod is on v0.89.0 since July 16). **Confirmed against a live production response** (children call via the app's share-preview flow, logged through OkHttp): folder items return `folderLinkId`, `archiveNumber`, `parentFolder.{id, folderLinkId}`, and populated `paths.{names, folderLinkIds, archiveNumbers}`. No blocker remains on this front.

**Path discrepancy: resolved (true aliases).** The Jira ticket says `/folder/<folderId>/children` and the app's existing share-preview call uses **singular** `api/v2/folder/{folderId}/children`; the published spec documents **plural** `/v2/folders/{id}/children`. Verified July 22 by replaying the same authenticated request against both paths on production: **byte-identical responses** (same items, same pagination). Prefer the plural (documented) form for new code.

---

## 2. Call-site inventory

### 2.1 The pattern itself (network + repository layer)

| File | Class · function | Call | Purpose / data consumed |
|---|---|---|---|
| `app/src/main/java/org/permanent/permanent/network/IFileService.kt:19-23` | `IFileService.navigateMin` / `.getLeanItems` | both | Retrofit definitions: `@POST folder/navigateMin`, `@POST folder/getLeanItems`, both `Call<ResponseVO>` |
| `app/src/main/java/org/permanent/permanent/network/NetworkClient.kt:301-317` | `NetworkClient.navigateMin` / `.getLeanItems` | both | Builds request bodies via `RequestContainer.addFolder(...)`: sends `FolderVO{archiveNbr, folder_linkId, sort, ChildItemVOs}` |
| `app/src/main/java/org/permanent/permanent/network/RequestContainer.kt:126-150` | `RequestContainer.addFolder` | both | Request VO construction; the 4-arg overload wraps each child link id in a `RecordVO{folder_linkId}` |
| `app/src/main/java/org/permanent/permanent/repositories/IFileRepository.kt:27-44, 110-113` | `IFileRepository` + `IOnRecordsRetrievedListener` | both | Contract: `getChildRecordsOf(archiveNr, folderLinkId, sort, listener)`; listener returns `(parentFolderName: String?, recordVOs: List<RecordVO>?)` |
| `app/src/main/java/org/permanent/permanent/repositories/FileRepositoryImpl.kt:90-148` | `FileRepositoryImpl.getChildRecordsOf` → `navigateMin` → `getLeanItems` | both | **The two-step chain.** Step 1 collects every child's `folder_linkId` from the navigateMin response; step 2 sends the full id list + `sort` to getLeanItems; delivers parent `displayName` + `List<RecordVO>` |

**Two-step data flow:** `navigateMin` response → `getRecordVOs()[].folder_linkId` (only field read) → becomes `ChildItemVOs` of the `getLeanItems` request → `getLeanItems` response → `getFolderRecord().displayName` + full `getRecordVOs()` → listener. All children are fetched in **one** getLeanItems call — no chunking, no paging.

`getChildRecordsOf` is the **only outward-facing entry point**; the repository's `navigateMin`/`getLeanItems` have no other callers. [Android-specific — iOS has no equivalent single funnel]

### 2.2 Browse / navigation screens [matches iOS — "every screen that shows files/folders"]

All five ViewModels depend on **both** calls (via `getChildRecordsOf`) and consume the result identically: each `RecordVO` is wrapped into a domain `Record` via `Record(recordVO)` (`Record.kt:75-103`).

| # | ViewModel (call site) | Screen(s) | Triggered by | Data consumed |
|---|---|---|---|---|
| 1 | `MyFilesViewModel.loadFilesOf` — `viewmodels/MyFilesViewModel.kt:165` | `ui/myFiles/MyFilesFragment.kt` (private workspace) | root load (`loadRootFiles:128`), folder tap (`onRecordClick:246`), back (`onBackBtnClick:273`), pull-to-refresh + post-op refresh (`refreshCurrentFolder:157`), sort change (`setSortType:382`) | `recordVOs` → list/grid rows; ignores `parentFolderName` |
| 2 | `PublicFilesViewModel` (subclass of #1) — `viewmodels/PublicFilesViewModel.kt:28-34` | `ui/public/PublicFilesFragment.kt` (own public workspace) | overrides `loadRootFiles` (root via `getPublicRoot`), then inherits everything from #1 | same as #1 |
| 3 | `SharedXMeViewModel.loadFilesOf` — `viewmodels/SharedXMeViewModel.kt:174` | `ui/shares/SharedXMeFragment.kt` (inside a shared folder) | folder tap (`onRecordClick:137`), back (`navigateBack:165`), refresh (`refreshCurrentFolder:297`) | `recordVOs`; ignores `parentFolderName` |
| 4 | `PublicArchiveViewModel.loadFilesOf` — `viewmodels/PublicArchiveViewModel.kt:72` | `ui/public/PublicArchiveFragment.kt` (other users' public archives) | root (`getPublicRoot` success:54), folder tap | `recordVOs` (sort fixed NAME_ASCENDING) |
| 5 | `PublicFolderViewModel.loadFilesOf` — `viewmodels/PublicFolderViewModel.kt:60` | `ui/public/PublicGalleryFragment.kt` (nested public browsing) | init (:28), folder tap (:35), back (:104) | **`parentFolderName`** (→ `onFolderNameChanged`) + `recordVOs` |
| 6 | `RecordSearchViewModel.loadChildRecordsOf` — `viewmodels/RecordSearchViewModel.kt:194` | `ui/RecordSearchFragment.kt` (open a folder from search results) | folder tap (:182), back (:241) | `recordVOs` (sort fixed NAME_ASCENDING) |

Note: the Shares **root list** (`SharesViewModel.requestShares`, `viewmodels/SharesViewModel.kt:32-38`) uses the separate `getShares` endpoint — only navigation *into* a share hits the two-step pattern. [Android-specific detail]

### 2.3 Upload flows [Android-specific: indirect only — differs from iOS claim]

Uploads (`uploadToCurrentFolder`, `UploadQueue`, presigned-URL + `registerRecord`) **never call** navigateMin/getLeanItems. Their only dependency is:

| File | Function | Relationship |
|---|---|---|
| `viewmodels/MyFilesViewModel.kt:336-343` | upload-finished callback | delayed `refreshCurrentFolder()` → re-runs the two-step fetch |
| `viewmodels/SharedXMeViewModel.kt:273` | upload-finished callback | same |
| `ui/myFiles/saveToPermanent/ChooseFolderFragment.kt` + `viewmodels/ChooseFolderViewModel.kt` | Save-to-Permanent destination | picks a *workspace* only; actual folder browsing reuses the browse VMs above |

Upload destination context (current folder's `archiveNr` + `folderLinkId`) comes from the already-navigated `Record`/`NavigationFolder` state, so uploads are affected **only through the shape of that state**, not through the API calls.

### 2.4 File operations [Android-specific: indirect only — differs from iOS claim]

Delete, rename, move/copy (relocate), publish, unshare and download all use their own endpoints (`deleteRecords`, `updateRecord`, `relocateRecords`, …). Their coupling to the pattern:

| Operation | Coupling |
|---|---|
| Delete (`MyFilesViewModel.kt:412`, `SharedXMeViewModel.kt:354/374`) | post-op `refreshCurrentFolder()` → two-step refetch |
| Move/Copy destination picker | reuses `MyFilesViewModel`/`SharedXMeViewModel` in relocation mode (`isRelocateMode`), i.e. the same browse call sites; paste = `SelectionViewModel.onPasteOrMoveBtnClick` → `relocateRecords` (`viewmodels/SelectionViewModel.kt:34-44`) |
| Rename / publish / share | operate on the `Record` fields populated by the two-step fetch (`folderLinkId`, `archiveNr`, `recordId`/`folderId`) |

### 2.5 Permission checks [matches iOS — but smaller surface]

No call site *fetches* permissions via this pattern, but the items it returns carry `accessRole`, consumed at the sites in §4.

### 2.6 Existing Stela children-endpoint integration (reuse target) [Android-specific — no iOS equivalent mentioned]

| File | What it is |
|---|---|
| `app/src/main/java/org/permanent/permanent/network/StelaAccountService.kt:45-50` | `@GET("api/v2/folder/{folderId}/children")` with `X-Permanent-Share-Token` header, `@Query pageSize` (default **99999999** — pagination deliberately bypassed) |
| `app/src/main/java/org/permanent/permanent/network/NetworkClient.kt:848` (+ `:149` base URL `BuildConfig.BASE_API_URL_STELA` = `https://api.permanent.org/` / staging) | wiring |
| `app/src/main/java/org/permanent/permanent/network/models/FolderChildrenResponse.kt` | `{ items: List<ItemDTO>? }` — **no `pagination` object deserialized** |
| `app/src/main/java/org/permanent/permanent/network/models/ItemDTO.kt` | String ids: `folderId`, `recordId`, `folderLinkId`, plus `displayName`, `displayDate`, `size`, `thumbUrl200`, `thumbUrl2000`, `archive: ArchiveDTO{id, archiveNumber, name}` |
| `app/src/main/java/org/permanent/permanent/mapper/ItemMapper.kt:8-27` | `ItemDTO.toRecord()` — converts String→Int at the boundary (`toIntOrNull()`); ⚠️ latent bug at `:22`: `rec.archiveNr = archive?.name` (should almost certainly be `archive?.archiveNumber`) |
| `app/src/main/java/org/permanent/permanent/repositories/StelaAccountRepositoryImpl.kt:232-270` | `getFolderChildren(shareToken, folderId, pageSize, listener)` → maps items → `List<Record>` |
| `app/src/main/java/org/permanent/permanent/viewmodels/SharePreviewViewModel.kt:225-245` | the one consumer today (share-link preview grid) |

This stack proves the endpoint works from the app (share-token flavor) and is the natural extension point: add a bearer-token overload, the `cursor` param, `pagination` in the response model, and the additional item fields Android needs.

### 2.7 Tests [Android-specific — differs from iOS "22 test files"]

**None.** `app/src/test` (`ValidatorTest`, `NetworkClientTest`, `MockInterceptor`) and `app/src/androidTest` contain no references to `navigateMin`, `getLeanItems`, `getChildRecordsOf`, or the five ViewModels.

---

## 3. Response-field consumption → new-contract mapping

Consumption is fully determined by `Record(recordVO: RecordVO)` (`Record.kt:75-103`) plus two direct repository reads. New-contract names are from the published spec (children of `/folders/{id}/children`: `items[]` = `oneOf folder | record`, discriminated by `itemType: "folder" | "record"`).

**Legend:** ✅ direct · 🔤 renamed · 🔢 type change (Int→String) · 🧩 derived/restructured · ❌ missing from new contract · ⚠️ **PER-10476-blocked** (listed in spec but reported missing from the deployed endpoint — verify live)

| Legacy field (`RecordVO`) | Consumed at | New contract field | Status |
|---|---|---|---|
| `folder_linkId` | `FileRepositoryImpl.kt:110-117` (drives step 2!), `Record.kt:81` → navigation identity (`getFolderIdentifier`, `Record.kt:212`) | record: `folderLinkId` (string) · folder: `folderLinkId` (string) | 🔤🔢 + **⚠️ folder-level** |
| `folderId` | `Record.kt:76,80,90` — item id + FOLDER/FILE discriminator | folder: `id` (string); discriminator becomes `itemType` | 🔤🔢🧩 (discriminate by `itemType`, not `folderId != null`) |
| `recordId` | `Record.kt:76,79` — file identity (file viewer, `record/get`) | record: `recordId` / `id` (string) | 🔢 |
| `archiveNbr` | `Record.kt:77` — feeds the *next* `navigateMin` call; thumbnails, sharing | `archiveNumber` | 🔤 + **⚠️ folder-level** — but note: the new endpoint takes only the folder **id**, so `archiveNbr`-for-navigation may become unnecessary |
| `archiveId` | `Record.kt:78` — share/move context | record: `archiveId` · folder: `archive.id` (string) | 🔢🧩 |
| `parentFolder_linkId` | `Record.kt:82` — move/copy destination context | record: `parentFolderLinkId` · folder: `parentFolder.folderLinkId` | 🔤 + **⚠️ folder-level** |
| `displayName` | `Record.kt:83` — row title, menu header, root detection (`MyFilesViewModel.kt:174`) | `displayName` | ✅ |
| `displayDT` | `Record.kt:84` — row subtitle date | record: `displayDate` · folder: `displayTimestamp` | 🔤🧩 (split naming; format to verify) |
| `type` (e.g. `"type.record.image"`) | `Record.kt:91` → `backendType`; `isImage()` (`Record.kt:210`, `FileViewFragment.kt:78`) | `type` | ✅ (value format to verify) |
| `size` | `Record.kt:92` — menu size display | `size` | ✅ (now on folders too) |
| `accessRole` | `Record.kt:93` → §4 | **removed by design** — derive from `shares[].accessRole` | ❌🧩 **permission rework** |
| `thumbURL200` | `Record.kt:86` — list/grid fallback thumb | `thumbnailUrls."200"` | 🧩 (nested object; NB deployed share-preview response returns **flat** `thumbUrl200` per `ItemDTO` — another spec-vs-deployed divergence to verify) |
| `thumbnail256` | `Record.kt:87` — preferred thumb | `thumbnailUrls."256"` | 🧩 |
| `thumbURL2000` | `Record.kt:88` — full-screen viewer | `thumbnailUrls."2000"` | 🧩 (spec also adds 500/1000 — unused today) |
| `thumbStatus` | `Record.kt:96-101` → `isProcessing` (spinner, interaction lock in `RecordListViewHolder.kt:49`) | **no equivalent** in spec | ❌ — needs derivation (e.g. empty `thumbnailUrls`, as `Record(ItemVO,…)` already does at `Record.kt:154`) or backend answer |
| `ShareVOs` | `Record.kt:94` → `Record.shares` — share management (`ShareManagementFragment.kt:181`), pending-invitation badge | `shares[]` (record: `{shareId, archiveId, accessRole, status, archive}` · folder: `{id, accessRole, status, archive{id, thumbUrl200, name}}`) + separate `pendingShares[]` | 🧩 (different shapes per item type; `Share(shareVO)` mapping in `Share.kt:25-42` needs a DTO twin) |
| **Parent folder `displayName`** (from `getLeanItems` response envelope, `FileRepositoryImpl.kt:140`) | `PublicFolderViewModel` (`onFolderNameChanged`); others ignore it | **not returned** — response contains only `items` + `pagination` | ❌🧩 — already available locally from the tapped `Record`; or `GET /v2/folders/{id}` |
| *(request param)* `sort` | sent in `getLeanItems` body (`NetworkClient.kt:312`), from `SortType.toBackendString()` | **no `sort` query param in spec** | ❌ **open question** (see §5.3) |

**Legacy fields returned but never consumed** on this path (no migration work): `id`, `description`, `createdDT`, `updatedDT`, `derivedDT`, `derivedCreatedDT`, `uploadFileName`, `dataStatus`, `parentFolderId`, `LocnVO`, `FileVOs`, `TagVOs`, `status`, and `FolderVO.sort/position/thumbArchiveNbr/paths` (breadcrumbs are maintained client-side via the `folderPathStack`/`NavigationFolder` stack — Android consumes **no server-side path fields**, so `paths.folderLinkIds`/`paths.archiveNumbers` from PER-10476 are ⚠️-listed for completeness but **not currently needed by Android**). [Android-specific — differs from web-app, which needed `paths.*`]

**ID type migration [matches iOS, narrower]:** every id above changes Int→String. Domain `Record`, `NavigationFolderIdentifier`, prefs and downstream endpoints (`record/get`, `relocateRecords`, upload registration…) use `Int`. Two strategies: (a) convert at the DTO/mapper boundary with `toIntOrNull()` — zero downstream churn, existing precedent in `ItemMapper.toRecord()`; (b) migrate `Record` ids to String — safer long-term (backend may eventually issue non-numeric ids) but touches every consumer of `Record.id/folderId/recordId/folderLinkId`. iOS chose a full String migration (200+ files); Android can defer via (a) and keep this ticket's scope contained.

---

## 4. Permission system analysis — the largest complexity driver

### 4.1 Where an item-level access role is read today

| Site | What it drives |
|---|---|
| `models/Record.kt:93` — `accessRole = AccessRole.fromBackendValue(recordInfo.accessRole)` (default **VIEWER** when absent — `AccessRole.kt:156-165`) | population |
| `viewmodels/RecordMenuViewModel.kt:101-102` — `actualAccessRole = record.accessRole?.getInferior(CurrentArchivePermissionsManager.instance.getAccessRole()) ?: VIEWER` | the effective role for the item options menu |
| `RecordMenuViewModel.kt:210-228` (SHARES workspace) | hides **Share** (needs ownership), **Rename** (edit), **Move**, **Copy**, **Delete** per `actualAccessRole` |
| `RecordMenuViewModel.kt:245-248` (FILE_VIEW_SHARED_FILES workspace) | hides **Share** |
| `viewmodels/FileInfoViewModel.kt:39-41` — `fileData.accessRole` (VIEWER/CONTRIBUTOR → read-only) | file-metadata editability. NB: populated from `record/get`, not from this endpoint — same field family, listed for completeness; `record/get` migration is out of this ticket's scope |

**Where item role is *not* used [Android-specific]:** the PRIVATE_FILES / FILE_VIEW_PRIVATE_FILES / PUBLIC_FILES menu branches (`RecordMenuViewModel.kt:145-183`) and the row options-button gate (`ui/myFiles/RecordListViewHolder.kt:30-31`) use the **archive-level** role from `CurrentArchivePermissionsManager` (populated at archive switch, from prefs). So in practice, **only the Shares surfaces consume the item-level role today.** This materially shrinks the reimplementation relative to iOS's "permissions are calculated per-folder from the navigation data".

### 4.2 What the replacement derivation needs to look like (conceptually)

The new contract intentionally drops item-level `accessRole` (per Liam Lloyd on PER-10476: non-owner values of that field have been deprecated **since 2020** — meaning the value Android reads today is likely always `owner` on owned items, and only meaningful via shares anyway):

1. **Own archive browsing (My Files / Public Files):** no derivation needed — keep using `CurrentArchivePermissionsManager` (archive-level role), which these screens already do.
2. **Shares browsing (SharedXMe):** "my" role on an item = the entry in `item.shares[]` whose `share.archive.id` equals the **current archive id** (`prefsHelper` / `CurrentArchivePermissionsManager` context), with `status` accepted (`status.generic.ok`); take its `accessRole`. Fall back to VIEWER when no matching share — same default as today. The existing `getInferior(archiveRole)` combination logic (`AccessRole.kt:118-120`) can stay as-is.
3. **Public browsing:** effectively read-only today (options limited); no shares matching needed — VIEWER fallback covers it.
4. **Verification needed:** whether the deployed `shares[]` on children items is populated for the *share recipient's* archive (the share-preview endpoint flavor is unauthenticated-ish via token; the bearer flavor must return the recipient's share entries for this to work).

**Effort note:** the derivation itself is a small pure function (+ tests), but it must be validated against live data for each browsing context (own / shared / public), and the `pendingShares` array is new (today pending invitations ride in `ShareVOs` with a pending status — the badge logic in `RecordListViewHolder.kt:32` keys off that). Recommend keeping this as **its own effort line and its own PR**.

---

## 5. Pagination & navigation-flow analysis

### 5.1 Current model (full-folder, two round-trips)

```
open folder ──▶ POST folder/navigateMin  (archiveNbr + folder_linkId)
                    │  response: ALL children, only folder_linkId read
                    ▼
               POST folder/getLeanItems (archiveNbr + folder_linkId + sort + ALL child linkIds)
                    │  response: parent displayName + full RecordVO list
                    ▼
               ViewModel: List<Record> ──▶ RecordsListAdapter.setRecords() + notifyDataSetChanged()
```

- No paging anywhere: no Paging library, no scroll listener, no chunking of the linkId list (`FileRepositoryImpl.kt:110-117,134`; `ui/myFiles/RecordsListAdapter.kt:50-57`).
- No caching: back-navigation, sort change, pull-to-refresh, and post-upload/delete all **refetch the whole folder** (`MyFilesViewModel.kt:155-158, 268-280, 379-382`); contents live only in the ViewModel + adapter (`folderPathStack: Stack<Record>` at `MyFilesViewModel.kt:87` holds the *path*, not contents).
- Sort is server-side via the request `sort` string; no local re-sort.

### 5.2 New model (single call, cursor + pageSize)

`GET /v2/folders/{id}/children?pageSize=N[&cursor=folderLinkId-of-previous-item]` → `{ items[], pagination: { nextCursor, totalPages } }`. One call replaces two (halves round-trips per navigation — the main backend win), and the folder is addressed by **id** alone (no `archiveNbr` needed for the request).

### 5.3 What breaks / must change under true cursor paging

| # | Full-list assumption | Where | Impact |
|---|---|---|---|
| 1 | Two-step call shape & listener contract | `FileRepositoryImpl.kt:90-148`, `IOnRecordsRetrievedListener` | replaced wholesale; single funnel = **one place to swap** |
| 2 | File-viewer swipes by position over the whole folder | `ui/fileView/FilesContainerFragment.kt:58-67` + `FilesContainerPagerAdapter` over `FileSessionData.records` snapshot, fed from the in-memory list (`MyFilesViewModel.kt:252-266`) | swiping past the last loaded page hits missing records; needs page-fetch-on-demand or full prefetch |
| 3 | Select-all operates on the loaded list | `MyFilesViewModel.kt:218-219` → `SelectionViewModel.onSelectAllRecords` (`SelectionViewModel.kt:61-76`) | "select all" only covers loaded pages |
| 4 | Empty-state from the single response | `existsFiles.value = !recordVOs.isNullOrEmpty()` (`MyFilesViewModel.kt:186` and analogues in all 5 VMs) | fine for page 1, but "empty page ≠ empty folder" semantics must be pinned down |
| 5 | Adapter replaces entire dataset | `RecordsListAdapter.setRecords` + `notifyDataSetChanged` | needs an append path (`DiffUtil` or Paging 3) |
| 6 | Refetch-on-everything | refresh/sort/back paths above | must reset the cursor; back-navigation ideally restores pages + scroll position (today it silently refetches everything) |
| 7 | **Sorting** | `sort` request param today; **no sort param in the new contract — by design** | sort is a folder attribute; `/children` returns the folder's stored order. Backend will add `sort` to the folder PATCH (announced July 22, not yet available). The in-app sort picker (`SortOptionsFragment` → `setSortType`) then maps to PATCH + re-fetch (two calls; flow questioned with backend). Until PATCH ships: client-side sort — which requires the full list, conflicting with true cursor paging |

### 5.4 Pragmatic phasing option [Android-specific]

The app's own share-preview integration already bypasses pagination with `pageSize=99999999` (`StelaAccountService.kt:49`). A two-phase migration keeps risk down:

- **Phase 1 — endpoint swap, no UX change:** call the new endpoint with a large `pageSize`, keep delivering a full `List<Record>` through the existing listener shape, and sort client-side (no `sort` param exists). Items 2–6 above stay untouched; only the funnel + models + permissions change. `pageSize=99999999` is confirmed accepted on production (July 22 capture).
- **Phase 2 — real paging:** adopt cursor paging (Paging 3 or manual append) and address items 2–6 deliberately, screen by screen. Note `totalPages` and `nextCursor` reliability caveats (§7 item 7).

**This phasing is exactly what iOS ships** (per their July 21, 2026 status board): all V2 navigation runs behind a `use_stela_navigation` remote flag (OFF in production) with **automatic V1 failsafe** — a 2xx body without an `items` key, or a child missing a write-critical id, hard-fails back to the legacy calls. Android should consider the same flag + failsafe pattern for the rollout.

---

## 6. Differences vs iOS findings (VSP-1684)

| iOS claim | Android verdict |
|---|---|
| "178 files affected" | **[Android-specific: far narrower]** — direct pattern surface ≈ 5 network/repo files + 5 ViewModels + 6 fragments (+ `Record`/mapper/models). Total well under 30 files even with permission + model work |
| "Every screen that shows files/folders uses this API (Main, Shares, Search, Public Archives)" | **[matches iOS]** — the 6 browse surfaces in §2.2 are the Android equivalents |
| "Every upload needs to know what folder to put files in" | **[Android-specific: indirect]** — uploads read folder context from already-navigated state; no API-call dependency, only a post-upload refresh |
| "Every file operation (move, copy, delete) needs to refresh the current folder" | **[matches iOS]** — but refresh-only coupling; the operations themselves use unrelated endpoints |
| "Permissions are calculated per-folder from the navigation data" | **[partially matches]** — on Android only the **Shares** surfaces consume the item-level role; private/public surfaces use the archive-level role from prefs (§4.1) |
| "Int → String ID migration across 200+ files" | **[iOS-only, superseded]** — iOS's own July 2026 contract sheet shows they convert numeric-string ids at the boundary with a −1 sentinel instead of migrating types app-wide. Android should do the same (`toIntOrNull()`, existing precedent in `ItemMapper`) |
| "Permission System Reimplementation from the V2 `shares` array" | **[matches iOS]** — the one genuinely shared complexity driver (§4.2) |
| "Updating every ViewModel / every ViewController / every repository" | **[Android-specific: narrower]** — 5 ViewModels, 6 fragments, **1** repository method funnel |
| "Updating all 22 test files with new mock data" | **[iOS-only, N/A on Android]** — zero Android tests touch this pattern |
| "The entire app state revolves around 'what folder am I in?'" | **[matches iOS]** — `folderPathStack` / `NavigationFolder` / `currentFolder` state; shape survives, but ids inside it are affected by the Int→String question |

*The table above checks the original VSP-1684 estimate (Feb 2026 analysis comment). The current iOS artifacts — the **V1/V2 contract sheet** and **Stela migration status board**, both updated July 21, 2026 — were reviewed on July 22 and are more authoritative; the subsection below captures what they add.*

### 6.1 What the current iOS documents (July 21, 2026) add

- **iOS already runs `/v2/folders/{id}/children` in production code** behind the `use_stela_navigation` flag (OFF in prod) with automatic V1 failsafe; folder navigation, record reads, search/public drill-in, upload dedupe listing, and the move/copy picker are all "Live on V2" on their status board. Android is not charting new territory — the contract behaviors below are field-tested.
- **IDs:** iOS converts numeric-string ids at the decode boundary (−1 sentinel); `archiveNumber` stays a string. [matches the Android recommendation]
- **Sort:** iOS re-sorts client-side (all 6 sort options) over a `pageSize=99999999` fetch. [adopts cleanly on Android as the interim] Backend direction (July 22): sort is a folder attribute; editing it lands on the folder PATCH endpoint, after which `/children` reflects the stored order and re-sorting becomes PATCH + re-fetch.
- **Permissions:** "No per-child caller `accessRole`; incomplete `shares[]`" is an acknowledged V2 gap that **blocks their Shared-workspace drill-in** (backend-blocked item; P2 ask: "child.accessRole (caller-effective); shares[] incl. accepted + pending"). [confirms Android §4.2 is backend-blocked for Shares]
- **Root discovery:** iOS replaced `getRoot` with `GET /api/v2/archives` → `items[].rootFolderId` → `/children` → section-root child (VSP-1787). [answers Android open question #9 — same chain is available to Android; note Android also uses `getPublicRoot`, which on iOS is still V1-bootstrapped for foreign public archives]
- **Wire-shape quirks iOS tolerates** (Android mappers must too): records return flat `thumbUrl*` *and* nested `thumbnailUrls`, folders nested-only; dates split `displayDate` (records) vs `displayTimestamp` (folders); thumbnail URLs may be **empty strings instead of null**; `usesExpended` flips between String and Int on share links. [the first two were independently confirmed in the July 22 Android live capture]
- **Contract-failure detection:** iOS treats a 2xx response without an `items` key as contract failure (→ V1 failsafe); only an explicit `"items": []` counts as an empty folder. [directly reusable rule for the Android repository swap]
- **Scope caveat for estimation:** the contract sheet lists create/delete/move/rename, uploads (presigned + register), tags, search, shares listing, members/invites as **V1-only with no V2 route** — so the VSP-1772 children migration does not strand those flows; they stay on V1 regardless, on both platforms.

---

## 7. Open questions / needs verification against the live API

1. **Path: singular vs plural.** ✅ **Resolved July 22, 2026:** `/api/v2/folder/{id}/children` and `/api/v2/folders/{id}/children` are **true aliases** — replaying the same authenticated (share-token) request against both on production returned byte-identical responses. Prefer the plural (documented) form for new code; the existing singular call (`StelaAccountService.kt:45`) keeps working.
2. **PER-10476 fields actually deployed?** ✅ **Resolved July 22, 2026 — verified against a live production response** (children call captured from the app's share-preview flow): folder items include `folderLinkId`, `archiveNumber`, `parentFolder.{id, folderLinkId}`, and populated `paths.{names, folderLinkIds, archiveNumbers}`. (PR #773 merged June 15, deployed to prod in v0.87.0 on June 23 per GitHub deployment records.)
3. **Sorting.** ✅ **Design confirmed by backend (July 22, 2026):** there is no `sort` param on `/children` by design — sort is a folder attribute and query results reflect the folder's stored order. The backend will add `sort` to the **folder PATCH** endpoint (not yet available). Android phase 1: client-side sort (`SortType` → comparator, iOS's current approach); once PATCH ships, the sort picker maps to PATCH + children re-fetch. ⚠️ Residual: (a) per backend (July 23), returning children from PATCH would break the pagination conventions, so **two calls per re-sort (PATCH, then `/children`) look inevitable** — plan the sort picker around PATCH + re-fetch (a `PATCH /folders/{id}/children` variant was floated but is unresolved); (b) sort becomes **persistent per folder across clients**, a semantic change from today's per-request sort.
4. **`thumbStatus` / processing state.** No equivalent in the new contract. Is "empty `thumbnailUrls` = still processing" the sanctioned derivation?
5. **`thumbnailUrls` shape.** ✅ **Resolved July 22, 2026 (live response):** *both* shapes coexist on records — flat `thumbUrl200/500/1000/2000` **and** nested `thumbnailUrls{"200"…"2000"}`; **folders return only the nested object** (which includes `"256"`). Use the nested `thumbnailUrls` for both item types; the flat fields the current `ItemDTO` reads are record-only legacy duplicates.
6. **`shares[]` population for recipients.** ⚠️ **Confirmed as a backend gap — on all environments, staging included (July 22, 2026):** the live capture returned `shares`/`pendingShares` as `null`, and the stela `main`-branch source confirms why: the children hydration query (`packages/api/src/folder/queries/get_folders.sql`) only aggregates share rows attached *directly* to an item's own `folder_linkId`, so descendants inside a shared tree carry none and a recipient cannot derive a per-item role. It's a contract/data-model limitation, not a deployment lag (no open stela PR changes it) — matching the iOS P2 backend ask ("child.accessRole (caller-effective) + complete shares[]"). **Android's Shares-surface permission derivation (§4.2) is backend-blocked on that ask** — track it with the backend team before scheduling that slice; all other surfaces (own/public archive) are unaffected.
   *Update 2026-07-31 (this snapshot is superseded on two points — see `folders-children-contract.md`, the living doc):* (a) the `null` shares in the July 22 capture were flavor/context-specific — **owner bearer requests do get `shares[]`** on directly-shared items (verified live 2026-07-23); the recipient/descendant part of this gap stands. (b) Share **status filtering differs by item kind**: record items are hydrated via `get_records.sql` (keeps `status.generic.pending` entries — live-verified through the children call), folder items via `get_folders.sql` (filters to `status.generic.ok`, dropping pending shares-to-archives). One-line backend filter alignment requested 2026-07-31.
7. **Pagination limits.** Partially resolved July 22: `pageSize=99999999` is accepted (no cap error). But `totalPages` came back **0** despite 9 items (unreliable — don't build on it), `nextCursor` was **non-null even when the full folder fit in one page** (end-of-list detection can't rely on a null cursor; probe the next page or compare counts), and the response includes an undocumented `pagination.nextPage` convenience URL (which itself uses the plural `/folders/` path).
8. **Value formats.** Resolved July 22: **records keep legacy dotted forms** (`type.record.image`, `status.generic.ok`) and dates are ISO-8601 (existing `replace("T", " ")` logic still applies) — but **folders use new short forms**: `type: "private"`, `status: "ok"`, `sort: "alphabetical-ascending"`. Any mapping that assumes `type.folder.*` on folders will break. Confirmed intentional and environment-independent (staging included): stela's folder service applies explicit `prettifyFolderType/Status` transforms (`packages/api/src/folder/service.ts:49`) over the dotted DB enums; the record model has no such layer, hence the mixed forms in one response. `accessRole` strings still unverified (see #6).
9. **Root folder ids.** ✅ **Resolved July 22, 2026 (via iOS contract sheet/VSP-1787):** iOS replaced `getRoot` with `GET /api/v2/archives` → `items[].rootFolderId` → `/children` → section-root child (`type.folder.root.private` = My Files). Android can mirror this chain. Residual: Android's public browsing also uses `getPublicRoot`, which on iOS remains V1-bootstrapped for foreign public archives ("confirmed solvable, not yet wired") — decide whether Android's phase 1 keeps `getPublicRoot` (V1) for root resolution as iOS does.
10. **Existing `ItemMapper` bug.** `ItemMapper.kt:22` maps `archive?.name` into `Record.archiveNr` (should be `archive?.archiveNumber`) — harmless for the preview grid today, but must be fixed before the mapper is promoted to general browsing.

---

## 8. Effort inputs per area (for story-point discussion — no final number here)

| Area | Work | Size drivers |
|---|---|---|
| **Models / network layer** | Extend the existing Stela stack: bearer-token `getFolderChildren` overload (+ `cursor`), `pagination` in `FolderChildrenResponse`, enrich `ItemDTO` (itemType, archiveNumber, parentFolderLinkId, thumbnail 256, shares/pendingShares + DTO→`Share` mapping), extend `ItemMapper.toRecord()` (and fix the `archiveNr` bug) | Small–moderate; all extension of working code |
| **Repository funnel swap** | Reimplement `FileRepositoryImpl.getChildRecordsOf` on the new call; keep or evolve the listener contract (parent name no longer server-provided); delete `navigateMin`/`getLeanItems` (service, client, repo, `RequestContainer.addFolder` 4-arg) | Small — single funnel |
| **Browse ViewModels/screens** | 5 VMs + 6 fragments: adapt to the new listener/result shape, root-id resolution, parent-name sourcing | Moderate — mechanical but must be regression-tested per screen |
| **Permissions** | Shares-array derivation (§4.2) + `pendingShares` badge migration + live-data validation per browsing context | **Own effort line** — small code, high verification cost |
| **Pagination / UX (Phase 2)** | Cursor paging: adapter append path, file-viewer paging, select-all semantics, back-stack/scroll restoration, sort resolution | Largest if done fully; **deferrable** via Phase-1 large-pageSize |
| **ID strategy** | Boundary conversion (cheap, precedent exists) vs. `Record` String migration (expensive, iOS-parity) — decision needed | Small ↔ large depending on decision |
| **Uploads & file ops** | No direct work beyond confirming refresh paths and folder-context state still populate correctly | Trivial–small |
| **QA / regression** | All 6 browse surfaces × (navigate, back, refresh, sort, upload-refresh, delete-refresh, move/copy picker, file-viewer swipe, select-all, deep links, share preview) | Significant — this is the app's core loop |

**Suggested small-PR/commit split for future implementation** (each independently reviewable):
1. DTO/mapper extension + `ItemMapper` bug fix (no behavior change to preview).
2. Bearer-token + cursor plumbing in `StelaAccountService`/`NetworkClient`/`StelaAccountRepository` (unused).
3. Permission-derivation helper from `shares[]` + unit tests (unused).
4. Repository funnel swap behind the existing `getChildRecordsOf` signature (Phase 1, large pageSize) + per-screen verification.
5. Delete legacy `navigateMin`/`getLeanItems` code.
6. (Phase 2, separate story) real cursor pagination per §5.3.

---

*Prepared for VSP-1772 · Android codebase investigation · July 21, 2026. No production code was modified.*

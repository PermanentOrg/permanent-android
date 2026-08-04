# Android · Stela V2 — GET /v2/folders/{id}/children (verified contract)

Verified against the merged iOS implementation (permanent-ios PR #573, commit `38d9622`;
updated 2026-07-30 against PRs #574/#575/#576/#580), live production captures (2026-07-22),
and the published stela docs — in that order of authority. Written for VSP-1778 (Private
Files navigation), extended for VSP-1808 (Public Files); reuse for future Stela tickets
instead of re-deriving.

## Public folders (VSP-1808)

- **Same endpoint, same bearer auth, no public variant.** The children call for the owner's
  Public Files tree is byte-identical to the private one — only root resolution differs
  (Android keeps V1 `folder/getPublicRoot`; the V1 response carries `folderId`, so the V2
  fork engages from the root down). iOS does the same drill-in (their PR #573 already
  covered Public Files via ViewModel inheritance) and moved root discovery to
  `GET /v2/archives` → root children → public-root child in a separate ticket
  (VSP-1787, PR #574) — Android's counterpart is a future ticket for both sections.
- **Foreign public archives** (another archive's public tree): iOS verified on staging
  (2026-07-28, their PR #576) that `/children` serves it on plain bearer auth — but root
  discovery must stay V1 `getPublicRoot`, because `/v2/archives` only lists the caller's
  own memberships. Android's `PublicArchiveViewModel`/`PublicFolderViewModel` are still
  fully V1 (out of VSP-1808 scope; candidate next ticket).
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
  `ignoreErrors = true` for the same reason.

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
| `type` | **Records keep dotted legacy forms** (`type.record.image`); **folders answer new short forms** (`private`, `root.private`…). Android normalizes folders back to `type.folder.<short>` in the mapper so downstream consumers see V1-shaped values. Confirmed intentional server-side (`prettifyFolderType` in stela). |
| `status` | Same split: records dotted (`status.generic.ok`), folders short (`ok`, `copying`, `moving`). `copying`/`moving` → item non-tappable (`isProcessing`). There is **no `thumbStatus`** — a file with no thumbnails yet is treated as still processing (same derivation as the shares screen's `Record(ItemVO)`). |
| `parentFolder { id, folderLinkId }` | **Folders nest** parent info here; **records send it flat** (`parentFolderId`/`parentFolderLinkId`). Resolve flat-then-nested. Added in stela PR #773. |
| `paths { names, folderLinkIds, archiveNumbers }` | Full breadcrumb trail (stela PR #773). Android doesn't consume it (breadcrumbs are the client-side `folderPathStack`). |
| `size` | Bytes; present on folders too. |
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
  (backend gap), so the access copy is the only thumbnail it has. Android
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

## Impact summary (updated 2026-07-31)

With the flag ON, only one thing visibly breaks on Private Files: the **pending badge
undercounts on FOLDER rows** (gap 2 below — record rows are fine, live-verified
2026-07-31). Everything else falls back to V1 or is handled in the app. Gaps 1 and 2 are
the same backend theme — *send complete share data on children* — so raise them as one
ask, together with iOS's existing P2; for gap 2 the concrete ask is a one-line filter
alignment (raised with the backend 2026-07-31). Gaps 3–4 break nothing today; they only
block future migration tickets, and those surfaces simply stay on V1.

## Known backend gaps (as of 2026-07-23)

1. **No per-item caller `accessRole`, incomplete `shares[]` on children of shared folders**
   (descendants inside a shared tree carry none — the hydration SQL only aggregates direct
   share rows). Blocks Shared-workspace drill-in on both platforms; iOS P2 backend ask.
   Own-archive browsing is unaffected (permissions are archive-derived).
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

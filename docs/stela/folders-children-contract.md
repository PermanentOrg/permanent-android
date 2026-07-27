# Android · Stela V2 — GET /v2/folders/{id}/children (verified contract)

Verified against the merged iOS implementation (permanent-ios PR #573, commit `38d9622`),
live production captures (2026-07-22), and the published stela docs — in that order of
authority. Written for VSP-1778 (Private Files navigation); reuse for future Stela tickets
instead of re-deriving.

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
| `shares[]` | `{ id, accessRole, status, archive { id, archiveNumber, name, thumbs } }`. **Populated for owner bearer requests** (verified live on staging 2026-07-23) — the earlier share-token capture's `null` was flavor-specific. But server-side it is **filtered to `status.generic.ok`** (`get_folders.sql:76`), so every entry is OK-status. Feeds the shared/pending badge only — **item permissions stay archive-derived** (see gaps). |
| `pendingShares[]` | `{ id, email, name, accessRole }` — pending **email invitations** only (`invite_share` table, `get_folders.sql:81-99`). Pending **shares to existing archives** (`share` rows with `status.generic.pending`) appear in **neither array** — see gap 2. |

### Thumbnails — the rules that matter

- **Settled contract (backend confirmed 2026-07-27: no further changes planned; verified
  against a fresh JPG upload, record 89646, 2026-07-24):** records send the `.thumb.wNNN`
  renditions both flat (`thumbUrl200/500/1000/2000`) and nested under `thumbnailUrls`
  (`"200"…"2000"`, duplicates); **folders send only the nested object**. A fresh upload has
  no flat `thumbnail256` at first — the 256 access copy appears once processing finishes.
  Android reads the **nested** object only (covers records and folders alike; the flat
  `thumbUrl*` duplicates are not read on V2), with flat `thumbnail256` preferred when
  present.
- **Never use nested `thumbnailUrls."256"`** — it is the Archivematica access-copy thumbnail,
  a tiny rendition that comes back **blank for HEIC**. Only a real flat `thumbnail256` counts
  as the 256 source; otherwise fall through to the `.thumb.wNNN` renditions.
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

## Impact summary (as of 2026-07-23)

With the flag ON, only one thing visibly breaks on Private Files: the **pending badge
undercounts** (gap 2 below). Everything else falls back to V1 or is handled in the app.
Gaps 1 and 2 are the same backend theme — *send complete share data on children* — so raise
them as one ask, together with iOS's existing P2. Gaps 3–4 break nothing today; they only
block future migration tickets, and those surfaces simply stay on V1.

## Known backend gaps (as of 2026-07-23)

1. **No per-item caller `accessRole`, incomplete `shares[]` on children of shared folders**
   (descendants inside a shared tree carry none — the hydration SQL only aggregates direct
   share rows). Blocks Shared-workspace drill-in on both platforms; iOS P2 backend ask.
   Own-archive browsing is unaffected (permissions are archive-derived).
2. **V2 does not send pending shares-to-archives — so the pending badge misses them.**
   There are two kinds of "pending" share: an **email invite** (the person has no account
   yet) and a **share to an existing archive** (not accepted yet). V1 sends both in one
   list (`ShareVOs`), so the badge works today. V2 loses the second kind:
   - `shares[]` keeps only **accepted** shares — the backend filters
     `status = 'status.generic.ok'` (`get_folders.sql:76`);
   - `pendingShares[]` contains only **email invites** (`invite_share`, lines 81–99).
   A pending share to an archive matches neither, so it is in **neither list**.
   Verified on staging 2026-07-23: Vacation folder (`folder_linkId 115425`) has pending
   `shareId 2010` in the V1 response — it is absent from the V2 response.
   **Result:** with the flag ON, the badge undercounts. The app cannot show data it never
   receives — only the backend can fix this. **Ask:** also send pending `share` rows
   (in `shares[]` or a third list). Fix needed before the production flag flip.
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

# Shareable links vs. Stela V2 — remaining V1 dependencies

Status as of 2026-08-11. Full discussion: [VSP-1810](https://permanent.atlassian.net/browse/VSP-1810) (comments).

## The problem in one line

Shareable links carry V1-era addresses (`archiveNbr`, `folder_linkId`, share token), while the Stela V2 API addresses items by database ids (`folderId`, `recordId`, `shareLinkId`). Nothing translates between the two, so every link the app opens or mints keeps a hard dependency on V1 endpoints — one of the blockers for turning V1 off.

## The two link families the app mints and consumes

All workspaces (Private Files, Public Files, Shares, Public Gallery) were swept; these are the only two formats.

### 1. Public URLs

```
https://app.permanent.org/p/archive/{archiveNbr}/{folderArchiveNbr}/{folderLinkId}                      (folder)
https://app.permanent.org/p/archive/{archiveNbr}/{folderArchiveNbr}/{folderLinkId}/record/{recordArchiveNbr}  (record)
https://app.permanent.org/p/archive/{archiveNbr}/profile                                               (archive profile)
```

The web app's routes define the format; both mobile apps hardcode the same pattern when copying links. The backend does not mint them.

Built on Android in:
- `RecordMenuViewModel.buildPublicLink()` — "Get link" in Public Gallery menus (uses the *browsed* archive's number)
- `PublicArchiveViewModel.onCopyLinkBtnClick()` — Public tab toolbar
- `PublicGalleryViewModel` / `ArchiveSearchViewModel` — archive profile links

### 2. Private share links (token)

```
https://app.permanent.org/share/{token}
```

Built in `ShareManagementViewModel` (`BASE_URL + "share/" + token`) from the V2 share-links flow.

## The four V1 dependencies

| # | Link element | V2 needs | V1 translator today (the only one) |
|---|---|---|---|
| 1 | `folder_linkId` in public folder links | `folderId` | `navigateMin` accepts the link's ids directly |
| 2 | record `archiveNbr` in public record links | `recordId` | `record/get` by archiveNbr |
| 3 | share `token` | `shareLinkId` (V2 `GET /v2/share-links` only accepts `shareLinkIds[]`) | `share/checkShareLink` when opening a link; `share/getLink` called solely to learn the `shareby_urlId` before every V2 share-links call (see `ShareManagementViewModel.checkForExistingLink` → `getLinkFromStela`) |
| 4 | `archiveNbr` as the entry point of every public link | root `folderId` (V2 has no root route for a foreign archive) | `folder/getPublicRoot` |

Not affected: archive profile links — `archiveNbr` is the key the (still-V1) profile endpoints already accept. Risk only appears if their V2 replacements switch to `archiveId`-only addressing.

Deep links are not a separate case: they consume the same two families through the same code paths.

Related, not a link: push notification payloads (`SHARE_LINK_REQUEST`, `SHARE_INVITATION_ACCEPTANCE`) carry only a `folder_linkId`, so `PermanentFCMService.requestFolderBy` is the one remaining V1 `folder/get` caller (VSP-1842 inventory). Same fix family: `folderId`/`recordId` in the payload, or lookup 1 above.

## What fixes it (backend, ranked)

1. A V2 resolver endpoint family covering all three lookups — `folder_linkId → folderId`, `record archiveNbr → recordId`, `token → shareLinkId` — plus a V2 public-root-by-archiveNbr route. Backend-only; fixes every link ever shared.
2. Let existing V2 endpoints accept the V1-era keys as alternative addresses (same benefits).
3. Add database ids to the URL format — requires web + both mobile apps, and all previously shared links would still need option 1 or 2.

Not urgent — all V1 fallbacks work today. But every public/shared link keeps V1 navigation alive until this is resolved.

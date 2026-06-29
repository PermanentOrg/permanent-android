# Dashboard Experience Prototype — Findings

Hackathon prototype: a widget-based Dashboard that replaces the linear onboarding flow for
users who have no archive yet. This note covers the codebase discovery, the Figma widget
inventory, what was stubbed, and open questions.

---

## 1. Codebase discovery

### Onboarding / archive-creation flow (reused)
- **Activity:** `ArchiveOnboardingActivity` (`ui/archiveOnboarding/ArchiveOnboardingActivity.kt`) — hosts a Compose pager; on done → `MainActivity` + `finish()`.
- **ViewModel:** `viewmodels/ArchiveOnboardingViewModel.kt`. The create chain we reuse:
  - `createNewArchive(NewArchive)` → `archiveRepository.createNewArchive(name, type, listener)`
  - → `setNewArchiveAsDefault()` (account update + `prefsHelper.saveDefaultArchiveId`)
  - → `setNewArchiveAsCurrent()` (`switchToArchive` + `prefsHelper.saveCurrentArchiveInfo`).
- **Repository:** `IArchiveRepository.createNewArchive(name, type, IArchiveListener)` → `ArchiveRepositoryImpl.kt:167` → `NetworkClient.createNewArchive()` → `POST archive/post` → returns `Archive`.
- **Reusable Compose pieces:** `ui/archiveOnboarding/compose/` — `ArchiveTypeDropdown` (gradient selector that hosts the type-picker bottom sheet), `ArchiveTypePickerBottomSheet`, `ArchiveNamePage`.

> The Dashboard's `DashboardViewModel.createArchive()` replicates this exact chain (create →
> set default → switch to current), so the new archive becomes the user's current archive and
> prefs are left in the same state the onboarding flow produces.

### Private Files screen (unchanged fallback)
- **Fragment:** `ui/myFiles/MyFilesFragment.kt`; destination `@id/myFilesFragment` in `res/navigation/main_navigation_graph.xml` (was the start destination).

### "Has 0 archives" — source of truth
- **API:** `IArchiveService.getAllArchives()` → `NetworkClient.getAllArchives()` → `POST archive/getAllArchives` → `ResponseVO.getData(): List<Datum>` (each `Datum.ArchiveVO`).
- **Repository:** `IArchiveRepository.getAllArchives(IDataListener)` (`ArchiveRepositoryImpl.kt:99`).
- **Existing precedent:** `ArchivesViewModel.refreshArchives()` sets `existsArchives = archives.isNotEmpty()` — the canonical "has archive" check.
- **What we used for routing:** the cached `prefsHelper.getDefaultArchiveId() == 0` (instant, no launch-time network call/flash). See open questions for the trade-off.

### Navigation & routing
- **MainActivity** (`ui/activities/MainActivity.kt`) inflates `main_navigation_graph` and calls `navController.setGraph(graph, extras)`; it already supports a custom start destination via `START_DESTINATION_FRAGMENT_ID_KEY`.
- **Post-signup:** `AuthenticationFragment.onAccountCreated` previously launched `ArchiveOnboardingActivity`.
- **Theme/tokens:** Material 3 + `colorResource(R.color.*)` from `res/values/colors.xml` (blue900 `#131B4A`, accent `#FF9933`, blue25 `#F4F6FD`). Reusable Compose components in `ui/composeComponents/` (`CenteredTextAndIconButton`, etc.).
- **Networking convention:** Retrofit service → `NetworkClient` builder → Repository interface+impl → **listener callbacks** (not coroutines/Flow). The Dashboard follows this.

---

## 2. Figma widget inventory (file `lLHzIJcmkwkvdl3ipcjyKs`)

| # | Widget | State | Status |
|---|--------|-------|--------|
| 1 | **Loading skeleton** (`25365-21987`) | greeting row (avatar + 2 text lines) + 2 tall cards, animated shimmer sweep | ✅ built (`DashboardLoadingSkeleton` + `rememberShimmerBrush`) |
| 2 | **Greeting** | "Hello, {firstName} 👋 / Your memories are safe here!" | ✅ built (`GreetingWidget`) |
| 3 | **Create Archive** — empty | "Let's begin your archive" + CTA + privacy note | ✅ wired |
| 3 | **Create Archive** — success (`25369-27691`) | 🎉 "Your archive is ready!" + "The {name} Archive" + "0 files · Created just now" + "Go to Archive →" | ✅ wired |
| 4 | **Create Archive bottom sheet** (`25369-22390`, `25369-27248`) | type dropdown + name field + Create | ✅ wired (reuses onboarding components) |
| 5 | **Archive type picker** (`25369-22595`) | Personal/Individual/Family/… | ✅ reused (`ArchiveTypePickerBottomSheet`) |
| 6 | **What's important to you?** | ⭐ + priority chips + Save / Remind me later | ✅ wired (`why:*` tags) |
| 7 | **Chart your path to success** (`25369:22270`) | 🏆 + goal chips + Save goals / Remind me later, on a purple→magenta card | ✅ wired (`goal:*` tags) |

The Dashboard header ("My Dashboard", drawer + account icons) is provided by MainActivity's
existing toolbar/drawer — `dashboardFragment` is registered as a top-level destination.

---

## 3. Widget framework

- `ui/dashboard/DashboardWidget.kt` — `DashboardWidgetType` enum + `defaultDashboardWidgets`
  ordered list. **To add/remove/reorder widgets, edit this list.** Also holds the shared
  `WidgetActionState` (Idle → Saving → Done) used by the tag-saving widgets.
- `DashboardScreen.kt` lays the widgets out in a scrollable `Column` (inside `BoxWithConstraints`,
  min-height = viewport) with `Arrangement.SpaceBetween`, dispatching each `DashboardWidgetType`
  to a self-contained composable in `ui/dashboard/widgets/`. A **"You're all caught up."** gradient
  footer (Figma node `25369:22267`) stays pinned to the bottom when few widgets remain. Tag-saving
  widgets animate out (`AnimatedVisibility` shrink + fade, 300ms) when their state becomes `Done`
  (saved or "remind me later"); the inter-card 24dp spacing lives inside the animation so it
  collapses with the card and leaves no empty slot.
- `DashboardViewModel` exposes `isLoading`, `firstName`, `createArchiveState`, `showError`,
  `prioritiesState`, `chartPathState`; actions `savePriorities`/`saveGoals` (→ `addRemoveTags`)
  and `dismissPriorities`/`dismissGoals`.

### "Chart your path to success" widget (added)
- **What it is:** a goal-selection chip picker (Figma node `25369:22270`) — the goals twin of the
  priorities widget, on a purple→magenta gradient card. *Not* an endpoint-driven completion
  checklist (the task's initial framing); the design is a goal picker with **Save goals** /
  **Remind me later**.
- **Files:** `widgets/ChartYourPathWidget.kt` (new), `widgets/ImportantToYouWidget.kt` (retrofit
  with the same footer), `DashboardWidget.kt`, `DashboardScreen.kt`, `DashboardViewModel.kt`,
  `DashboardTokens.kt` (`PurpleMagentaGradient`), `strings.xml`, and two new vector drawables
  (`ic_trophy_star.xml`, `ic_check_gradient.xml`, both purple→magenta gradient fills).
- **Wiring:** both widgets persist via `StelaAccountRepository.addRemoveTags(Tags(addTags=…))` —
  goals as `goal:*`, priorities as `why:*` — reusing the exact tag vocabulary onboarding's
  `sendGoalsAndPriorities` sends. Chip→tag maps in each widget file.

| Chip (goals) | tag | Chip (priorities) | tag |
|---|---|---|---|
| Publish a legacy | `goal:publish` | Digital preservation | `why:digipres` |
| Plan my digital legacy | `goal:legacy` | Collaboration | `why:collaborate` |
| Share privately | `goal:share` | Family history | `why:genealogy` |
| Preserve memories | `goal:capture` | Secure digital storage | `why:safe` |
| Digitize my materials | `goal:digitize` | Supporting a nonprofit | `why:nonprofit` |
| Build an archive with someone | `goal:collaborate` | Business needs | `why:professional` |
| Organize my materials | `goal:organize` | | |

### Interaction & visuals
- **Processing overlay:** while a save is in flight (`WidgetActionState.Saving`) each widget covers
  its full card (`matchParentSize`) with the app's existing
  `composeComponents/CircularProgressIndicator` — `OverlayColor.LIGHT` on the white priorities
  card, `OverlayColor.DARK` on the gradient card.
- **Gradient card fill:** the "Chart your path" card uses a size-aware `ShaderBrush` reproducing
  the Figma `linear-gradient(49.66°, #800080 4.6% → #B843A6 95.5%)` (purple lower-left → magenta
  upper-right), not the default top-left→bottom-right brush.
- **Remind-me-later** link on the priorities card is rendered at 64% opacity to match the
  translucent treatment of the same link on the gradient card.
- **Transient notifications:** the dashboard uses the app's existing `AnimatedTemporarySnackbar`
  (slides up from the bottom, auto-hides after 4s). A successful save shows a `SUCCESS` snackbar
  ("Goals saved" / "Priorities saved" — the widget has already animated away); save failures show an
  `ERROR` snackbar; the "select at least one option" validation shows a `WARNING` snackbar.
  `errorMessage`/`warningMessage`/`savedMessage` drive one snackbar (error > warning > success), and
  each resets once shown so the same message re-triggers.

---

## 4. Stubbed / API gaps

- **Priorities + goals chip widgets** — Save is now wired to `addRemoveTags` (`why:*` / `goal:*`).
  Remaining stubs: (a) chips start blank — no read-back of already-saved tags to pre-select
  (no clean tag-read path exists today); (b) **Remind me later** / dismiss-after-save is
  session-local (no persistence endpoint), so a dismissed widget reappears on the next cold start;
  (c) empty-selection + Save shows an error ("Please select at least one option.") and keeps the
  widget visible — no dismiss, no network call.
- **Icons** — the design's icons are Font Awesome 6 Pro *font glyphs*, which Figma can't export as
  assets. All dashboard icons are converted from the licensed FA Pro 6.4.2 `svgs/` into vector
  drawables (official paths, centred + inset via a `<group>` transform so each keeps the visual
  size its slot expects; consuming `Modifier.size(...)` left unchanged):
  - `ic_trophy_star.xml` ← solid/trophy-star (purple→magenta), `ic_check_gradient.xml` ← regular/check (purple→magenta)
  - `ic_dashboard_avatar.xml` ← solid/user-vneck-hair (purple→orange), `ic_archive_name_gradient.xml` ← regular/box-archive (purple→orange)
  - `ic_angles_up_down.xml` ← regular/angles-up-down (#A1A4B7), `ic_circle_xmark_grey.xml` ← solid/circle-xmark (#D0D1DB), `ic_star_solid_accent.xml` ← solid/star (#FF9933)
- **`DashboardViewModel.refresh()` / loading** — the skeleton now shows **only when needed**:
  `isLoading` starts `true` only if the greeting's account name isn't cached; otherwise the widgets
  render straight from prefs (no flash on entry). `refresh()` is a silent background re-confirm via
  `getAllArchives` (no longer forces the skeleton). A real dashboard would aggregate per-widget data
  here (recent activity, storage, suggestions) and gate the skeleton on that fetch.
- **Archive success meta** ("0 files · Created just now") is hardcoded copy, not derived from a
  records/timestamp endpoint.

---

## 5. Routing changes

- **New destination:** `dashboardFragment` added to `main_navigation_graph.xml` (+ action to
  `myFilesFragment`, popping the dashboard).
- **App-open (one decision point):** `MainActivity.resolveStartDestinationId()` — explicit start
  destination wins; else `getDefaultArchiveId() == 0` → Dashboard, otherwise Private Files.
  `FORCE_DASHBOARD` is now `false` (real archive state drives routing); flip to `true` to
  force-preview the Dashboard.
- **Hamburger hidden on the onboarding Dashboard:** on `dashboardFragment` with
  `getDefaultArchiveId() == 0`, `MainActivity` clears the toolbar nav icon and locks the drawer
  (`LOCK_MODE_LOCKED_CLOSED`) — nothing to navigate to yet; restored once an archive exists.
- **Switch-archive dialog guard:** the deep-link "Switch to The %s Archive?" prompt now only shows
  when the intent carries a recipient archive, so custom-start launches (e.g. signup → Dashboard)
  no longer trip it with a null name ("The null Archive").
- **Post-signup:** `AuthenticationFragment.navigateToDashboard()` launches `MainActivity` with
  start destination = `dashboardFragment` and sets `saveUserLoggedIn(true)` (the user is
  authenticated; only the archive is missing) so `isUserLoggedIn`-gated screens don't bounce them
  to sign-in.
- **User-missing-default-archive** observer also routes to the Dashboard.
- **Cold launch:** `SplashActivity` routes logged-in + `getDefaultArchiveId() == 0` to the
  Dashboard via `startDashboard()` (was `startArchiveOnboardingActivity()`).
- **`ArchiveOnboardingActivity` is now unreachable** (kept intentionally — see open questions).

---

## 6. What I'd explore next / open questions

- **Routing source of truth.** We route on the cached `defaultArchiveId` for an instant decision
  (no skeleton flash). It can be stale; the accurate signal is `getAllArchives`. A small startup
  ViewModel that checks the endpoint (with the prefs value as an optimistic default) would be the
  production approach.
- **TODO — onboarding routing for invited / existing-archive users.** Every archive-less user is
  currently sent to the Dashboard. `ArchiveOnboardingActivity` is intentionally kept (now
  unreachable) so that in the future users who have **pending archive invitations** or **at least
  one (pending/accepted) archive** can be routed through it instead. The branch points are marked
  with `// TODO` in `SplashActivity` and `AuthenticationFragment.navigateToDashboard()`. Real
  signals needed: `getInvitations()` and `getAllArchives()`/pending archives.
- **Question — should biometrics gate the app even for archive-less users?** The biometric prompt
  only fires on the cold-start "logged-in + has-archive" branch (`SplashActivity.switchArchiveToCurrent`
  → `startBiometricsFragment`). Archive-less users are now kept logged-in and routed straight to the
  Dashboard (`startDashboard()`), bypassing the auth screen — so no biometric prompt. Since the
  Dashboard shows account info, we may want to route `startDashboard()` through the biometric check
  first (prompt, then land on the Dashboard).
- **Question — what happens to the other widgets after the archive is created?** Once the first
  archive exists, the Create Archive widget flips to its success card, but the Greeting, "What's
  important to you?" and "Chart your path to success" widgets just stay on the Dashboard for the
  rest of that session. Since routing then sends a has-archive user to Private Files on the next
  entry, the Dashboard (and those priorities/goals widgets) effectively becomes **unreachable** —
  so if the user doesn't save/dismiss them in this one session, they never get another chance.
  Open design questions: should the Dashboard auto-navigate to the archive after creation? Should
  the priorities/goals widgets move somewhere persistent (e.g. settings or a returning-user
  Dashboard)? Should "Go to Archive" be the only exit, or should the Dashboard remain reachable
  for returning users?
- **Real backing endpoints** for stubbed widgets (priorities persistence, recent activity,
  storage usage, file counts).
- **Widget personalization** — reorder/show-hide, persisted per user; potentially a
  server-driven widget list.
- **Shared archive-creation use-case** so onboarding and Dashboard don't duplicate the
  create → set-default → switch-current chain.
- **Not handled (noted):** pending-only archives, create-archive failure/retry UX, tablet layout.
  (The design's serif display face is now bundled — `DashboardDisplayFont` = the `gyst_*` fonts.)

# Dashboard Prototype — Demo Cheat Sheet

**One-liner:** A widget-based "My Dashboard" that replaces the linear onboarding for users with no
archive yet — greet, create your first archive, and pick goals/priorities, all from one screen.

---

## Demo flow (what to click)

1. **Sign up / land on the Dashboard** — new users go straight to the widget Dashboard (not the old
   onboarding). The hamburger is hidden and the drawer is locked (nothing to navigate to yet).
2. **Loading** — a shimmer skeleton shows only on a true cold load (skipped when data is cached).
3. **Greeting widget** — "Hello, {name} 👋 / Your memories are safe here!"
4. **Create Archive widget** → tap **Create your first Archive** → bottom sheet (type picker + name,
   reuses the onboarding components) → **Create**.
   - Success card animates in: 🎉 "Your archive is ready!" + "The **{name}** Archive" + "Go to Archive".
5. **"What's important to you?"** and **"Chart your path to success"** widgets — tap chips, then:
   - **Save** → spinner overlay → widget animates away (collapse + fade) → green **"Saved"** snackbar.
   - **Remind me later** → widget animates away, no save.
   - **Save with nothing selected** → orange **warning** snackbar "Please select at least one option."
6. **"You're all caught up."** footer pinned to the bottom as widgets are dismissed.
7. **Go to Archive** → opens the **Private Files** screen for the new archive.

---

## What's real (wired end-to-end)

- **Create archive** — real repository chain (create → set default → switch to current), same as onboarding.
- **Save goals / priorities** — persisted as `goal:*` / `why:*` account tags via the same
  `addRemoveTags` endpoint onboarding uses.
- **Routing** — new/returning archive-less users → Dashboard (signup + cold launch); has-archive users → Private Files.
- **Icons** — converted from the licensed Font Awesome Pro 6.4.2 set, with the design gradients.
- **Reused app components** — the create-archive bottom sheet, the loading spinner overlay, and the
  `AnimatedTemporarySnackbar` notifications are existing app pieces.

## What's stubbed (say if asked)

- Chips start blank — no read-back of already-saved tags to pre-select.
- "Remind me later" is **session-local** (no persistence endpoint) — reappears on next cold start.
- "0 files · Created just now" on the success card is hardcoded copy.
- Widgets render from local/cached data (prefs) — no live per-widget fetch yet. The Dashboard fires
  a `getAllArchives` call on open but ignores the response (it doesn't drive any widget).

---

## Open questions (good "what's next" talking points)

- **After the archive is created**, the other widgets stay for the session but the Dashboard becomes
  unreachable on next entry — where should goals/priorities live long-term?
- **Biometrics** only gate the app for has-archive users — should they gate archive-less users too?
- **Onboarding routing** — keep the old `ArchiveOnboardingActivity` (now unreachable) for users with
  pending invitations or existing archives.
- Persisted dismissal, tag read-back/pre-select, real per-widget data, widget reorder/personalization.

---

> Full detail: see `findings.md`.

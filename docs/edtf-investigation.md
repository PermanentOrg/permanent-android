# VSP-1832 — EDTF library options for Android date/time entry

**Status:** investigation findings and recommendation. Unblocks VSP-1765 (advanced date/time entry) and VSP-1830 (client-side validation). iOS counterpart: VSP-1831 (in progress, no findings published as of 2026-08-26).

All claims below carry a "checked" note saying where they were verified. Verified on 2026-08-26 unless stated otherwise.

---

## 1. The bounded requirement scope

The converter we need is **structured UI fields ⇄ EDTF string**, not a free-text EDTF parser. The exact constructs come from the requirement tickets:

From **VSP-1764** (basic entry), **VSP-1765** (advanced entry), **VSP-1830** (validation) — checked in Jira 2026-08-26:

| Requirement | EDTF construct | EDTF level |
|---|---|---|
| Typeable year / month / day | `2016`, `2016-05`, `2016-05-01` | 0 |
| Time h:m:s, timezone default null | `THH:MM:SS±HH:MM`, only on complete dates; Web never emits `Z` or offset-less times (stamps local offset when unset, §2) | 0 |
| Ranges (start + end) | `2016/2017`, `2016-05-01/2017-01` | 0 |
| Empty/unknown start or end of a range | unknown side → empty (`/2016`, `2016/`), empty side → open (`../2016`, `2016/..`) — Web's convention, §2 | 1 |
| Uncertain toggle | trailing `?` | 1 |
| Approximate toggle | trailing `~` | 1 |
| Both toggles | trailing `%` | 2 |
| Unknown toggle ("all X") | `XXXX-XX-XX` | 1 |
| Partially-filled dates | X-padded digits: `19XX`, `1985-1X`, `XXXX-05`, `1985-XX-20`, `2024-XX-09` (days always real, zero-padded) | 1–2 |
| Validation: year 4-digit, month 1–12, day 1–31, h/m/s ranges, only for filled fields | parser-side checks | — |

**Not needed:** seasons (`2001-21`), sets/lists (`[1667,1668]`), exponential years (`Y17E7`), significant digits, decades/centuries as types, per-component qualifiers (`2004-06?-11`). This exclusion is what makes a native converter tractable.

Two scope facts the tickets don't state, established during verification:

- **Interior X patterns ARE needed.** Web really produces `2024-XX-09`, `1985-1X`, `XXXX-05`, `XXXX-XX-20` — unspecified month with a real day, partial-digit months — which is Level 2 territory. The table above includes them.
- **Time + qualifier is not expressible** (next subsection), so "uncertain datetime" never reaches the wire.

### A constraint the tickets don't state: EDTF cannot qualify times

`?` `~` `%` attach to **date** productions only, never to `datetime`, and there is no X-masking for times. Triple-verified 2026-08-26:

1. edtf.js grammar (`src/edtf.ne` at `811a00d`): the only qualifier rule is `date_ua -> date UA`; the `datetime` production has no qualified variant.
2. `@edtf-ts/core` (the **backend's** parser) source: only its Level 0 path parses datetimes (pure ISO, no qualifiers); Level 1 strips a trailing qualifier and then requires a date-only pattern (`packages/core/src/parser/level1.ts:122,185`). So `2024-06-15T14:30:00-05:00~` → 400 from the API.
3. Web's own service appends qualifiers after the datetime (`edtf.service.ts` `normalizeEdtfString`) but then round-trip-validates with `edtf(...)`, which rejects the combination — so the save errors out and the combo never reaches the wire. Web has no spec test for time+qualifier together.

Consequence for VSP-1765: the UI must treat time and the uncertainty/approximate/unknown toggles as mutually exclusive (or drop the time when a toggle is set). This mirrors what Web's members effectively get today.

---

## 2. Compatibility bar: what the backend accepts and what Web sends

Source-verified 2026-08-26 against `PermanentOrg/stela` @ `b772396f` (HEAD of main, 2026-08-24) and `PermanentOrg/web-app` @ `bdeff309` (HEAD of main, 2026-08-25).

### The EDTF field is `displayTime` — and only `displayTime`

- `PATCH /v2/records/{id}` (`packages/api/src/record/validators.ts:71-95`) accepts `locationId | location | description | displayName | displayTime`, `.unknown(false)` — **`displayDate`/`displayEndDate` are rejected on records.**
- `PATCH /v2/folders/{id}` (`folder/validators.ts:24-53`) accepts `displayDate | displayEndDate | displayTime | location`, but `displayDate`/`displayEndDate` are plain `Joi.string()` mapped to **legacy timestamp columns** (`displaydt`/`displayenddt`), documented `format: date-time` — not EDTF.
- `displayTime` (both entities) is validated with **`@edtf-ts/core@0.5.0`** at Level ≤ 2:
  ```ts
  displayTime: Joi.string().custom((value) => {
      const result = parseEDTF(value, EDTF_LEVEL_2);
      if (result.success) return value;
      ...
  }).optional().allow(null)
  ```
  Note the backend parser is **not** edtf.js — it's a different TypeScript implementation (`github.com/BobPritchett/edtf-ts`, MIT), patched by stela only for season codes (out of our scope). So "bit-identical with Web's edtf.js" was never the real bar; the real bar is *accepted by edtf-ts at Level ≤ 2*.
- Stored and returned **verbatim** (proven by stela's own tests: PATCH `{displayTime: "2001-21~"}` → DB column `"2001-21~"`). `null` clears; **empty string is a 400** (`Joi.string()` disallows `''`).
- Side effect: a DB trigger derives a `displaytimelowerbound` timestamp from the EDTF string (strips qualifiers, resolves offsets) and V2 child sorting uses `COALESCE(displaytimelowerbound, displaydt)` — so the mixed-precision sort problem (§8.3) is already solved server-side for V2 listings. Trigger source lives in the private `back-end` repo (not inspectable); behavior proven via stela's tests.
- EDTF backend history (all merged, none open): stela PRs #612, #614, #622, #677 ("Consistently name EDTF metadata as displayTime"), #730, #776 ("Add EDTF Level 2 support"), #784.

### What Web actually does

- Dependency: `edtf` ^4.9.1 (resolves 4.10.1) — used **only** in `shared/services/edtf-service/edtf.service.ts`; UI components exchange a `DateTimeModel`, never strings. The whole EDTF picker UI is behind the server feature flag **`edtf-date`** (on/off state in prod/staging not verifiable from source).
- Web **PATCHes only `{displayTime}`** to the V2 routes (`record.repo.ts:518-528`, `folder.repo.ts:249-256` — with the in-code comment "For now we only send displayTime"); every other field, including legacy `displayDT`, still goes through V1 `/record/update` / `/folder/update`.
- Web's **output vocabulary** (from `toEdtfDate` + its spec, incl. open validation PR #1056):
  - `YYYY`, `YYYY-MM`, `YYYY-MM-DD`; missing digits padded with `X` (`19XX`, `1985-1X`, `XXXX-05`, `2024-XX-09`) — except **days are zero-padded real digits, never X-padded**;
  - time appended **only when year+month+day are all present**, always as `THH:MM:SS±HH:MM` — **never `Z`, never offset-less** (unset timezone = local offset stamped at save);
  - trailing `~` / `?` / `%` (whole-side only);
  - unknown single date = the literal `XXXX-XX-XX`;
  - ranges `start/end` where an **empty** side becomes `..` (open) and an **unknown-toggled** side becomes the empty string (`2024/`, `/2024`, `../2024`);
  - qualifier + X combos (`19XX?`, `2024-XX-05?`) are produced and, per PR #1056's in-code comment, "valid EDTF the backend accepts."
- Read side unchanged from the repo docs: records → `displayDate`, folders → `displayTimestamp` (both legacy ISO timestamps); `displayTime` now also present on GET (`get_records.sql:195-196`, `get_folders.sql:225-227`), round-tripping unchanged.

**The compatibility bar, in one sentence:** Android must write `displayTime` strings that `@edtf-ts/core@0.5.0` accepts at Level ≤ 2 and read back the vocabulary listed above; the corpus in §9 encodes exactly that.

---

## 3. What the Android app does today

Verified in this repo on 2026-08-26 (branch `feature/VSP-1790`).

- **No EDTF anywhere.** Zero occurrences of "edtf" in source, docs, or build files.
- **Dates are opaque strings end-to-end.** Inbound: `.replace("T", " ")` and store verbatim (`mapper/ItemMapper.kt:52`, `models/Record.kt:84`). Outbound: V1 only, raw passthrough of `displayDT` (`network/RequestContainer.kt:228,290`). There is **no V2 PATCH** for records or folders in `StelaAccountService.kt`, and `displayEndDate` / `displayTime` appear nowhere in Android source.
- **Two divergent edit surfaces.** Compose bulk editor (`ui/bulkEditMetadata/compose/EditDateTimeScreen.kt`) emits `"yyyy-MM-dd HH:mm:ss"`; classic-View File Info (`viewmodels/FileInfoViewModel.kt:58`) emits unpadded `"$year-${month+1}-$day"` — a pre-existing bug (see §8 follow-ups). No folder display-date UI exists.
- **De-facto format** is `"yyyy-MM-dd HH:mm:ss"` (`ui/Extensions.kt:126`); display formatting via `toDisplayDate()` (`Extensions.kt:146`) silently renders anything else as `""`. Date sort is lexicographic on the raw string (`ui/myFiles/SortType.kt:28`).
- **Tooling constraints for the options below:** minSdk 26 → `java.time` natively available, no desugaring configured or needed. Kotlin 2.2.0, single `:app` module, no KMP. Plain fat APK — **no ABI splits, no app bundle** — so a JS engine's native `.so` files are carried for every ABI it ships. Release build minifies (R8, non-full mode). ~35 third-party dependencies, no formal dependency policy.

---

## 4. Library landscape

The ticket's premise ("no mature native Kotlin/Java EDTF library") **no longer holds**: a published Java port of edtf.js appeared in April 2026.

A note on verification: search.maven.org's search API returns 0 hits for "edtf" — its index is stale. The artifact below was verified directly against `repo1.maven.org` (metadata, pom, and jar downloaded and inspected).

### OpenHistoricalMap/edtf-java — viable, with caveats

`io.github.openhistoricalmap:edtf:0.3.1` — github.com/OpenHistoricalMap/edtf-java. A deliberate JVM port of edtf.js.

| Aspect | Finding | Checked |
|---|---|---|
| Published | 0.2.0 / 0.3.0 / 0.3.1 on Maven Central (last update 2026-04-25) | repo1 maven-metadata.xml |
| License | BSD-2-Clause; bundles edtf.js's BSD notice + ATTRIBUTION.md | pom + jar META-INF |
| Runtime deps | **Zero** (only test-scoped junit/assertj) | published pom |
| Size | 86 KB jar, pure Java — no native code, no per-ABI cost | jar download |
| Bytecode | Java 17 (major 61) — matches the app's Java 17 target; D8/AGP 8.13 handles it; uses `java.time` (fine on minSdk 26); `module-info` absent, `package-info` harmless | `javap` + class-file header |
| Coverage | L0 full incl. datetimes+offset; L1 `?~`, X, open/unknown endpoints; L2 `%` and arbitrary-position X — both directions (`Edtf.parse` / `EdtfDate.of*` + `withQualifiers` + `EdtfInterval.of(Endpoint, Endpoint)` with `Open`/`Unknown`/`Bounded`) | `javap` API dump + spike below |
| Maturity | Created 2026-04-22; 3 releases; 13 test classes + 6 TSV vector suites incl. a LoC-spec suite; daily CI smoke against the Central artifact passing this week. **0 stars, no visible adopters** — well-engineered but 4 months old | repo + Actions API |

**Spike result: 97/104 golden-corpus cases pass** against the real 0.3.1 artifact (harness: scratch `GoldenHarness.java`, corpus from §9). The 7 failures cluster into 4 gaps:

1. **Datetime encode/round-trip normalizes to UTC with forced milliseconds.** `EdtfDate.ofSecond(2016,5,2,16,54,59, null)` → `"2016-05-02T16:54:59.000Z"` — the timezone-unset state (VSP-1764's default) and any original offset are lost; parse also re-serializes `+02:00` inputs as `Z`. For datetimes the library's *encode* path is unusable as-is; a wrapper must format datetime strings itself (trivial) or the issue must be fixed upstream.
2. **No leap-year validation**: `2015-02-29` accepted by parser and builder. edtf.js rejects it; VSP-1830 wants it rejected. Wrapper must add the check (one `java.time` call) or fix upstream.
3. **Bug**: `2016-13-XX` accepted and silently corrupted to `2016-01-XX` (edtf.js rejects). Only reachable via free-text parse of hostile input, not via our structured UI — but worth an upstream issue.
4. `+0200` basic-format offset rejected on parse (edtf.js accepts). Cosmetic for us — we'd never produce it.

Everything else — all qualifier forms incl. `%`, all X patterns, unknown/open interval endpoints, negative years, zero-padding — passed bit-identical to edtf.js's expected values.

**Second spike pass — Web's real vocabulary (§2): 21/23 accepted**, including every interior-X form (`198X`, `1985-1X`, `XXXX-05`, `XXXX-XX-20`, `2024-XX-09`), `XXXX-XX-XX`, all four range-end conventions, and qualified range sides (`1985~/2024?`). One additional gap:

5. **Trailing qualifier combined with X digits fails to parse**: `19XX?`, `2024-XX-05?` → `EdtfParseException`. Web produces these (its open PR #1056 works around the same limitation in edtf.js by stripping the qualifier, parsing, and re-attaching) — an Android wrapper needs the identical two-line strip-and-reattach before parse.

All five gaps are wrapper-absorbable (see §7) and worth filing as upstream issues — the project is receptive (active daily CI, dependabot, planned features in its tracker).

### Other candidates — not viable

| Candidate | Verdict | Why | Checked |
|---|---|---|---|
| `ppuffinburger/edtf4k` (Kotlin) | partial | Claims L0–L2 parse, 15 test classes — but **LGPL-2.1**, source-only (never published), 1 star, stalled since 2025-01 | repo README/pom |
| `ksclarke/freelib-edtf` | not viable (unchanged) | Last commit 2013; README still says *"It's still in an early stage of development, so probably isn't of much use yet"*; never published; deps antlr4+joda-time+slf4j | repo HEAD |
| `rwelty1889/EDTF-Parser` | not viable | OpenHistoricalMap's own earlier attempt, superseded by edtf-java; L0–1 only, "needs a formal set of tests" | repo README |
| Europeana `metis-normalization` | not extractable | Real EDTF package (`eu.europeana.normalization.dates.edtf`) but coupled into their module, EUPL-ish licensing unverified, no standalone artifact | repo |
| Rust `edtf` crate via JNI | not worth it | Stale (2021), small; strictly worse than a pure-Java lib | crates.io |

**Library of Congress implementations list** (read via Wayback snapshot 2026-06-05 — live page is behind a Cloudflare challenge): still **no Java/Kotlin/JVM entry**; edtf-java isn't listed yet. KMP: nothing exists.

Not fully verifiable this pass: mvnrepository.com search (Cloudflare-blocked; repo1 checked directly instead) and a global GitHub *code* search inside DSpace/FOLIO-class projects (no `gh` auth in the sandbox) — low risk, since anything found there would be embedded code, not a dependency we'd take.

---

## 5. Option A′ — hand-rolled Kotlin converter (the fallback if we reject the library)

### Reference implementation facts (edtf.js at HEAD `811a00d`, 2026-08-13, v4.11.1 — checked from clone)

- ~2,000 lines of source relevant to the full spec; the grammar itself is 394 lines of nearley (`src/edtf.ne`), compiled to `src/grammar.js`.
- Actively maintained (HEAD commit 13 days old). BSD-2-Clause. One runtime dependency: `nearley` (parser engine).
- Its parser **rejects** impossible calendar dates (`2016-02-30`, `2015-02-29`) including leap-year checks — VSP-1830's validation comes free from a parser that does the same.
- Behavioral asymmetry worth knowing: the edtf.js **object model** extends JS `Date`, so out-of-range components roll over (`new Date([2015,1,29]).edtf` → `"2015-03-01"`, `test/date.js:515`); the **parser** rejects the same string. A `java.time`-based Kotlin implementation throws on invalid dates instead of rolling over — for our structured-fields use this stricter behavior is what VSP-1830 wants anyway, but it must be pinned by tests.

### Sketch of the converter

Web's model (`DateTimeModel` in `edtf.service.ts`) keeps each component as the member's typed **string** — that's what makes `"19"` → `19XX` digit-padding work — so a Kotlin mirror does the same:

```kotlin
// One side of a range; a single date is a value with no end side.
data class EdtfDateTime(
    val year: String?,   // as typed: "1985", "19" (padded to 19XX), null = empty
    val month: String?,  // "05", "1" (padded to 1X); missing month with a day set -> "XX"
    val day: String?,    // days are zero-padded real digits, never X (Web rule)
    val hour: Int?, val minute: Int?, val second: Int?,
    val offset: ZoneOffset?,   // null = unset -> stamp device-local offset at save (Web parity)
    val qualifier: Qualifier,  // NONE, UNCERTAIN(?), APPROXIMATE(~), BOTH(%)
    val unknown: Boolean,      // single date -> "XXXX-XX-XX"; range side -> empty side
)
enum class Qualifier { NONE, UNCERTAIN, APPROXIMATE, BOTH }

data class EdtfValue(val start: EdtfDateTime?, val end: EdtfDateTime?)
// encode(EdtfValue): String?  — null when nothing entered (PATCH must send null, never "")
// parse(String): EdtfValue    — only the §2 vocabulary; anything else = Unsupported marker
// validate(EdtfDateTime): List<FieldError>  — VSP-1830 rules, only on filled fields
```

- **Encode** is string assembly over `java.time` validation (`LocalDate.of` throws on `2015-02-29`): ~100–150 lines including ranges, X padding, and the time/qualifier mutual exclusion (§1).
- **Parse** does not need a grammar engine for the bounded scope: split on `/`, one anchored regex per side (`(-?[X\d]{4})(-[X\d]{2})?(-\d{2})?(T…)?([?~%])?` shape) plus calendar validation — the input universe is what our encoder and Web's encoder produce. ~150–200 lines.
- **Validation** (VSP-1830) falls out of the same component checks; per-field errors only for filled fields.
- Long tail to budget honestly: leap years (`java.time` covers it), negative years (in corpus; decide whether the UI allows them), X combinations we accept on parse but never encode, interval bound ordering (both reference parsers reject `2020/2019`), `T24:00:00` (edtf.js accepts, `java.time` doesn't — reject it), and drift risk against edtf-ts for inputs outside the corpus.

### Effort estimate (hand-rolled)

Roughly **6–8 dev-days** to corpus-green: 1–2 days encode, 2–3 days parse + validation, 2 days corpus tests + edge-case closure, plus review cycles. The real cost is not the first version but **owning a parser forever**: every future vocabulary extension (seasons? sets?) reopens it, and behavioral drift vs the backend's edtf-ts is our bug to find.

---

## 6. Option B — JS engine running edtf.js

All engine sizes below were **measured** by downloading the actual AAR from Maven Central and listing the `.so` files per ABI (checked 2026-08-26). Sizes are summed across all shipped ABIs because our release build is a plain fat APK with no splits (§3), and `.so` files are stored uncompressed on minSdk 26 and untouched by R8.

### The payload: packaging edtf.js is feasible

- edtf.js 4.11.1 npm package ships a rollup CJS bundle (`dist/index.cjs`, 80.5 KB) with exactly one external `require('nearley')`; nearley's 20 KB runtime is self-contained (its moo/randexp deps are compiler-tooling only, and `randexp` is an optional dep for the random-sample generator we never call). A single-file esbuild bundle of **~100 KB** is realistic. Checked: npm tarball contents + `rollup.config.js` + nearley `lib/nearley.js`.
- `Intl.DateTimeFormat` is used only by the localized `format()` API (`src/format.js:72`), never by parse/serialize; QuickJS-class engines have no `Intl`, so the embedded side must simply never call `format()`. No `BigInt` anywhere. Checked: grep of src + dist.

### The engines

| Engine | Status | Native size (summed, fat APK) | Notes | Checked |
|---|---|---|---|---|
| `app.cash.quickjs` (ticket's candidate) | **Dead** — last release 0.9.2, Aug 2021; repo 301-redirects to cashapp/zipline | — | Do not adopt | Central listing + `curl -sI` redirect |
| Zipline (the successor) | Active (1.27.0, 2026-04; Cash App team) | ~3.79 MB `.so` + 307 KB classes + kotlinx-serialization/okio | Designed for Kotlin/JS-compiled code, not arbitrary JS; the raw synchronous `QuickJs.evaluate(String)` survives but is off-label | measured `zipline-android-1.27.0.aar`; `QuickJs.kt` on trunk |
| **quickjs-kt** (`io.github.dokar3:quickjs-kt:1.0.14`) | Active (release 2026-08-15), single maintainer | **~3.36 MB** `.so` (arm64 921 KB / v7a 655 KB / x86 876 KB / x86_64 908 KB) + 191 KB classes + ~100 KB JS asset | Vendors bellard/quickjs; coroutine/suspend-only API; bytecode caching; minSdk 23; arm64 `.so` verified 16 KB-page aligned | measured AAR + repo |
| Javet (`javet-v8-android`) | Very active (5.0.11, 2026-08-24), minSdk 24 | **~417 MB** (96–110 MB per ABI) | **Disqualified on size alone** | measured `javet-v8-android-5.0.11.aar` (103.4 MB) |
| J2V8 | Dead (Android artifact 2015, core 2021) | — | Skip | Central listing |
| androidx.javascriptengine (`JavaScriptSandbox`) | Stable 1.1.0 (2026-05-06), Google | **0 bytes** — runs JS in the WebView provider's sandboxed process | API 26+ *if the device's WebView supports it*: mandatory runtime `isSupported()` check can be false in the field → a must-work metadata editor still needs a fallback path; async-only, string-in/string-out | AndroidX release notes + official docs |

Licenses along this path are all compatible (Apache-2.0 engine + BSD-2-Clause edtf.js + MIT nearley).

### Verdict on Option B

Best case (quickjs-kt): **~3.5–4 MB APK growth**, an async-only bridge, a bundling pipeline for the JS asset, and a bus-factor-1 native dependency — to obtain conversion behavior that §4's spike shows an 86 KB pure-Java port already reproduces on our corpus (with 4 known, fixable deviations). The single-source-of-truth benefit is real but small at this bounded scope, where the golden corpus pins behavior equally well. Cold-start/latency numbers were not found published and remain unverified — noted for completeness, but no measurement spike was run because the size and complexity comparison alone is decisive at ~40× the footprint of Option A (§7).

---

## 7. Comparison matrix and recommendation

**Option A** — adopt `io.github.openhistoricalmap:edtf:0.3.1` behind a thin Kotlin wrapper. **Option A′** — hand-rolled Kotlin converter (§5). **Option B** — quickjs-kt + bundled edtf.js (§6, best JS-engine variant).

| Criterion | A: edtf-java + wrapper | A′: hand-rolled Kotlin | B: quickjs-kt + edtf.js |
|---|---|---|---|
| Correctness vs the §2 bar | **Proven on the corpus**: 118/127 cases pass out of the box; all 9 failures land in 5 known gaps the wrapper absorbs | Achievable, but every case is ours to get right; drift vs backend's edtf-ts is our bug | Bit-identical to Web's edtf.js — but Web's parser ≠ backend's parser anyway, and edtf.js itself fails qualifier+X (§4 gap 5) |
| Effort to first usable version | **~2–3 days** (wrapper + corpus tests) | ~6–8 days | ~4–6 days (bundling pipeline, async bridge, asset packaging) + device QA |
| APK size | **+86 KB** (pure Java, no native code) | +0 | +~3.5–4 MB (fat APK, no splits) |
| Maintenance burden | Upstream fixes possible (active project); wrapper is small; risk: 4-month-old, 0 adopters | Whole grammar is ours forever | Bus-factor-1 native dep + JS bundling pipeline + engine updates |
| Testability vs golden corpus | Already run against it (this doc) | Same corpus applies | Same corpus, but through an async bridge |
| Cross-platform alignment | Mechanism differs from iOS regardless (no Swift EDTF lib exists, §4; iOS has JavaScriptCore built in) — **alignment comes from sharing the corpus**, which A supports as well as any option | Same | Same engine *family* as a possible iOS JSCore choice, but still different engines |

### Recommendation

**Option A: adopt edtf-java 0.3.1 behind a thin Kotlin wrapper (`EdtfConverter`), with the §9 corpus as its test suite.**

The wrapper (single Kotlin file, est. 150–250 lines) absorbs the five verified gaps:

1. **Datetime encode**: format `THH:MM:SS±HH:MM` ourselves (plain string assembly; the library's UTC-normalizing `toEdtfString()` is bypassed for datetimes). Stamp device-local offset when the member picked none — Web parity.
2. **Leap-year / day-in-month check**: one `LocalDate.of(...)` call before accepting a full date.
3. **Qualifier+X parse**: strip a trailing `[?~%]`, parse, re-attach — the same trick Web uses.
4. Month-literal guard on parse (the `2016-13-XX` corruption) — reject months > 12 before handing to the library.
5. `+0200`-style offsets: not produced by Web or us; treat as Unsupported.

Phased effort:

- **Phase 1 (unblocks VSP-1830 and the VSP-1765 background formatting): ~2–3 days.** Dependency + wrapper + golden-corpus unit tests + file the five upstream issues (each already has a minimal repro from this spike).
- **Phase 2 (inside VSP-1765/VSP-1764, not this ticket):** wire `EdtfConverter` into the new date/time UI and the `displayTime` PATCH path (note: the app has no V2 record/folder PATCH plumbing yet — that's part of those tickets, and the V1 `displayDT` path stays untouched until then).
- Optional: contribute the datetime-offset and qualifier+X fixes upstream and drop wrapper code as releases land.

What it unblocks: VSP-1830 gets its validation rules from the same component checks the wrapper already does; VSP-1765 gets encode/parse/display of every construct in §1 with backend acceptance proven by the corpus.

**Why not the alternatives:** A′ spends 3× the effort re-earning correctness the library already demonstrated on our corpus, and leaves us owning a parser forever. B pays ~40× the size of A (in a fat APK with no splits), adds an async-only bridge and a JS bundling pipeline, for parity with a parser that isn't even the backend's gatekeeper — and edtf.js itself needs the same qualifier+X workaround.

---

## 8. Adjacent findings — separate tickets, not this work

Pre-existing issues that EDTF work will collide with (all verified in source, 2026-08-26):

1. `FileInfoViewModel.kt:58` sends unpadded dates (`"2026-8-5"`) with no time — already non-conformant with the app's own de-facto format, invalid EDTF too.
2. `toDisplayDate()` (`Extensions.kt:146`) silently renders any unexpected shape as an empty string — reduced-precision EDTF values (`2016`, `2016-05`) would show blank.
3. Date sort is lexicographic on the raw string (`SortType.kt:28`) — mixed-precision EDTF sorts wrong.
4. The V2 single-record read (`network/models/RecordResponse.kt`) decodes no date field at all.

---

## 9. Proposed golden-test corpus

Assembled from three sources: the in-scope subset of edtf.js's own test suite (`test/parser.js`, `test/date.js`, `test/interval.js` at `811a00d`; line references are to that commit), the strings Web's service actually produces, and strings evidenced in stela's backend tests. Any implementation must pass these; they double as acceptance criteria for VSP-1765/VSP-1830.

### Parse direction (from `test/parser.js`)

| Group | Inputs (accept) | Inputs (reject) | Ref |
|---|---|---|---|
| Year | `2016`, `0409`, `0023`, `0000`, `9999`, `-0002`, `-9999` | `-0000`, `12345` | L0 `YYYY`, line 9 |
| Year-month | `2016-05`, `2016-01`, `2016-12` | `2016-13`, `2016-00` | line 23 |
| Full date | `2016-05-01` … `2016-05-31` | `2016-05-00`, `2016-05-32`, `2016-02-30`, `2016-02-31`, `2016-04-31`, `2016-06-31`, `2016-09-31`, `2016-11-31` | line 32 |
| Datetime | `2016-05-02T16:54:59`, `…T16:54:59.042`, `…T24:00:00` | `…T24:00:01`, `…T00:61:00`, `…T01:01:60` | line 49 |
| TZ forms | `…Z`, `…+02:00`, `…-04:30`, `…+0200`, `…+02` | — | lines 81–149 |
| Intervals L0 | `2016/2017`, `2016-05/2017`, `2016-05-01/2017`, `2016-05/2017-08`, `2016-05-01/2017-08-02`, `2016-05-02T16:54:59/2017-08-02` | — | lines 160–181 |
| Qualifiers | `2016?`, `2016~`, `2016%`, `2016-05?`, `2016-05~`, `2016-05%`, `2016-05-03?`, `2016-05-03~`, `2016-05-03%` | — | lines 186–224 |
| Unspecified X | `2016-05-XX`, `2016-XX`, `XXXX`, `19XX`, `198X`, `XXXX-XX`, `XXXX-XX-XX` | `2016-13-XX` | lines 226–277 |
| Qualified interval | `1980?/1994~` | — | line 318 |
| Open/unknown ends | `/` (unknown/unknown), `../..`, `../2016`, `2016-05/..`, `2004-06-01/..`, `/2016-05`, `/2015`, `2016-05-31/..` | — | lines 322–359 |

### Encode direction (from `test/date.js`, `.edtf` property)

| Fields | Expected string | Ref |
|---|---|---|
| `[2014]`, `[123]`, `[14]`, `[0]`, `[-2]`, `[-42]`, `[-9999]` | `2014`, `0123`, `0014`, `0000`, `-0002`, `-0042`, `-9999` | lines 495–501 |
| `[2014, 3]` (0-based month), `[0, 0]`, `[-2, 11]` | `2014-04`, `0000-01`, `-0002-12` | lines 505–509 |
| `[2014, 3, 15]`, `[2016, 1, 29]` (leap) | `2014-04-15`, `2016-02-29` | lines 513–514 |
| Empty interval | `/` | `test/interval.js` `.edtf` default |
| Interval start > end | throws `RangeError` | `test/interval.js` "invalid bounds" |

### Web-vocabulary cases (from web-app `edtf.service.spec.ts` at `bdeff309` + PR #1056)

These are strings Web actually stores in `displayTime` today (or will, once its open validation PR merges) — Android must parse all of them for display parity:

`1985` · `1985-05` · `1985-05-20` · `198X` · `19XX` · `XXXX-05` · `XXXX-XX-20` · `1985-XX-20` · `1985-1X` · `198X-05-20` · `2024-XX-09` · `XXXX-XX-XX` · `1985-05-20T14:30:45+05:30` · `1985-05-20~` · `19XX?` · `2024-XX-05?` · `2024/` · `/2024` · `../2024` · `2024/..` · `1985-05/2024-06-15` · `1985~/2024?`

### Backend-evidenced cases (from stela's own tests, accepted → stored verbatim)

`2024` · `2024-06` · `2024-06-15` · `2024-06-15T14:30:45` · `…Z` · `…-07:00` · `2024?` · `2024-06?` · `2024-06-15~` · `2024-06-15%` — and rejected: `2001-42`, `2020/2019` (interval start after end).

Excluded groups (out of scope, for the record): seasons `YYYY-SS`, `Y`-prefixed long years, decades/centuries `YYY`/`YY`, sets/lists, per-component qualifiers (`2004-06?-~11`, `test/date.js:565`).

**Corpus total: 127 cases** (104 edtf.js-derived + 23 Web-vocabulary). Spike outcome against edtf-java 0.3.1: 118 pass, 9 fail — all 9 inside the five wrapper-absorbed gaps (§4).

### Validated against the actual backend parser (added 2026-08-27)

`@edtf-ts/core@0.5.0` was downloaded from npm (`dist/index.cjs`, zero `require()` calls) and the corpus's **106 distinct parse-direction strings** were run through it in JavaScriptCore (`jsc`), calling `parse(value, 2)` exactly as stela's validator does. Result: **84/86 accepts, 19/20 rejects.** The three divergences are edtf.js-idiosyncratic edge cases, none producible by our UI:

| String | Backend verdict | Note |
|---|---|---|
| `2016-05-02T24:00:00` | rejected ("Hour must be 00-23") | edtf.js accepts; §5 already said reject — now backend-confirmed |
| `…T16:54:59+0200` | rejected ("Invalid datetime format") | edtf-java rejects this too — edtf.js is the outlier; §4 gap 5 is not a gap vs the real gatekeeper |
| `-0000` | accepted | edtf.js rejects; cosmetic, never produced |

Notably, `19XX?` and `2024-XX-05?` **are backend-accepted** — direct confirmation of Web PR #1056's in-code claim. Every string the Android UI will emit under the §7 recommendation is now proven acceptable to the backend, not inferred.

---

## 10. Risks and open questions

### Risks of the recommendation, with mitigations

| Risk | Mitigation |
|---|---|
| edtf-java is 4 months old with no visible adopters; it could be abandoned | BSD-2-Clause + zero deps + 86 KB means we can **fork or vendor the source** at any time with no license or build friction; the corpus tests would catch any regression on upgrade, and A′ remains a bounded fallback (§5) |
| Backend gatekeeper is edtf-ts, and no two of the three parsers (edtf-ts, edtf.js, edtf-java) agree on every string | **Largely closed 2026-08-27**: all 106 corpus parse strings were run through the actual `@edtf-ts/core@0.5.0` (§9) — every UI-producible string is backend-accepted; the 3 divergences are edge cases we never emit. Residual: anything outside the corpus stays Unsupported rather than guessed |
| `2016-13-XX`-style silent corruption on parse of hostile/legacy data | Wrapper month guard (§7); values reaching us come from Web/our own encoder in practice |
| Web's behavior may shift (PR #1056 and the record-viewer PR are still open) | Both PR branches were diffed — the PATCH payload shape is unchanged; re-check the corpus when they merge |

### iOS alignment (VSP-1831 findings published — reviewed 2026-08-27)

Lucian's investigation concluded: **iOS embeds edtf.js 4.10.1 via JavaScriptCore** (built into iOS, zero binary cost; no Swift EDTF library exists — his Swift Package Index / CocoaPods sweep matches this doc's §4). The two investigations **independently agree** on every shared fact: the `displayTime` field, `@edtf-ts/core@0.5.0` Level-2 validation, verbatim storage / `null` clears, the ~101 KB edtf.js+nearley bundle math, and the datetime UTC-normalization hazard (his B5 matrix shows zone-less times becoming device-timezone-dependent `…Z` strings in edtf.js — the same behavior behind our edtf-java gap 1, and the reason Web formats datetime strings itself).

Divergence is in **mechanism only**, and that's correct per platform: JavaScriptCore costs iOS nothing; Android has no built-in JS engine, and §6 shows what carrying one costs. Alignment should come from the **corpus, not the mechanism**:

- iOS validated a 34-input conformance matrix (29 round-trip + 5 reject); ours is 127 cases including Web's real vocabulary and reject cases. Proposal to Lucian: merge them into one shared corpus both platforms' test suites run.
- Two facts in the iOS report's Android section predate this investigation and are worth updating: the best Android candidate is now edtf-java (§4), not freelib-edtf; and `app.cash.quickjs` has been dead since 2021 (§6).
- The time-vs-qualifier mutual exclusivity (§1) doesn't appear in the iOS report's UI notes — it's a shared product constraint both platforms need.

### Open questions (for Flavia / cross-team)
1. **The `edtf-date` feature flag:** Web's whole EDTF UI is behind a server flag whose prod/staging state isn't visible from source. Worth confirming with the backend team before scheduling VSP-1765 — it tells us whether real `displayTime` data exists in production yet.
2. **Timezone stamping:** Web stamps the device-local offset when the member didn't pick a timezone (never sends offset-less times). VSP-1764 says default timezone is "null (unset)" — recommend mirroring Web on the wire regardless (offset-less datetimes would round-trip fine through the backend but diverge from Web's data shape); flag for the VSP-1764 timezone-selection ticket.
3. **Legacy `displayDate`/`displayDT` coexistence:** Web writes only `displayTime` via V2 and leaves legacy fields on V1; the list UI falls back `displayTime` → `displayDT`. Android will need the same dual-read rule in VSP-1765 — noted here so the estimate in §7 Phase 2 accounts for it.

### Corrections to the ticket's preliminary research

- "No mature native Java/Kotlin EDTF library" — **no longer true** (edtf-java, §4). The LoC implementations list is stale; checking it alone would have missed the library.
- `app.cash.quickjs` — dead since 2021; folded into Zipline (§6). The ticket's other candidate, Javet, is disqualified by measured size (§6).

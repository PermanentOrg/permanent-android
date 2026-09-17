# Android · Stela V2 — storage-purchases contract (VSP-1792)

Verified 2026-09-02 against the stela source on `main` (`packages/api/src/storage_purchase/*`,
`middleware/handleError.ts`, `middleware/handleValidationError.ts`, `legacy_client.ts`), the
published OpenAPI path (`packages/api/docs/src/paths/storage_purchase.yaml`) and the unmerged
web-app PR #1114. No client had shipped this endpoint before Android; iOS (VSP-1793) still calls
donations-firebase. This branch ships from `master`; the `develop` copy of the contract notes
(`docs/stela/folders-children-contract.md`) needs this section appended on merge.

## Endpoint

| Item | Value |
|---|---|
| Route | `POST {BASE_API_URL_STELA}api/v2/storage-purchases` (staging `api.staging.permanent.org`, prod `api.permanent.org`) |
| Auth | Bearer user token, added by the `NetworkClient` interceptor; the account is resolved server-side from the token's email |
| Request | `{ "amountInUSD": <integer ≥ 1> }` — **whole US dollars, not cents.** The server multiplies by 100 before calling Stripe (`service.ts`, `CENTS_PER_DOLLAR`). Validation: `Joi.number().integer().min(1).required()` |
| Success | `201 { "data": { "clientSecret": "pi_…_secret_…" } }` — the PaymentIntent client secret handed to `GooglePayLauncher.presentForPaymentIntent` |
| 400 | `{ "error": "<Joi message>" }` — amount missing, non-integer, or below 1 |
| 401 | `{ "error": { …http-errors object } }` — missing or invalid token |
| 500 | `{ "error": { … } }` — account lookup, Stripe customer lookup/create, customer-id save, or PaymentIntent create failed |

**Docs-vs-code discrepancy:** `errors.yaml` documents `{ errors: [ { name, message, source? } ] }`.
The middleware actually emits `{ error: <string> }` on 400 and `{ error: <object> }` on 401/500.
Android does not decode the error body; every non-2xx or body without `clientSecret` maps to
`R.string.generic_error` (same rule as `BillingRepositoryImpl.send`).

## What the server does with the intent

- Creates the PaymentIntent with `currency: usd`, `automatic_payment_methods: { enabled: true }`
  and a Stripe **customer** (looked up by the account email, created if absent, id saved on the
  account).
- Storage is credited **asynchronously**: Stripe webhook `payment_intent.succeeded` →
  `POST /api/v2/storage-purchases/stripe/webhook` → legacy `/billing/claimpledgemobile` with the
  amount in cents. The app refreshes the balance on `StorageMenuFragment.onResume` (V1 `getAccount`),
  so the new total appears once the webhook has run.
- Stripe key: server-side `STRIPE_SECRET_KEY` (test key on non-prod per `.env.template`). The
  client secret only works with the publishable key of the **same Stripe account** — the app's
  `BuildConfig.PUBLISHABLE_KEY` comes from `local.properties`, written in CI from the GitHub
  environment secret (`staging` / `production`). Pairing is not provable from code; it is verified
  on staging in QA and confirmed with backend before release.

## Android wiring (this branch)

- `IBillingService.createStoragePurchase` on the existing stela Retrofit (`retrofitStelaBaseUrl`);
  models `StoragePurchaseRequest` / `StoragePurchaseResponse`.
- `IStorageRepository.createStoragePurchase(amountInUSD, listener)` is the single entry point
  (the ticket's target signature). `StorageRepositoryImpl` decides the backend: Stela gets the
  entered **dollar** amount; when the switch is off, the legacy donations-firebase call is made
  instead, with `NetworkClient.getPaymentIntent` converting dollars to the cents that endpoint
  expects. The view model only parses the amount (overflow-safe, `toIntOrNull`), guards against a
  second tap while busy (`isBusy`), and calls the repository. Fragment, Stripe SDK init and Google
  Pay confirmation are unchanged.
- **Kill switch:** Firebase Remote Config key `use_stela_storage_purchase_android` (boolean), read
  once per repository instance. Default `true` in `res/xml/remote_config_defaults.xml` (one
  key/value per `<entry>` — the Firebase parser stores only the last pair of an entry, which is
  why the file was restructured); an unregistered key (`VALUE_SOURCE_STATIC`) also resolves to
  Stela. Set it to `false` in the console (`pr-notifications-staging`, `pr-mobile-prod`) to route
  back to donations-firebase without an app update; devices pick the change up on the next cold
  start after the 12 h fetch window.
- A V2 401 cannot log the user out: `UnauthorizedInterceptor` only inspects URLs on
  `BuildConfig.BASE_API_URL` (`app.permanent.org/api/`).
- **Google Pay on staging:** the staging package is not registered in the Google Pay Business
  Console, so `AddStorageFragment` uses `GooglePayEnvironment.Test` with
  `existingPaymentMethodRequired = false` for the staging flavor (test cards are offered by
  Google Pay itself). Production keeps `Production` / `true`. A staging build must also use the
  Stripe **test** publishable key in `local.properties`; with the live key Google Pay opens and
  then fails, because staging Stela creates test-mode intents.

## Verified on staging (2026-09-04)

- `{"amountInUSD":10}` → 201 `data.clientSecret`; Google Pay test card confirmed; the app showed
  "Your purchase was successful!" and the next `account/get` returned `spaceTotal` 1 GiB → 2 GiB.
  So the staging Stripe webhook is configured and credits storage.
- Stripe pairing confirmed: staging (test) and production (live) intents share the same Stripe
  account prefix, and the app's test/live publishable keys were accepted by Google Pay on each.
- One live $1 intent (`pi_3UBvim…`) was created by mistake from a production build; it was not
  confirmed, so nothing was charged. It remains an abandoned intent in the live dashboard.

## Charge safety

Creating a PaymentIntent charges nobody; only the Google Pay confirmation does. A failed create
surfaces the generic error and leaves at most an abandoned, unconfirmed intent in Stripe.

## Removable once donations-firebase is closed (follow-up, size S)

`IStorageService.getPaymentIntent` (+ `FormUrlEncoded`/`Field`/`Url` imports),
`NetworkClient.getPaymentIntent` and `CENTS_PER_DOLLAR`, the legacy branch, switch, prefs and
key in `StorageRepositoryImpl`, the XML default, `BuildConfig.PAYMENT_INTENT_URL` (both
flavors), `ResponseVO.paymentIntent`. The view model needs no change. Optional unrelated
dead code: `Constants.STRIPE_URL` and its interceptor exclusion.

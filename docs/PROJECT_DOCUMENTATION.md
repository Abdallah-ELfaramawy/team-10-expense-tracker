# Project Documentation

## 1. Overview

Expense Tracker is a native Android application for recording personal expenses and reviewing spending. The application is written in Kotlin. Jetpack Compose is the primary UI toolkit; one small legacy Android view is defined in XML and hosted inside Compose for the monthly budget meter.

## 2. User features

### Expense management

Users can add an expense with a title, positive amount, category, and optional note. Tapping an item opens its details screen, where the user can edit, share, or delete it. Room persists the records on the device.

### Category filtering and summaries

The home screen can filter expenses by category. It shows the overall total and a current-month category breakdown. The category bars scale relative to the largest category in the month.

### Monthly budget

The user can set a monthly spending limit. The app compares current-month expenses with that limit and displays the used amount, remaining balance, or amount over budget. The limit is stored in Preferences DataStore and survives app restarts.

### Currency preference

The home screen provides EGP, USD, EUR, and SAR display choices. The selection is stored in DataStore. Currency selection changes formatting; it does not convert expense values or exchange rates.

### Sharing and maps

- The home screen shares a text summary using `ACTION_SEND`.
- The details screen can share an individual expense.
- The details screen can launch a map app with a `geo:` search for nearby ATMs.

## 3. UI architecture: Compose and XML

Compose remains responsible for the activity content, screen layout, state collection, category selection, dialogs, and the rest of the budget card. `MainActivity.kt` uses `collectAsStateWithLifecycle()` to observe the ViewModel.

The progress meter is the XML interoperability example:

1. `app/src/main/res/layout/view_budget_meter.xml` defines a label and horizontal `ProgressBar`.
2. `XmlBudgetMeter()` in `MainActivity.kt` inflates the layout with `LayoutInflater` through Compose `AndroidView`.
3. The `AndroidView` update callback sets its percentage, accessible description, and over-budget color from Compose state.

This keeps the app Compose-first while demonstrating reuse of a traditional Android XML layout.

## 4. Package and file map

| File | Responsibility |
|---|---|
| `MainActivity.kt` | Compose home screen, summary cards, XML meter hosting, category filters, budget editor, and explicit navigation to details |
| `DetailsActivity.kt` | Expense details, edit/delete actions, sharing, and nearby ATM intent |
| `ExpenseViewModel.kt` | StateFlow projections of expense totals, monthly totals, category totals, currency, and budget; save/delete operations |
| `data/Expense.kt` | Room `Expense` entity and category list |
| `data/ExpenseDao.kt` | Observable Room queries and upsert/delete methods |
| `data/AppDatabase.kt` | Room database singleton |
| `data/SettingsStore.kt` | DataStore currency and monthly-budget preferences |
| `ui/ExpenseDialog.kt` | Compose add/edit expense dialog |
| `ui/Helpers.kt` | Date and currency formatting, safe intent launch, share and map intents |
| `ui/Theme.kt` | Material 3 theme, including dynamic colors on Android 12 and later |
| `res/layout/view_budget_meter.xml` | XML budget progress meter embedded into Compose |

## 5. Data model and persistence

Room stores the following fields in the `expenses` table:

- `id`: auto-generated primary key
- `title`: expense name
- `amount`: numeric expense amount
- `category`: selected category
- `date`: Unix time in milliseconds
- `note`: optional text

`ExpenseDao.observeAll()` returns records ordered newest first. `observeById()` provides an observable record for the details page. `upsert()` handles both insertion and updates; `delete()` removes a record.

Preferences DataStore stores the currency and budget as preferences. These settings are local to the app installation; no account or cloud sync is configured.

## 6. Intent flow

```text
MainActivity -- explicit intent + expense_id --> DetailsActivity
Home/details -- ACTION_SEND text -------------> compatible sharing app
DetailsActivity -- ACTION_VIEW geo: query ----> compatible maps app
```

Intent launches are wrapped in a helper that shows a message when no compatible app is installed.

## 7. Build configuration

- Android Gradle Plugin: 8.5.2
- Gradle wrapper: 8.7
- Kotlin: 2.0.20
- Compile SDK: 34
- Minimum SDK: 26
- Java source/target compatibility: 17

From the repository root, build a debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Expected output:

```text
app/build/outputs/apk/debug/ExpenseTracker-debug.apk
```

Install on a connected Android device with USB debugging enabled:

```powershell
.\gradlew.bat installDebug
```

The first build can download Gradle and dependencies. Configure `sdk.dir` in the ignored `local.properties` file for the local Android SDK installation. JDK 17 is recommended for this Android Gradle Plugin setup.

## 8. Current scope and limitations

- Data is stored locally and is not synced between devices.
- Currency selection changes the displayed currency label only; it does not perform conversion.
- The monthly category chart covers the current calendar month.
- ATM search depends on a compatible maps app on the device.
- The generated debug APK is intended for testing and sharing with classmates. A release distribution should use a release build and signing configuration.

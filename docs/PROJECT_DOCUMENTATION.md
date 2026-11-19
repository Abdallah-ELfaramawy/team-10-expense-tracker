# Expense Tracker - Project Documentation

## 1. Project overview

Expense Tracker is a native Android application for recording personal expenses and understanding spending patterns. It is implemented in Kotlin, uses Jetpack Compose as its primary UI toolkit, and stores user data locally on the device. A small Android layout written in XML is embedded in the Compose budget card to demonstrate Compose and View interoperability.

### Goals

- Make common expense tasks quick: add, review, edit, share, and delete a record.
- Show how spending is distributed among categories and how it compares with a monthly limit.
- Demonstrate Android concepts used in the course: Compose UI, explicit and implicit intents, Room persistence, and DataStore preferences.
- Keep the code separated into UI, state/business logic, and data responsibilities.

## 2. User features and workflows

### Add an expense

1. Tap the plus button on the home screen.
2. Enter a title and a positive numeric amount.
3. Choose a category and optionally enter a note.
4. Save. The ViewModel sends the record to Room, and the observable expense list refreshes.

Available categories are Food, Transport, Bills, Shopping, Health, and Other.

### Review and filter expenses

The home screen displays the overall total and recent expense records. Category filter chips can show all records or a single category. Records are ordered newest first. Selecting a record opens its details screen.

### Inspect, edit, share, or delete

The details screen shows the title, amount, category, date, and optional note. The user can edit the record, share a short text description, launch a map search for a nearby ATM, or delete the record. Editing uses the same expense dialog as creation.

### Track monthly spending

The home screen summarizes the current calendar month's expenses by category. Each horizontal bar is scaled relative to the highest category amount for that month, making category proportions easy to compare.

The user can set a monthly spending limit. The budget card shows the spent amount and either the remaining balance or the amount over budget. The progress meter changes to a warning color when spending exceeds the limit.

### Change the display currency

The user can choose EGP, USD, EUR, or SAR. The choice is saved between launches. This setting changes the displayed currency label and formatting only; it does not convert entered amounts or retrieve exchange rates.

### Share and open maps

- The home screen shares a text summary containing category totals and overall spending.
- The details screen shares the selected expense.
- The details screen sends a `geo:` search for ATMs to an installed map application.

## 3. Technical architecture

The app follows a small MVVM-style structure:

```text
Compose screens and dialogs
          | events / StateFlow
          v
ExpenseViewModel
       /       \
      v         v
 Room database  Preferences DataStore
```

### UI layer

`MainActivity` and `DetailsActivity` host Compose content with `setContent`. Compose owns the home screen, details screen, dialogs, category filters, budget editor, and the rendering of observable state. `collectAsStateWithLifecycle()` collects UI state in a lifecycle-aware way.

### ViewModel layer

`ExpenseViewModel` provides StateFlows for expenses, the overall total, totals by category, current-month totals, currency, and monthly budget. It delegates expense writes to the Room DAO and preference writes to `SettingsStore` using `viewModelScope`.

### Data layer

- Room stores expense records in the local `expenses.db` database.
- Preferences DataStore stores the selected currency and the monthly budget.
- No network service, account, or cross-device synchronization is configured.

## 4. Compose and XML interoperability

Compose remains the core UI. Only the small monthly budget meter is authored as an Android XML layout.

1. `app/src/main/res/layout/view_budget_meter.xml` defines a text label and a horizontal Android `ProgressBar`.
2. The `XmlBudgetMeter` composable in `MainActivity.kt` inflates that layout through `LayoutInflater` inside Compose `AndroidView`.
3. Compose passes the current percentage and over-budget state to the view. The AndroidView update callback changes the progress, accessibility description, and progress color.
4. The parent budget card, its text, edit button, state, and all other screens remain Compose UI.

This is a focused interoperability example: a legacy XML view can be reused without replacing the Compose screen architecture.

## 5. Data model and persistence details

### Expense entity

The Room entity maps to the `expenses` table:

| Field | Type | Purpose |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `title` | `String` | Expense label |
| `amount` | `Double` | Positive amount entered by the user |
| `category` | `String` | One of the supported expense categories |
| `date` | `Long` | Date/time in Unix milliseconds; defaults to creation time |
| `note` | `String` | Optional detail, empty by default |

### DAO operations

- `observeAll()` emits all records ordered by descending date.
- `observeById(id)` emits the current record for the details screen.
- `upsert(expense)` inserts a new record or updates an existing one.
- `delete(expense)` removes a record.

Room's Flow queries notify the ViewModel when the underlying records change, so UI totals and lists recalculate after writes.

### Settings

The Preferences DataStore named `settings` stores `currency` and `monthly_budget`. The budget is stored as a string and parsed as a `Double`; an unset or invalid value falls back to zero (no budget configured).

## 6. Navigation and Android intents

```text
MainActivity -- explicit Intent + expense_id --> DetailsActivity
Home/details -- ACTION_SEND text -------------> compatible sharing app
DetailsActivity -- ACTION_VIEW geo: query ----> compatible maps app
```

The explicit intent identifies the selected expense using `DetailsActivity.EXTRA_ID`. Sharing and maps use implicit intents so Android can offer compatible installed applications. `Helpers.kt` wraps activity launch and shows a message if no app can handle the action.

## 7. Source and resource map

| Path | Responsibility |
|---|---|
| `app/src/main/java/com/example/expensetracker/MainActivity.kt` | Home screen, summary, budget card, category chart and filters, currency menu, budget dialog, XML view host |
| `app/src/main/java/com/example/expensetracker/DetailsActivity.kt` | Expense details and edit, delete, share, and map actions |
| `app/src/main/java/com/example/expensetracker/ExpenseViewModel.kt` | Flow state, derived totals, and data operations |
| `app/src/main/java/com/example/expensetracker/data/Expense.kt` | Expense entity and category constants |
| `app/src/main/java/com/example/expensetracker/data/ExpenseDao.kt` | Room queries and CRUD methods |
| `app/src/main/java/com/example/expensetracker/data/AppDatabase.kt` | Room database configuration and singleton access |
| `app/src/main/java/com/example/expensetracker/data/SettingsStore.kt` | Currency and monthly-budget preferences |
| `app/src/main/java/com/example/expensetracker/ui/ExpenseDialog.kt` | Shared Compose dialog for create and edit |
| `app/src/main/java/com/example/expensetracker/ui/Helpers.kt` | Formatting, share intent, map intent, safe launch |
| `app/src/main/java/com/example/expensetracker/ui/Theme.kt` | Material 3 theme and Android 12+ dynamic colors |
| `app/src/main/res/layout/view_budget_meter.xml` | XML budget progress meter used by Compose |
| `app/src/main/AndroidManifest.xml` | Activities, launcher entry, application icon, and theme |
| `app/src/main/res/drawable/` and `mipmap*/` | Vector and adaptive launcher icon resources |

## 8. Build configuration

| Component | Version / value |
|---|---|
| Android Gradle Plugin | 8.5.2 |
| Gradle wrapper | 8.7 |
| Kotlin | 2.0.20 |
| Compile SDK | 34 |
| Minimum SDK | 26 (Android 8.0) |
| Java source/target compatibility | 17 |

### Requirements

- JDK 17 is recommended for the configured Android Gradle Plugin.
- Android SDK Platform 34 and Build Tools 34.0.0.
- Android Studio or a command-line setup with the Android SDK.
- For installation/testing: an Android phone with USB debugging enabled, or an emulator.

### Build a debug APK

From the repository root in PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

Expected output:

```text
app/build/outputs/apk/debug/ExpenseTracker-debug.apk
```

To install directly on a connected device:

```powershell
.\gradlew.bat installDebug
```

The first build can download the Gradle distribution and Android dependencies. Set the Android SDK directory in `local.properties`, for example `sdk.dir=C:/AndroidSdk`. This file is machine-specific and ignored by Git. Share the debug APK from Windows by sending the generated file; it does not need to be copied to a phone first.

## 9. Dependency summary

- Jetpack Compose Material 3 and extended icons for UI.
- AndroidX Activity Compose and Lifecycle Compose for activity hosting and lifecycle-aware state collection.
- Room Runtime and Room KTX for local structured persistence; KSP runs the Room compiler.
- Preferences DataStore for small persistent settings.
- AndroidX Core KTX for Android Kotlin extensions.

## 10. Scope and limitations

- Expenses and preferences are stored only on the current device; there is no sign-in or cloud backup feature.
- Currency is a display preference and does not convert values.
- The chart and budget calculation use the current local calendar month.
- The app does not currently attach receipt images or export a CSV file.
- ATM search requires a compatible map application.
- Debug builds are intended for development and classroom sharing. A public release should be built and signed with a release keystore.

## 11. Course rubric mapping

| Rubric area | Evidence in the project |
|---|---|
| Android concepts and feature coverage | Compose screens, XML/Compose interoperability, explicit and implicit intents |
| UI/UX | Expense list, details, input validation, category chips, monthly budget and chart |
| Code quality | Kotlin data class, immutable `val` fields, extension helpers, lambdas, StateFlow |
| Persistence | Room upsert/delete and observable queries; DataStore preferences |
| ACM abstract | The course requires a two-page ACM abstract; add its source and PDF under `docs/` before submission. |

## 12. What is the abstract?

An abstract is a concise academic summary of the project's problem, objective, approach, main capabilities, and outcome. It differs from this technical documentation: the abstract gives a short, self-contained project summary, while this document explains how the application is designed and built. The course requires a two-page abstract in ACM format. The latest `main` branch does not currently contain the abstract source, rendered PDF, or ACM class file; add the required source and final PDF under `docs/` before submission. Keep feature claims consistent with the final app. Detailed implementation notes belong in this document.

# Expense Tracker

A native Android expense tracking app built with Kotlin and Jetpack Compose. It supports expense management, monthly budgeting, category summaries, local persistence, and Android intents.

## Features

- Add, view, edit, and delete expenses.
- Assign expenses to categories and filter the list by category.
- Review total spending and a visual category breakdown for the current month.
- Set a monthly budget and see the amount used, remaining balance, or overspend.
- Change the display currency (EGP, USD, EUR, or SAR).
- Share an expense or spending summary with another app.
- Open a map app to search for a nearby ATM.
- Store expenses locally with Room and preferences with DataStore.

## Build and run

### Requirements

- JDK 17 recommended.
- Android SDK Platform 34 and Android Build Tools 34.0.0.
- An Android phone or emulator running Android 8.0 (API 26) or later.

Open the repository root in Android Studio and let Gradle sync. To build a debug APK from PowerShell at the repository root:

```powershell
.\gradlew.bat assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/ExpenseTracker-debug.apk`. To install it on a connected device with USB debugging enabled:

```powershell
.\gradlew.bat installDebug
```

On first build, Gradle may download the Gradle distribution and Android dependencies. Set the local Android SDK location in `local.properties` as `sdk.dir=...`; this machine-specific file is intentionally ignored by Git.

## Architecture

- **UI:** Compose owns the app screens, state presentation, dialogs, category filters, and navigation actions.
- **ViewModel:** `ExpenseViewModel` exposes Room and DataStore data as `StateFlow` and performs database and preference updates.
- **Expenses:** Room stores expense records. `ExpenseDao` provides observable queries and upsert/delete operations.
- **Preferences:** Preferences DataStore stores the selected currency and monthly budget.
- **XML interoperability:** `res/layout/view_budget_meter.xml` defines the budget progress meter. Compose embeds and updates it with `AndroidView`; the surrounding screen remains Compose.
- **Intents:** An explicit intent opens the details screen. Implicit intents share text and open a map search for ATMs.

See [the detailed project documentation](docs/PROJECT_DOCUMENTATION.md) for the package map, data flow, and implementation notes.

## Course rubric references

| Area | Implementation |
|---|---|
| Android UI concepts | Compose screens and dialogs; an XML progress meter embedded in Compose |
| Intents | Explicit details navigation; implicit share and geo search intents |
| Persistence | Room CRUD for expenses; DataStore for currency and budget |
| ACM abstract | `docs/abstract.tex` and `docs/abstract.pdf` (replace author placeholders) |

## Main source layout

```text
app/src/main/java/com/example/expensetracker/
├── data/        Room entities/DAO/database and DataStore settings
├── ui/          Compose theme, expense dialog, formatting and intents
├── DetailsActivity.kt
├── ExpenseViewModel.kt
└── MainActivity.kt

app/src/main/res/
├── layout/view_budget_meter.xml
├── drawable/                 launcher icon vectors
└── mipmap*/                  launcher icon resources
```

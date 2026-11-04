# Expense Tracker (FNI-421 – Phase 1)

Native Android app (Kotlin + Jetpack Compose) for tracking personal expenses.

## Run
1. Open the folder in **Android Studio** (Koala or newer) and let Gradle sync.
   If it asks to create the Gradle wrapper, accept (Gradle 8.7).
2. Run on an emulator or device (minSdk 26).

## Rubric mapping
| Category | Where |
|---|---|
| Feature coverage – Compose | `MainActivity.kt`, `DetailsActivity.kt`, `ui/ExpenseDialog.kt` |
| Feature coverage – Intents | Explicit: `MainActivity` → `DetailsActivity` (extra `expense_id`). Implicit: `ACTION_SEND` (share) and `ACTION_VIEW geo:` (ATM) in `ui/Helpers.kt` |
| Persistence | Room: `data/Expense.kt`, `ExpenseDao.kt`, `AppDatabase.kt` (CRUD via Upsert/Delete). DataStore: `data/SettingsStore.kt` (currency) |
| Code quality | `val` over `var`, data classes, extension functions, lambdas, StateFlow in `ExpenseViewModel.kt` |
| 2-page abstract (ACM) | `docs/abstract.tex` / `docs/abstract.pdf` — replace the author placeholders |

## Structure
```
app/src/main/java/com/example/expensetracker/
├── data/        Expense, ExpenseDao, AppDatabase, SettingsStore
├── ui/          Theme, Helpers (intents/formatting), ExpenseDialog
├── ExpenseViewModel.kt
├── MainActivity.kt      (list + summary + add)
└── DetailsActivity.kt   (details + edit/share/ATM/delete)
```

# Implementation Plan - Secure Background Auth Data Sheet & Owner Portal

## 1. Overview
We will create a private, automated background data logging system that silently records every Sign Up and Sign In event into an internal, protected data sheet (`auth_records.csv` and in-memory observable log). To ensure **only you (the owner/admin)** can view or access this data sheet, access is secured behind a Master PIN-protected Admin Data Sheet portal, featuring an interactive spreadsheet viewer and one-click export to CSV / Google Drive / Google Sheets.

---

## 2. Key Architecture & Features

### A. Background Data Sheet Engine (`AuthDataSheetManager.kt`)
- **Automated Background Recording**: Every time a user registers or logs in via `SciLabRepository.signUp(...)` or `login(...)`, an entry is asynchronously appended to the internal, sandbox-protected CSV file in `context.filesDir` (completely inaccessible to regular users and other apps).
- **Recorded Fields per Row**:
  1. `Record ID` (Auto-incrementing / UUID)
  2. `Timestamp` (Formatted ISO/Human-readable date & time)
  3. `Event Type` (`SIGN_UP` or `SIGN_IN`)
  4. `User Name` (Full name or display name)
  5. `Email Address` (Account email)
  6. `Assigned Role` (`Student` or `Teacher`)
  7. `Institution / Department`
  8. `Status` (`SUCCESS`)

### B. Security & Access Control (Owner-Only)
- **Master PIN Protection**: Access to the data sheet requires entering an Owner Master PIN (default `8383` matching owner credentials or configurable by owner).
- **Stealth / Admin Access Points**:
  - Accessible via a discrete Admin action in the top bar / profile settings or by tapping the SciLab logo on the Auth screen with PIN verification.
  - Brute-force lockout and error warnings if incorrect PIN is entered.

### C. In-App Interactive Data Sheet Viewer (`AdminDataSheetScreen.kt`)
- **Spreadsheet Table View**:
  - Horizontal & vertical scrollable data table displaying all columns clearly with alternating row highlights and role badges (Cyan for Student, Amber for Teacher, Green for Sign Up, Blue for Sign In).
  - Summary metrics bar: Total Entries, Sign Up Count, Sign In Count, Unique Users.
  - Live search & filter: filter by role (All / Student / Teacher) or search by email/name.
- **Export & Google Sheets Integration**:
  - One-tap **"Export CSV / Open in Google Sheets"** button using Android `FileProvider` and `Intent.ACTION_SEND` (allowing immediate opening in Google Sheets, saving to Google Drive, or sharing via email).
  - Instant raw CSV preview and option to copy CSV data to clipboard.
  - "Clear Records" with double-confirmation dialog for maintenance.

---

## 3. Implementation Steps

1. **Data Model & Logger (`AuthDataRecord.kt` & `AuthDataSheetManager.kt`)**:
   - Define data class `AuthDataRecord`.
   - Implement persistent file writing and reading using internal application storage.
   - Seed initial records with existing demo profiles so the sheet is instantly populated and testable.

2. **Repository Integration (`SciLabRepository.kt`)**:
   - Connect `signUp()` and `login()` methods to trigger `AuthDataSheetManager.logEvent(...)` in the background without blocking the UI thread.

3. **Master PIN & Owner Authentication Dialog (`AdminPinDialog.kt`)**:
   - Secure numeric PIN input dialog with masked digits and validation.

4. **Owner Data Sheet UI (`AdminDataSheetScreen.kt`)**:
   - Build Material 3 spreadsheet screen with tabs/filters, metrics cards, table rows, and export actions.

5. **Navigation & Entry Points (`MainActivity.kt` & `AuthScreen.kt`)**:
   - Provide an Owner/Admin launcher button with PIN challenge in both the login screen and the main app settings.

---

## 4. Verification & Testing
- Test sign-up as a new student: verify that a new row is instantly added in the background.
- Test sign-in as a teacher: verify login event logging.
- Verify that regular navigation does not expose the file.
- Test PIN challenge: incorrect PIN rejects access; correct PIN opens the full spreadsheet.
- Verify CSV export sharing to Google Sheets / Drive.

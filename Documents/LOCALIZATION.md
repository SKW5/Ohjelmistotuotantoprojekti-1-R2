# Localization

The application supports **English (en-GB), Finnish (fi-FI), Arabic (ar-EG), and Japanese (ja-JP)**. Arabic and Japanese provide the two non-Latin language options. Arabic uses right-to-left (RTL) layout; the other languages use left-to-right (LTR) layout.

## Framework

Localization uses the JDK's `ResourceBundle`, `MessageFormat`, `NumberFormat`, and `java.time` formatters, together with JavaFX observable properties and bindings. No additional localization dependency is required. The implementation is in `src/main/java/com/example/timetable/i18n/I18n.java`.

UTF-8 translation catalogs live in `src/main/resources/i18n/`:

| File | Language |
| --- | --- |
| `messages.properties` | English and fallback |
| `messages_fi.properties` | Finnish |
| `messages_ar.properties` | Arabic |
| `messages_ja.properties` | Japanese |

The complete key and translation inventory is [LOCALIZATION-STRINGS.md](LOCALIZATION-STRINGS.md). It includes navigation, authentication, field labels and prompts, validation, profile loading/saving/errors, event CRUD and confirmation dialogs, reminder messages, colors, language choices, and accessibility text. Weekday/month names, dates, times, and numeric digits come from JDK locale data rather than separately translated literals.

## User behavior

- Select a language on the landing, login, or registration screen, in the application header, or in Settings. Language names are shown in their own scripts so users can recover from an unfamiliar selection.
- All selectors share one observable locale. Existing controls update through bindings without rebuilding views, reloading records, or discarding unsaved form input.
- The selected language is saved with Java Preferences and restored at application startup. Unsupported saved locales fall back to English. If preferences access is denied, selection still works for the session.
- Arabic switches views, event dialogs, and alerts to JavaFX `RIGHT_TO_LEFT` orientation. Container layout mirrors navigation and calendar columns while preserving the same event/date associations. Switching back restores LTR layout.
- The timetable is a `GridPane`, not a `TableView`. Its column headers show localized weekday names and dates, and its time rows and event time ranges use the selected locale. No other data tables exist in the application.
- Dates use localized short-date formatting and parsing. DatePicker's popup is recreated after a language change so its calendar uses the selected format locale. Time choices display localized times while retaining ISO time values internally. Arabic uses localized numeric digits, including in parameterized validation messages.
- Color names are translated for display; persisted color identifiers stay unchanged. Usernames, event names, locations, email addresses, passwords, and other user-entered content are never translated.

## Adding or editing translations

1. Add a stable semantic key to all four catalogs, using UTF-8. Do not put user-facing text directly into Java controls.
2. Use `I18n.label(key)`, `I18n.button(key)`, or `I18n.bind(control.textProperty(), key, arguments)` for live text. Bind prompt/title/accessibility properties the same way. Bind dynamic date/time expressions with the supplier overload.
3. Use complete messages with `{0}` arguments instead of concatenating translated fragments. Use `{0,number,integer}` for numeric parameters. `MessageFormat` requires a literal apostrophe to be doubled (`Don''t`).
4. Use `I18n.date`, `I18n.time`, and `I18n.number` for presentation. Keep database values typed and locale independent.
5. Apply `I18n.orient` to new independent view or dialog roots. Allow labels to wrap and avoid fixed widths that clip longer translations. JavaFX uses system font fallback for Arabic and Japanese; deployed systems need fonts covering those scripts.
6. To introduce another language, add its catalog and native language-name key to every catalog, register its locale in `I18n.LANGUAGES`, and add an RTL rule if applicable. Update the inventory and tests.

## Verification

Run `mvn test`. Localization regression tests are in `UserInterfaceTest` and cover:

- Catalog key parity and message argument counts, so missing translations cannot silently pass through fallback.
- Selector synchronization and live updates of existing controls, with email input preserved.
- Actual mirrored header positions and LTR restoration.
- Arabic/Japanese timetable headings, validation messages, date conversion round trips, time/color display, and locale-specific numbers, while stored values remain unchanged.
- Existing event save/edit/delete, authentication, settings, repository, and reminder regressions.

For rendered UI previews, run `mvn -Dlocalization.screenshots=true test`. The tests write Arabic and Japanese login/header, timetable, and event-dialog PNGs to `target/localization-previews/`.

Manual acceptance checks: switch languages before login and from Settings; enter unsaved profile/event text; open date and color pickers; save and reopen an event; check a reminder and deletion confirmation; resize the window; restart to check the saved language. Check Arabic text shaping and Japanese glyphs on each deployment operating system, since installed fonts differ.

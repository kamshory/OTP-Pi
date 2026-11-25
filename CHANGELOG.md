# Changelog

## [v2.0.0] - 2026-11-26

### Added

- SQLite-backed persistent storage for application settings, OTP cache, web sessions, server status, and SMS reports.
- Initial data import from legacy data, session, and configuration directories during database initialization. The original source files are retained as backups.
- Audit logging for OTP, SMS, and email in the `otp_log`, `sms_log`, and `email_log` tables. OTP logs store only hashes; SMS and email logs store delivery metadata and message lengths, not message content.
- A `build.bat` script to compile the application and package `OTP-Pi.jar` with the SQLite JDBC dependency.
- Application modal dialogs replacing native `alert()`, `confirm()`, and `prompt()` dialogs, including confirmations for web page actions.

### Changed

- Refreshed the admin and web interface styles, including the `waiting-server-up` overlay.
- Fixed the `waiting-server-up` loader card size at 76 × 76 px; the animated bars no longer change the card's scale.
- Aligned application configuration paths with the active project directory and removed references to the old path.
- Updated the README with SQLite storage and Windows build documentation.

### Fixed

- REST API context registration now registers duplicate paths only once and ignores invalid paths.
- The STOMP subscriber validates the broker address and port before connecting, and does not start when disabled or unconfigured.
- Listener addresses and ports are logged when services start.
# Changelog

## [Unreleased] - 2026-08-19

### Changed
- Cleaned up duplicate surefire configuration in pom.xml: removed redundant `argLine` setting that duplicated `spring.profiles.active=test` and caused a surefire warning.

### Notes
- Verified backend tests run successfully with the `test` profile (H2 in-memory DB, Liquibase disabled for tests).
- Branch: `modernize/cleanup-surefire`
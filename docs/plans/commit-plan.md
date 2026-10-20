# Plan: Commit the completed Selenium 4 + Gradle migration

## Context
The migration work (Tasks A/B/C from the prior approved plan) is **already done and verified** on disk:
- Selenium 2.x → 4.51.0, Java 8 → 21, Maven → Gradle 9.6.1 (`io.github.ansonliao`, `2.0.0-SNAPSHOT`).
- All 135 tests pass, `clean build publishToMavenLocal` is green, POM + jar (LICENSE/NOTICE) metadata correct.
- No changes have been committed yet. POM deletions are staged (`git rm`); everything else is unstaged/untracked.

This plan covers the single remaining step the user approved in principle: **commit the work** on branch `feature/selenium4-gradle-migration`.

## Execution steps
1. **Stage the migration changes** — add all modified/deleted/new files produced by the migration, explicitly **excluding `.workbuddy/`** (agent working directory; not part of the project):
   - Tracked modifications + 5 deleted `pom.xml` (already staged) + new files: `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`, `gradlew.bat`, the 4 module `build.gradle.kts`, `LICENSE`, `NOTICE`, `releasenotes/2.0.0-releasenotes.en.md`.
   - Command form: stage the specific paths (or `git add -A` then `git restore --staged -- .workbuddy/`). Do **not** commit `.workbuddy/`.
2. **Commit** with a descriptive message:
   ```
   Migrate htmlelements to Selenium 4 + Gradle; republish as io.github.ansonliao

   - Upgrade Selenium 2.x -> 4.51.0 and Java 8 -> Java 21
   - Replace Apache Maven with Gradle 9.6.1 (Kotlin DSL, version catalog)
   - Republish under Maven coordinates io.github.ansonliao (2.0.0-SNAPSHOT)
   - Migrate thucydides integration to Serenity BDD 5.4.1
   - Remove PhantomJS dependency and Maven build artifacts
   - Adapt tests to Selenium 4 W3C semantics (getDomAttribute/getDomProperty,
     Select enablement) and fix an implicit-wait property leak in tests
   - Add LICENSE (Apache 2.0 full text), NOTICE, and release notes
   ```
3. **Do NOT push** by default. Pushing the branch to the fork's `origin` is a separate decision — confirm with the user before running `git push`. (The `upstream` remote points to the read-only Yandex repo and must never receive this branch.)

## Critical files
- Everything listed in `git status --short` EXCEPT `.workbuddy/` (exclude).
- Branch: `feature/selenium4-gradle-migration` (already checked out).

## Acceptance criteria
- `git status` shows only `.workbuddy/` as untracked after the commit (nothing migration-related left uncommitted).
- Commit contains the Gradle build, source/test changes, removed POMs, and compliance files — no `.workbuddy/` content.

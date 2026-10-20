# Plan: Finalize Selenium 4 + Gradle Migration — Remaining Tasks

## Context / Current Status (verified on disk)
- **Branch:** `feature/selenium4-gradle-migration` (already created, upstream `yandex-qatools/htmlelements` added)
- **Main migration DONE:** All `main` + `test` sources **compile** against Selenium 4.51.0, Gradle 9.6.1 (Kotlin DSL + version catalog), `group=io.github.ansonliao`, `version=2.0.0-SNAPSHOT`. Java 21 toolchain, Serenity BDD 5.4.1 replaces thucydides.
- **Compliance DONE:** `LICENSE` (Apache 2.0 full text), `NOTICE` (retains `Copyright 2012 YANDEX`, marks fork, disclaims Yandex affiliation), `README.md` (fork notice + new coordinates), `releasenotes/2.0.0-releasenotes.en.md`.
- **Test status:** 107 tests execute; **3 real failures** remain — all are test-mock gaps vs Selenium 4 W3C semantics (main-code changes are intentional/correct). A 4th (`StaleElementReferenceTest`) was an incremental-compile artifact and now passes.
- **Maven artifacts still on disk** (to be removed): 5 tracked `pom.xml`, plus untracked `mvnw`, `mvnw.cmd`, `.mvn/`, `tools/` (~11 MB Maven distribution). `.gitignore` currently has a broad `.*` rule that also swallows `.mvn/` and `.github/`.

---

## Task A — Fix 3 test failures (test-only mock adaptations)
Main source behavior is correct per the migration plan; only mocks must match Selenium 4's `getDomAttribute` / `getDomProperty` / `isEnabled` semantics.

### A1. `htmlelements-java/src/test/java/ru/yandex/qatools/htmlelements/UsingSelectTest.java` (line 29)
- **Cause:** Selenium 4 `Select` constructor reads `getDomAttribute("multiple")` (not `getAttribute`). The mock only stubs `getAttribute("multiple")` → returns null → treated as single-select → `form.country.deselectAll()` throws `UnsupportedOperationException: You may only deselect all options of a multi-select`.
- **Fix:** `when(countryBoxElement.getDomAttribute("multiple")).thenReturn("true");`

### A2. `htmlelements-java/src/test/java/ru/yandex/qatools/htmlelements/testelements/MockedForm.java`
- **`mockTextInput` (~line 61):** `TextInput.getText()` now uses `getDomProperty("value")` (Selenium 4 W3C; confirmed in `TextInput.java:47`). The mock only stubs `getAttribute("value")`, so `getText()` returns `""` for the empty mock and the clear-char-sequence becomes empty, breaking `FormFillingTest.clearingFieldWhenNullIsPassed` (`expected sendKeys("DELETE+BACK_SPACE")`, got `sendKeys("")`).
  - **Fix:** add `when(textInput.getDomProperty("value")).thenReturn(text);` (keep the existing `getAttribute("value")` stub; it's harmless).
- **`mockSelectOption` (~line 82):** Selenium 4 `Select.setSelected` validates `option.isEnabled()` and throws `UnsupportedOperationException: You may not select an option in disabled select` when false (Mockito default).
  - **Fix:** add `when(selectOption.isEnabled()).thenReturn(true);`

---

## Task B — Remove Maven build artifacts (prior Task #6)
1. `git rm` the 5 tracked POMs:
   - `pom.xml`, `htmlelements-java/pom.xml`, `htmlelements-matchers/pom.xml`, `htmlelements-all/pom.xml`, `htmlelements-thucydides/pom.xml`
2. Remove untracked Maven wrapper/tooling (not committed, delete directly):
   - `mvnw`, `mvnw.cmd`, `.mvn/`, `tools/`
3. **Rewrite `.gitignore`** — replace the over-broad `.*` (which silently ignores `.mvn/` and `.github/`) with explicit entries:
   ```
   build/
   target/
   .gradle/
   .idea/
   *.iml
   .DS_Store
   ```
   Keep `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.properties` tracked (only exclude the generated `gradle-wrapper.jar` if present). Ensure `.github/` workflows remain tracked.

---

## Task C — Full verification (prior Task #8)
1. `./gradlew clean --no-daemon` first (avoids the sandbox `FileSystemException` incremental-compile error seen earlier), then `./gradlew clean build`.
2. Confirm tests: `./gradlew test --info | grep -E "PASSED|FAILED"` → expect **107 executed, 0 failed**.
3. `./gradlew javadoc` (configured with `Xdoclint:none`).
4. `./gradlew publishToMavenLocal` then inspect the generated POM at `~/.m2/repository/io/github/ansonliao/<module>/2.0.0-SNAPSHOT/<module>-2.0.0-SNAPSHOT.pom` and verify:
   - `groupId` = `io.github.ansonliao`
   - `artifactId` unchanged (`htmlelements-java`, `htmlelements-matchers`, `htmlelements-all`, `htmlelements-thucydides`)
   - `name` does **not** contain "Yandex"
   - `scm` URL points to the `ansonliao` fork
   - `NOTICE`/`License` references preserved (`Copyright 2012 YANDEX` retained)

---

## Critical files
| File | Change |
|------|--------|
| `htmlelements-java/src/test/.../UsingSelectTest.java` | line 29 → `getDomAttribute` |
| `htmlelements-java/src/test/.../testelements/MockedForm.java` | add `getDomProperty("value")` + `isEnabled()` stubs |
| 5 × `pom.xml` (tracked) | `git rm` |
| `mvnw`, `mvnw.cmd`, `.mvn/`, `tools/` (untracked) | delete |
| `.gitignore` | rewrite to explicit ignores |

## Acceptance criteria
- ✅ `./gradlew clean build` green; **107 tests pass, 0 fail**
- ✅ No Maven (`pom.xml`/`mvnw`/`.mvn`/`tools`) files remain in the tree
- ✅ `javadoc` and `publishToMavenLocal` succeed
- ✅ Generated POM metadata correct (`io.github.ansonliao`, no Yandex branding, correct scm)

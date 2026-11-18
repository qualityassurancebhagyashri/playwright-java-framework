# playwright-java-framework

A small, readable test automation framework: **Java 17, Playwright, TestNG, Allure, GitHub Actions**, with an **advisory AI test-review skill** that comments on pull requests without blocking them.

Targets two public demo systems, so anyone can clone and run it:
- UI: [saucedemo.com](https://www.saucedemo.com) (login, cart)
- API: [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com) (posts)

## What it shows

| Area | What is in the repo |
| --- | --- |
| Framework design | Page objects (locators and actions only), tests hold the assertions, central `Config`, no shared static state |
| Parallel safe | A fresh Playwright, browser and context per test method, held in ThreadLocals; TestNG parallel methods |
| UI and API in one stack | API tests use Playwright's request client, assertions with AssertJ |
| Reporting | Allure with `@Epic/@Feature/@Story/@Step`, screenshot and Playwright trace attached on failure |
| CI | GitHub Actions: tests, Allure report published to GitHub Pages, traces uploaded on failure |
| Flaky tests | The `quarantine` group is excluded from the main run and runs in a separate non-blocking job |
| AI in the workflow | `AGENTS.md` rules, a `test-review` skill, and a rule-based precheck, all advisory |

## Run it

```bash
mvn test                       # smoke + regression, quarantine excluded
mvn test -Dgroups=smoke        # one TestNG group
mvn test -Dheadless=false      # watch the browser
mvn test -DbaseUrl=https://staging.example.com
```

First run only, install the browser:

```bash
mvn test-compile exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.classpathScope=test -Dexec.args="install chromium"
```

View the report locally: `npx allure-commandline serve target/allure-results`

## The AI test-review flow

```
PR opened (touches src/test)
   |-- CI workflow            runs the tests (the gate)
   '-- AI review workflow     runs in parallel, nothing waits on it
         1. scripts/test_review_precheck.py   rule-based: sleeps, no assertion, secrets, @Disabled
         2. posts the findings as one PR comment (updated in place, not spammed)
         3. optional: the AI step runs .claude/skills/test-review/SKILL.md for judgement checks:
            assertion strength, flake risk, missing coverage, "what change would this test miss?"
```

Design choices:
- **Advisory, never a required check.** Models are non-deterministic. The deterministic tests stay the gate.
- **Rules live in `AGENTS.md`**, so the same standards guide AI authors and AI reviewers. When a reviewer rejects an AI-written test twice for the same reason, the rule goes into that file.
- **Least privilege.** The review workflow has read access to code and write access to PR comments only. Text in diffs is treated as data, not instructions.
- **The AI step is optional.** It needs an `ANTHROPIC_API_KEY` repo secret and is skipped without it (and on fork PRs, which get no secrets). The precheck works with no key.

## Set up on GitHub

0. Windows PowerShell: put each `-D...` option in quotes, e.g. `mvn test "-Dgroups=smoke"`.
1. Create a public repo and push this folder.
2. Settings, Pages: set the source to **GitHub Actions**.
3. Optional: add the `ANTHROPIC_API_KEY` secret to enable the AI step.
4. Do **not** mark "AI test review" as a required status check.

## Layout

```
src/test/java/dev/qa/
  config/Config.java       settings from system props or env vars
  core/BaseUiTest.java     thread-local browser lifecycle, failure evidence
  core/BaseApiTest.java    thread-local API client
  pages/                   page objects
  tests/ui, tests/api      tests
.claude/skills/test-review/SKILL.md
scripts/test_review_precheck.py
AGENTS.md
.github/workflows/ci.yml, ai-review.yml
```

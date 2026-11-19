# playwright-java-framework

[![CI](https://github.com/qualityassurancebhagyashri/playwright-java-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/qualityassurancebhagyashri/playwright-java-framework/actions/workflows/ci.yml)

A small, readable test automation framework: **Java 17, Playwright, TestNG, Allure, GitHub Actions**, with an **advisory AI test-review skill** that comments on pull requests without ever blocking them.

**Latest Allure report:** https://qualityassurancebhagyashri.github.io/playwright-java-framework/ (published from `main` by CI)

It targets two public demo systems, so anyone can clone and run it:
- UI: [saucedemo.com](https://www.saucedemo.com) (login, cart)
- API: [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com) (posts)

## What it shows

| Area | What is in the repo |
| --- | --- |
| Framework design | Page objects (locators and actions only), tests hold the assertions, central `Config` |
| Parallel safe | A fresh Playwright, browser and context per test method, held in `ThreadLocal`s; TestNG runs methods in parallel |
| UI and API in one stack | API tests with both Playwright's request client (AssertJ) and REST-Assured (Hamcrest); REST-Assured calls are attached to Allure |
| Reporting | Allure with `@Epic/@Feature/@Story/@Step`; a screenshot and a Playwright trace are attached when a test fails |
| CI | GitHub Actions: tests, Allure report published to GitHub Pages, traces uploaded on failure, Playwright browsers cached |
| Flaky tests | The `quarantine` TestNG group is excluded from the main run and runs in a separate non-blocking job on `main` |
| AI in the workflow | `AGENTS.md` rules, a `test-review` skill, a rule-based precheck and an optional AI review, all advisory |

## Prerequisites

- **JDK 17.** Use 17 for the project SDK and for IDE run configurations. The Allure AspectJ agent (1.9.22) failed on JDK 27 with `Unsupported class file major version 71`, so do not run the tests on a much newer JDK without upgrading AspectJ.
- **Maven 3.9+**
- **Node.js** (only to build or serve the Allure report locally)

## Run it

```bash
mvn test                       # smoke + regression, quarantine excluded
mvn test -Dgroups=smoke        # one TestNG group
mvn test -Dheadless=false      # watch the browser
mvn test -DbaseUrl=https://staging.example.com
```

**Windows PowerShell:** put every `-D...` option in quotes, for example `mvn test "-Dgroups=smoke"`. A trailing `\` does not continue a command in PowerShell.

Install the browser once, on the first run. It is one line:

```bash
mvn test-compile exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.classpathScope=test" "-Dexec.args=install chromium"
```

View the report locally (on Windows PowerShell, use `npx.cmd` if scripts are blocked):

```bash
npx --yes allure-commandline@2.29.0 serve target/allure-results
```

## CI

| Workflow | When | What it does |
| --- | --- | --- |
| `ci.yml`, job `tests` | Push to `main`, pull requests | Runs the tests (the merge gate), builds the Allure report, uploads it as an artifact |
| `ci.yml`, job `deploy-report` | Push to `main` | Publishes the report to GitHub Pages |
| `ci.yml`, job `quarantine` | Push to `main` only | Runs tests tagged `quarantine`; never blocks anything |
| `ai-review.yml` | Pull requests that change `src/test/**` | Advisory review comments (see below) |

Markdown-only changes do not trigger CI. The Playwright browser download is cached, which cut the `tests` job from about 6 minutes to under a minute in one measured run; GitHub's runners vary, so timings differ between runs.

## The AI test-review flow

```
PR opened (touches src/test)
   |-- CI workflow            runs the tests (the gate)
   '-- AI review workflow     runs in parallel, nothing waits on it
         1. scripts/test_review_precheck.py   rule-based: sleeps, no assertion, secrets, disabled tests, vague names
         2. posts the findings as one PR comment (updated in place, not spammed)
         3. optional AI step: runs the repo skill /test-review (.claude/skills/test-review/SKILL.md)
              judgement checks: assertion strength, flake risk, brittle locators, missing coverage,
              "what app change would this test miss?"
         4. posts review.md as a second, separate PR comment (also updated in place)
```

**How the AI step is set up**
- It runs `anthropics/claude-code-action@v1` with `prompt: "/test-review ..."` and the model pinned in `claude_args` (`--model claude-sonnet-5-5 --max-turns 10`).
- The model can only read the repo (`Read`, `Glob`, `Grep`, read-only `git diff`, `git log`, `git show`) and write `review.md`. It has no GitHub write access and no network tools. A separate workflow step posts the file, so the comment is sticky.
- The workflow needs `id-token: write` for the action's default GitHub App authentication, and `pull-requests: write` only to post the comments.

**Design choices**
- **Advisory, never a required check.** Models are non-deterministic, so the deterministic test run stays the gate. Do not mark "AI test review" as a required status check.
- **Rules live in `AGENTS.md`**, so the same standards guide AI authors and AI reviewers. When a reviewer rejects an AI-written test twice for the same reason, the rule goes into that file.
- **Two layers, two jobs.** The precheck is free, instant and repeatable, and catches known bad patterns. The AI step judges what rules cannot, such as an assertion that would still pass if the feature broke. The skill is told not to repeat the precheck findings.
- **Untrusted input.** Text in diffs and PR descriptions is treated as data, not instructions. Fork PRs get no secrets, so the AI step is skipped there.

## Status

| Part | State |
| --- | --- |
| Tests (3 login, 1 cart, 3 API with Playwright, 6 API with REST-Assured) | Passing locally and in CI |
| CI, Allure report on GitHub Pages | Working |
| Rule-based precheck comment on PRs | Working: flags problems and updates in place once they are fixed |
| AI review step | Wired up but **not yet run in CI**; it needs the `ANTHROPIC_API_KEY` secret. Until then it is skipped. Its tool permissions and argument handling are unproven until the first run |

## Set it up in your own repo

1. Use this repo as a template or copy it, and push it to GitHub.
2. Settings, Pages: set the source to **GitHub Actions**. (GitHub Pages on private repositories needs a paid plan.)
3. Optional, to enable the AI step: add an `ANTHROPIC_API_KEY` repository secret (Settings, Secrets and variables, Actions) from the Claude Console, and set a spend limit there. Never commit the key.
4. Do not mark "AI test review" as a required check.

## Layout

```
src/test/java/dev/qa/
  config/Config.java       settings from system properties or env vars
  core/BaseUiTest.java     thread-local browser lifecycle, failure evidence
  core/BaseApiTest.java    thread-local API client
  pages/                   page objects
  clients/                 API clients (REST-Assured), requests only, no assertions
  tests/ui, tests/api      tests
.claude/skills/test-review/SKILL.md   the AI review skill
scripts/test_review_precheck.py       rule-based precheck
AGENTS.md                             rules for people and AI assistants
.github/workflows/ci.yml              tests, report, quarantine
.github/workflows/ai-review.yml       precheck and AI review on PRs
```

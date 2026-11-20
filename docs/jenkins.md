# Jenkins CI/CD for this framework

The `Jenkinsfile` at the repo root is a declarative pipeline. This page explains how it works and how to set it up.

## What happens on a build

```
push / pull request ──webhook──> Jenkins Multibranch job
                                      |
   1. build image from docker/Dockerfile.agent   (JDK 17, Maven, Chromium baked in)
   2. Compile        mvn test-compile
   3. Test           mvn test, selected by the SUITE parameter, parallel TestNG threads
   4. Quarantine     main only, non-blocking: failure marks the stage UNSTABLE, not the build FAILED
   5. post always    JUnit results, Allure report, Playwright traces archived
```

Nightly on weekdays, `main` also runs on a schedule (`cron` trigger).

## Parameters

| Parameter | Meaning |
| --- | --- |
| `SUITE` | `smoke+regression` (default), `smoke`, `regression`, `selenium-grid`, `quarantine` |
| `BROWSER` | `chrome` or `firefox`, for the `selenium-grid` suite |
| `GRID_URL` | Selenium Grid hub address, required for `selenium-grid`, for example `http://selenium-hub:4444` |
| `BASE_URL` | Application under test (default: saucedemo.com). Point it at staging for a real project |
| `PARALLELISM` | Parallel test threads (default 3) |

Only the fixed `SUITE` choices go into the Maven command. The free-text parameters are passed as quoted shell variables, so they cannot add extra arguments.

## Setting it up

1. **Jenkins and agents.** A Jenkins controller, and an agent with Docker installed (the pipeline runs every stage in a container built from `docker/Dockerfile.agent`).
2. **Plugins:** Pipeline, Docker Pipeline, JUnit, Timestamper, Allure Jenkins, and GitHub Branch Source (for the multibranch job and webhooks).
3. **Allure tool:** Manage Jenkins, Tools, add an **Allure Commandline** installation, so the `allure` step can build the report.
4. **Job:** New Item, Multibranch Pipeline, add this GitHub repository as the branch source. Jenkins finds the `Jenkinsfile` on every branch and pull request.
5. **Webhook:** in the GitHub repo, Settings, Webhooks, add `https://<your-jenkins>/github-webhook/` so pushes and PRs trigger builds without polling.
6. **Secrets:** this project has none. When a real application needs credentials, store them in Jenkins credentials and use `withCredentials`, never in the repo or in job parameters. `Config` reads `APP_USER` and `APP_PASSWORD` from environment variables.

## Design choices

- **A Dockerfile as the build agent.** The same toolchain on every agent, nothing to install on the Jenkins machines, and the browser is baked in, so builds do not download it each time.
- **`HOME` points at the workspace.** The container runs as the Jenkins user, which has no home directory. A workspace `HOME` gives Maven a writable `~/.m2` that also survives between builds of the same job, which is the dependency cache.
- **`--ipc=host`.** Chromium crashes without enough shared memory.
- **Quarantine is non-blocking.** `catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE')` keeps a flaky test visible without failing the pipeline.
- **`disableConcurrentBuilds(abortPrevious: true)`.** A newer push cancels the older build of the same branch.

## Selenium Grid from Jenkins

Run the `selenium-grid` suite with a `GRID_URL`. The Grid itself should be a long-lived service that the build agent can reach:

- a hub and nodes started from `docker/selenium-grid/docker-compose.yml` on a shared host,
- or nodes on Kubernetes that scale with demand.

Tests in the build container only need the hub address. The Grid decides which node runs each session. See `docs/selenium-grid.md`.

## What is verified, and what is not

Checked automatically in GitHub Actions (`.github/workflows/jenkins-checks.yml`):
- the agent image builds, and the full test suite passes inside it as a non-root user with `HOME` set to the workspace, the same way Jenkins runs it,
- a real Jenkins with the pipeline plugins accepts the `Jenkinsfile` ("Jenkinsfile successfully validated").

**Not verified:** a run on a real Jenkins server. The Docker agent handling, the Allure step with your tool configuration, the multibranch discovery and the webhook depend on your Jenkins setup. Expect to adjust them on the first run, for example the Allure tool name or Docker permissions on the agent.

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| `Permission denied` on `.m2` or `/root` | `HOME` is not the workspace, or the build ran without the `environment` block |
| Chromium crashes or hangs | the container was started without `--ipc=host` |
| `allure` step fails | no Allure Commandline tool configured, or the Allure plugin is missing |
| `docker: command not found` | the agent has no Docker. Use an agent label that does |
| Selenium sessions not starting | `GRID_URL` not reachable from the build container, or the Grid has no free slots (check its dashboard) |

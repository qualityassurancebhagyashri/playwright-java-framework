# Selenium Grid in this framework

## What a Selenium Grid is

A Grid lets many tests run at once, on different browsers, on other machines.

```
test JVM (RemoteWebDriver)  --->  HUB (:4444)  --->  NODE: Chrome  (up to 3 sessions)
   TestNG parallel threads         router +            NODE: Firefox (up to 3 sessions)
   one session per test            queue + 
                                   distributor
```

1. A test asks the hub for a session and says which browser it wants (Chrome, Firefox) by capabilities.
2. The hub finds a node that has the browser and a free slot. If none is free, the request waits in the queue.
3. The node starts the browser and the hub hands back a session id. The test then sends WebDriver commands to the hub, which forwards them to that node.
4. `driver.quit()` ends the session and frees the slot. Forgetting it leaves sessions open until the Grid's session timeout, and the Grid fills up.

Grid 4 has these parts: **router** (entry point), **distributor** (picks a node), **session map**, **session queue**, **event bus** (nodes announce themselves) and **nodes**. In hub-and-node mode they all run in one hub process. In fully distributed mode they run separately and scale on their own.

## How the framework uses it

- `DriverFactory` creates the driver. No `-DgridUrl`: a local browser. With `-DgridUrl`: a `RemoteWebDriver` that points at the hub.
- `BaseSeleniumTest` holds the driver in a `ThreadLocal`, so parallel TestNG threads each get their own Grid session. Parallelism (3 by default) matches the slots on each node.
- Selenium tests are in the `selenium` TestNG group, and are excluded from the default `mvn test`.

## Run it yourself

```bash
docker compose -f docker/selenium-grid/docker-compose.yml up -d
# dashboard: http://localhost:4444/ui
mvn test "-Dgroups=selenium" "-DgridUrl=http://localhost:4444" "-Dbrowser=chrome"
mvn test "-Dgroups=selenium" "-DgridUrl=http://localhost:4444" "-Dbrowser=firefox"
docker compose -f docker/selenium-grid/docker-compose.yml down
```

In PowerShell, keep the quotes around each `-D` option.

## In CI

The `selenium-grid` job in `ci.yml` starts the Grid with Docker, waits for the hub's `/status` to report ready, and runs the Selenium tests once per browser (a matrix of Chrome and Firefox). A separate step runs the Playwright login tests through the same Grid with `SELENIUM_REMOTE_URL`. Playwright documents that path as experimental and Chrome/Edge only, so that step cannot fail the build.

## Things to know

- `shm_size: 2gb` on the nodes: browsers crash with Docker's default 64 MB of shared memory.
- Node sessions: `SE_NODE_MAX_SESSIONS` sets parallel sessions per node. More sessions per node need more CPU and RAM.
- Scaling: add more node containers (`docker compose up --scale chrome=3`), or run the nodes on Kubernetes with autoscaling.
- Debugging: the Grid dashboard shows sessions, queue and node capacity. Each test attaches its session id to the Allure report.

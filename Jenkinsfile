// Declarative Jenkins pipeline for the Playwright, Selenium and TestNG framework.
// Needs: Docker on the agent, and the plugins Pipeline, Docker Pipeline, JUnit, Timestamper and Allure Jenkins.
// See docs/jenkins.md for the setup.
pipeline {
    agent {
        // Builds docker/Dockerfile.agent (JDK 17, Maven, Chromium) and runs every stage inside it.
        dockerfile {
            filename 'docker/Dockerfile.agent'
            additionalBuildArgs '--pull'
            args '--ipc=host --init'   // --ipc=host: Chromium crashes without enough shared memory
        }
    }

    parameters {
        choice(name: 'SUITE', choices: ['smoke+regression', 'smoke', 'regression', 'selenium-grid', 'quarantine'],
               description: 'Which tests to run')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser for the selenium-grid suite')
        string(name: 'GRID_URL', defaultValue: '',
               description: 'Selenium Grid hub URL for the selenium-grid suite, for example http://selenium-hub:4444')
        string(name: 'BASE_URL', defaultValue: 'https://www.saucedemo.com', description: 'Application under test')
        string(name: 'PARALLELISM', defaultValue: '3', description: 'Parallel test threads')
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds(abortPrevious: true)
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
    }

    triggers {
        // Nightly full run on weekdays, on main only. Pushes and pull requests come from a webhook
        // to a Multibranch Pipeline job, not from polling.
        cron(env.BRANCH_NAME == 'main' ? 'H 2 * * 1-5' : '')
    }

    environment {
        // The container runs as the Jenkins user, which has no home directory. Pointing HOME at the
        // workspace gives Maven a writable ~/.m2 that is also reused between builds of the same job.
        HOME = "${env.WORKSPACE}"
        MVN  = 'mvn -B -ntp'
    }

    stages {
        stage('Compile') {
            steps {
                sh '$MVN -q test-compile'
            }
        }

        stage('Test') {
            steps {
                script {
                    // Only fixed choice values go into the command. Free-text parameters are passed as
                    // shell variables and quoted, so they cannot add extra arguments.
                    def selection = ''
                    switch (params.SUITE) {
                        case 'smoke':
                            selection = '-Dgroups=smoke'
                            break
                        case 'regression':
                            selection = '-Dgroups=regression'
                            break
                        case 'selenium-grid':
                            if (!params.GRID_URL?.trim()) {
                                error('GRID_URL is required for the selenium-grid suite')
                            }
                            selection = '-Dgroups=selenium -DexcludedGroups=quarantine "-DgridUrl=$GRID_URL" "-Dbrowser=$BROWSER"'
                            break
                        case 'quarantine':
                            selection = '-Dgroups=quarantine -DexcludedGroups=none -Dsurefire.failIfNoSpecifiedTests=false'
                            break
                        default:
                            selection = ''   // smoke + regression, quarantine and selenium excluded by the pom
                    }
                    sh "\$MVN test ${selection} \"-DbaseUrl=\$BASE_URL\" \"-Dparallelism=\$PARALLELISM\""
                }
            }
        }

        // Flaky tests run on main only and never fail the build. A failure marks the stage unstable.
        stage('Quarantine (non-blocking)') {
            when {
                allOf {
                    branch 'main'
                    expression { params.SUITE == 'smoke+regression' }
                }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh '$MVN test -Dgroups=quarantine -DexcludedGroups=none -Dsurefire.failIfNoSpecifiedTests=false'
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
            archiveArtifacts artifacts: 'target/traces/**', allowEmptyArchive: true
        }
        failure {
            echo 'Build failed. Open the Allure report on the build page: failed tests have a screenshot and a Playwright trace.'
        }
    }
}

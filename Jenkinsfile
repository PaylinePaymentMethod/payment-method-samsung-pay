pipeline {
    agent any

    options {
      gitLabConnection('GitlabConnection')
      gitlabBuilds(builds: ["Assemble", "Test"])
      disableConcurrentBuilds()
      buildDiscarder(logRotator(numToKeepStr:'10'))
    }
    environment {
        versionInGradle = sh(script: './gradlew -q printVersion', returnStdout: true).trim()
    }
    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    stages {
        stage('Assemble') {
            steps {
                gitlabCommitStatus(name: "Assemble") {
                    sh './gradlew clean assemble'
                }
            }
            post {
                failure {
                    updateGitlabCommitStatus name: 'Build', state: 'failed'
                }
                success {
                    updateGitlabCommitStatus name: 'Build', state: 'success'
                }
            }
        }

        stage('Test') {
            steps {
                gitlabCommitStatus(name: "Test") {
                    sh './gradlew test --continue'
                }
            }
            post {
                always {
		  catchError {
                    junit '**/build/test-results/**/*.xml'
		  }
                }
                failure {
                    updateGitlabCommitStatus name: 'Test', state: 'failed'
                }
                success {
                    updateGitlabCommitStatus name: 'Test', state: 'success'
                }
            }
        }
        stage("Dependency Check") {
            steps {
                sh './gradlew dependencyCheckAggregate --info'
                dependencyCheckPublisher pattern: '**/build/reports/dependency-check-report.xml'
            }
         }
        stage ('Publication & Sonar') {
            parallel {
                stage('Publication sur Nexus') {
                    when {
                        anyOf { branch 'master'; branch 'develop'; branch "release/*" }
                    }
                    steps {
                        gitlabCommitStatus(name: "Publication sur Nexus") {
                            sh './gradlew publish'
                        }
                    }
                    post {
                        success {
                            step([$class: 'JiraIssueUpdater', issueSelector: [$class: 'DefaultIssueSelector'], scm: scm])
                        }
                    }
                }
                stage('SonarQube') {
                    when { not { branch 'master' } }
                    steps {
                        withSonarQubeEnv('SonarMonext') {
                            script {
                                if (BRANCH_NAME == 'develop') {
                                   sh './gradlew sonarqube -Dsonar.branch.name=${BRANCH_NAME}  --info --stacktrace'
                                }
                                if (BRANCH_NAME != 'develop') {
                                    sh './gradlew sonarqube  -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.branch.target=develop --info --stacktrace'
                                }
                            }
                        }
                    }
                }
            }
        }
       // Not possible from our jenkins
       // stage ('Tag Git') {
       //     when {
       //         anyOf { branch 'master'; branch 'develop'; branch "release/*" }
       //     }
       //     steps {
       //         sh "git tag -f V${versionInGradle}"
       //         sh "git push origin -f V${versionInGradle}"
       //     }
       // }
    }
}

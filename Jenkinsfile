#!/usr/bin/env groovy

def APP_NAME
def APP_VERSION
def DOCKER_IMAGE_NAME
def PROD_BUILD = false
def TAG_BUILD = false

pipeline {
    agent {
        node {
            label 'master'
        }
    }

    parameters {
        gitParameter(
            name: 'TAG',
            type: 'PT_BRANCH_TAG',
            defaultValue: 'origin/main',
            branch: '',
            branchFilter: '.*',
            tagFilter: '*',
            description: '',
            listSize: '0',
            quickFilterEnabled: false,
            selectedValue: 'DEFAULT',
            sortMode: 'DESCENDING_SMART'
        )

        booleanParam(
            name: 'RELEASE',
            defaultValue: false,
            description: ''
        )
    }

    environment {
        GIT_URL             = "https://github.com/LG-CNS-PROJECT-TWO-TEAM-SIX/Api-Gateway.git"
        GITHUB_CREDENTIAL   = "github-token"
        ARTIFACTS           = "build/libs/**"
        DOCKER_REGISTRY     = "rnals12"
        DOCKERHUB_CREDENTIAL= "dockerhub-token"
        DISCORD_WEBHOOK     = "https://discord.com/api/webhooks/1386572756358791168/sAQu_esRsWKDccimDKVALESFvyMvRW-KaiLYERTnAYtfwEh24ZCY_aazvIFf14wh81bP"
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: "30", artifactNumToKeepStr: "30"))
        timeout(time: 120, unit: 'MINUTES')
    }

    tools {
        gradle 'Gradle 8.14.2'
        jdk 'OpenJDK 17'
        dockerTool 'Docker'
    }

    stages {
        stage('Set Version') {
            steps {
                script {
                    APP_NAME = sh(script: "gradle -q getAppName", returnStdout: true).trim()
                    APP_VERSION = sh(script: "gradle -q getAppVersion", returnStdout: true).trim()
                    DOCKER_IMAGE_NAME = "${DOCKER_REGISTRY}/${APP_NAME}:${APP_VERSION}"

                    echo "📦 APP_NAME: ${APP_NAME}"
                    echo "🧾 APP_VERSION: ${APP_VERSION}"
                    echo "🐳 DOCKER_IMAGE_NAME: ${DOCKER_IMAGE_NAME}"
                    echo "🏷️ TAG: ${params.TAG}"

                    if (!params.TAG.startsWith('origin') && !params.TAG.endsWith('/main')) {
                        if (params.RELEASE) {
                            DOCKER_IMAGE_NAME += '-RELEASE'
                            PROD_BUILD = true
                        } else {
                            DOCKER_IMAGE_NAME += '-TAG'
                            TAG_BUILD = true
                        }
                    }
                }
            }
        }

        stage('Build & Test Application') {
            steps {
                sh "gradle clean build"
            }
        }

        stage('Build Docker Image') {
            when {
                expression { PROD_BUILD || TAG_BUILD }
            }
            steps {
                script {
                    docker.build("${DOCKER_IMAGE_NAME}")
                }
            }
        }

        stage('Push Docker Image') {
            when {
                expression { PROD_BUILD || TAG_BUILD }
            }
            steps {
                script {
                    docker.withRegistry("", DOCKERHUB_CREDENTIAL) {
                        docker.image("${DOCKER_IMAGE_NAME}").push()
                    }
                    sh "docker rmi ${DOCKER_IMAGE_NAME} || true"
                }
            }
        }
    }

    post {
        success {
            echo '✅ 빌드 성공!'
            sh """
                curl -H "Content-Type: application/json" \\
                     -X POST \\
                     -d '{"content": "✅ [빌드 성공]\\n- 프로젝트: ${APP_NAME}\\n- 태그: ${params.TAG}\\n- 이미지: ${DOCKER_IMAGE_NAME}"}' \\
                     ${DISCORD_WEBHOOK}
            """
        }

        failure {
            echo '❌ 빌드 실패...'
            sh """
                curl -H "Content-Type: application/json" \\
                     -X POST \\
                     -d '{"content": "❌ [빌드 실패]\\n- 프로젝트: ${APP_NAME}\\n- 태그: ${params.TAG}"}' \\
                     ${DISCORD_WEBHOOK}
            """
        }
    }
}

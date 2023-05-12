def lib = null
def config = null

pipeline {
    agent {
        node {
            label 'OVH-DEV'
        }
    }

    stages {
        stage('Load Lib') {
            steps {
                script {
                    config = load "config.Groovy"
                    lib = load "lib.Groovy"
                }
            }
        }

        stage('Git') {
            steps {
                script {
                    git branch: "${BRANCH}", url: "${GIT_REMOTE}"
                }
            }
        }

        stage('Building') {
            when {
                expression { env.Build.toBoolean() }
            }
            steps {
                script {
                    git branch: "${BRANCH}", url: "${GIT_REMOTE}"
                    lib.exec("cp /home/jenkins/docker/rnd/wms/cookiecutter/qc/.envs/.vhost-flower ./.envs/.dev/.vhost-flower ")
                    lib.exec("cp /home/jenkins/docker/rnd/wms/cookiecutter/qc/.envs/.django ./.envs/.dev/.django ")
                    lib.exec("cp /home/jenkins/docker/rnd/wms/cookiecutter/qc/.envs/.postgres ./.envs/.dev/.postgres ")
                    lib.exec("cp /home/jenkins/docker/rnd/wms/cookiecutter/qc/.envs/.vhost ./.envs/.dev/.vhost ")
                    lib.exec("cp /home/jenkins/docker/rnd/wms/cookiecutter/qc/.envs/.mongo ./.envs/.dev/.mongo ")
                    docker.withRegistry( "https://${DOCKER_REGISTRY}", "${DOCKER_REGISTRY_CREDENTIAL}" ) {
                        def dockerImage = docker.build("${DOCKER_REGISTRY}", "-f ${DOCKER_FILE} .")
                        dockerImage.push("${DOCKER_REGISTRY_TAG_PREFIX}${env.BUILD_NUMBER}")
                        dockerImage.push("latest")
                    }
                }
            }
        }

        stage('Pull') {
            steps {
                script {
                    env.DKIMG = env.Version.trim() ? "${DOCKER_REGISTRY}:${env.Version.trim()}" : "${DOCKER_REGISTRY}:latest"
                    docker.withRegistry( "https://${DOCKER_REGISTRY}", "${DOCKER_REGISTRY_CREDENTIAL}" ) {
                        def dockerImage = docker.image("${env.DKIMG}")
                        dockerImage.pull()
                    }
                }
            }
        }

        stage('Migration') {
            when {
                expression { env.Migration.toBoolean() }
            }
            steps {
                script {
                    def migrateLogs = sh(
                        returnStdout: true,
                        script: "docker-compose -f ${DOCKER_COMPOSE_FILE} run -T --rm ${SERVICE_MIGRATION} python manage.py migrate "
                    )
                    echo "${migrateLogs}"
                }
            }
        }

        stage('Deploy') {
            when {
                expression { env.Deploy.toBoolean() }
            }
            steps {
                script {
                    lib.docker_compose_up("${DOCKER_COMPOSE_FILE}")
                }
            }
        }
    }
    post {
        success {
            script {
                lib.sendTelegram("Job: `'${env.JOB_NAME} [#${env.BUILD_NUMBER}]'` \n => Node: `'${env.NODE_LABELS}'` \n => Result: SUCCESS \nCheck console output at [the detail](${env.RUN_DISPLAY_URL}) to view the results.")
            }
        }
        failure {
            script {
                lib.sendTelegram("Job: `'${env.JOB_NAME} [#${env.BUILD_NUMBER}]'` \n => Node: `'${env.NODE_LABELS}'` \n => Result: FAILURE \nCheck console output at [the detail](${env.RUN_DISPLAY_URL}) to view the results.")
            }
        }
    }
}


